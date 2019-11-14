package kr.co.core.wetok.dialog;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.app.Activity;
import android.os.Bundle;
import android.view.WindowManager;

import kr.co.core.wetok.R;
import kr.co.core.wetok.databinding.DialogPasswordBinding;

public class PasswordDlg extends AppCompatActivity {
    DialogPasswordBinding binding;
    Activity act;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.dialog_password, null);
        act = this;

        // dialog theme 을 적용했을때, 패딩제거 목적
        WindowManager.LayoutParams wmlp = getWindow().getAttributes();
        wmlp.width = WindowManager.LayoutParams.MATCH_PARENT;

    }
}
