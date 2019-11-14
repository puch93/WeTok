package kr.co.core.wetok.server.netUtil;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Handler;
import android.util.Log;

import kr.co.core.wetok.server.CustomLoading;
import kr.co.core.wetok.server.inter.OnAfterConnection;
import kr.co.core.wetok.server.inter.OnParsingResult;
import kr.co.core.wetok.util.StringUtil;

import java.io.File;

/*
서버 통신 슈퍼 클래스.
이후 이 클래스를 상속받은 자식 클래스를 만들어서 사용하면 됩니다.
자식 클래스에서 OnParsingResult, OnAfterConnection 인터페이스 작성해서 사용하면 됨.
 */
public abstract class BaseReq implements OnParsingResult, OnAfterConnection {
    private Context context;
    public String TAG;

    //실제로 서버와 통신을 하는 RequestConnection 객체
    RequestConnection connection;
    //ProgressDialog, 개인이 커스텀한 다이어로그로 변경 가능
    CustomLoading customDialog;
    ProgressDialog defaultDialog;
    private Handler handler;

    public BaseReq(Context context, String url) {
        this.context = context;
        this.connection = new RequestConnection(url);
        //OnParsingResult는 자식 클래스에서 정의
        this.connection.setOnParsingResult(this);
        //OnAfterConnection은 자식 클래스에서 정의
        this.connection.setOnAfter(new OnAfterConnection() {
            @Override
            public void onAfter(final int resultCode, final HttpResult resultData) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        BaseReq.this.onAfter(resultCode, resultData);
                        setProgress(false);
                    }
                });
            }
        });

        this.handler = new Handler(context.getMainLooper());
    }

    public void execute(boolean createNewThread, boolean showProgress) {
        if (showProgress) {
            setProgress(true);
        }
        connection.execute(createNewThread);
    }

    public void setProgress(final boolean isVisible) {
        handler.post(new Runnable() {
            public void run() {
                /* custom dialog */
                if (isVisible) {
                    if (customDialog == null) {
                        customDialog = new CustomLoading(context);
                    }

                    if (!((Activity) context).isFinishing())
                        customDialog.progressON("loading..");

                } else {
                    if (customDialog != null) {
                        customDialog.progressOFF();
                    }
                }


                /* default dialog */
//                if (isVisible) {
//                    if (defaultDialog == null) {
//                        //Custom Progress Dialog를 사용하면 해당 Dialog로 바꾸세요.
//                        defaultDialog = new ProgressDialog(context);
//                        defaultDialog.setMessage("LOADING");
//                        defaultDialog.setCancelable(false);
//                    }
//
//                    if (!((Activity) context).isFinishing())
//                        defaultDialog.show();
//
//                } else {
//                    if (defaultDialog != null && defaultDialog.isShowing()) {
//                        defaultDialog.dismiss();
//                    }
//                }
            }
        });
    }

    public void setTag(String TAG) {
        this.TAG = TAG;
        Log.e(StringUtil.TAG, TAG + " Put Info");
    }


    //일반 파라미터 추가
    public void addParams(String key, String value) {
        connection.addParams(key, value);
        if(!StringUtil.isNull(value))
            Log.e(StringUtil.TAG, "key: " + key + ", value: " + value);
        else
            Log.e(StringUtil.TAG, "key: " + key + ", value: null");
    }

    //파일 파라미터 추가
    public void addFileParams(String key, File file) {
        connection.addFileParams(key, file);
        Log.e(StringUtil.TAG, "(file)" + key + " = " + file.length());
    }

    public void addArrParams(String key, String[] value) {
        connection.addArrParams(key, value);
    }
}