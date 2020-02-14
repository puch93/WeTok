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
import kr.co.core.wetok.databinding.ActivityRemittanceSubBinding;
import kr.co.core.wetok.dialog.AccPasswordDialog;

public class RemittanceSubAct extends AppCompatActivity implements View.OnClickListener {
    ActivityRemittanceSubBinding binding;
    Activity act;
    ActionBar actionBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_remittance_sub, null);
        act = this;

        firstSet();
    }

    private void firstSet() {
        setActionBar();

        binding.tvSearchOther.setOnClickListener(this);
        binding.tvSearchFriend.setOnClickListener(this);
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

            case R.id.tv_search_other:
            case R.id.tv_search_friend:
                startActivity(new Intent(act, SearchUserAct.class));
                break;
        }
    }
}
