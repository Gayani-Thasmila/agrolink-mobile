package lk.jiat.agrolink.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import lk.jiat.agrolink.R;
import lk.jiat.agrolink.adapter.UserAdapter;
import lk.jiat.agrolink.model.User;
import lk.jiat.agrolink.network.ApiClient;
import lk.jiat.agrolink.network.ApiService;
import lk.jiat.agrolink.util.UserManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ViewUsersActivity extends AppCompatActivity {

    private static final String TAG = "ViewUsersActivity";
    private RecyclerView recyclerView;
    private UserAdapter adapter;
    private List<User> userList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_users);

        recyclerView = findViewById(R.id.recyclerViewUsers);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        userList = new ArrayList<>();
        adapter = new UserAdapter(userList);
        recyclerView.setAdapter(adapter);

        loadAllUsers();
    }

    private void loadAllUsers() {
        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        String token = UserManager.getToken(this);

        if (token == null || token.isEmpty()) {
            Toast.makeText(this, "Admin Token not found. Please login via Admin Login.", Toast.LENGTH_LONG).show();
            return;
        }

        apiService.getAdminUsers("Bearer " + token).enqueue(new Callback<List<User>>() {
            @Override
            public void onResponse(Call<List<User>> call, Response<List<User>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    userList.clear();
                    userList.addAll(response.body());
                    adapter.notifyDataSetChanged();
                    if (userList.isEmpty()) {
                        Toast.makeText(ViewUsersActivity.this, "No users found in database.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.e(TAG, "Failed to load users. Code: " + response.code());
                    Toast.makeText(ViewUsersActivity.this, "Failed to load users. Code: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<User>> call, Throwable t) {
                Log.e(TAG, "Network Error: " + t.getMessage());
                Toast.makeText(ViewUsersActivity.this, "Network Error", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
