package kr.co.core.wetok.preference;

import android.content.Context;
import android.content.SharedPreferences;

public class UserPref {
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
}
