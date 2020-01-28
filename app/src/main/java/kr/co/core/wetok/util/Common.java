package kr.co.core.wetok.util;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.regex.Pattern;

import kr.co.core.wetok.R;
import kr.co.core.wetok.server.netUtil.NetUrls;

public class Common {
    public static void showToast(final Activity act, final String msg) {
        if(null != act) {
            act.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(act, msg, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    public static void showToastLong(final Activity act, final String msg) {
        if(null != act) {
            act.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(act, msg, Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    public static void showToastNetwork(final Activity act) {
        if(null != act) {
            act.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(act, act.getString(R.string.toast_network), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    public static void showToastDevelop(final Activity act) {
        if(null != act) {
            act.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(act, act.getString(R.string.toast_develop), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }


    // get device id
    public static String getDeviceId(Context ctx) {
        String deviceID = "";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ctx.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                deviceID = ((TelephonyManager) ctx.getSystemService(Context.TELEPHONY_SERVICE)).getDeviceId();
            }
        } else {
            deviceID = ((TelephonyManager) ctx.getSystemService(Context.TELEPHONY_SERVICE)).getDeviceId();
        }

        if (StringUtil.isNull(deviceID)) {
            deviceID = "35" +
                    Build.BOARD.length() % 10 + Build.BRAND.length() % 10 +
                    Build.CPU_ABI.length() % 10 + Build.DEVICE.length() % 10 +
                    Build.DISPLAY.length() % 10 + Build.HOST.length() % 10 +
                    Build.ID.length() % 10 + Build.MANUFACTURER.length() % 10 +
                    Build.MODEL.length() % 10 + Build.PRODUCT.length() % 10 +
                    Build.TAGS.length() % 10 + Build.TYPE.length() % 10 +
                    Build.USER.length() % 10;
            if (TextUtils.isEmpty(deviceID)) {
                deviceID = UUID.randomUUID().toString();
            }
        }

        return deviceID;
    }

    public static String getRandomPhoneNumber() {
        Random random = new Random();

        String result = "";
        for (int i = 0; i < 8; i++) {
            result += String.valueOf(random.nextInt(9));
        }

        return result;
    }

    // get phone num
    public static String getPhoneNumber(Activity act) {
        TelephonyManager tm = (TelephonyManager) act.getSystemService(Context.TELEPHONY_SERVICE);
        String phoneNum = tm.getLine1Number();
        if (StringUtil.isNull(phoneNum)) {
            return null;
        } else {
//            phoneNum = "13906385737";
            Log.e(StringUtil.TAG, "phoneNum: " + phoneNum);

            if (phoneNum.startsWith("+82")) {
                phoneNum = phoneNum.replace("+82", "0");
            }

//            // 코리아
//            if (phoneNum.contains("82")) {
//                if (phoneNum.charAt(0) == '8' && phoneNum.charAt(1) == '2') {
//                    Log.e(StringUtil.TAG, "case 1");
//                    phoneNum = phoneNum.replaceFirst("82", "0");
//
//                } else if (phoneNum.charAt(1) == '8' && phoneNum.charAt(2) == '2') {
//                    Log.e(StringUtil.TAG, "case 2");
//                    phoneNum = phoneNum.replaceFirst("[+]", "");
//                    phoneNum = phoneNum.replaceFirst("82", "0");
//                }
//
//                // 차이나
//            } else if (phoneNum.contains("86")) {
//                if (phoneNum.charAt(0) == '8' && phoneNum.charAt(1) == '6') {
//                    Log.e(StringUtil.TAG, "case 3");
//                    phoneNum = phoneNum.replaceFirst("86", "1");
//
//                } else if (phoneNum.charAt(1) == '8' && phoneNum.charAt(2) == '6') {
//                    Log.e(StringUtil.TAG, "case 4");
//                    phoneNum = phoneNum.replaceFirst("[+]", "");
//                    phoneNum = phoneNum.replaceFirst("86", "1");
//                }
//            }

//            if(!phoneNum.contains("+82") && !phoneNum.contains("+86")) {
//                    // 한국 번호 +82
//                Log.e(StringUtil.TAG, "phoneNum true");
//                if (!Pattern.matches("^01(?:0|1|[6-9]) - (?:\\d{3}|\\d{4}) - \\d{4}$", phoneNum)) {
//                    phoneNum = phoneNum.substring(1);
//                    phoneNum = "+82" + phoneNum;
//
//                    // 중국 번호 +86
//                } else if (!Pattern.matches("^1[3|4|5|8][0-9] - \\d{8}$", phoneNum)) {
//                    phoneNum = "86" + phoneNum;
//                }
//            }


            Log.e(StringUtil.TAG, "phoneNum result: " + phoneNum);

            return phoneNum;
        }
    }

    public static String converTimeSimpleLong(long original) {
        Date date = new Date(original);
        SimpleDateFormat dateFormat;
        dateFormat = new SimpleDateFormat("m:ss", java.util.Locale.getDefault());
        return dateFormat.format(date);
    }

    public static String converTimeSimpleLInt(int original) {
        Date date = new Date(original);
        SimpleDateFormat dateFormat;
        dateFormat = new SimpleDateFormat("m:ss", java.util.Locale.getDefault());
        return dateFormat.format(date);
    }

    public static String converTime(String original) {
        //아이템별 시간
        String time1 = original;
        SimpleDateFormat dateFormat1 = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", java.util.Locale.getDefault());
        Date date1 = null;
        try {
            date1 = dateFormat1.parse(time1);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        SimpleDateFormat dateFormat2 = new SimpleDateFormat("a hh:mm", java.util.Locale.getDefault());
        String time2 = dateFormat2.format(date1);
        return time2;
    }

    public static boolean isAppTopRun(Context ctx, String baseClassName) {
        ActivityManager activityManager = (ActivityManager) ctx.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> info;
        info = activityManager.getRunningTasks(1);
        if (info == null || info.size() == 0) {
            return false;
        }
        if (info.get(0).baseActivity.getClassName().equals(baseClassName)) {
            return true;
        } else {
            return false;
        }
    }

    private static class TIME_MAXIMUM {
        static final int SEC = 60;
        static final int MIN = 60;
        static final int HOUR = 24;
        static final int DAY = 30;
        static final int MONTH = 12;
    }

    public static String formatImeString(Date tempDate, Activity act) {
        long curTime = System.currentTimeMillis();
        long regTime = tempDate.getTime();
        long diffTime = (curTime - regTime) / 1000;

        String msg = null;
        if (diffTime < 0) {
            msg = "0" + act.getString(R.string.ago_seconds);
        } else if (diffTime < TIME_MAXIMUM.SEC) {
            msg = diffTime + act.getString(R.string.ago_seconds);
        } else if ((diffTime /= TIME_MAXIMUM.SEC) < TIME_MAXIMUM.MIN) {
            msg = diffTime + act.getString(R.string.ago_minutes);
        } else if ((diffTime /= TIME_MAXIMUM.MIN) < TIME_MAXIMUM.HOUR) {
            msg = (diffTime) + act.getString(R.string.ago_hours);
        } else if ((diffTime /= TIME_MAXIMUM.HOUR) < TIME_MAXIMUM.DAY) {
            msg = (diffTime) + act.getString(R.string.ago_days);
        } else if ((diffTime /= TIME_MAXIMUM.DAY) < TIME_MAXIMUM.MONTH) {
            msg = (diffTime) + act.getString(R.string.ago_months);
        } else {
            msg = (diffTime) + act.getString(R.string.ago_years);
        }

        return msg;
    }
}
