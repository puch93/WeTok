package kr.co.core.wetok.preference;

import android.content.Context;
import android.content.SharedPreferences;

public class SystemPref {

    public static String getLanguage(Context context) {
        SharedPreferences pref = context.getSharedPreferences("syetem", context.MODE_PRIVATE);
        return pref.getString("language", "ko");
    }

    public static void setLanguage(Context context, String language) {
        SharedPreferences pref = context.getSharedPreferences("syetem", context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString("language", language);
        editor.commit();
    }

    public static int getKeyBoardHeight(Context context) {
        SharedPreferences pref = context.getSharedPreferences("syetem", context.MODE_PRIVATE);
        return pref.getInt("keyboard", 600);
    }

    public static void setKeyBoardHeight(Context context, int height) {
        SharedPreferences pref = context.getSharedPreferences("syetem", context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putInt("keyboard", height);
        editor.commit();
    }

    public static int getViewHeight(Context context) {
        SharedPreferences pref = context.getSharedPreferences("syetem", context.MODE_PRIVATE);
        return pref.getInt("view", 1200);
    }

    public static void setViewHeight(Context context, int height) {
        SharedPreferences pref = context.getSharedPreferences("syetem", context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putInt("view", height);
        editor.commit();
    }
}
