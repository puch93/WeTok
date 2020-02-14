package kr.co.core.wetok.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

import kr.co.core.wetok.R;
import kr.co.core.wetok.databinding.ActivityRemittanceBinding;
import kr.co.core.wetok.preference.UserPref;
import kr.co.core.wetok.server.ReqBasic;
import kr.co.core.wetok.server.netUtil.HttpResult;
import kr.co.core.wetok.server.netUtil.NetUrls;
import kr.co.core.wetok.util.Common;
import kr.co.core.wetok.util.StringUtil;

public class RemittanceAct extends AppCompatActivity implements View.OnClickListener {
    ActivityRemittanceBinding binding;
    Activity act;
    ActionBar actionBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_remittance, null);
        act = this;
        firstSet();
    }

    private void firstSet() {
        setActionBar();
        setClickListener();
        binding.tvExchangeReceive.performClick();
    }

    private void setClickListener() {
        binding.tvExchangeReceive.setOnClickListener(this);
        binding.tvExchangeSend.setOnClickListener(this);
        binding.flAccountSetting.setOnClickListener(this);
    }

    private void setActionBar() {
        setSupportActionBar(binding.toolbar);
        actionBar = getSupportActionBar();
        actionBar.setTitle(null);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeAsUpIndicator(R.drawable.btn_back);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        finish();
        return true;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_exchange_receive:
                binding.tvExchangeSend.setSelected(false);
                binding.tvExchangeReceive.setSelected(true);
                break;

            case R.id.tv_exchange_send:
                binding.tvExchangeReceive.setSelected(false);
                binding.tvExchangeSend.setSelected(true);
                break;

            case R.id.fl_account_setting:
                startActivity(new Intent(act, AccountSettingAct.class));
                break;
        }

    }
}
