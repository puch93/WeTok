package kr.co.core.wetok.fragment.join;



import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.bumptech.glide.Glide;

import org.json.JSONException;
import org.json.JSONObject;

import kr.co.core.wetok.R;
import kr.co.core.wetok.databinding.FragmentJoin04Binding;
import kr.co.core.wetok.fragment.BaseFrag;
import kr.co.core.wetok.preference.UserPref;
import kr.co.core.wetok.server.ReqBasic;
import kr.co.core.wetok.server.netUtil.HttpResult;
import kr.co.core.wetok.server.netUtil.NetUrls;
import kr.co.core.wetok.util.Common;

public class Join04Frag extends BaseFrag implements View.OnClickListener {
    private FragmentJoin04Binding binding;
    private AppCompatActivity act;
    String id, pw;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_join_04, container, false);
        act = (AppCompatActivity) getActivity();

        String name = getArguments().getString("name");
        String image = getArguments().getString("image");
        id = getArguments().getString("id");
        pw = getArguments().getString("pw");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            binding.ivImage.setClipToOutline(true);
        }

        Glide.with(act)
                .load(image)
                .into(binding.ivImage);

        binding.tvName.setText(name);

        binding.tvConfirm.setOnClickListener(this);

        return binding.getRoot();
    }

    private void setLogin() {
        ReqBasic server = new ReqBasic(act, NetUrls.ADDRESS) {
            @Override
            public void onAfter(int resultCode, HttpResult resultData) {

                if (resultData.getResult() != null) {
                    try {
                        JSONObject jo = new JSONObject(resultData.getResult());

                        if(jo.getString("result").equalsIgnoreCase("Y")) {
                            // set auto login
                            UserPref.setAutoLogin(act, true);
                            UserPref.setId(act, id);
                            UserPref.setPw(act, pw);

                            String midx = jo.getString("MEMCODE");
                            UserPref.setMidx(act, midx);

                            act.setResult(Activity.RESULT_OK);
                            act.finish();
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

        server.setTag("Login");
        server.addParams("siteUrl", NetUrls.SITEURL);
        server.addParams("CONNECTCODE", "APP");

        server.addParams("dbControl", NetUrls.USER_LOGIN);
        server.addParams("m_regi", UserPref.getFcmToken(act));
        server.addParams("m_uniq", UserPref.getDeviceId(act));
        server.addParams("m_id", id);
        server.addParams("m_pass", pw);
        server.execute(true, true);
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.tv_confirm) {
            setLogin();
        }
    }
}