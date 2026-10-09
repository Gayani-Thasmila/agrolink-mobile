package lk.jiat.agrolink.util;

import android.content.Context;
import android.content.SharedPreferences;

public class UserManager {

    private static final String PREF_NAME = "AgroLinkUserPref";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_EMAIL = "user_email";
    private static final String KEY_TOKEN = "user_token";
    private static final String KEY_ROLE = "user_role";

    public static String token;

    // Login (save user id, email, token and role)
    public static void login(Context context, int userId, String email, String userToken, String role) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(KEY_USER_ID, userId);
        editor.putString(KEY_EMAIL, email);
        editor.putString(KEY_TOKEN, userToken);
        editor.putString(KEY_ROLE, role);
        editor.apply();
        token = userToken;
    }

    // Get logged user email
    public static String getUser(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getString(KEY_EMAIL, null);
    }

    // Get logged user ID
    public static int getUserId(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getInt(KEY_USER_ID, -1);
    }

    // Get logged user Role
    public static String getRole(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getString(KEY_ROLE, "USER");
    }

    // Get saved token
    public static String getToken(Context context) {
        if (token != null) return token;
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        token = sharedPreferences.getString(KEY_TOKEN, null);
        return token;
    }

    // Logout (clear data)
    public static void logout(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
        token = null;
    }

    //  Check user logged in or not
    public static boolean isLoggedIn(Context context) {
        return getToken(context) != null && getUserId(context) != -1;
    }

    // Check if current user is ADMIN
    public static boolean isAdmin(Context context) {
        return "ADMIN".equalsIgnoreCase(getRole(context));
    }
}
