package lk.jiat.agrolink.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.CancellationTokenSource;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import lk.jiat.agrolink.R;
import lk.jiat.agrolink.model.User;
import lk.jiat.agrolink.network.ApiClient;
import lk.jiat.agrolink.network.ApiService;
import lk.jiat.agrolink.util.UserManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileActivity extends AppCompatActivity {

    private static final String TAG = "ProfileActivity";
    private static final int MAP_REQUEST_CODE = 2001;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 2002;

    private EditText editName, editEmail, editPhone, editAddress;
    private Button btnUpdate, btnOpenMap, btnUseCurrentLocation, btnLogout;
    private FusedLocationProviderClient fusedLocationClient;
    private int userId;
    private String token;
    private User currentUser;

    private Double selectedLat = null;
    private Double selectedLon = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        editName = findViewById(R.id.editProfileName);
        editEmail = findViewById(R.id.editProfileEmail);
        editPhone = findViewById(R.id.editProfilePhone);
        editAddress = findViewById(R.id.editProfileAddress);
        btnUpdate = findViewById(R.id.btnSaveProfile);
        btnOpenMap = findViewById(R.id.btnOpenMap);
        btnUseCurrentLocation = findViewById(R.id.btnUseCurrentLocation);
        btnLogout = findViewById(R.id.btnLogout);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        userId = UserManager.getUserId(this);
        token = UserManager.getToken(this);

        if (userId == -1) {
            Toast.makeText(this, "User not found. Please login again.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        loadUserDetails();

        btnUpdate.setOnClickListener(v -> updateProfile());

        btnOpenMap.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, MapActivity.class);
            startActivityForResult(intent, MAP_REQUEST_CODE);
        });

        btnUseCurrentLocation.setOnClickListener(v -> useCurrentLocation());

        btnLogout.setOnClickListener(v -> {
            UserManager.logout(ProfileActivity.this);
            Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == MAP_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            selectedLat = data.getDoubleExtra("lat", 0);
            selectedLon = data.getDoubleExtra("lon", 0);
            getAddressFromLocation(selectedLat, selectedLon);
        }
    }

    private void getAddressFromLocation(double lat, double lon) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(lat, lon, 1);
            if (addresses != null && !addresses.isEmpty()) {
                String fullAddress = addresses.get(0).getAddressLine(0);
                editAddress.setText(fullAddress);
                Toast.makeText(this, "Location updated!", Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            Log.e(TAG, "Geocoder error: " + e.getMessage());
        }
    }

    private void useCurrentLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
            return;
        }

        btnUseCurrentLocation.setEnabled(false);
        fusedLocationClient.getCurrentLocation(
                Priority.PRIORITY_HIGH_ACCURACY,
                new CancellationTokenSource().getToken()
        ).addOnSuccessListener(location -> {
            btnUseCurrentLocation.setEnabled(true);
            if (location == null) {
                Toast.makeText(this, "Current location could not be found. Try again outdoors.", Toast.LENGTH_SHORT).show();
                return;
            }
            selectedLat = location.getLatitude();
            selectedLon = location.getLongitude();
            getAddressFromLocation(selectedLat, selectedLon);
            Toast.makeText(this, "Current location selected. Tap UPDATE PROFILE to save it.", Toast.LENGTH_LONG).show();
        }).addOnFailureListener(error -> {
            btnUseCurrentLocation.setEnabled(true);
            Toast.makeText(this, "Could not get current location", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Location error", error);
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                useCurrentLocation();
            } else {
                Toast.makeText(this, "Location permission is needed to save your delivery location", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void loadUserDetails() {
        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        String authHeader = (token != null) ? "Bearer " + token : "";

        apiService.getUserDetails(authHeader, userId).enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful() && response.body() != null) {
                    currentUser = response.body();
                    editName.setText(currentUser.getName());
                    editEmail.setText(currentUser.getEmail());
                    editPhone.setText(currentUser.getPhone() != null ? currentUser.getPhone() : "");
                    editAddress.setText(currentUser.getAddress() != null ? currentUser.getAddress() : "");
                    selectedLat = currentUser.getLatitude();
                    selectedLon = currentUser.getLongitude();
                }
            }
            @Override
            public void onFailure(Call<User> call, Throwable t) {
                Log.e(TAG, "Error: " + t.getMessage());
            }
        });
    }

    private void updateProfile() {
        if (currentUser == null) return;

        String name = editName.getText().toString().trim();
        String phone = editPhone.getText().toString().trim();
        String address = editAddress.getText().toString().trim();

        if (name.isEmpty()) {
            Toast.makeText(this, "Name cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        currentUser.setName(name);
        currentUser.setPhone(phone);
        currentUser.setAddress(address);
        currentUser.setLatitude(selectedLat);
        currentUser.setLongitude(selectedLon);

        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        String authHeader = (token != null) ? "Bearer " + token : "";

        apiService.updateUserProfile(authHeader, userId, currentUser).enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(ProfileActivity.this, "Profile Updated!", Toast.LENGTH_SHORT).show();
                } else {
                    try {
                        if (response.errorBody() != null) {
                            Log.e(TAG, "Update Failed: " + response.errorBody().string());
                        }
                    } catch (IOException e) { e.printStackTrace(); }
                    Toast.makeText(ProfileActivity.this, "Update Failed: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<User> call, Throwable t) {
                Toast.makeText(ProfileActivity.this, "Network Error", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
