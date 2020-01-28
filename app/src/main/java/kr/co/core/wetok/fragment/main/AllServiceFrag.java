package kr.co.core.wetok.fragment.main;



import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;

import org.json.JSONException;
import org.json.JSONObject;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.exceptions.RealmMigrationNeededException;
import io.realm.exceptions.RealmPrimaryKeyConstraintException;
import kr.co.core.wetok.R;
import kr.co.core.wetok.activity.ProfileAct;
import kr.co.core.wetok.activity.SettingAct;
import kr.co.core.wetok.data.UserData;
import kr.co.core.wetok.databinding.FragmentAllServiceBinding;
import kr.co.core.wetok.fragment.BaseFrag;
import kr.co.core.wetok.preference.UserPref;
import kr.co.core.wetok.server.ReqBasic;
import kr.co.core.wetok.server.netUtil.HttpResult;
import kr.co.core.wetok.server.netUtil.NetUrls;
import kr.co.core.wetok.util.Common;
import kr.co.core.wetok.util.CustomApplication;
import kr.co.core.wetok.util.StringUtil;

public class AllServiceFrag extends BaseFrag implements View.OnClickListener {
    FragmentAllServiceBinding binding;
    private AppCompatActivity act;

    private ActionBar actionBar;

    UserData myInfoFromDB;
    Realm realm = null;

    private String idx;
    private String id;
    private String pw;
    private String hp;
    private String intro;
    private String name;
    private String birth;

    private String profile_img;
    private String background_img;

    private static final int TYPE_PROFILE = 1001;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.e(StringUtil.TAG, "onCreateView: AllServiceFrag");
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_all_service, container, false);
        act = (AppCompatActivity) getActivity();
        setHasOptionsMenu(true);

        setClickListener();

        setActionBar();

        setClickListener();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            binding.ivProfile.setClipToOutline(true);
        }

        /* set realm and get my info */
        CustomApplication application = (CustomApplication) act.getApplication();
        realm = application.getRealmObject();
        checkMyInfo();

        return binding.getRoot();
    }

    private void checkMyInfo() {
        myInfoFromDB = realm.where(UserData.class).equalTo("idx_db", "0").findFirst();
        if(null != myInfoFromDB) {
            setMyInfo();
        } else {
            getMyInfo();
        }
    }

    private void setMyInfo() {
        act.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(!StringUtil.isNull(myInfoFromDB.getProfile_img())) {
                    // set profile image
                    Glide.with(act)
                            .load(NetUrls.DOMAIN + myInfoFromDB.getProfile_img())

                            .into(binding.ivProfile);
                }

                // set name
                binding.tvName.setText(myInfoFromDB.getName());

                // set id
                binding.tvId.setText(myInfoFromDB.getId());
            }
        });
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

    private void setClickListener() {
        binding.tvProfileModify.setOnClickListener(this);

        binding.ivShare.setOnClickListener(this);
        binding.tvCharge.setOnClickListener(this);
        binding.tvRemittance.setOnClickListener(this);

        binding.llTvArea.setOnClickListener(this);
        binding.llMovieArea.setOnClickListener(this);
        binding.llDestinyArea.setOnClickListener(this);
        binding.llLuckArea.setOnClickListener(this);
        binding.llExchangeArea.setOnClickListener(this);
        binding.llJobArea.setOnClickListener(this);
        binding.llEstateArea.setOnClickListener(this);
        binding.llShoppingArea.setOnClickListener(this);
    }

    private void setActionBar() {
        act.setSupportActionBar(binding.toolbar);
        actionBar = act.getSupportActionBar();
        actionBar.setTitle(null);
        actionBar.setDisplayHomeAsUpEnabled(false);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.appbar_actionbar_all, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.action_search:
            case R.id.action_qrcode:
                Common.showToastDevelop(act);
                return true;

            case R.id.action_setting:
                startActivity(new Intent(act, SettingAct.class));
                return true;

        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case TYPE_PROFILE:
                    checkMyInfo();
                    break;
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_profile_modify:
                startActivityForResult(new Intent(act, ProfileAct.class), TYPE_PROFILE);
                break;

            case R.id.iv_share:
            case R.id.tv_charge:
            case R.id.tv_remittance:
            case R.id.ll_tv_area:
            case R.id.ll_movie_area:
            case R.id.ll_destiny_area:
            case R.id.ll_luck_area:
            case R.id.ll_exchange_area:
            case R.id.ll_job_area:
            case R.id.ll_estate_area:
            case R.id.ll_shopping_area:
                Common.showToastDevelop(act);
                break;
        }
    }
}