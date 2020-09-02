package kr.co.core.wetok.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.Window;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import io.realm.Realm;
import io.realm.exceptions.RealmPrimaryKeyConstraintException;
import kr.co.core.wetok.R;
import kr.co.core.wetok.data.UserData;
import kr.co.core.wetok.databinding.ActivityLoginBinding;
import kr.co.core.wetok.preference.SystemPref;
import kr.co.core.wetok.preference.UserPref;
import kr.co.core.wetok.server.ReqBasic;
import kr.co.core.wetok.server.netUtil.HttpResult;
import kr.co.core.wetok.server.netUtil.NetUrls;
import kr.co.core.wetok.util.Common;
import kr.co.core.wetok.util.CustomApplication;
import kr.co.core.wetok.util.KeyboardHeightObserver;
import kr.co.core.wetok.util.KeyboardHeightProvider;
import kr.co.core.wetok.util.StringUtil;

public class LoginAct extends AppCompatActivity implements View.OnClickListener {
    ActivityLoginBinding binding;
    public static Activity act;

    private static final int CODE_JOIN = 1001;
    private static final int CODE_FIND = 1002;

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

    int origin = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_login, null);
        act = this;

        setClickListener();

        // 키보드 높이 구하기
        Window mRootWindow = getWindow();
        View mRootView = mRootWindow.getDecorView().findViewById(android.R.id.content);
        mRootView.getViewTreeObserver().addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {
                    public void onGlobalLayout() {
                        Rect r = new Rect();
                        View view = mRootWindow.getDecorView();
                        view.getWindowVisibleDisplayFrame(r);

                        if (origin == 0) {
                            origin = r.bottom;
                            SystemPref.setViewHeight(act, origin);
                        }
                        // r.left, r.top, r.right, r.bottom

                        Log.e(StringUtil.TAG, "onGlobalLayout: " + r.bottom + ", " + r.top);

                        int heightDiff = origin - r.bottom;
                        if(heightDiff > 0) {
                            SystemPref.setKeyBoardHeight(act, heightDiff);
                        }
                        Log.e(StringUtil.TAG, "heightDiff: " + heightDiff);
                    }
                });
    }


    // 연락처 테스트
    private void contactsTest() {
        try {
            JSONObject total = new JSONObject();
            JSONArray ja = new JSONArray();


            JSONObject jo1 = new JSONObject();
            jo1.put("address", "821074717614");
            ja.put(jo1);

            JSONObject jo3 = new JSONObject();
            jo3.put("address", "+8218295285762");
            ja.put(jo3);

            total.put("address_list", ja);
            setSychronizeNumber(total);
        } catch (JSONException e) {
            e.printStackTrace();
        }
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
                    if (cursor != null) {
                        while (cursor.moveToNext()) {

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

    private void setLogin() {
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
                            UserPref.setId(act, binding.etId.getText().toString());
                            UserPref.setPw(act, binding.etPw.getText().toString());

                            getMyInfo();
                        } else {
                            String message = jo.getString("message");
                            Common.showToast(act, message);
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

        server.setTag("Login");
        server.addParams("siteUrl", NetUrls.SITEURL);
        server.addParams("CONNECTCODE", "APP");

        server.addParams("dbControl", NetUrls.USER_LOGIN);
        server.addParams("m_regi", UserPref.getFcmToken(act));
        server.addParams("m_uniq", UserPref.getDeviceId(act));
        server.addParams("m_id", binding.etId.getText().toString());
        server.addParams("m_pass", binding.etPw.getText().toString());
        server.execute(true, true);
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

    private void setClickListener() {
        binding.tvFindPw.setOnClickListener(this);
        binding.tvJoin.setOnClickListener(this);
        binding.tvLogin.setOnClickListener(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case CODE_JOIN:
                    getMyInfo();
                    break;

                case CODE_FIND:
                    Common.showToast(act, getString(R.string.login_pw_after));
                    break;
            }
        }
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv_find_pw:
                startActivityForResult(new Intent(act, FindPwAct.class), CODE_FIND);
                break;

            case R.id.tv_join:
                startActivityForResult(new Intent(act, JoinAct.class), CODE_JOIN);
                break;

            case R.id.tv_login:
                if (binding.etId.length() == 0) {
                    Common.showToast(act, getString(R.string.login_id_warning));
                    return;
                }

                if (binding.etPw.length() == 0) {
                    Common.showToast(act, getString(R.string.login_pw_warning));
                    return;
                }

                if (binding.etPw.length() < 8 || binding.etPw.length() > 16) {
                    Common.showToast(act, getString(R.string.login_pw_length_warning));
                    return;
                }

                setLogin();
                break;
        }
    }
}
