package kr.co.core.wetok.fragment.join;


import android.graphics.Rect;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.regex.Pattern;

import kr.co.core.wetok.R;
import kr.co.core.wetok.activity.JoinAct;
import kr.co.core.wetok.databinding.FragmentJoin01Binding;
import kr.co.core.wetok.fragment.BaseFrag;
import kr.co.core.wetok.preference.SystemPref;
import kr.co.core.wetok.server.ReqBasic;
import kr.co.core.wetok.server.netUtil.HttpResult;
import kr.co.core.wetok.server.netUtil.NetUrls;
import kr.co.core.wetok.util.Common;
import kr.co.core.wetok.util.StringUtil;

public class Join01Frag extends BaseFrag implements View.OnClickListener {
    private FragmentJoin01Binding binding;
    private AppCompatActivity act;

    private int origin = 0;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_join_01, container, false);
        act = (AppCompatActivity) getActivity();

        binding.tvConfirm.setOnClickListener(this);
        binding.etId.addTextChangedListener(new TextWatcher() {
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
        });

        // 키보드 높이 구하기
        Window mRootWindow = act.getWindow();
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
                        if (heightDiff > 0) {
                            SystemPref.setKeyBoardHeight(act, heightDiff);
                        }
                        Log.e(StringUtil.TAG, "heightDiff: " + heightDiff);
                    }
                });

        return binding.getRoot();
    }

    private void setCheckId() {
        ReqBasic server = new ReqBasic(act, NetUrls.ADDRESS) {
            @Override
            public void onAfter(int resultCode, HttpResult resultData) {
                if (resultData.getResult() != null) {
                    try {
                        JSONObject jo = new JSONObject(resultData.getResult());

                        if (jo.getString("result").equalsIgnoreCase("Y")) {
                            nextProcess();
                        } else {
                            Common.showToast(act, getString(R.string.join_id_check_warning01));
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

        server.setTag("Check Id");
        server.addParams("dbControl", NetUrls.CHECK_JOIN_ID);
        server.addParams("id", binding.etId.getText().toString());
        server.execute(true, false);
    }

    private void checkButtonActivation() {
        if (
                !StringUtil.isNull(binding.etId.getText().toString()) &&
                        Pattern.matches("^[a-zA-Z0-9]*$", binding.etId.getText().toString()) &&
                        binding.etId.length() >= 4
        ) {
            binding.tvConfirm.setBackgroundResource(R.drawable.wt_btn360_enable_191022);
        } else {
            binding.tvConfirm.setBackgroundResource(R.drawable.wt_btn360_disable_191022);
        }
    }

    private void nextProcess() {
        BaseFrag fragment = new Join02Frag();

        Bundle bundle = new Bundle(1);
        bundle.putString("id", binding.etId.getText().toString());
        fragment.setArguments(bundle);

        ((JoinAct) act).replaceFragment(fragment);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.tv_confirm) {
            // 아이디 길이 0 검사
            if (StringUtil.isNull(binding.etId.getText().toString())) {
                Common.showToast(act, getString(R.string.join_id_check_warning02));
                return;
            }

            // 아이디 길이 검사
            if (binding.etId.length() < 4) {
                Common.showToast(act, getString(R.string.join_id_check_warning04));
                return;
            }

            // 아이디 정규식 검사
            if (!Pattern.matches("^[a-zA-Z0-9]*$", binding.etId.getText().toString())) {
                Common.showToast(act, getString(R.string.join_id_check_warning03));
                return;
            }

            setCheckId();
        }
    }
}