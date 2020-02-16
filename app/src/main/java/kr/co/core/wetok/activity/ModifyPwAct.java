package kr.co.core.wetok.activity;

import android.app.Activity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import org.json.JSONException;
import org.json.JSONObject;

import kr.co.core.wetok.R;
import kr.co.core.wetok.databinding.ActivityModifyPwBinding;
import kr.co.core.wetok.preference.UserPref;
import kr.co.core.wetok.server.ReqBasic;
import kr.co.core.wetok.server.netUtil.HttpResult;
import kr.co.core.wetok.server.netUtil.NetUrls;
import kr.co.core.wetok.util.Common;

public class ModifyPwAct extends AppCompatActivity implements View.OnClickListener {
    ActivityModifyPwBinding binding;
    public Activity act;

    ActionBar actionBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_modify_pw, null);
        act = this;

        binding.etPwOld.addTextChangedListener(textWatcher);
        binding.etPw.addTextChangedListener(textWatcher);
        binding.etPwConfirm.addTextChangedListener(textWatcher);

        binding.tvConfirm.setOnClickListener(this);

        setActionBar();
    }

    private void setCheckPassword() {
        ReqBasic server = new ReqBasic(act, NetUrls.ADDRESS) {
            @Override
            public void onAfter(int resultCode, HttpResult resultData) {
                if (resultData.getResult() != null) {
                    try {
                        JSONObject jo = new JSONObject(resultData.getResult());
                        String message = jo.getString("message");
                        String result = jo.getString("result");
                        if(result.equalsIgnoreCase("Y")) {
                            Common.showToast(act, message);

                            UserPref.setPw(act, binding.etPw.getText().toString());
                            setResult(RESULT_OK);
                            finish();
                        } else {
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

        server.setTag("Modify Password");
        server.addParams("siteUrl", NetUrls.SITEURL);
        server.addParams("CONNECTCODE", "APP");
        server.addParams("_APP_MEM_IDX", UserPref.getMidx(act));

        server.addParams("dbControl", NetUrls.SET_MODIFY_PW_MEMBER);
        server.addParams("old_pw", binding.etPwOld.getText().toString());
        server.addParams("pw", binding.etPw.getText().toString());
        server.addParams("pw_confirm", binding.etPwConfirm.getText().toString());
        server.execute(true, false);
    }

    private TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void afterTextChanged(Editable s) {
            checkButtonActivation();
        }
    };

    private void setActionBar() {
        setSupportActionBar(binding.toolbar);
        actionBar = getSupportActionBar();
        actionBar.setTitle(null);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeAsUpIndicator(R.drawable.wt_icon_back_wh_191022);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        finish();
        return true;
    }

    private void checkButtonActivation() {
        if (binding.etPwOld.length() != 0 &&
                binding.etPw.length() != 0 &&
                binding.etPwConfirm.length() != 0 &&
                binding.etPw.getText().toString().equalsIgnoreCase(binding.etPwConfirm.getText().toString())
        ) {
            binding.tvConfirm.setBackgroundResource(R.drawable.wt_btn360_enable_191022);
        } else {
            binding.tvConfirm.setBackgroundResource(R.drawable.wt_btn360_disable_191022);
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.tv_confirm) {
            if (binding.etPwOld.length() == 0) {
                Common.showToast(act, getString(R.string.find_pw_old_warning));
                return;
            }

            if (binding.etPw.length() == 0) {
                Common.showToast(act, getString(R.string.find_pw_warning));
                return;
            }

            if (binding.etPwConfirm.length() == 0) {
                Common.showToast(act, getString(R.string.find_pw_confirm_warning));
                return;
            }

            if (binding.etPwOld.length() < 8 || binding.etPwOld.length() > 16 &&
                    binding.etPw.length() < 8 || binding.etPw.length() > 16 &&
                    binding.etPwConfirm.length() < 8 || binding.etPwConfirm.length() > 16) {
                Common.showToast(act, getString(R.string.find_pw_length_warning));
                return;
            }

            if (!binding.etPw.getText().toString().equals(binding.etPwConfirm.getText().toString())) {
                Common.showToast(act, getString(R.string.find_pw_same_warning));
                return;
            }

            setCheckPassword();
        }
    }
}
