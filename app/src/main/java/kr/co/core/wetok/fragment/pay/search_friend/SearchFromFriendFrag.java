package kr.co.core.wetok.fragment.pay.search_friend;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import kr.co.core.wetok.R;
import kr.co.core.wetok.databinding.FragmentSearchFromFriendBinding;
import kr.co.core.wetok.fragment.BaseFrag;

public class SearchFromFriendFrag extends BaseFrag implements View.OnClickListener {
    private FragmentSearchFromFriendBinding binding;
    private AppCompatActivity act;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_search_from_friend, container, false);
        act = (AppCompatActivity) getActivity();

        return binding.getRoot();
    }

    @Override
    public void onClick(View v) {
    }
}