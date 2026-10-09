package lk.jiat.agrolink.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import lk.jiat.agrolink.R;
import lk.jiat.agrolink.network.ApiClient;
import lk.jiat.agrolink.network.ApiService;
import lk.jiat.agrolink.util.UserManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminLoginActivity extends AppCompatActivity {

    private static final String TAG = "AdminLoginActivity";
    EditText editAdminEmail, editAdminPassword;
    Button adminLoginBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_login);

        editAdminEmail = findViewById(R.id.editAdminEmail);
        editAdminPassword = findViewById(R.id.editAdminPassword);
        adminLoginBtn = findViewById(R.id.adminLoginBtn);

        adminLoginBtn.setOnClickListener(v -> {
            String email = editAdminEmail.getText().toString().trim();
            String password = editAdminPassword.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill all fields!", Toast.LENGTH_SHORT).show();
                return;
            }

            ApiService.LoginRequest loginRequest = new ApiService.LoginRequest(email, password);

            ApiService apiService = ApiClient.getClient().create(ApiService.class);

            // Admin login request with its specific callback
            apiService.adminLogin(loginRequest).enqueue(new Callback<ApiService.AdminAuthResponse>() {
                @Override
                public void onResponse(Call<ApiService.AdminAuthResponse> call, Response<ApiService.AdminAuthResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        ApiService.AdminAuthResponse authResponse = response.body();

                        if (authResponse.success) {
                            int userId = (authResponse.user != null) ? authResponse.user.getId() : -1;
                            String role = (authResponse.user != null) ? authResponse.user.getRole() : "UNKNOWN";

                            Log.d(TAG, "Admin Login Successful. Role: " + role);

                            // Save admin details. Ensure UserManager handles admin role correctly.
                            UserManager.login(AdminLoginActivity.this, userId, email, authResponse.token, role);

                            Toast.makeText(AdminLoginActivity.this, "Admin Login Success!", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(AdminLoginActivity.this, MainActivity.class)); // Navigate to main activity or admin dashboard
                            finish();
                        } else {
                            Toast.makeText(AdminLoginActivity.this, "Admin Login Failed: " + authResponse.message, Toast.LENGTH_LONG).show();
                        }
                    } else {
                        Toast.makeText(AdminLoginActivity.this, "Server Error: " + response.code(), Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "Server Error: " + response.code() + " - " + response.message());
                    }
                }

                @Override
                public void onFailure(Call<ApiService.AdminAuthResponse> call, Throwable t) {
                    Log.e(TAG, "Network Error: " + t.getMessage());
                    Toast.makeText(AdminLoginActivity.this, "Network Error!", Toast.LENGTH_LONG).show();
                }
            });
        });
    }
}
