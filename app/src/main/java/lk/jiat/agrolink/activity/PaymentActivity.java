package lk.jiat.agrolink.activity;

import android.os.Bundle;
import android.util.Log;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import lk.jiat.agrolink.R;
import lk.jiat.agrolink.network.ApiService;

import java.nio.charset.StandardCharsets;
import java.net.URLEncoder;

public class PaymentActivity extends AppCompatActivity {

    private static final String TAG = "PAYHERE_DEBUG";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);

        WebView webView = findViewById(R.id.webViewPayment);
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);

        webView.setWebViewClient(new WebViewClient());

        ApiService.PayHereCheckoutResponse data = (ApiService.PayHereCheckoutResponse) getIntent().getSerializableExtra("payhere_data");

        if (data != null) {
            processPayment(data, webView);
        } else {
            Toast.makeText(this, "Critical Error: No payment data received from server!", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void processPayment(ApiService.PayHereCheckoutResponse data, WebView webView) {
        try {
            // Check if any mandatory data is null
            if (safe(data.merchantId).isEmpty() || safe(data.hash).isEmpty() || safe(data.amount).isEmpty()) {
                Toast.makeText(this, "Invalid data received from Backend. Check your logs.", Toast.LENGTH_LONG).show();
                Log.e(TAG, "MISSING DATA: MerchantID=" + data.merchantId + ", Hash=" + data.hash + ", Amount=" + data.amount);
                return;
            }

            StringBuilder postData = new StringBuilder();
            postData.append("merchant_id=").append(URLEncoder.encode(safe(data.merchantId), "UTF-8"));
            postData.append("&return_url=").append(URLEncoder.encode(safe(data.returnUrl), "UTF-8"));
            postData.append("&cancel_url=").append(URLEncoder.encode(safe(data.cancelUrl), "UTF-8"));
            postData.append("&notify_url=").append(URLEncoder.encode(safe(data.notifyUrl), "UTF-8"));
            postData.append("&order_id=").append(URLEncoder.encode(safe(data.orderId), "UTF-8"));
            postData.append("&items=").append(URLEncoder.encode(safe(data.items), "UTF-8"));
            postData.append("&currency=").append(URLEncoder.encode(safe(data.currency, "LKR"), "UTF-8"));
            postData.append("&amount=").append(URLEncoder.encode(safe(data.amount), "UTF-8"));
            postData.append("&first_name=").append(URLEncoder.encode(safe(data.firstName), "UTF-8"));
            postData.append("&last_name=").append(URLEncoder.encode(safe(data.lastName), "UTF-8"));
            postData.append("&email=").append(URLEncoder.encode(safe(data.email), "UTF-8"));
            postData.append("&phone=").append(URLEncoder.encode(safe(data.phone), "UTF-8"));
            postData.append("&address=").append(URLEncoder.encode(safe(data.address), "UTF-8"));
            postData.append("&city=").append(URLEncoder.encode(safe(data.city), "UTF-8"));
            postData.append("&country=").append(URLEncoder.encode(safe(data.country, "Sri Lanka"), "UTF-8"));
            postData.append("&hash=").append(URLEncoder.encode(safe(data.hash), "UTF-8"));

            if (data.sandbox) {
                postData.append("&sandbox=1");
            }

            Log.d(TAG, "POST URL: " + data.checkoutUrl);
            Log.d(TAG, "POST DATA: " + postData.toString());

            webView.postUrl(data.checkoutUrl, postData.toString().getBytes(StandardCharsets.UTF_8));

        } catch (Exception e) {
            Log.e(TAG, "Exception: " + e.getMessage());
            Toast.makeText(this, "Payment Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }

    private String safe(String value, String fallback) {
        String clean = safe(value);
        return clean.isEmpty() ? fallback : clean;
    }
}
