package kr.co.core.wetok.dialog;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.os.Bundle;
import android.view.Gravity;
import android.view.WindowManager;

import kr.co.core.wetok.R;
import kr.co.core.wetok.databinding.DialogAccPasswordBinding;

public class AccPasswordDialog extends AppCompatActivity {
    DialogAccPasswordBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.dialog_acc_password, null);

        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        params.height = WindowManager.LayoutParams.MATCH_PARENT;
//        params.windowAnimations = R.style.AnimationPopupStyle;
        getWindow().setAttributes(params);
    }
}
