package kr.co.core.wetok.activity;

import androidx.appcompat.app.ActionBar;
import androidx.core.app.NotificationCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.LinearLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.google.firebase.iid.FirebaseInstanceId;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import kr.co.core.wetok.R;
import kr.co.core.wetok.data.UserData;
import kr.co.core.wetok.databinding.ActivityMainBinding;
import kr.co.core.wetok.fragment.main.AllServiceFrag;
import kr.co.core.wetok.fragment.BaseFrag;
import kr.co.core.wetok.fragment.main.ChattingFrag;
import kr.co.core.wetok.fragment.main.FriendListFrag;
import kr.co.core.wetok.preference.SystemPref;
import kr.co.core.wetok.preference.UserPref;
import kr.co.core.wetok.server.ReqBasic;
import kr.co.core.wetok.server.netUtil.HttpResult;
import kr.co.core.wetok.server.netUtil.NetUrls;
import kr.co.core.wetok.util.BackPressCloseHandler;
import kr.co.core.wetok.util.Common;
import kr.co.core.wetok.util.KeyboardHeightObserver;
import kr.co.core.wetok.util.KeyboardHeightProvider;
import kr.co.core.wetok.util.SoftKeyboard;
import kr.co.core.wetok.util.StringUtil;

public class MainAct extends BaseAct implements View.OnClickListener {
    ActivityMainBinding binding;
    public static Activity act;

    ActionBar actionBar;
    String token;

    FriendListFrag friendListFrag = new FriendListFrag();
    ChattingFrag chattingFrag = new ChattingFrag();
    AllServiceFrag allServiceFrag = new AllServiceFrag();

    BaseFrag currentFragment;

    private onKeyBackPressedListener mOnKeyBackPressedListener;
    private BackPressCloseHandler backPressCloseHandler;

    private KeyboardHeightProvider keyboardHeightProvider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main, null);
        act = this;

        keyboardHeightProvider = new KeyboardHeightProvider(this);
        View view = findViewById(R.id.ll_all);
        view.post(new Runnable() {
            public void run() {
                keyboardHeightProvider.start();
            }
        });


        backPressCloseHandler = new BackPressCloseHandler(this);

        setClickListener();

        binding.flMenu01.performClick();

        Log.e(StringUtil.TAG, "number(main): " + Common.getPhoneNumber(act));

        //fcm 토큰 저장
        token = FirebaseInstanceId.getInstance().getToken();
        Log.e(StringUtil.TAG, "fcm_token: " + token);

        // 안읽은 갯수 가져오기
        getReadCount();
    }

    public void checkFragment() {
        if (currentFragment instanceof ChattingFrag) {
            ((ChattingFrag) currentFragment).getChatList();
        }
    }

    private void setBadge(int value) {
        Log.e(StringUtil.TAG, "setBadge in main, badge_count: " + value);

        Intent intent = new Intent("android.intent.action.BADGE_COUNT_UPDATE");
        intent.putExtra("badge_count_package_name", getPackageName());
        intent.putExtra("badge_count_class_name", getLauncherClassName(getApplicationContext()));
        intent.putExtra("badge_count", value);
        sendBroadcast(intent);
    }

    public void showPushToast(String msg) {
        Common.showToast(act, msg);
    }

    public static String getLauncherClassName(Context context) {
        PackageManager pm = context.getPackageManager();
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> resolveInfos = pm.queryIntentActivities(intent, 0);
        for (ResolveInfo resolveInfo : resolveInfos) {
            String pkgName = resolveInfo.activityInfo.applicationInfo.packageName;
            if (pkgName.equalsIgnoreCase(context.getPackageName())) {
                String className = resolveInfo.activityInfo.name;
                return className;
            }
        }
        return null;
    }

    public void getReadCount() {
        ReqBasic server = new ReqBasic(act, NetUrls.ADDRESS) {
            @Override
            public void onAfter(int resultCode, HttpResult resultData) {
                if (resultData.getResult() != null) {
                    try {
                        JSONObject jo = new JSONObject(resultData.getResult());

                        final String result = jo.getString("result");
                        final String read_count = jo.getString("sum");

                        if (result.equalsIgnoreCase("Y")) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if (read_count == null || read_count.length() < 1 || read_count.equals("0")) {
                                        binding.tvReadCount.setVisibility(View.GONE);
                                    } else {
                                        binding.tvReadCount.setVisibility(View.VISIBLE);
                                        binding.tvReadCount.setText(read_count);
                                    }

                                    setBadge(Integer.parseInt(read_count));
                                }
                            });
                        } else {
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                        Common.showToastNetwork(act);
                    }
                } else {
                    Common.showToastNetwork(act);
                }
            }
        };

        server.setTag("Chat All Count");
        server.addParams("siteUrl", NetUrls.SITEURL);
        server.addParams("CONNECTCODE", "APP");
        server.addParams("_APP_MEM_IDX", UserPref.getMidx(act));

        server.addParams("dbControl", "getNotReadAllSum");
        server.addParams("multi_is", "N");
        server.execute(true, false);
    }

    public interface onKeyBackPressedListener {
        public void onBack();
    }

    public void setOnKeyBackPressedListener(onKeyBackPressedListener listener) {
        mOnKeyBackPressedListener = listener;
    }

    @Override
    public void onBackPressed() {
        if (mOnKeyBackPressedListener != null) {
            mOnKeyBackPressedListener.onBack();
        } else {
            backPressCloseHandler.onBackPressed();
        }
    }

    private void setClickListener() {
        binding.flMenu01.setOnClickListener(this);
        binding.flMenu02.setOnClickListener(this);
        binding.flMenu03.setOnClickListener(this);
        binding.flMenu04.setOnClickListener(this);
    }

    private void replaceLayout(int id) {
        binding.flMenu01.setSelected(false);
        binding.flMenu02.setSelected(false);
        binding.flMenu03.setSelected(false);
        binding.flMenu04.setSelected(false);

        (findViewById(id)).setSelected(true);
    }

    private void replaceFragment(BaseFrag frag, String tag) {
        currentFragment = frag;

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.ll_replacements, frag, tag)
                .commit();
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.fl_menu01:
                replaceLayout(id);

                if (friendListFrag == null) {
                    friendListFrag = new FriendListFrag();
                }
                replaceFragment(friendListFrag, "friend");
                break;

            case R.id.fl_menu02:
                replaceLayout(id);

                if (chattingFrag == null) {
                    chattingFrag = new ChattingFrag();
                }
                replaceFragment(chattingFrag, "chatting");
                break;

            case R.id.fl_menu03:
                replaceLayout(id);

                if (allServiceFrag == null) {
                    allServiceFrag = new AllServiceFrag();
                }
                replaceFragment(allServiceFrag, "service");
                break;

            case R.id.fl_menu04:
                Common.showToastDevelop(act);
                break;
        }
    }
}
