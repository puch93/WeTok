package kr.co.core.wetok.activity.pay;

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
import kr.co.core.wetok.databinding.ActivityRemittanceMainBinding;

public class RemittanceMainAct extends AppCompatActivity implements View.OnClickListener {
    ActivityRemittanceMainBinding binding;
    Activity act;
    ActionBar actionBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_remittance_main, null);
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
        binding.llRemittance.setOnClickListener(this);
        binding.llHistory.setOnClickListener(this);
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
            case R.id.ll_history:
                startActivity(new Intent(act, PayHistoryAct.class));
                break;

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

            case R.id.ll_remittance:
                startActivity(new Intent(act, RemittanceSubAct.class));
                break;
        }

    }
}
