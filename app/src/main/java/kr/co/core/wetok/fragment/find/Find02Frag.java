package kr.co.core.wetok.fragment.find;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import kr.co.core.wetok.R;
import kr.co.core.wetok.databinding.FragmentFind02Binding;
import kr.co.core.wetok.fragment.BaseFrag;
import kr.co.core.wetok.util.Common;

public class Find02Frag extends BaseFrag implements View.OnClickListener {
    private FragmentFind02Binding binding;
    private AppCompatActivity act;

    private ActionBar actionBar;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_find_02, container, false);
        act = (AppCompatActivity) getActivity();

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
                binding.etPw.getText().toString().equalsIgnoreCase(binding.etPwConfirm.getText().toString())
        ) {
            binding.tvConfirm.setBackgroundResource(R.drawable.wt_btn360_enable_191022);
        } else {
            binding.tvConfirm.setBackgroundResource(R.drawable.wt_btn360_disable_191022);
        }
    }

    private void showDialog() {
        LayoutInflater layout = LayoutInflater.from(act);
        View dialogLayout = layout.inflate(R.layout.dialog_password, null);
        final Dialog dialog = new Dialog(act, R.style.CodeDialog);

        dialog.setContentView(dialogLayout);
        dialog.setCancelable(false);
        dialog.show();

        TextView btn_ok = (TextView) dialogLayout.findViewById(R.id.tv_confirm);

        btn_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (dialog.isShowing()) {
                    dialog.dismiss();
                }

                act.setResult(Activity.RESULT_OK);
                act.finish();
            }
        });
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.tv_confirm) {
            if (binding.etPw.length() == 0) {
                Common.showToast(act, getString(R.string.find_pw_warning));
                return;
            }

            if (binding.etPwConfirm.length() == 0) {
                Common.showToast(act, getString(R.string.find_pw_confirm_warning));
                return;
            }

            if (binding.etPw.length() < 8 || binding.etPw.length() > 16 &&
                    binding.etPwConfirm.length() < 8 || binding.etPwConfirm.length() > 16) {
                Common.showToast(act, getString(R.string.find_pw_length_warning));
                return;
            }

            if (!binding.etPw.getText().toString().equals(binding.etPwConfirm.getText().toString())) {
                Common.showToast(act, getString(R.string.find_pw_same_warning));
                return;
            }

            showDialog();
        }
    }
}