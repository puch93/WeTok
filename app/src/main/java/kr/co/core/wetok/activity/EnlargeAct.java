package kr.co.core.wetok.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;

import com.bumptech.glide.Glide;

import kr.co.core.wetok.R;
import kr.co.core.wetok.databinding.ActivityEnlargeBinding;
import kr.co.core.wetok.util.Common;
import kr.co.core.wetok.util.StringUtil;

public class EnlargeAct extends AppCompatActivity {
    ActivityEnlargeBinding binding;
    Activity act;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_enlarge, null);
        act = this;

        String imageUrl = getIntent().getStringExtra("imageUrl");
        if(StringUtil.isNull(imageUrl)) {
            Common.showToast(act, getString(R.string.act_enlarge_warning));
        } else {
            Glide.with(act)
                    .load(imageUrl)
                    .into(binding.photoView);
        }

        binding.flBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}
