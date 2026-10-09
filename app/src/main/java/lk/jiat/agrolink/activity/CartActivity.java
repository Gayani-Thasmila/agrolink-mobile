package lk.jiat.agrolink.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import lk.jiat.agrolink.R;
import lk.jiat.agrolink.adapter.CartAdapter;
import lk.jiat.agrolink.model.CartItem;
import lk.jiat.agrolink.model.Order;
import lk.jiat.agrolink.network.ApiClient;
import lk.jiat.agrolink.network.ApiService;
import lk.jiat.agrolink.util.CartManager;
import lk.jiat.agrolink.util.UserManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CartActivity extends AppCompatActivity {

    private static final String TAG = "CartActivity";
    private static final int MAP_REQUEST_CODE = 100;

    private RecyclerView recyclerView;
    private TextView textTotal;
    private Button btnOrder;

    private Double selectedLat = null;
    private Double selectedLon = null;

    private CartAdapter cartAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        recyclerView = findViewById(R.id.recyclerViewCart);
        textTotal = findViewById(R.id.textTotal);
        btnOrder = findViewById(R.id.btnOrder);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        cartAdapter = new CartAdapter(
                CartManager.getCart(),
                this::updateCartUI
        );

        recyclerView.setAdapter(cartAdapter);

        updateCartUI();

        btnOrder.setOnClickListener(v -> {
            if (CartManager.getCart().isEmpty()) {
                Toast.makeText(this, "Cart is empty!", Toast.LENGTH_SHORT).show();
                return;
            }

            Intent intent = new Intent(CartActivity.this, MapActivity.class);
            startActivityForResult(intent, MAP_REQUEST_CODE);
        });
    }

    private void updateCartUI() {
        double total = 0;
        for (CartItem item : CartManager.getCart()) {
            total += item.getTotalPrice();
        }
        textTotal.setText("Total: Rs. " + String.format("%.2f", total));
        cartAdapter.notifyDataSetChanged();
        if (CartManager.getCart().isEmpty()) {
            textTotal.setText("Total: Rs. 0.00");
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == MAP_REQUEST_CODE && resultCode == RESULT_OK && data != null) {

            selectedLat = data.getDoubleExtra("lat", 0.0);
            selectedLon = data.getDoubleExtra("lon", 0.0);

            if (selectedLat != 0.0 && selectedLon != 0.0) {
                createOrderWithLocation();
            } else {
                Toast.makeText(this, "Location not selected correctly!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void createOrderWithLocation() {
        btnOrder.setEnabled(false);
        final String userEmail = UserManager.getUser(this);
        final int userId = UserManager.getUserId(this);
        List<CartItem> cartList = CartManager.getCart();

        double tempTotal = 0;
        List<ApiService.CreateOrderItemRequest> orderItemRequests = new ArrayList<>();
        StringBuilder itemsDescriptionBuilder = new StringBuilder();

        for (CartItem item : cartList) {
            tempTotal += item.getTotalPrice();
            orderItemRequests.add(new ApiService.CreateOrderItemRequest(
                            item.getProduct().getId(),
                            item.getQuantity(),
                            item.getProduct().getPrice()
                    )
            );
            itemsDescriptionBuilder.append(item.getProduct().getName()).append(" x").append(item.getQuantity()).append(", ");
        }

        if (itemsDescriptionBuilder.length() > 2) {
            itemsDescriptionBuilder.setLength(itemsDescriptionBuilder.length() - 2);
        }

        final double finalTotal = tempTotal;
        final String finalItemsDescription = itemsDescriptionBuilder.toString();


        ApiService.CreateOrderRequest orderRequest = new ApiService.CreateOrderRequest(
                finalTotal,
                "PENDING",
                userId,
                orderItemRequests,
                selectedLat,
                selectedLon
        );

        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        apiService.createOrder(orderRequest).enqueue(new Callback<Order>() {
            @Override
            public void onResponse(Call<Order> call, Response<Order> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Order createdOrder = response.body();
                    Toast.makeText(CartActivity.this, "Order Created!", Toast.LENGTH_SHORT).show();
                    initiatePayHere(createdOrder.getId(), finalItemsDescription, userEmail, finalTotal);
                } else {
                    btnOrder.setEnabled(true);
                    Toast.makeText(CartActivity.this, "Order Failed: " + response.code(), Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Order creation failed: " + readError(response));
                }
            }
            @Override
            public void onFailure(Call<Order> call, Throwable t) {
                btnOrder.setEnabled(true);
                Log.e(TAG, "Order creation request failed", t);
                Toast.makeText(CartActivity.this, "Network Error!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void initiatePayHere(int orderId, String items, String email, double amount) {
        ApiService apiService = ApiClient.getClient().create(ApiService.class);

        // Constructor matches ApiService definition with 10 parameters
        ApiService.PayHereRequest payHereRequest = new ApiService.PayHereRequest(
                orderId, items, amount, "Customer", "User", email, "0771234567", "Colombo", "Colombo", "Sri Lanka"
        );

        apiService.createPayHereSession(payHereRequest).enqueue(new Callback<ApiService.PayHereCheckoutResponse>() {
            @Override
            public void onResponse(Call<ApiService.PayHereCheckoutResponse> call, Response<ApiService.PayHereCheckoutResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Intent intent = new Intent(CartActivity.this, PaymentActivity.class);
                    intent.putExtra("payhere_data", response.body());
                    startActivity(intent);
                    CartManager.clearCart();
                    finish();
                } else {
                    btnOrder.setEnabled(true);
                    String error = readError(response);
                    Log.e(TAG, "PayHere session failed. HTTP " + response.code() + ": " + error);
                    // FIXED: Toast.LENGTH_LONG
                    Toast.makeText(CartActivity.this,
                            "Payment setup failed (" + response.code() + "). " + error,
                            Toast.LENGTH_LONG).show();
                }
            }
            @Override
            public void onFailure(Call<ApiService.PayHereCheckoutResponse> call, Throwable t) {
                btnOrder.setEnabled(true);
                Log.e(TAG, "PayHere session request failed", t);
                Toast.makeText(CartActivity.this, "Connection Error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String readError(Response<?> response) {
        try {
            return response.errorBody() == null ? "No error details returned" : response.errorBody().string();
        } catch (IOException e) {
            Log.w(TAG, "Unable to read API error response", e);
            return "Unable to read error details";
        }
    }
}
