package kr.co.core.wetok.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import org.json.JSONException;
import org.json.JSONObject;

import kr.co.core.wetok.R;
import kr.co.core.wetok.databinding.ActivityPermissionBinding;
import kr.co.core.wetok.server.ReqBasic;
import kr.co.core.wetok.server.netUtil.HttpResult;
import kr.co.core.wetok.server.netUtil.NetUrls;
import kr.co.core.wetok.util.Common;
import kr.co.core.wetok.util.StringUtil;

public class PermissionAct extends AppCompatActivity {
    ActivityPermissionBinding binding;
    Activity act;

    ActionBar actionBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_permission, null);
        act = this;

        binding.tvAgree.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestPermission();
            }
        });

        binding.tvPrivate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://greatluck.alrigo.co.kr/term.siso"));
                startActivity(intent);
            }
        });

        setActionBar();
    }

    private void getTerms(String type) {
        ReqBasic server = new ReqBasic(act, NetUrls.ADDRESS) {
            @Override
            public void onAfter(int resultCode, HttpResult resultData) {
                Log.i(StringUtil.TAG, "getTerms:  " + resultData.getResult() + "\ncode: " + resultCode);

                if (resultData.getResult() != null) {
                    try {
                        JSONObject jo = new JSONObject(resultData.getResult());

                        if(jo.getString("result").equalsIgnoreCase("Y")) {

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

        server.setTag("Terms");
        server.addParams("dbControl", "getTerm");
        server.addParams("type", type);
        server.execute(true, false);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        setResult(RESULT_CANCELED);
        finish();
        return true;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        setResult(RESULT_CANCELED);
        finish();
    }

    private void setActionBar() {
        setSupportActionBar(binding.toolbar);
        actionBar = getSupportActionBar();
        actionBar.setTitle(null);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeAsUpIndicator(R.drawable.wt_icon_back_wh_191022);
    }

    private void requestPermission() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            requestPermissions(new String[]{
                    Manifest.permission.WRITE_CONTACTS,
                    Manifest.permission.READ_CONTACTS
            },0);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (

                    checkSelfPermission(Manifest.permission.WRITE_CONTACTS) != PackageManager.PERMISSION_GRANTED ||
                            checkSelfPermission(Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED
            ) {
                finish();
                finishAffinity();
            } else {
                setResult(RESULT_OK);
                finish();
            }
        }
    }
}
