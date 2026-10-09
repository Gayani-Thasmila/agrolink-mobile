package lk.jiat.agrolink.activity;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import lk.jiat.agrolink.R;
import lk.jiat.agrolink.adapter.OrderAdapter;
import lk.jiat.agrolink.model.Order;
import lk.jiat.agrolink.network.ApiClient;
import lk.jiat.agrolink.network.ApiService;
import lk.jiat.agrolink.util.UserManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OrderActivity extends AppCompatActivity {

    private static final String TAG = "OrderActivity";
    private static final String DEBUG_TAG = "!!! MY_ORDERS_DEBUG !!!";
    RecyclerView recyclerView;
    ArrayList<Order> orderList;
    OrderAdapter adapter;
    TextView txtUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order);

        recyclerView = findViewById(R.id.recyclerViewOrders);
        txtUser = findViewById(R.id.txtUser);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        String currentUserEmail = UserManager.getUser(this);
        txtUser.setText(currentUserEmail != null ? "Orders of " + currentUserEmail : "My Orders");

        orderList = new ArrayList<>();
        adapter = new OrderAdapter(orderList);
        recyclerView.setAdapter(adapter);

        loadUserOrdersFromBackend();
    }

    private void loadUserOrdersFromBackend() {
        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        String token = UserManager.getToken(this);
        int userId = UserManager.getUserId(this);

        Log.d(DEBUG_TAG, "1. Requesting orders for userId: " + userId);

        if (userId == -1) {
            Toast.makeText(this, "User details not found. Please login again.", Toast.LENGTH_SHORT).show();
            return;
        }


        String authHeader = (token != null) ? "Bearer " + token : "";

        apiService.getUserOrders(authHeader, userId).enqueue(new Callback<List<Order>>() {
            @Override
            public void onResponse(Call<List<Order>> call, Response<List<Order>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Order> orders = response.body();
                    Log.d(DEBUG_TAG, "2. Orders received from Backend. Count: " + orders.size());
                    Log.d(DEBUG_TAG, ">> Data JSON: " + new Gson().toJson(orders));

                    orderList.clear();
                    orderList.addAll(orders);
                    adapter.notifyDataSetChanged();

                    if (orderList.isEmpty()) {
                        Toast.makeText(OrderActivity.this, "No orders found for this user.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.e(DEBUG_TAG, "2. Request Failed. Code: " + response.code());
                    try {
                        if (response.errorBody() != null) {
                            Log.e(DEBUG_TAG, ">> Error Body: " + response.errorBody().string());
                        }
                    } catch (Exception e) { e.printStackTrace(); }
                    Toast.makeText(OrderActivity.this, "Failed to load orders: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Order>> call, Throwable t) {
                Log.e(DEBUG_TAG, "2. Network Failure: " + t.getMessage());
                Toast.makeText(OrderActivity.this, "Network Error", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
