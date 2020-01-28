package kr.co.core.wetok.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import kr.co.core.wetok.R;
import kr.co.core.wetok.databinding.ActivitySettingBinding;
import kr.co.core.wetok.preference.UserPref;
import kr.co.core.wetok.util.StringUtil;

public class SettingAct extends AppCompatActivity implements View.OnClickListener {
    ActivitySettingBinding binding;
    Activity act;

    ActionBar actionBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_setting, null);
        act = this;

        setActionBar();
        setClickListener();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        finish();
        return true;
    }

    private void setClickListener() {
        binding.llNotice.setOnClickListener(this);
        binding.llTermsPrivate.setOnClickListener(this);
        binding.llTermsUse.setOnClickListener(this);
        binding.llVersion.setOnClickListener(this);
        binding.llLogout.setOnClickListener(this);
    }

    private void setActionBar() {
        setSupportActionBar(binding.toolbar);
        actionBar = getSupportActionBar();
        actionBar.setTitle(null);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeAsUpIndicator(R.drawable.wt_icon_back_wh_191022);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ll_version:
                startActivity(new Intent(act, VersionAct.class));
                break;
            case R.id.ll_notice:
                startActivity(new Intent(act, NoticeAct.class));
                break;

            case R.id.ll_terms_use:
            case R.id.ll_terms_private:
                Intent intent = new Intent(act, TermsAct.class);
                if(v.getId() == R.id.ll_terms_use) {
                    intent.putExtra("type", StringUtil.TYPE_TERMS_USE);
                } else {
                    intent.putExtra("type", StringUtil.TYPE_TERMS_PRIVATE);
                }
                startActivity(intent);
                break;

            case R.id.ll_logout:
                UserPref.setAutoLogin(act, false);

                startActivity(new Intent(act, LoginAct.class));
                MainAct.act.finish();
                finish();
                break;
        }
    }
}
