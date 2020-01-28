package kr.co.core.wetok.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.ContextThemeWrapper;
import android.view.MenuItem;
import android.view.View;

import org.json.JSONException;
import org.json.JSONObject;

import kr.co.core.wetok.R;
import kr.co.core.wetok.databinding.ActivityVersionBinding;
import kr.co.core.wetok.server.ReqBasic;
import kr.co.core.wetok.server.netUtil.HttpResult;
import kr.co.core.wetok.server.netUtil.NetUrls;
import kr.co.core.wetok.util.Common;
import kr.co.core.wetok.util.StringUtil;

public class VersionAct extends AppCompatActivity {
    ActivityVersionBinding binding;
    Activity act;

    ActionBar actionBar;
    String device_version;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_version, null);
        act = this;

        try {
            device_version = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
            binding.tvVersion.setText(device_version);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        setActionBar();

        checkVersion();
    }

    private void checkVersion() {
        ReqBasic server = new ReqBasic(act, NetUrls.ADDRESS) {
            @Override
            public void onAfter(int resultCode, HttpResult resultData) {

                final String res = resultData.getResult();

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            if (!StringUtil.isNull(res)) {
                                JSONObject jo = new JSONObject(res);
                                if (StringUtil.getStr(jo, "result").equalsIgnoreCase("Y")) {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            binding.llUpdate.setVisibility(View.VISIBLE);
                                            binding.llUpdate.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    //TODO 관련코드 작성
                                                    Common.showToast(act, "버전 업데이트하기");
                                                }
                                            });
                                        }
                                    });
                                } else {
                                }
                            } else {
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        };

        server.setTag("App Version");
        server.addParams("siteUrl", NetUrls.SITEURL);
        server.addParams("CONNECTCODE", "APP");

        server.addParams("dbControl", NetUrls.GET_VERSION);
        server.addParams("thisVer", device_version);
        server.execute(true, false);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        finish();
        return true;
    }

    private void setActionBar() {
        setSupportActionBar(binding.toolbar);
        actionBar = getSupportActionBar();
        actionBar.setTitle(null);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeAsUpIndicator(R.drawable.wt_icon_back_wh_191022);
    }
}
