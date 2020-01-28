package kr.co.core.wetok.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.databinding.DataBindingUtil;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.WindowManager;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Timer;
import java.util.TimerTask;

import io.realm.Realm;
import kr.co.core.wetok.R;
import kr.co.core.wetok.data.UserData;
import kr.co.core.wetok.databinding.ActivitySplashBinding;
import kr.co.core.wetok.preference.UserPref;
import kr.co.core.wetok.server.ReqBasic;
import kr.co.core.wetok.server.netUtil.HttpResult;
import kr.co.core.wetok.server.netUtil.NetUrls;
import kr.co.core.wetok.util.Common;
import kr.co.core.wetok.util.CustomApplication;
import kr.co.core.wetok.util.StringUtil;

public class SplashAct extends BaseAct {
    ActivitySplashBinding binding;
    Activity act;

    private static final int PERMISSION = 1000;
    private static final int NETWORK = 1001;

    private Timer timer = new Timer();
    boolean isReady = true;
    String fcm_token, device_version;


    /* auto login */
    String id;
    String pw;
    String hp;
    String intro;
    String name;
    String birth;
    String profile_img;
    String background_img;

    Realm realm;
    UserData myInfoFromDB;

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

        checkVersion();
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

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            if (!StringUtil.isNull(res)) {
                                JSONObject jo = new JSONObject(res);
                                if (StringUtil.getStr(jo, "result").equalsIgnoreCase("Y")) {
                                    android.app.AlertDialog.Builder alertDialogBuilder =
                                            new android.app.AlertDialog.Builder(new ContextThemeWrapper(act, android.R.style.Theme_DeviceDefault_Light_Dialog_Alert));
                                    alertDialogBuilder.setTitle("업데이트");
                                    alertDialogBuilder.setMessage("새로운 버전이 있습니다.")
                                            .setPositiveButton("업데이트 바로가기", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialogInterface, int i) {
                                                    //TODO 관련코드 작성
                                                    Common.showToast(act, "버전 업데이트하기");
                                                    startProgram();
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
                                } else {
                                    startProgram();
                                }
                            } else {
                                startProgram();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        };

        server.setTag("App Version");
        server.addParams("siteUrl", NetUrls.SITEURL);
        server.addParams("CONNECTCODE", "APP");

        server.addParams("dbControl", NetUrls.GET_VERSION);
        server.addParams("thisVer", device_version);
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

                            if (UserPref.getAutoLogin(act)) {
                                setAutoLogin();
                            } else {
                                startActivity(new Intent(act, LoginAct.class));
                                finish();
                            }

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
                            checkSelfPermission(android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
                            checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED
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


    /* auto login */
    private void setAutoLogin() {
        ReqBasic server = new ReqBasic(act, NetUrls.ADDRESS) {
            @Override
            public void onAfter(int resultCode, HttpResult resultData) {

                if (resultData.getResult() != null) {
                    try {
                        JSONObject jo = new JSONObject(resultData.getResult());

                        if (jo.getString("result").equalsIgnoreCase("Y")) {
                            // set midx
                            UserPref.setMidx(act, jo.getString("MEMCODE"));

                            // set auto login
                            UserPref.setAutoLogin(act, true);

                            getMyInfo();
                        } else {

                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                        Common.showToastNetwork(act);
                    }
                } else {
                    Common.showToastNetwork(act);
                }
            }
        };

        server.setTag("Auto Login");
        server.addParams("siteUrl", NetUrls.SITEURL);
        server.addParams("CONNECTCODE", "APP");

        server.addParams("dbControl", NetUrls.USER_LOGIN);
        server.addParams("m_regi", fcm_token);
        server.addParams("m_uniq", UserPref.getDeviceId(act));
        server.addParams("m_id", UserPref.getId(act));
        server.addParams("m_pass", UserPref.getPw(act));
        server.execute(true, false);
    }

    private void getMyInfo() {
        ReqBasic server = new ReqBasic(act, NetUrls.ADDRESS) {
            @Override
            public void onAfter(int resultCode, HttpResult resultData) {
                if (resultData.getResult() != null) {
                    try {
                        JSONObject jo = new JSONObject(resultData.getResult());

                        if (resultData.getResult() != null) {
                            id = jo.getString("m_id");
                            pw = jo.getString("m_pass");
                            hp = jo.getString("m_hp");
                            intro = jo.getString("m_intro");
                            name = jo.getString("m_nickname");
                            birth = jo.getString("m_birthday");
                            profile_img = jo.getString("m_profile");
                            background_img = jo.getString("m_background");

                            processDB();

                        } else {
                            Common.showToastNetwork(act);
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                        Common.showToastNetwork(act);
                    }
                } else {
                    Common.showToastNetwork(act);
                }
            }
        };

        server.setTag("My Info");
        server.addParams("siteUrl", NetUrls.SITEURL);
        server.addParams("CONNECTCODE", "APP");
        server.addParams("_APP_MEM_IDX", UserPref.getMidx(act));

        server.addParams("dbControl", NetUrls.GET_MY_INFO);
        server.execute(true, false);
    }

    private void processDB() {
        CustomApplication application = (CustomApplication) act.getApplication();
        application.setFriend_count(null);

        realm = application.getRealmObject();

        myInfoFromDB = realm.where(UserData.class).equalTo("idx_db", "0").findFirst();
        if (null != myInfoFromDB) {
            updateDB();
        } else {
            writeDB();
        }
    }

    private void writeDB() {
        realm.executeTransactionAsync(new Realm.Transaction() {
            @Override
            public void execute(@NonNull Realm realm) {
                UserData data = realm.createObject(UserData.class, "0");
                data.setData(UserPref.getMidx(act), id, pw, hp, intro, name, birth, profile_img, background_img);
            }
        }, new Realm.Transaction.OnSuccess() {
            @Override
            public void onSuccess() {
                getContacts01(hp);
            }
        }, new Realm.Transaction.OnError() {
            @Override
            public void onError(Throwable error) {
                Log.e(StringUtil.TAG, "onError: " + error.getMessage());
                updateDB();
            }
        });
    }

    private void updateDB() {
        realm.executeTransactionAsync(new Realm.Transaction() {
            @Override
            public void execute(@NonNull Realm realm) {
                UserData data = realm.where(UserData.class).equalTo("idx_db", "0").findFirst();
                if (data != null) {
                    data.setData(UserPref.getMidx(act), id, pw, hp, intro, name, birth, profile_img, background_img);
                }
            }
        }, new Realm.Transaction.OnSuccess() {
            @Override
            public void onSuccess() {
                getContacts01(hp);
            }
        }, new Realm.Transaction.OnError() {
            @Override
            public void onError(Throwable error) {
                Log.e(StringUtil.TAG, "updateDB onError: " + error.getMessage());
            }
        });
    }

    private void getContacts01(String hp) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    JSONObject total = new JSONObject();
                    JSONArray ja = new JSONArray();

                    // 1. Resolver 가져오기(데이터베이스 열어주기)
                    ContentResolver resolver = getContentResolver();

                    // 2. 전화번호가 저장되어 있는 테이블 주소값(Uri)을 가져오기
                    Uri phoneUri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;

                    // 3. 테이블에 정의된 칼럼 가져오기
                    String[] projection = {ContactsContract.CommonDataKinds.Phone.CONTACT_ID
                            , ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME
                            , ContactsContract.CommonDataKinds.Phone.NUMBER};

                    // 4. ContentResolver로 쿼리를 날림 -> resolver 가 provider 에게 쿼리하겠다고 요청
                    Cursor cursor = resolver.query(phoneUri, projection, null, null, ContactsContract.Data.RAW_CONTACT_ID + " ASC");

                    // 5. 커서로 리턴된다. 반복문을 돌면서 cursor 에 담긴 데이터를 하나씩 추출
//                    int i = 0;
                    if (cursor != null) {
                        while (cursor.moveToNext()) {
//                            if (i > 4) {
//                                break;
//                            }
//                            i++;

                            JSONObject jo = new JSONObject();

                            // 4.1 이름으로 인덱스를 찾아준다
                            int idIndex = cursor.getColumnIndex(projection[0]); // 이름을 넣어주면 그 칼럼을 가져와준다.
                            int nameIndex = cursor.getColumnIndex(projection[1]);
                            int numberIndex = cursor.getColumnIndex(projection[2]);

                            // 4.2 해당 index 를 사용해서 실제 값을 가져온다.
                            String id = cursor.getString(idIndex);
                            String name = cursor.getString(nameIndex);
                            String number = cursor.getString(numberIndex);

                            Log.e("TEST_HOME", "id: " + id);
                            Log.e("TEST_HOME", "name: " + name);
                            Log.e("TEST_HOME", "number: " + number);
                            Log.e("TEST_HOME", "--------------------------------------------");


                            number = number.replace(" ", "");
                            number = number.replace("-", "");
                            number = number.replace("//", "");

                            if (!hp.equalsIgnoreCase(number)) {
                                jo.put("address", number);
                                ja.put(jo);
                            }
                        }
                    }
                    // 데이터 계열은 반드시 닫아줘야 한다.
                    cursor.close();
                    total.put("address_list", ja);

                    Log.e(StringUtil.TAG, "total(JSONArray): " + total);

                    // send server
                    setSychronizeNumber(total);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void setSychronizeNumber(final JSONObject total) {
        ReqBasic server = new ReqBasic(act, NetUrls.ADDRESS) {
            @Override
            public void onAfter(int resultCode, HttpResult resultData) {
                if (resultData.getResult() != null) {
                    try {
                        JSONObject jo = new JSONObject(resultData.getResult());

                        if (jo.getString("result").equalsIgnoreCase("Y")) {
                            startActivity(new Intent(act, MainAct.class));
                            finish();
                        } else {
                            startActivity(new Intent(act, MainAct.class));
                            finish();
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                        Common.showToastNetwork(act);

                        startActivity(new Intent(act, MainAct.class));
                        finish();
                    }
                } else {
                    Common.showToastNetwork(act);

                    startActivity(new Intent(act, MainAct.class));
                    finish();
                }
            }
        };

        server.setTag("Synchro Number");
        server.addParams("siteUrl", NetUrls.SITEURL);
        server.addParams("CONNECTCODE", "APP");
        server.addParams("_APP_MEM_IDX", UserPref.getMidx(act));

        server.addParams("dbControl", NetUrls.SET_FRIEND_SYNC);
        server.addParams("addr_list", total.toString());
        server.execute(true, false);
    }
}
