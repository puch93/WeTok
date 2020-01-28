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
import kr.co.core.wetok.databinding.ActivityInfoBinding;
import kr.co.core.wetok.util.StringUtil;

public class InfoAct extends AppCompatActivity implements View.OnClickListener {
    ActivityInfoBinding binding;
    Activity act;

    ActionBar actionBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_info, null);

        binding.tvInformation.setText(getIntent().getStringExtra("value"));

        String type = getIntent().getStringExtra("type");
        if(!StringUtil.isNull(type)) {
            if(type.equalsIgnoreCase(StringUtil.TYPE_INFO_ID)) {
                binding.tvTitle.setText(getString(R.string.act_info_id_title));
                binding.tvExplanation.setText(getString(R.string.act_info_id_explanation));
            } else {
                binding.tvTitle.setText(getString(R.string.act_info_number_title));
                binding.tvExplanation.setText(getString(R.string.act_info_number_explanation));
            }
        }


        setActionBar();
        setClickListener();
    }

    private void setClickListener() {
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

            case R.id.ll_number:
            case R.id.ll_id:
                break;
        }
    }
}
