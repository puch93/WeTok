package kr.co.core.wetok.activity;

import androidx.appcompat.app.ActionBar;
import androidx.databinding.DataBindingUtil;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.google.firebase.iid.FirebaseInstanceId;

import org.json.JSONException;
import org.json.JSONObject;

import kr.co.core.wetok.R;
import kr.co.core.wetok.databinding.ActivityMainBinding;
import kr.co.core.wetok.fragment.main.AllServiceFrag;
import kr.co.core.wetok.fragment.BaseFrag;
import kr.co.core.wetok.fragment.main.ChattingFrag;
import kr.co.core.wetok.fragment.main.FriendListFrag;
import kr.co.core.wetok.server.ReqBasic;
import kr.co.core.wetok.server.netUtil.HttpResult;
import kr.co.core.wetok.server.netUtil.NetUrls;
import kr.co.core.wetok.util.Common;
import kr.co.core.wetok.util.StringUtil;

public class MainAct extends BaseAct implements View.OnClickListener {
    ActivityMainBinding binding;
    Activity act;

    ActionBar actionBar;
    String token;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main, null);
        act = this;

        setClickListener();

        binding.flMenu01.performClick();
        //fcm 토큰 저장
        token = FirebaseInstanceId.getInstance().getToken();
        Log.e(StringUtil.TAG, "fcm_token: " + token);
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

    private void replaceFragment(BaseFrag frag) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.ll_replacements, frag)
                .commit();
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.fl_menu01:
                replaceLayout(id);
                replaceFragment(new FriendListFrag());
                break;

            case R.id.fl_menu02:
                replaceLayout(id);
                replaceFragment(new ChattingFrag());
                break;

            case R.id.fl_menu03:
                replaceLayout(id);
                replaceFragment(new AllServiceFrag());
                break;

            case R.id.fl_menu04:
                Common.showToastDevelop(act);
//                replaceLayout(id);
                break;
        }
    }
}
