package kr.co.core.wetok.preference;

import android.content.Context;
import android.content.SharedPreferences;

public class UserPref {
    // auto login
    public static void setAutoLogin(Context context, boolean midx) {
        SharedPreferences pref = context.getSharedPreferences("user", context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putBoolean("auto", midx);
        editor.commit();
    }
    public static boolean getAutoLogin(Context context) {
        SharedPreferences pref = context.getSharedPreferences("user", context.MODE_PRIVATE);
        return pref.getBoolean("auto", false);
    }



    // id
    public static void setId(Context context, String midx) {
        SharedPreferences pref = context.getSharedPreferences("user", context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString("id", midx);
        editor.commit();
    }
    public static String getId(Context context) {
        SharedPreferences pref = context.getSharedPreferences("user", context.MODE_PRIVATE);
        return pref.getString("id", null);
    }

    // pw
    public static void setPw(Context context, String midx) {
        SharedPreferences pref = context.getSharedPreferences("user", context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString("pw", midx);
        editor.commit();
    }
    public static String getPw(Context context) {
        SharedPreferences pref = context.getSharedPreferences("user", context.MODE_PRIVATE);
        return pref.getString("pw", null);
    }


    // idx
    public static void setMidx(Context context, String midx) {
        SharedPreferences pref = context.getSharedPreferences("user", context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString("midx", midx);
        editor.commit();
    }
    public static String getMidx(Context context) {
        SharedPreferences pref = context.getSharedPreferences("user", context.MODE_PRIVATE);
        return pref.getString("midx", null);
    }

    // imei
    public static void setDeviceId(Context ctx, String value) {
        SharedPreferences pref = ctx.getSharedPreferences("user", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString("imei", value);
        editor.commit();
    }
    public static String getDeviceId(Context ctx) {
        SharedPreferences pref = ctx.getSharedPreferences("user", Context.MODE_PRIVATE);
        return pref.getString("imei", null);
    }

    // fcm
    public static void setFcmToken(Context ctx, String value) {
        SharedPreferences pref = ctx.getSharedPreferences("user", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString("fcm", value);
        editor.commit();
    }
    public static String getFcmToken(Context ctx) {
        SharedPreferences pref = ctx.getSharedPreferences("user", Context.MODE_PRIVATE);
        return pref.getString("fcm", null);
    }

    // profile image
    public static void setProfileImage(Context ctx, String value) {
        SharedPreferences pref = ctx.getSharedPreferences("user", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString("image", value);
        editor.commit();
    }
    public static String getProfileImage(Context ctx) {
        SharedPreferences pref = ctx.getSharedPreferences("user", Context.MODE_PRIVATE);
        return pref.getString("image", null);
    }
}
