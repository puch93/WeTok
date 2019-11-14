package kr.co.core.wetok.activity;

import androidx.appcompat.app.ActionBar;
import androidx.databinding.DataBindingUtil;

import android.app.Activity;
import android.os.Bundle;
import android.view.MenuItem;

import kr.co.core.wetok.R;
import kr.co.core.wetok.databinding.ActivityChatBinding;
import kr.co.core.wetok.util.Common;

public class ChatAct extends BaseAct {
    ActivityChatBinding binding;
    Activity act;

    ActionBar actionBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_chat, null);
        act = this;

        setActionBar();
    }

    private void setActionBar() {
        setSupportActionBar(binding.toolbar);
        actionBar = getSupportActionBar();
        actionBar.setTitle(null);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeAsUpIndicator(R.drawable.wt_icon_back_wh_191022);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Common.showToast(act, "back");
        return true;
    }
}
