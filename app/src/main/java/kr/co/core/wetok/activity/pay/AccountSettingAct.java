package kr.co.core.wetok.activity.pay;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import com.google.android.material.tabs.TabLayout;

import kr.co.core.wetok.R;
import kr.co.core.wetok.adapter.AccountSettingPagerAdapter;
import kr.co.core.wetok.databinding.ActivityAccountSettingBinding;
import kr.co.core.wetok.util.FragmentResultListener;

public class AccountSettingAct extends AppCompatActivity implements FragmentResultListener, View.OnClickListener {
    ActivityAccountSettingBinding binding;
    ActionBar actionBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_account_setting, null);

        setActionBar();
        setClickListener();

        binding.viewPager.setAdapter(new AccountSettingPagerAdapter(getSupportFragmentManager()));

        binding.viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(binding.layoutTab));

        binding.layoutTab.getTabAt(0).select();

        binding.layoutTab.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                binding.viewPager.setCurrentItem(tab.getPosition(), true);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
    }

    private void setClickListener() {
        binding.tvConfirm.setOnClickListener(this);
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
    public void isResultOk(boolean isOk) {
        if(isOk) {
            binding.viewPager.setVisibility(View.GONE);
            binding.llResultArea.setVisibility(View.VISIBLE);
        } else {
            finish();
        }
    }

    @Override
    public void onClick(View v) {

    }
}
