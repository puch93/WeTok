package kr.co.core.wetok.dialog;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;

import androidx.databinding.DataBindingUtil;

import com.bumptech.glide.Glide;

import kr.co.core.wetok.R;
import kr.co.core.wetok.activity.BaseAct;
import kr.co.core.wetok.databinding.DialogPopupAdBinding;
import kr.co.core.wetok.server.netUtil.NetUrls;
import kr.co.core.wetok.util.StringUtil;

public class PopUpAdDlg extends BaseAct {
    DialogPopupAdBinding binding;
    Activity act;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // 여백터치시 꺼지지 않게
        setFinishOnTouchOutside(false);

        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.dialog_popup_ad, null);
        act = this;

        // dialog theme 을 적용했을때, 패딩제거 목적
        WindowManager.LayoutParams wmlp = getWindow().getAttributes();
        wmlp.width = WindowManager.LayoutParams.MATCH_PARENT;

        String imageUrl = getIntent().getStringExtra("imageUrl");
        Log.e(StringUtil.TAG, "imageUrl: " + imageUrl);
        final String url = getIntent().getStringExtra("url");

        Glide.with(act).load(NetUrls.DOMAIN + imageUrl).into(binding.ivContents);

        /* ok button listener */
        binding.ivClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
                notificationManager.cancel(1);

                finish();
            }
        });

        binding.ivContents.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
                notificationManager.cancel(1);

                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
                finish();
            }
        });
    }

    @Override
    public void onBackPressed() {
//        super.onBackPressed();
    }
}
