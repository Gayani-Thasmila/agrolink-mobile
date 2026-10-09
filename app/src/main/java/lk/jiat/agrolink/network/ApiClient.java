package lk.jiat.agrolink.network;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import java.util.concurrent.TimeUnit;

public class ApiClient {

    // NOTE: Replace this IP address with your PC's IP or Hosted Backend URL for local testing.
    // Ensure your phone and PC are on the same Wi-Fi network.
    private static final String BASE_URL = "http://192.168.8.118:8080/";
    private static final String IMGBB_BASE_URL = "https://api.imgbb.com/";

    private static Retrofit retrofit = null;
    private static Retrofit imgBBRetrofit = null;

    public static Retrofit getClient() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(getOkHttpClient())
                    .build();
        }
        return retrofit;
    }


    public static Retrofit getImgBBClient() {
        if (imgBBRetrofit == null) {
            imgBBRetrofit = new Retrofit.Builder()
                    .baseUrl(IMGBB_BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(getOkHttpClient())
                    .build();
        }
        return imgBBRetrofit;
    }

    private static OkHttpClient getOkHttpClient() {
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        return new OkHttpClient.Builder()
                .addInterceptor(logging)
                .connectTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .build();
    }
}
