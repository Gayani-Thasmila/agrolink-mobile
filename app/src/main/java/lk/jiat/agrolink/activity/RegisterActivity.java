package lk.jiat.agrolink.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import lk.jiat.agrolink.R;
import lk.jiat.agrolink.model.User;
import lk.jiat.agrolink.network.ApiClient;
import lk.jiat.agrolink.network.ApiService;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterActivity extends AppCompatActivity {

    EditText editName, editEmail, editPassword, editPhone, editAddress;
    Button registerBtn;
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        editName = findViewById(R.id.editName);
        editEmail = findViewById(R.id.editEmail);
        editPassword = findViewById(R.id.editPassword);
        editPhone = findViewById(R.id.editPhone);
        editAddress = findViewById(R.id.editAddress);
        registerBtn = findViewById(R.id.registerBtn);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Creating account...");
        progressDialog.setCancelable(false);

        registerBtn.setOnClickListener(v -> {

            String name = editName.getText().toString().trim();
            String email = editEmail.getText().toString().trim();
            String password = editPassword.getText().toString().trim();
            String phone = editPhone.getText().toString().trim();
            String address = editAddress.getText().toString().trim();
            String role = "USER";

            // Required field validation
            if (name.isEmpty() || email.isEmpty() || password.isEmpty()
                    || phone.isEmpty() || address.isEmpty()) {

                Toast.makeText(
                        this,
                        "Please fill all fields!",
                        Toast.LENGTH_SHORT
                ).show();
                return;
            }

            // Email validation
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                editEmail.setError("Please enter a valid email address");
                editEmail.requestFocus();
                return;
            }

            // Phone validation - Sri Lankan 10 digit number
            if (!phone.matches("^[0-9]{10}$")) {
                editPhone.setError("Please enter a valid 10-digit phone number");
                editPhone.requestFocus();
                return;
            }

            // Password validation
            if (!password.matches("^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d).{8,}$")) {
                editPassword.setError(
                        "Use 8+ characters with uppercase, lowercase and a number"
                );
                editPassword.requestFocus();
                return;
            }

            progressDialog.show();

            User user = new User(
                    0,
                    name,
                    email,
                    password,
                    phone,
                    address,
                    role
            );

            ApiService apiService =
                    ApiClient.getClient().create(ApiService.class);

            apiService.register(user).enqueue(
                    new Callback<ApiService.AuthResponse>() {

                        @Override
                        public void onResponse(
                                Call<ApiService.AuthResponse> call,
                                Response<ApiService.AuthResponse> response
                        ) {

                            progressDialog.dismiss();

                            if (response.isSuccessful()
                                    && response.body() != null) {

                                if (response.body().success) {

                                    Toast.makeText(
                                            RegisterActivity.this,
                                            "Registration Successful!",
                                            Toast.LENGTH_SHORT
                                    ).show();

                                    startActivity(
                                            new Intent(
                                                    RegisterActivity.this,
                                                    LoginActivity.class
                                            )
                                    );

                                    finish();

                                } else {

                                    Toast.makeText(
                                            RegisterActivity.this,
                                            "Failed: "
                                                    + response.body().message,
                                            Toast.LENGTH_LONG
                                    ).show();
                                }

                            } else {

                                if (response.code() == 400) {

                                    editEmail.setError(
                                            "This email is already registered"
                                    );

                                    editEmail.requestFocus();

                                    Toast.makeText(
                                            RegisterActivity.this,
                                            "An account already exists with this email",
                                            Toast.LENGTH_SHORT
                                    ).show();

                                } else {

                                    Toast.makeText(
                                            RegisterActivity.this,
                                            "Server Error: "
                                                    + response.code(),
                                            Toast.LENGTH_SHORT
                                    ).show();
                                }
                            }
                        }

                        @Override
                        public void onFailure(
                                Call<ApiService.AuthResponse> call,
                                Throwable t
                        ) {

                            progressDialog.dismiss();

                            Log.e(
                                    "RegisterActivity",
                                    "Network Error: "
                                            + t.getMessage()
                            );

                            Toast.makeText(
                                    RegisterActivity.this,
                                    "Network Error: Cannot connect to server!",
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                    }
            );
        });
    }
}