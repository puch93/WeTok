package kr.co.core.wetok.preference;

import android.content.Context;
import android.content.SharedPreferences;

public class UserPref {
    public static void setID(Context context, String midx) {
        SharedPreferences pref = context.getSharedPreferences("syetem", context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString("midx", midx);
        editor.commit();
    }
    public static String getID(Context context) {
        SharedPreferences pref = context.getSharedPreferences("syetem", context.MODE_PRIVATE);
        return pref.getString("midx", null);
    }


    //fcm
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
