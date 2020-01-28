package kr.co.core.wetok.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.databinding.DataBindingUtil;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.r0adkll.slidr.Slidr;
import com.r0adkll.slidr.model.SlidrConfig;
import com.r0adkll.slidr.model.SlidrInterface;
import com.r0adkll.slidr.model.SlidrListener;
import com.r0adkll.slidr.model.SlidrPosition;

import org.json.JSONException;
import org.json.JSONObject;

import io.realm.Realm;
import kr.co.core.wetok.R;
import kr.co.core.wetok.data.UserData;
import kr.co.core.wetok.databinding.ActivityProfileMeBinding;
import kr.co.core.wetok.preference.UserPref;
import kr.co.core.wetok.server.ReqBasic;
import kr.co.core.wetok.server.netUtil.HttpResult;
import kr.co.core.wetok.server.netUtil.NetUrls;
import kr.co.core.wetok.util.Common;
import kr.co.core.wetok.util.CustomApplication;
import kr.co.core.wetok.util.StringUtil;

public class ProfileMeAct extends BaseAct implements View.OnClickListener {
    ActivityProfileMeBinding binding;
    Activity act;

    private static final int PROFILE = 1000;

    private SlidrInterface slidr;


    UserData myInfoFromDB;
    Realm realm = null;

    private String id;
    private String pw;
    private String hp;
    private String intro;
    private String name;
    private String birth;

    private String profile_img;
    private String background_img;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_profile_me, null);
        act = this;

        /* full screen effect */
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);

        /* set slide animation effect */
        SlidrConfig config = new SlidrConfig.Builder()
                .position(SlidrPosition.TOP)
                .sensitivity(1f) // 1f -> 끌어내릴때 민감도인듯
                .scrimColor(Color.BLACK)
                .scrimStartAlpha(0f) // 0.8f
                .scrimEndAlpha(0f)
                .velocityThreshold(2400)  // 2400
                .distanceThreshold(0.25f) // 0.25f -> 끌어내릴때 마지노선
                .edge(false)
                .edgeSize(0.18f) // The % of the screen that counts as the edge, default 18%
                .build();
        slidr = Slidr.attach(this, config);

        /* close button margin */
        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) binding.flClose.getLayoutParams();
        params.topMargin = statusBarHeight();
        binding.flClose.setLayoutParams(params);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            binding.ivProfile.setClipToOutline(true);
        }

        /* my code */
        /* set click listener */
        setClickListener();

        /* set realm and get my info */
        CustomApplication application = (CustomApplication) act.getApplication();
        realm = application.getRealmObject();
        checkMyInfo();
    }

    private void checkMyInfo() {
        myInfoFromDB = realm.where(UserData.class).equalTo("idx_db", "0").findFirst();
        if (null != myInfoFromDB) {
            setMyInfo();
        } else {
            getMyInfo();
        }
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

                            writeDB();
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
                act.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        myInfoFromDB = realm.where(UserData.class).equalTo("idx_db", "0").findFirst();
                        setMyInfo();
                    }
                });
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
                act.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        myInfoFromDB = realm.where(UserData.class).equalTo("idx_db", "0").findFirst();
                        setMyInfo();
                    }
                });
            }
        }, new Realm.Transaction.OnError() {
            @Override
            public void onError(Throwable error) {
                Log.e(StringUtil.TAG, "updateDB onError: " + error.getMessage());
            }
        });
    }


    private int statusBarHeight() {
        int res_id = getResources().getIdentifier("status_bar_height", "dimen", "android");

        if (res_id > 0) {
            return getResources().getDimensionPixelSize(res_id);
        } else {
            return 0;
        }

    }

    private void setMyInfo() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // set name
                binding.tvName.setText(myInfoFromDB.getName());

                // set id
                binding.tvId.setText(myInfoFromDB.getId());

                // set profile image
                Glide.with(act)
                        .load(NetUrls.DOMAIN + myInfoFromDB.getProfile_img())
                        .into(binding.ivProfile);

                // set background image
                Glide.with(act)
                        .load(NetUrls.DOMAIN + myInfoFromDB.getBackground_img())
                        .into(
                                binding.ivBackground);
            }
        });
    }

    private void setClickListener() {
        binding.ivProfile.setOnClickListener(this);
        binding.ivBackground.setOnClickListener(this);

        binding.flClose.setOnClickListener(this);
        binding.flShare.setOnClickListener(this);

        binding.flProfileModify.setOnClickListener(this);

        binding.llUnse.setOnClickListener(this);
        binding.llMoney.setOnClickListener(this);
        binding.llStory.setOnClickListener(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == PROFILE) {
                checkMyInfo();
                setResult(RESULT_OK);
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_profile:
                if (null == myInfoFromDB || StringUtil.isNull(myInfoFromDB.getProfile_img()))
                    return;

                Intent profileIntent = new Intent(act, EnlargeAct.class);
                profileIntent.putExtra("imageUrl", NetUrls.DOMAIN + myInfoFromDB.getProfile_img());
                startActivity(profileIntent);
                break;

            case R.id.iv_background:
                if (null == myInfoFromDB || StringUtil.isNull(myInfoFromDB.getProfile_img()))
                    return;

                Intent backgroundIntent = new Intent(act, EnlargeAct.class);
                backgroundIntent.putExtra("imageUrl", NetUrls.DOMAIN + myInfoFromDB.getBackground_img());
                startActivity(backgroundIntent);
                break;


            case R.id.fl_close:
                finish();
                break;

            case R.id.fl_share:
                Common.showToastDevelop(act);
                break;

            case R.id.fl_profile_modify:
                startActivityForResult(new Intent(act, ProfileAct.class), PROFILE);
                break;

            case R.id.ll_unse:
                Common.showToastDevelop(act);
                break;

            case R.id.ll_money:
                Common.showToastDevelop(act);
                break;

            case R.id.ll_story:
                startActivity(new Intent(act, StoryAct.class));
                break;
        }
    }
}
