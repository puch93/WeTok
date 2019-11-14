package kr.co.core.wetok.activity;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import kr.co.core.wetok.R;
import kr.co.core.wetok.databinding.ActivityLoginBinding;
import kr.co.core.wetok.util.Common;

public class LoginAct extends AppCompatActivity implements View.OnClickListener {
    ActivityLoginBinding binding;
    Activity act;

    private static final int CODE_JOIN = 1001;
    private static final int CODE_FIND = 1002;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_login, null);
        act = this;

        setClickListener();
    }

    private void setClickListener() {
        binding.tvFindPw.setOnClickListener(this);
        binding.tvJoin.setOnClickListener(this);
        binding.tvLogin.setOnClickListener(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case CODE_JOIN:
                if(resultCode == RESULT_OK) {
                    startActivity(new Intent(act, MainAct.class));
                    finish();
                }
                break;

            case CODE_FIND:
                break;
        }
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv_find_pw:
                startActivityForResult(new Intent(act, FindPwAct.class), CODE_FIND);
                break;

            case R.id.tv_join:
                startActivityForResult(new Intent(act, JoinAct.class), CODE_JOIN);
                break;

            case R.id.tv_login:
                if(binding.etId.length() == 0) {
                    Common.showToast(act, getString(R.string.login_id_warning));
                    return;
                }

                if(binding.etPw.length() == 0) {
                    Common.showToast(act, getString(R.string.login_pw_warning));
                    return;
                }

                if (binding.etPw.length() < 8 || binding.etPw.length() > 16) {
                    Common.showToast(act, getString(R.string.login_id_warning));
                    return;
                }

                startActivity(new Intent(act, MainAct.class));
                finish();
                break;
        }
    }
}
