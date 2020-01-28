package kr.co.core.wetok.fragment.join;



import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import java.util.regex.Pattern;

import kr.co.core.wetok.R;
import kr.co.core.wetok.activity.JoinAct;
import kr.co.core.wetok.databinding.FragmentJoin01Binding;
import kr.co.core.wetok.databinding.FragmentJoin02Binding;
import kr.co.core.wetok.fragment.BaseFrag;
import kr.co.core.wetok.util.Common;
import kr.co.core.wetok.util.StringUtil;

public class Join02Frag extends BaseFrag implements View.OnClickListener {
    private FragmentJoin02Binding binding;
    private AppCompatActivity act;
    private String id;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_join_02, container, false);
        act = (AppCompatActivity) getActivity();

        id = getArguments().getString("id");

        binding.tvConfirm.setOnClickListener(this);

        binding.etPw.addTextChangedListener(textWatcher);
        binding.etPwConfirm.addTextChangedListener(textWatcher);

        return binding.getRoot();
    }

    private TextWatcher textWatcher = new TextWatcher() {
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
    };


    private void checkButtonActivation() {
        if (binding.etPw.length() != 0 &&
                binding.etPwConfirm.length() != 0 &&
                binding.etPw.getText().toString().equalsIgnoreCase(binding.etPwConfirm.getText().toString()) &&
                binding.etPw.length() >= 8 &&
                Pattern.matches("^[a-zA-Z0-9!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?~`]+$", binding.etPw.getText().toString())
        ) {
            binding.tvConfirm.setBackgroundResource(R.drawable.wt_btn360_enable_191022);
        } else {
            binding.tvConfirm.setBackgroundResource(R.drawable.wt_btn360_disable_191022);
        }
    }

    private void nextProcess() {
        BaseFrag fragment = new Join03Frag();

        Bundle bundle = new Bundle(1);
        bundle.putString("id", id);
        bundle.putString("pw", binding.etPw.getText().toString());
        fragment.setArguments(bundle);

        ((JoinAct) act).replaceFragment(fragment);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.tv_confirm) {
            // 비밀번호 길이 0 검사
            if (binding.etPw.length() == 0 || binding.etPwConfirm.length() == 0) {
                Common.showToast(act, getString(R.string.join_pw_length_warning));
                return;
            }

            // 비밀번호 길이 검사
            if (binding.etPw.length() < 8 && binding.etPwConfirm.length() < 8) {
                Common.showToast(act, getString(R.string.join_pw_length_warning));
                return;
            }

            // 비밀번호 같은지 검사
            if (!binding.etPw.getText().toString().equals(binding.etPwConfirm.getText().toString())) {
                Common.showToast(act, getString(R.string.join_pw_same_warning));
                return;
            }

            // 정규식 검사 (특수문자, 영어, 숫자)
            if(!Pattern.matches("^[a-zA-Z0-9!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?~`]+$", binding.etPw.getText().toString())) {
                Common.showToast(act, getString(R.string.join_pw_check_warning));
                return;
            }

            nextProcess();
        }
    }
}