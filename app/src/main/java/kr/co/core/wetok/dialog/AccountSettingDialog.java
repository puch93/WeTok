package kr.co.core.wetok.dialog;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import kr.co.core.wetok.R;
import kr.co.core.wetok.databinding.DialogAccPasswordConfirmBinding;
import kr.co.core.wetok.databinding.DialogAccountSettingBinding;

public class AccountSettingDialog extends AppCompatActivity implements View.OnClickListener {
    DialogAccountSettingBinding binding;
    Activity act;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.dialog_account_setting, null);
        act = this;

        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        params.height = WindowManager.LayoutParams.MATCH_PARENT;
        getWindow().setAttributes(params);
//        getWindow().setGravity(Gravity.BOTTOM);

        /* set click listener */
        binding.tvOtherArea.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_other_area:
                finish();
                break;
        }
    }
}
