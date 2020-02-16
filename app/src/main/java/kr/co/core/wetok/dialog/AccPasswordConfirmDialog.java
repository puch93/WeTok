package kr.co.core.wetok.dialog;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import java.util.ArrayList;

import kr.co.core.wetok.R;
import kr.co.core.wetok.databinding.DialogAccPasswordBinding;
import kr.co.core.wetok.databinding.DialogAccPasswordConfirmBinding;
import kr.co.core.wetok.util.Common;

public class AccPasswordConfirmDialog extends AppCompatActivity implements View.OnClickListener {
    DialogAccPasswordConfirmBinding binding;
    Activity act;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.dialog_acc_password_confirm, null);
        act = this;

        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        params.height = WindowManager.LayoutParams.MATCH_PARENT;
        getWindow().setAttributes(params);
//        getWindow().setGravity(Gravity.BOTTOM);

        /* set click listener */
        binding.tvOtherArea.setOnClickListener(this);
        binding.tvConfirm.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_other_area:
            case R.id.tv_confirm:
                finish();
                break;
        }
    }
}
