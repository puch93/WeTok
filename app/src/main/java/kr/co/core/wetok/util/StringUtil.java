package kr.co.core.wetok.util;

import org.json.JSONObject;

import java.text.DecimalFormat;

public class StringUtil {
    public static final String TAG = "TEST_HOME";

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
            s = jo.getString(key);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return s;
    }
}
