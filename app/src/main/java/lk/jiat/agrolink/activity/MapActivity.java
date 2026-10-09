package lk.jiat.agrolink.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import org.osmdroid.config.Configuration;
import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.MapEventsOverlay;
import org.osmdroid.views.overlay.Marker;

import java.util.ArrayList;

import lk.jiat.agrolink.R;

public class MapActivity extends AppCompatActivity {

    private MapView map = null;
    private GeoPoint selectedPoint;
    private Marker startMarker;
    private boolean isViewMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // OSM Configuration
        Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this));
        setContentView(R.layout.activity_map);

        map = findViewById(R.id.mapview);
        map.setMultiTouchControls(true);
        map.getController().setZoom(15.0);

        Button btnConfirm = findViewById(R.id.btnConfirmLocation);
        startMarker = new Marker(map);
        startMarker.setTitle("Delivery Location");

        double viewLat = getIntent().getDoubleExtra("view_lat", 0.0);
        double viewLon = getIntent().getDoubleExtra("view_lon", 0.0);

        if (viewLat != 0.0 && viewLon != 0.0) {
            // Admin View Mode
            isViewMode = true;
            selectedPoint = new GeoPoint(viewLat, viewLon);
            map.getController().setCenter(selectedPoint);
            startMarker.setPosition(selectedPoint);
            map.getOverlays().add(startMarker);

            btnConfirm.setVisibility(View.GONE);
        } else {
            // Customer Selection Mode
            isViewMode = false;
            GeoPoint startPoint = new GeoPoint(6.9271, 79.8612); // Default Colombo
            map.getController().setCenter(startPoint);

            // Click event to select location
            MapEventsReceiver mReceive = new MapEventsReceiver() {
                @Override
                public boolean singleTapConfirmedHelper(GeoPoint p) {
                    selectedPoint = p;
                    startMarker.setPosition(p);
                    map.getOverlays().remove(startMarker);
                    map.getOverlays().add(startMarker);
                    map.invalidate();
                    return true;
                }
                @Override
                public boolean longPressHelper(GeoPoint p) { return false; }
            };
            MapEventsOverlay mapEventsOverlay = new MapEventsOverlay(mReceive);
            map.getOverlays().add(0, mapEventsOverlay);
        }

        btnConfirm.setOnClickListener(v -> {
            if (selectedPoint != null) {
                Intent resultIntent = new Intent();
                resultIntent.putExtra("lat", selectedPoint.getLatitude());
                resultIntent.putExtra("lon", selectedPoint.getLongitude());
                setResult(RESULT_OK, resultIntent);
                finish();
            } else {
                Toast.makeText(this, "Please select a location", Toast.LENGTH_SHORT).show();
            }
        });

        requestPermissionsIfNecessary(new String[]{
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.ACCESS_FINE_LOCATION
        });
    }

    private void requestPermissionsIfNecessary(String[] permissions) {
        ArrayList<String> permissionsToRequest = new ArrayList<>();
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(permission);
            }
        }
        if (permissionsToRequest.size() > 0) {
            ActivityCompat.requestPermissions(this, permissionsToRequest.toArray(new String[0]), 1);
        }
    }

    @Override public void onResume() { super.onResume(); map.onResume(); }
    @Override public void onPause() { super.onPause(); map.onPause(); }
}
