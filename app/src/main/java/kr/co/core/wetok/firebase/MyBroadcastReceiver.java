package kr.co.core.wetok.firebase;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import kr.co.core.wetok.preference.UserPref;
import kr.co.core.wetok.server.ReqBasic;
import kr.co.core.wetok.server.netUtil.HttpResult;
import kr.co.core.wetok.server.netUtil.NetUrls;
import kr.co.core.wetok.util.StringUtil;

public class MyBroadcastReceiver extends BroadcastReceiver {
    Context ctx;

    @Override
    public void onReceive(Context context, Intent intent) {
        ctx = context;

        setChatNotiAllCount();
    }

    private void setChatNotiAllCount() {
        ReqBasic server = new ReqBasic(ctx, NetUrls.ADDRESS) {
            @Override
            public void onAfter(int resultCode, HttpResult resultData) {
                if (resultData.getResult() != null) {
                    try {
                        JSONObject jo = new JSONObject(resultData.getResult());

                        final String result = jo.getString("result");
                        final String read_count = jo.getString("sum");

                        if (result.equalsIgnoreCase("Y")) {
                            setBadge(Integer.parseInt(read_count));
                        } else {
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                }
            }
        };

        server.setTag("Chat All Count");
        server.addParams("siteUrl", NetUrls.SITEURL);
        server.addParams("CONNECTCODE", "APP");
        server.addParams("_APP_MEM_IDX", UserPref.getMidx(ctx));

        server.addParams("dbControl", "getNotReadAllSum");
        server.addParams("multi_is", "N");
        server.execute(true, false);
    }

    private void setBadge(int value) {
        Log.e(StringUtil.TAG, "setBadge in push, badge_count: " + value);
        Intent intent = new Intent("android.intent.action.BADGE_COUNT_UPDATE");
        intent.putExtra("badge_count_package_name", ctx.getPackageName());
        intent.putExtra("badge_count_class_name", getLauncherClassName(ctx));
        intent.putExtra("badge_count", value);
        ctx.sendBroadcast(intent);
    }

    public static String getLauncherClassName(Context context) {
        PackageManager pm = context.getPackageManager();
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> resolveInfos = pm.queryIntentActivities(intent, 0);
        for (ResolveInfo resolveInfo : resolveInfos) {
            String pkgName = resolveInfo.activityInfo.applicationInfo.packageName;
            if(pkgName.equalsIgnoreCase(context.getPackageName())) {
                String className = resolveInfo.activityInfo.name; return className;
            }
        }
        return null;
    }
}
