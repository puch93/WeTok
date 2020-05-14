package kr.co.core.wetok.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Field;

import kr.co.core.wetok.R;
import kr.co.core.wetok.adapter.WriteSpinnerAdapter;
import kr.co.core.wetok.data.UserData;
import kr.co.core.wetok.databinding.ActivityAddFriendBinding;
import kr.co.core.wetok.preference.UserPref;
import kr.co.core.wetok.server.ReqBasic;
import kr.co.core.wetok.server.netUtil.HttpResult;
import kr.co.core.wetok.server.netUtil.NetUrls;
import kr.co.core.wetok.util.Common;
import kr.co.core.wetok.util.CustomSpinner;
import kr.co.core.wetok.util.StringUtil;

public class AddFriendAct extends AppCompatActivity implements View.OnClickListener {
    ActivityAddFriendBinding binding;
    Activity act;

    ActionBar actionBar;

    private String[] country_codes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_add_friend, null);
        act = this;

        country_codes = getResources().getStringArray(R.array.country_code);

        setActionBar();
        setClickListener();

        binding.llFromNumber.performClick();

        setSpinner();
    }

    private void getFriendFromHp() {
        ReqBasic server = new ReqBasic(act, NetUrls.ADDRESS) {
            @Override
            public void onAfter(int resultCode, HttpResult resultData) {
                if (resultData.getResult() != null) {
                    try {
                        JSONObject jo = new JSONObject(resultData.getResult());

                        if (jo.getString("result").equalsIgnoreCase("Y")) {
                            JSONArray ja = jo.getJSONArray("value");
                            JSONObject job = ja.getJSONObject(0);

                            UserData data = new UserData();
                            data.setIdx(job.getString("m_idx"));
                            data.setId(job.getString("m_id"));
                            data.setPw(job.getString("m_pass"));
                            data.setHp(job.getString("m_hp"));
                            data.setIntro(job.getString("m_intro"));
                            data.setName(job.getString("m_nickname"));

                            data.setProfile_img(job.getString("m_profile"));
                            data.setBackground_img(job.getString("m_background"));

                            showDialog(data);
                        } else {
                            Common.showToast(act, jo.getString("message"));
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

        String hp = "";
        if(binding.spinner.getSelectedItemPosition() == 0) {
            hp = binding.etNumber.getText().toString();
        } else {
            hp = binding.spinner.getSelectedItem().toString() + binding.etNumber.getText().toString();
        }

        server.setTag("Get From Hp");
        server.addParams("siteUrl", NetUrls.SITEURL);
        server.addParams("CONNECTCODE", "APP");
        server.addParams("_APP_MEM_IDX", UserPref.getMidx(act));

        server.addParams("dbControl", NetUrls.GET_FRIEND_FROM_HP);
        server.addParams("y_name", binding.etName.getText().toString());
        server.addParams("y_hp", hp);
        server.execute(true, false);
    }

    private void getFriendFromId() {
        ReqBasic server = new ReqBasic(act, NetUrls.ADDRESS) {
            @Override
            public void onAfter(int resultCode, HttpResult resultData) {
                if (resultData.getResult() != null) {
                    try {
                        JSONObject jo = new JSONObject(resultData.getResult());

                        if (jo.getString("result").equalsIgnoreCase("Y")) {
                            JSONArray ja = jo.getJSONArray("value");
                            JSONObject job = ja.getJSONObject(0);

                            UserData data = new UserData();
                            data.setIdx(job.getString("m_idx"));
                            data.setId(job.getString("m_id"));
                            data.setPw(job.getString("m_pass"));
                            data.setHp(job.getString("m_hp"));
                            data.setIntro(job.getString("m_intro"));
                            data.setName(job.getString("m_nickname"));

                            data.setProfile_img(job.getString("m_profile"));
                            data.setBackground_img(job.getString("m_background"));

                            showDialog(data);

                        } else {
                            Common.showToast(act, jo.getString("message"));
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

        server.setTag("Get From Id");
        server.addParams("siteUrl", NetUrls.SITEURL);
        server.addParams("CONNECTCODE", "APP");
        server.addParams("_APP_MEM_IDX", UserPref.getMidx(act));

        server.addParams("dbControl", NetUrls.GET_FRIEND_FROM_ID);
        server.addParams("y_id", binding.etId.getText().toString());
        server.execute(true, false);
    }

    private void setFriendAdd(final UserData data) {
        ReqBasic server = new ReqBasic(act, NetUrls.ADDRESS) {
            @Override
            public void onAfter(int resultCode, HttpResult resultData) {
                if (resultData.getResult() != null) {
                    try {
                        JSONObject jo = new JSONObject(resultData.getResult());

                        if(jo.getString("result").equalsIgnoreCase("Y")) {
                            Common.showToast(act, "친구추가 되었습니다.");
                            setResult(RESULT_OK);
                            finish();
                        } else {
                            Common.showToast(act, jo.getString("message"));
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

        server.setTag("Friend Add");
        server.addParams("siteUrl", NetUrls.SITEURL);
        server.addParams("CONNECTCODE", "APP");
        server.addParams("_APP_MEM_IDX", UserPref.getMidx(act));

        server.addParams("dbControl", "setFriedAdd");
        server.addParams("y_idx", data.getIdx());
        server.execute(true, false);
    }

    private void showDialog(final UserData data) {
        LayoutInflater layout = LayoutInflater.from(act);
        View dialogLayout = layout.inflate(R.layout.dialog_friend_add, null);
        final Dialog dialog = new Dialog(act);
        dialog.setContentView(dialogLayout);

        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();

        ImageView iv_profile = (ImageView) dialogLayout.findViewById(R.id.iv_profile);
        TextView tv_name = (TextView) dialogLayout.findViewById(R.id.tv_name);
        TextView tv_intro = (TextView) dialogLayout.findViewById(R.id.tv_intro);
        TextView tv_confirm = (TextView) dialogLayout.findViewById(R.id.tv_confirm);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            iv_profile.setClipToOutline(true);
        }

        // set name
        tv_name.setText(data.getName());

        // set intro
        if(StringUtil.isNull(data.getIntro())) {
            tv_intro.setText(getString(R.string.dlg_friend_add_intro));
        } else {
            tv_intro.setText(data.getIntro());
        }

        // set profile image
        Glide.with(act)
                .load(NetUrls.DOMAIN + data.getProfile_img())

                .into(iv_profile);

        // set btn process
        tv_confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setFriendAdd(data);
                dialog.dismiss();
            }
        });
    }

    private void setClickListener() {
        binding.llFromId.setOnClickListener(this);
        binding.llFromNumber.setOnClickListener(this);
        binding.tvAddFromHp.setOnClickListener(this);
        binding.tvAddFromId.setOnClickListener(this);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        finish();
        return true;
    }

    private void setActionBar() {
        setSupportActionBar(binding.toolbar);
        actionBar = getSupportActionBar();
        actionBar.setTitle(null);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeAsUpIndicator(R.drawable.wt_icon_back_wh_191022);
    }

    private void setSpinner() {
        // set spinner
        WriteSpinnerAdapter adapter_area = new WriteSpinnerAdapter(act, android.R.layout.simple_spinner_item, country_codes);
        adapter_area.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_height_set(binding.spinner, 1000);
        binding.spinner.setAdapter(adapter_area);

        // set spinner open/close listener
        binding.spinner.setSpinnerEventsListener(new CustomSpinner.OnSpinnerEventsListener() {
            @Override
            public void onSpinnerOpened(Spinner spinner) {

                // 현재 focus 되어있는 view 가 있으면 키보드를 내리고, focus 제거
                View view = act.getCurrentFocus();
                if (view != null) {
                    InputMethodManager imm = (InputMethodManager) act.getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                    view.clearFocus();
                } else {
                    view = binding.etName;
                    InputMethodManager imm = (InputMethodManager) act.getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                }

                binding.ivArrow.setSelected(true);
            }

            @Override
            public void onSpinnerClosed(Spinner spinner) {
                binding.ivArrow.setSelected(false);
            }
        });
    }

    //Spinner 길이설정
    private void spinner_height_set(Spinner spinner, int height) {
        try {
            Field popup = Spinner.class.getDeclaredField("mPopup");
            popup.setAccessible(true);

            // Get private mPopup member variable and try cast to ListPopupWindow
            android.widget.ListPopupWindow popupWindow = (android.widget.ListPopupWindow) popup.get(spinner);

            // Set popupWindow height to 500px
            popupWindow.setHeight(height);
        } catch (NoClassDefFoundError | ClassCastException | NoSuchFieldException | IllegalAccessException e) {
            // silently fail...
        }
    }

    private void switchLayout(int type) {
        switch (type) {
            case 1:
                binding.llFromId.setSelected(true);
                binding.llFromNumber.setSelected(false);

                binding.ivFromNumber.setVisibility(View.INVISIBLE);
                binding.ivFromId.setVisibility(View.VISIBLE);

                binding.llFromNumberArea.setVisibility(View.GONE);
                binding.llFromIdArea.setVisibility(View.VISIBLE);
                break;

            case 2:
                binding.llFromId.setSelected(false);
                binding.llFromNumber.setSelected(true);

                binding.ivFromNumber.setVisibility(View.VISIBLE);
                binding.ivFromId.setVisibility(View.INVISIBLE);

                binding.llFromNumberArea.setVisibility(View.VISIBLE);
                binding.llFromIdArea.setVisibility(View.GONE);
                break;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ll_from_id:
                switchLayout(1);
                break;

            case R.id.ll_from_number:
                switchLayout(2);
                break;

            case R.id.tv_add_from_hp:
                if (StringUtil.isNull(binding.etName.getText().toString())) {
                    Common.showToast(act, getString(R.string.add_friend_warning01));
                    return;
                }

                if (StringUtil.isNull(binding.etNumber.getText().toString())) {
                    Common.showToast(act, getString(R.string.add_friend_warning02));
                    return;
                }

                getFriendFromHp();
                break;

            case R.id.tv_add_from_id:
                if (StringUtil.isNull(binding.etId.getText().toString())) {
                    Common.showToast(act, getString(R.string.add_friend_warning03));
                    return;
                }

                if(binding.etId.getText().toString().equalsIgnoreCase(UserPref.getId(act))) {
                    Common.showToast(act, getString(R.string.add_friend_warning04));
                    return;
                }

                getFriendFromId();
                break;
        }
    }
}