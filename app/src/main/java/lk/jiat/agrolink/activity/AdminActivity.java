package lk.jiat.agrolink.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import lk.jiat.agrolink.R;
import lk.jiat.agrolink.network.ApiClient;
import lk.jiat.agrolink.network.ApiService;
import lk.jiat.agrolink.util.UserManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminActivity extends AppCompatActivity {

    private static final String TAG = "AdminActivity";
    private TextView txtUserCount, txtProductCount, txtOrderCount, txtTotalRevenue;
    private Button btnAddProduct, btnManageProducts, btnManageOrders, btnViewUsers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        txtUserCount = findViewById(R.id.txtUserCount);
        txtProductCount = findViewById(R.id.txtProductCount);
        txtOrderCount = findViewById(R.id.txtOrderCount);
        txtTotalRevenue = findViewById(R.id.txtTotalRevenue);

        btnAddProduct = findViewById(R.id.btnAddProduct);
        btnManageProducts = findViewById(R.id.btnManageProducts);
        btnManageOrders = findViewById(R.id.btnManageOrders);
        btnViewUsers = findViewById(R.id.btnViewUsers);

        loadDashboardStats();

        btnAddProduct.setOnClickListener(v -> startActivity(new Intent(AdminActivity.this, AddProductActivity.class)));
        btnManageProducts.setOnClickListener(v -> startActivity(new Intent(AdminActivity.this, ManageProductsActivity.class)));
        btnManageOrders.setOnClickListener(v -> startActivity(new Intent(AdminActivity.this, ManageOrdersActivity.class)));
        btnViewUsers.setOnClickListener(v -> startActivity(new Intent(AdminActivity.this, ViewUsersActivity.class)));
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadDashboardStats();
    }

    private void loadDashboardStats() {
        String token = UserManager.getToken(this);
        Log.d(TAG, "Admin Token From Pref: " + token);

        if (token == null || token.isEmpty()) {
            Log.e(TAG, "Token is NULL. Dashboard cannot load.");
            return;
        }

        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        apiService.getAdminDashboard("Bearer " + token).enqueue(new Callback<ApiService.AdminDashboardResponse>() {
            @Override
            public void onResponse(Call<ApiService.AdminDashboardResponse> call, Response<ApiService.AdminDashboardResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiService.AdminDashboardResponse stats = response.body();
                    Log.d(TAG, "Stats Loaded: Users=" + stats.userCount + ", Products=" + stats.productCount);

                    txtUserCount.setText(String.valueOf(stats.userCount));
                    txtProductCount.setText(String.valueOf(stats.productCount));
                    txtOrderCount.setText(String.valueOf(stats.orderCount));
                    txtTotalRevenue.setText("Rs. " + String.format("%.2f", stats.totalRevenue));
                } else {
                    Log.e(TAG, "Response Error. Code: " + response.code() + " Body: " + response.errorBody());
                }
            }

            @Override
            public void onFailure(Call<ApiService.AdminDashboardResponse> call, Throwable t) {
                Log.e(TAG, "Network Failure: " + t.getMessage());
            }
        });
    }
}
