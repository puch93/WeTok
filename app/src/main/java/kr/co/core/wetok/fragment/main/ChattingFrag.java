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
import kr.co.core.wetok.activity.AddFriendAct;
import kr.co.core.wetok.databinding.FragmentChattingBinding;
import kr.co.core.wetok.fragment.BaseFrag;
import kr.co.core.wetok.util.Common;

public class ChattingFrag extends BaseFrag implements View.OnClickListener {
    FragmentChattingBinding binding;
    private AppCompatActivity act;

    private ActionBar actionBar;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_chatting, container, false);
        act = (AppCompatActivity) getActivity();
        setHasOptionsMenu(true);

        setActionBar();
        return binding.getRoot();
    }

    private void setActionBar() {
        act.setSupportActionBar(binding.toolbar);
        actionBar = act.getSupportActionBar();
        actionBar.setTitle(null);
        actionBar.setDisplayHomeAsUpEnabled(false);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.appbar_actionbar, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.action_search:
                Common.showToast(act, "action_search menu02");
                return true;

            case R.id.action_add:
                startActivity(new Intent(act, AddFriendAct.class));
                return true;

        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {

    }
}