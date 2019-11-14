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
import kr.co.core.wetok.databinding.ActivityProfileBinding;
import kr.co.core.wetok.util.StringUtil;

public class ProfileAct extends AppCompatActivity implements View.OnClickListener {
    ActivityProfileBinding binding;
    Activity act;

    ActionBar actionBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_profile, null);
        act = this;

        setActionBar();
        setClickListener();
    }

    private void setClickListener() {
        binding.llName.setOnClickListener(this);
        binding.llIntroduce.setOnClickListener(this);
        binding.llBirth.setOnClickListener(this);
        binding.llNumber.setOnClickListener(this);
        binding.llId.setOnClickListener(this);
        binding.llPw.setOnClickListener(this);
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

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ll_name:
                startActivity(new Intent(act, NameAct.class));
                break;

            case R.id.ll_birth:
                startActivity(new Intent(act, BirthAct.class));
                break;

            case R.id.ll_pw:
                startActivity(new Intent(act, ModifyPwAct.class));
                break;

            case R.id.ll_introduce:
                startActivity(new Intent(act, IntroduceAct.class));
                break;

            case R.id.ll_number:
            case R.id.ll_id:
                Intent intent = new Intent(act, InfoAct.class);
                if(v.getId() == R.id.ll_id) {
                    intent.putExtra("type", StringUtil.TYPE_INFO_ID);
                } else {
                    intent.putExtra("type", StringUtil.TYPE_INFO_NUMBER);
                }
                startActivity(intent);
                break;
        }
    }
}
