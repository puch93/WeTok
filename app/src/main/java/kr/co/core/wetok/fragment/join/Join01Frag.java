package kr.co.core.wetok.fragment.join;



import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import java.lang.reflect.Field;

import kr.co.core.wetok.R;
import kr.co.core.wetok.activity.JoinAct;
import kr.co.core.wetok.adapter.WriteSpinnerAdapter;
import kr.co.core.wetok.databinding.FragmentJoin01Binding;
import kr.co.core.wetok.fragment.BaseFrag;
import kr.co.core.wetok.util.Common;
import kr.co.core.wetok.util.CustomSpinner;
import kr.co.core.wetok.util.StringUtil;

public class Join01Frag extends BaseFrag implements View.OnClickListener {
    private FragmentJoin01Binding binding;
    private AppCompatActivity act;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_join_01, container, false);
        act = (AppCompatActivity) getActivity();

        binding.tvConfirm.setOnClickListener(this);
        binding.etId.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
            @Override
            public void afterTextChanged(Editable s) {
                checkButtonActivation();
            }
        });


        return binding.getRoot();
    }

    private void checkButtonActivation() {
        if(!StringUtil.isNull(binding.etId.getText().toString())) {
            binding.tvConfirm.setBackgroundResource(R.drawable.wt_btn360_enable_191022);
        } else {
            binding.tvConfirm.setBackgroundResource(R.drawable.wt_btn360_disable_191022);
        }
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.tv_confirm) {
            if(StringUtil.isNull(binding.etId.getText().toString())) {
                Common.showToast(act, "아이디를 입력해주세요");
                return;
            }

            ((JoinAct) act).replaceFragment(new Join02Frag());
        }
    }
}