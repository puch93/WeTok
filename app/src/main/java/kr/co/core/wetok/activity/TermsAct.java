package kr.co.core.wetok.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.app.Activity;
import android.os.Bundle;
import android.view.MenuItem;

import kr.co.core.wetok.R;
import kr.co.core.wetok.databinding.ActivityTermsBinding;
import kr.co.core.wetok.util.StringUtil;

public class TermsAct extends AppCompatActivity {
    ActivityTermsBinding binding;
    Activity act;

    ActionBar actionBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_terms, null);
        act = this;

        String type = getIntent().getStringExtra("type");
        if(!StringUtil.isNull(type)) {
            if(type.equalsIgnoreCase(StringUtil.TYPE_TERMS_USE)) {
                binding.tvTitle.setText(getString(R.string.act_terms_use_title));
            } else {
                binding.tvTitle.setText(getString(R.string.act_terms_private_title));
            }
        }

        setActionBar();
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
