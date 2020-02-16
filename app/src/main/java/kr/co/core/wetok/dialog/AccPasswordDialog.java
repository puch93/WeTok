package kr.co.core.wetok.dialog;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;

import java.util.ArrayList;

import kr.co.core.wetok.R;
import kr.co.core.wetok.databinding.DialogAccPasswordBinding;
import kr.co.core.wetok.util.Common;

public class AccPasswordDialog extends AppCompatActivity implements View.OnClickListener {
    DialogAccPasswordBinding binding;
    Activity act;

    int count = 0;
    ArrayList<Integer> array_pass_int = new ArrayList<>();
    String pass_string = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.dialog_acc_password, null);
        act = this;

        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        params.height = WindowManager.LayoutParams.MATCH_PARENT;
        getWindow().setAttributes(params);
//        getWindow().setGravity(Gravity.BOTTOM);

        /* set click listener */
        binding.button00.setOnClickListener(this);
        binding.button01.setOnClickListener(this);
        binding.button02.setOnClickListener(this);
        binding.button03.setOnClickListener(this);
        binding.button04.setOnClickListener(this);
        binding.button05.setOnClickListener(this);
        binding.button06.setOnClickListener(this);
        binding.button07.setOnClickListener(this);
        binding.button08.setOnClickListener(this);
        binding.button09.setOnClickListener(this);
        binding.buttonBack.setOnClickListener(this);
        binding.tvOtherArea.setOnClickListener(this);
        binding.flClose.setOnClickListener(this);

        binding.button00.setTag(0);
        binding.button01.setTag(1);
        binding.button02.setTag(2);
        binding.button03.setTag(3);
        binding.button04.setTag(4);
        binding.button05.setTag(5);
        binding.button06.setTag(6);
        binding.button07.setTag(7);
        binding.button08.setTag(8);
        binding.button09.setTag(9);
    }

    private void refreshPassStaste() {
        binding.ivState01.setSelected(false);
        binding.ivState02.setSelected(false);
        binding.ivState03.setSelected(false);
        binding.ivState04.setSelected(false);

        switch (count) {
            case 1:
                binding.ivState01.setSelected(true);
                break;

            case 2:
                binding.ivState01.setSelected(true);
                binding.ivState02.setSelected(true);
                break;

            case 3:
                binding.ivState01.setSelected(true);
                binding.ivState02.setSelected(true);
                binding.ivState03.setSelected(true);
                break;

            case 4:
                binding.ivState01.setSelected(true);
                binding.ivState02.setSelected(true);
                binding.ivState03.setSelected(true);
                binding.ivState04.setSelected(true);
                break;

        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_other_area:
            case R.id.fl_close:
                finish();
                break;

            case R.id.button_back:
                if (count > 0) {
                    array_pass_int.remove(count - 1);
                    --count;

                    refreshPassStaste();
                }
                break;

            case R.id.button00:
            case R.id.button01:
            case R.id.button02:
            case R.id.button03:
            case R.id.button04:
            case R.id.button05:
            case R.id.button06:
            case R.id.button07:
            case R.id.button08:
            case R.id.button09:
                ++count;

                array_pass_int.add((Integer) v.getTag());

                refreshPassStaste();

                if (count == 4) {
                    for (int i = 0; i < 4; i++) {
                        pass_string += array_pass_int.get(i);
                    }

                    startActivity(new Intent(act, AccPasswordConfirmDialog.class));
                    overridePendingTransition(R.anim.open, R.anim.close);

                    finish();
                }
                break;
        }
    }
}
