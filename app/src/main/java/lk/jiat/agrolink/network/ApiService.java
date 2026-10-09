package lk.jiat.agrolink.network;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;
import java.util.List;

import lk.jiat.agrolink.model.Product;
import lk.jiat.agrolink.model.User;
import lk.jiat.agrolink.model.Order;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService {

    @POST("api/auth/register")
    Call<AuthResponse> register(@Body User user);

    @POST("api/auth/login")
    Call<AuthResponse> login(@Body LoginRequest loginRequest);

    @GET("api/products")
    Call<List<Product>> getProducts(@Header("Authorization") String token);

    @POST("api/orders")
    Call<Order> createOrder(@Body CreateOrderRequest request);

    @GET("api/orders")
    Call<List<Order>> getUserOrders(@Header("Authorization") String token, @Query("userId") int userId);

    @GET("api/users/{id}")
    Call<User> getUserDetails(@Header("Authorization") String token, @Path("id") int userId);

    @PUT("api/users/{id}")
    Call<User> updateUserProfile(@Header("Authorization") String token, @Path("id") int userId, @Body User user);

    @POST("api/user/update-fcm-token")
    Call<Void> updateFCMToken(@Header("Authorization") String token, @Query("email") String email, @Query("fcmToken") String fcmToken);

    @POST("api/payments/payhere/session")
    Call<PayHereCheckoutResponse> createPayHereSession(@Body PayHereRequest payHereRequest);

    @POST("api/admin/login")
    Call<AdminAuthResponse> adminLogin(@Body LoginRequest loginRequest);

    @GET("api/admin/dashboard")
    Call<AdminDashboardResponse> getAdminDashboard(@Header("Authorization") String token);

    @POST("api/admin/products")
    Call<Product> addAdminProduct(@Header("Authorization") String token, @Body Product product);

    @PUT("api/admin/products/{id}")
    Call<Product> updateAdminProduct(@Header("Authorization") String token, @Path("id") int id, @Body Product product);

    @DELETE("api/admin/products/{id}")
    Call<Void> deleteAdminProduct(@Header("Authorization") String token, @Path("id") int id);

    @GET("api/admin/orders")
    Call<List<Order>> getAdminOrders(@Header("Authorization") String token);

    @PATCH("api/admin/orders/{id}/status")
    Call<Order> updateOrderStatus(@Header("Authorization") String token, @Path("id") int id, @Body UpdateStatusRequest request);

    @GET("api/admin/users")
    Call<List<User>> getAdminUsers(@Header("Authorization") String token);

    class AuthResponse {
        public boolean success;
        public String message;
        public String token;
        public User user;
    }

    class AdminAuthResponse {
        public boolean success;
        public String message;
        public String token;
        public User user;
    }

    class CreateOrderRequest {
        public Double totalPrice;
        public String status;
        public Integer userId;
        public List<CreateOrderItemRequest> orderItems;

        @SerializedName(value = "latitude", alternate = {"lat", "order_latitude"})
        public Double latitude;

        @SerializedName(value = "longitude", alternate = {"lon", "order_longitude"})
        public Double longitude;

        public CreateOrderRequest(Double totalPrice, String status, Integer userId, List<CreateOrderItemRequest> orderItems, Double latitude, Double longitude) {
            this.totalPrice = totalPrice;
            this.status = status;
            this.userId = userId;
            this.orderItems = orderItems;
            this.latitude = latitude;
            this.longitude = longitude;
        }
    }

    class CreateOrderItemRequest {
        @SerializedName("productId")
        public Integer productId;
        public Integer quantity;
        public Double price;

        public CreateOrderItemRequest(Integer productId, Integer quantity, Double price) {
            this.productId = productId;
            this.quantity = quantity;
            this.price = price;
        }
    }

    class AdminDashboardResponse {
        @SerializedName(value = "userCount", alternate = {"user_count"})
        public long userCount;
        @SerializedName(value = "productCount", alternate = {"product_count"})
        public long productCount;
        @SerializedName(value = "orderCount", alternate = {"order_count"})
        public long orderCount;
        @SerializedName(value = "totalRevenue", alternate = {"total_revenue"})
        public double totalRevenue;
    }

    class UpdateStatusRequest {
        public String status;
        public UpdateStatusRequest(String status) { this.status = status; }
    }

    class LoginRequest {
        public String email;
        public String password;

        public LoginRequest(String email, String password) {
            this.email = email;
            this.password = password;
        }
    }

    class PayHereRequest {
        @SerializedName("order_id")
        public Integer orderId;
        public String items;
        public String currency = "LKR";
        public Double amount;
        @SerializedName("first_name")
        public String firstName;
        @SerializedName("last_name")
        public String lastName;
        public String email;
        public String phone;
        public String address;
        public String city;
        public String country = "Sri Lanka";

        public PayHereRequest(
                Integer orderId, String items, Double amount, String firstName, String lastName,
                String email, String phone, String address, String city, String country
        ) {
            this.orderId = orderId;
            this.items = items;
            this.amount = amount;
            this.firstName = firstName;
            this.lastName = lastName;
            this.email = email;
            this.phone = phone;
            this.address = address;
            this.city = city;
            this.country = country;
        }
    }

    class PayHereCheckoutResponse implements Serializable {
        public boolean sandbox;

        @SerializedName(value = "checkout_url", alternate = {"checkoutUrl"})
        public String checkoutUrl;

        @SerializedName(value = "merchant_id", alternate = {"merchantId"})
        public String merchantId;

        @SerializedName(value = "return_url", alternate = {"returnUrl"})
        public String returnUrl;

        @SerializedName(value = "cancel_url", alternate = {"cancelUrl"})
        public String cancelUrl;

        @SerializedName(value = "notify_url", alternate = {"notifyUrl"})
        public String notifyUrl;

        @SerializedName(value = "order_id", alternate = {"orderId"})
        public String orderId;

        public String items;
        public String currency;
        public String amount;

        @SerializedName(value = "first_name", alternate = {"firstName"})
        public String firstName;

        @SerializedName(value = "last_name", alternate = {"lastName"})
        public String lastName;

        public String email;
        public String phone;
        public String address;
        public String city;
        public String country;
        public String hash;
    }
}
