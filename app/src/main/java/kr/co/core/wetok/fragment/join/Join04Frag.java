package kr.co.core.wetok.fragment.join;



import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import kr.co.core.wetok.R;
import kr.co.core.wetok.databinding.FragmentJoin03Binding;
import kr.co.core.wetok.databinding.FragmentJoin04Binding;
import kr.co.core.wetok.fragment.BaseFrag;

public class Join04Frag extends BaseFrag implements View.OnClickListener {
    private FragmentJoin04Binding binding;
    private AppCompatActivity act;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_join_04, container, false);
        act = (AppCompatActivity) getActivity();

        binding.tvConfirm.setOnClickListener(this);


        return binding.getRoot();
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.tv_confirm) {
            act.setResult(Activity.RESULT_OK);
            act.finish();
        }
    }
}