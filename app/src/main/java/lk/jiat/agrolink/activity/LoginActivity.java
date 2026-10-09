package lk.jiat.agrolink.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.messaging.FirebaseMessaging;

import lk.jiat.agrolink.R;
import lk.jiat.agrolink.network.ApiClient;
import lk.jiat.agrolink.network.ApiService;
import lk.jiat.agrolink.util.UserManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";
    Button loginBtn, registerBtn;
    EditText editEmail, editPassword;
    TextView txtLoginTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (UserManager.isLoggedIn(this)) {
            navigateToMain();
            return;
        }

        setContentView(R.layout.activity_login);

        loginBtn = findViewById(R.id.loginBtn);
        registerBtn = findViewById(R.id.registerBtn);
        editEmail = findViewById(R.id.editEmail);
        editPassword = findViewById(R.id.editPassword);
        txtLoginTitle = findViewById(R.id.txtLoginTitle);

        if (txtLoginTitle != null) {
            txtLoginTitle.setOnClickListener(v -> {
                startActivity(new Intent(LoginActivity.this, AdminLoginActivity.class));
            });
        }

        loginBtn.setOnClickListener(v -> {
            String email = editEmail.getText().toString().trim();
            String password = editPassword.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill all fields!", Toast.LENGTH_SHORT).show();
                return;
            }

            ApiService.LoginRequest loginRequest = new ApiService.LoginRequest(email, password);
            ApiService apiService = ApiClient.getClient().create(ApiService.class);

            apiService.login(loginRequest).enqueue(new Callback<ApiService.AuthResponse>() {
                @Override
                public void onResponse(Call<ApiService.AuthResponse> call, Response<ApiService.AuthResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        ApiService.AuthResponse authResponse = response.body();

                        if (authResponse.success) {
                            String role = (authResponse.user != null) ? authResponse.user.getRole() : "USER";

                            if ("ADMIN".equalsIgnoreCase(role)) {
                                performSilentAdminLogin(email, password);
                            } else {
                                int userId = (authResponse.user != null) ? authResponse.user.getId() : -1;
                                UserManager.login(LoginActivity.this, userId, email, authResponse.token, role);
                                sendFCMTokenToBackend(email, authResponse.token);
                                Toast.makeText(LoginActivity.this, "Login Success!", Toast.LENGTH_SHORT).show();
                                navigateToMain();
                            }
                        } else {
                            Toast.makeText(LoginActivity.this, "Login Failed: " + authResponse.message, Toast.LENGTH_LONG).show();
                        }
                    }
                }

                @Override
                public void onFailure(Call<ApiService.AuthResponse> call, Throwable t) {
                    Toast.makeText(LoginActivity.this, "Network Error!", Toast.LENGTH_LONG).show();
                }
            });
        });

        registerBtn.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
        });
    }

    private void performSilentAdminLogin(String email, String password) {
        ApiService.LoginRequest loginRequest = new ApiService.LoginRequest(email, password);
        ApiService apiService = ApiClient.getClient().create(ApiService.class);

        apiService.adminLogin(loginRequest).enqueue(new Callback<ApiService.AdminAuthResponse>() {
            @Override
            public void onResponse(Call<ApiService.AdminAuthResponse> call, Response<ApiService.AdminAuthResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiService.AdminAuthResponse adminRes = response.body();
                    int userId = (adminRes.user != null) ? adminRes.user.getId() : -1;
                    UserManager.login(LoginActivity.this, userId, email, adminRes.token, "ADMIN");
                    sendFCMTokenToBackend(email, adminRes.token);
                    Toast.makeText(LoginActivity.this, "Logged in as ADMIN", Toast.LENGTH_SHORT).show();
                    navigateToMain();
                } else {
                    Toast.makeText(LoginActivity.this, "Admin Auth Failed", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<ApiService.AdminAuthResponse> call, Throwable t) {
                Toast.makeText(LoginActivity.this, "Admin Network Error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void sendFCMTokenToBackend(String email, String authToken) {
        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                String fcmToken = task.getResult();
                ApiService apiService = ApiClient.getClient().create(ApiService.class);
                String header = (authToken != null) ? "Bearer " + authToken : "";
                apiService.updateFCMToken(header, email, fcmToken).enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {}
                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {}
                });
            }
        });
    }

    private void navigateToMain() {
        startActivity(new Intent(LoginActivity.this, MainActivity.class));
        finish();
    }
}
