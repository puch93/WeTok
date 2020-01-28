package kr.co.core.wetok.util;

import org.json.JSONObject;

import java.text.DecimalFormat;

public class StringUtil {
    public static final String TAG = "TEST_HOME";
    public static final String TAG_CHAT = "TEST_CHAT";
    public static final String TAG_PUSH = "TEST_PUSH";
    public static final String TAG_RTC = "TEST_RTC";
    public static final String TAG_SOCK = "TEST_SOCK";

    public static final String TYPE_INFO_ID = "id";
    public static final String TYPE_INFO_NUMBER = "number";
    public static final String TYPE_TERMS_USE = "use";
    public static final String TYPE_TERMS_PRIVATE = "private";

    public static boolean isNull(String str){
        if(str == null || str.length() == 0 || str.equals("null")){
            return true;
        }else{
            return false;
        }
    }

    public static String setNumComma(int price){
        DecimalFormat format = new DecimalFormat("###,###");
        return format.format(price);
    }

    public static String getStr(JSONObject jo, String key) {
        String s = null;
        try {
            if (jo.has(key)) {
                s = jo.getString(key);
            } else {
                s = "";
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return s;
    }
}
