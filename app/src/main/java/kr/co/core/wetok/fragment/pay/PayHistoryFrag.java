package kr.co.core.wetok.fragment.pay;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import kr.co.core.wetok.R;
import kr.co.core.wetok.databinding.FragmentPayHistoryBinding;
import kr.co.core.wetok.fragment.BaseFrag;

public class PayHistoryFrag extends BaseFrag implements View.OnClickListener {
    private FragmentPayHistoryBinding binding;
    private AppCompatActivity act;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_pay_history, container, false);
        act = (AppCompatActivity) getActivity();

        return binding.getRoot();
    }

    @Override
    public void onClick(View v) {
    }
}