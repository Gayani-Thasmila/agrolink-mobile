package lk.jiat.agrolink.network;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Query;

public interface ImgBBApiService {
    @Multipart
    @POST("1/upload")
    Call<ImgBBResponse> uploadImage(
            @Query("key") String apiKey,
            @Part MultipartBody.Part image
    );

    class ImgBBResponse {
        public Data data;
        public boolean success;
        public int status;

        public static class Data {
            public String url;
            public String display_url;
        }
    }
}
