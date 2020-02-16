package kr.co.core.wetok.activity.pay;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import kr.co.core.wetok.R;
import kr.co.core.wetok.databinding.ActivityPayChargingBinding;
import kr.co.core.wetok.dialog.AccPasswordDialog;

public class PayChargingAct extends AppCompatActivity implements View.OnClickListener {
    ActivityPayChargingBinding binding;
    Activity act;
    ActionBar actionBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_pay_charging, null);
        act = this;

        firstSet();
    }

    private void firstSet() {
        setActionBar();

        binding.tvRemittance.setOnClickListener(this);
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
            case R.id.tv_remittance:
                startActivity(new Intent(act, AccPasswordDialog.class));
                overridePendingTransition(R.anim.open, R.anim.close);
                break;
        }
    }
}
