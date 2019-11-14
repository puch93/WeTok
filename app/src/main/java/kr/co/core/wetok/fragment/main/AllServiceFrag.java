package kr.co.core.wetok.fragment.main;



import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import kr.co.core.wetok.R;
import kr.co.core.wetok.activity.ProfileAct;
import kr.co.core.wetok.activity.SettingAct;
import kr.co.core.wetok.databinding.FragmentAllServiceBinding;
import kr.co.core.wetok.fragment.BaseFrag;
import kr.co.core.wetok.util.Common;

public class AllServiceFrag extends BaseFrag implements View.OnClickListener {
    FragmentAllServiceBinding binding;
    private AppCompatActivity act;

    private ActionBar actionBar;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_all_service, container, false);
        act = (AppCompatActivity) getActivity();
        setHasOptionsMenu(true);

        setActionBar();
        setClickListener();

        return binding.getRoot();
    }

    private void setClickListener() {
        binding.tvProfileModify.setOnClickListener(this);

        binding.llTvArea.setOnClickListener(this);
        binding.llMovieArea.setOnClickListener(this);
        binding.llDestinyArea.setOnClickListener(this);
        binding.llLuckArea.setOnClickListener(this);
        binding.llExchangeArea.setOnClickListener(this);
        binding.llJobArea.setOnClickListener(this);
        binding.llEstateArea.setOnClickListener(this);
        binding.llShoppingArea.setOnClickListener(this);
    }

    private void setActionBar() {
        act.setSupportActionBar(binding.toolbar);
        actionBar = act.getSupportActionBar();
        actionBar.setTitle(null);
        actionBar.setDisplayHomeAsUpEnabled(false);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.appbar_actionbar_all, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.action_search:
                Common.showToast(act, "action_search menu03");
                return true;

            case R.id.action_qrcode:
                Common.showToast(act, "action_qrcode menu03");
                return true;

            case R.id.action_setting:
                startActivity(new Intent(act, SettingAct.class));
                return true;

        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_profile_modify:
                startActivity(new Intent(act, ProfileAct.class));
                break;
        }
    }
}