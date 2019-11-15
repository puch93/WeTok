package kr.co.core.wetok.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.databinding.DataBindingUtil;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.WindowManager;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Timer;
import java.util.TimerTask;

import kr.co.core.wetok.R;
import kr.co.core.wetok.databinding.ActivitySplashBinding;
import kr.co.core.wetok.preference.UserPref;
import kr.co.core.wetok.server.ReqBasic;
import kr.co.core.wetok.server.netUtil.HttpResult;
import kr.co.core.wetok.server.netUtil.NetUrls;
import kr.co.core.wetok.util.Common;
import kr.co.core.wetok.util.StringUtil;

public class SplashAct extends BaseAct {
    ActivitySplashBinding binding;
    Activity act;

    private static final int PERMISSION = 1000;
    private static final int NETWORK = 1001;

    private Timer timer = new Timer();
    boolean isReady = true;
    String fcm_token, device_version;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_splash, null);
        act = this;

        try {
            device_version = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        startProgram();
    }

    private void startProgram() {
        if (!checkPermission()) {
            startActivityForResult(new Intent(act, PermissionAct.class), PERMISSION);
        } else {
            UserPref.setDeviceId(act, Common.getDeviceId(act));
            getFcmToken();
            checkSetting();
        }
    }

    private void checkVersion() {
        ReqBasic server = new ReqBasic(act, NetUrls.ADDRESS) {
            @Override
            public void onAfter(int resultCode, HttpResult resultData) {

                final String res = resultData.getResult();

                if (res != null) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                JSONObject jo = new JSONObject(res);

                                if (jo.getString("result").equalsIgnoreCase("Y")) {
                                    String[] version = jo.getString("value").split("\\.");
                                    String[] version_me = device_version.split("\\.");

                                    for (int i = 0; i < 3; i++) {
                                        int tmp1 = Integer.parseInt(version[i]);
                                        int tmp2 = Integer.parseInt(version_me[i]);

                                        if (tmp2 < tmp1) {
                                            if (i < 2) {
                                                android.app.AlertDialog.Builder alertDialogBuilder =
                                                        new android.app.AlertDialog.Builder(new ContextThemeWrapper(act, android.R.style.Theme_DeviceDefault_Light_Dialog_Alert));
                                                alertDialogBuilder.setTitle("업데이트");
                                                alertDialogBuilder.setMessage("새로운 버전이 있습니다.")
                                                        .setPositiveButton("업데이트 바로가기", new DialogInterface.OnClickListener() {
                                                            @Override
                                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                                //TODO 업데이트 관련 프로세스 작성해야함
                                                                finish();
                                                            }
                                                        });
                                                android.app.AlertDialog alertDialog = alertDialogBuilder.create();
                                                alertDialog.setCanceledOnTouchOutside(false);
                                                alertDialog.show();

                                                return;
                                            } else {
                                                android.app.AlertDialog.Builder alertDialogBuilder =
                                                        new android.app.AlertDialog.Builder(new ContextThemeWrapper(act, android.R.style.Theme_DeviceDefault_Light_Dialog_Alert));
                                                alertDialogBuilder.setTitle("업데이트");
                                                alertDialogBuilder.setMessage("새로운 버전이 있습니다.")
                                                        .setPositiveButton("업데이트 바로가기", new DialogInterface.OnClickListener() {
                                                            @Override
                                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                                //TODO 업데이트 관련 프로세스 작성해야함
                                                                finish();
                                                            }
                                                        }).setNegativeButton("기존버전으로 계속하기", new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        startProgram();
                                                    }
                                                });
                                                android.app.AlertDialog alertDialog = alertDialogBuilder.create();
                                                alertDialog.setCanceledOnTouchOutside(false);
                                                alertDialog.show();

                                                return;
                                            }
                                        }
                                    }
                                    startProgram();
                                } else {
                                    startProgram();
                                }

                            } catch (JSONException e) {
                                Common.showToastNetwork(act);
                                e.printStackTrace();
                            }
                        }
                    });
                } else {
                    Common.showToastNetwork(act);
                }
            }
        };

        server.setTag("App Version");
        server.addParams("dbControl", NetUrls.GET_VERSION);
        server.execute(true, false);
    }

    private void getFcmToken() {
        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                    @Override
                    public void onComplete(@NonNull Task<InstanceIdResult> task) {
                        if (!task.isSuccessful()) {
                            Log.i(StringUtil.TAG, "getInstanceId failed", task.getException());
                            return;
                        }
                        // Get new Instance ID token
                        String token = task.getResult().getToken();
                        Log.i(StringUtil.TAG, "myFcmToken: " + token);
                        UserPref.setFcmToken(act, token);
                        fcm_token = token.replace("%3", ":");
                    }
                });
    }


    private void checkSetting() {
        checkNetwork(new Runnable() {
            @Override
            public void run() {
                checkTimer();
            }
        });
    }

    //데이터 또는 WIFI 켜져 있는지 확인 / 안켜져있으면 데이터 설정창으로
    private void checkNetwork(final Runnable afterCheckAction) {
        ConnectivityManager manager = (ConnectivityManager) act.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = null;
        if (manager != null) {
            networkInfo = manager.getActiveNetworkInfo();
        }
        if (networkInfo != null && networkInfo.isConnected()) {
            if (afterCheckAction != null) {
                afterCheckAction.run();
            }
        } else {
            showNetworkAlert();
        }
    }

    //네트워크 연결 다이얼로그
    public void showNetworkAlert() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(act);

        alertDialog.setCancelable(false);
        alertDialog.setTitle("네트워크 사용유무");
        alertDialog.setMessage("인터넷이 연결되어 있지 않습니다. \n설정창으로 가시겠습니까?");

        // OK 를 누르게 되면 설정창으로 이동합니다.
        alertDialog.setPositiveButton("Settings",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        Intent in = new Intent(Settings.ACTION_DATA_USAGE_SETTINGS);
                        in.addCategory(Intent.CATEGORY_DEFAULT);
                        startActivityForResult(in, NETWORK);
                    }
                });
        // Cancle 하면 종료 합니다.
        alertDialog.setNegativeButton("Cancel",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        finish();
                    }
                });
        alertDialog.show();
    }

    //로딩중 텍스트 애니메이션
    public void checkTimer() {
        TimerTask adTask = new TimerTask() {
            @Override
            public void run() {
                new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (fcm_token != null && isReady) {
                            isReady = false;
//                            loginInfo();
                            startActivity(new Intent(act, LoginAct.class));
                            finish();
                            timer.cancel();
                        }
                    }
                }, 0);
            }
        };
        timer.schedule(adTask, 0, 1000);
    }

    private boolean checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (
                    checkSelfPermission(android.Manifest.permission.WRITE_CONTACTS) != PackageManager.PERMISSION_GRANTED ||
                            checkSelfPermission(android.Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED ||
                            checkSelfPermission(android.Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED ||
                            checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                            checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                            checkSelfPermission(android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
            ) {
                return false;
            } else {
                return true;
            }
        } else {
            return true;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.e(StringUtil.TAG, "resultCode: " + resultCode);

        if (resultCode != RESULT_OK && resultCode != RESULT_CANCELED)
            return;

        switch (requestCode) {
            case PERMISSION:
                if (resultCode == RESULT_CANCELED) {
                    finish();
                } else {
                    startProgram();
                }
                break;

            case NETWORK:
                checkSetting();
                break;
        }
    }
}
