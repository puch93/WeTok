package kr.co.core.wetok.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.databinding.DataBindingUtil;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.r0adkll.slidr.Slidr;
import com.r0adkll.slidr.model.SlidrConfig;
import com.r0adkll.slidr.model.SlidrInterface;
import com.r0adkll.slidr.model.SlidrPosition;

import org.json.JSONException;
import org.json.JSONObject;

import kr.co.core.wetok.R;
import kr.co.core.wetok.data.UserData;
import kr.co.core.wetok.databinding.ActivityProfileMeBinding;
import kr.co.core.wetok.databinding.ActivityProfileOtherBinding;
import kr.co.core.wetok.preference.UserPref;
import kr.co.core.wetok.server.ReqBasic;
import kr.co.core.wetok.server.netUtil.HttpResult;
import kr.co.core.wetok.server.netUtil.NetUrls;
import kr.co.core.wetok.util.Common;
import kr.co.core.wetok.util.StringUtil;

public class ProfileOtherAct extends BaseAct implements View.OnClickListener {
    ActivityProfileOtherBinding binding;
    Activity act;

    UserData data;

    private SlidrInterface slidr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_profile_other, null);
        act = this;

        /* full screen effect */
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);

        /* set slide animation effect */
        SlidrConfig config = new SlidrConfig.Builder()
                .position(SlidrPosition.TOP)
                .sensitivity(1f)
                .scrimColor(Color.BLACK)
                .scrimStartAlpha(0.8f)
                .scrimEndAlpha(0f)
                .velocityThreshold(2400)  // 2400
                .distanceThreshold(0.25f) // 0.25f
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

        data = (UserData) getIntent().getSerializableExtra("user");
        if(data == null) {
            Common.showToastNetwork(act);
            finish();
        } else {
            setData();
        }

        setClickListener();
    }

    private int statusBarHeight() {
        int res_id = getResources().getIdentifier("status_bar_height", "dimen", "android");

        if (res_id > 0) {
            return getResources().getDimensionPixelSize(res_id);
        } else {
            return 0;
        }

    }

//    private void createRoomSingle() {
//        ReqBasic server = new ReqBasic(act, NetUrls.ADDRESS) {
//            @Override
//            public void onAfter(int resultCode, HttpResult resultData) {
//                if (resultData.getResult() != null) {
//                    try {
//                        JSONObject jo = new JSONObject(resultData.getResult());
//
//                        if(jo.getString("result").equalsIgnoreCase("Y")) {
//                            String room_idx = jo.getString("room_idx");
//                            if(!room_idx.contains("R")) {
//                                room_idx = "R" + room_idx;
//                            }
//
//                            Intent intent = new Intent(act, ChatAct.class);
//                            intent.putExtra("roomIdx", room_idx);
//                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//                            startActivity(intent);
//                        } else {
//                            String message = jo.getString("message");
//                            Common.showToast(act, message);
//                        }
//
//                    } catch (JSONException e) {
//                        e.printStackTrace();
//                        Common.showToastNetwork(act);
//                    }
//                } else {
//                    Common.showToastNetwork(act);
//                }
//            }
//        };
//
//        server.setTag("Create Room Single");
//        server.addParams("siteUrl", NetUrls.SITEURL);
//        server.addParams("CONNECTCODE", "APP");
//        server.addParams("_APP_MEM_IDX", UserPref.getMidx(act));
//
//        server.addParams("dbControl", "setNewRoomCreation");
//        server.addParams("user_idx", UserPref.getMidx(act));
//        server.addParams("guest_idx", data.getIdx());
//        server.addParams("m_os", "android");
//        server.execute(true, false);
//    }


    private void setCheckChattingRoom() {
        ReqBasic server = new ReqBasic(act, NetUrls.ADDRESS) {
            @Override
            public void onAfter(int resultCode, HttpResult resultData) {
                if (resultData.getResult() != null) {
                    try {
                        JSONObject jo = new JSONObject(resultData.getResult());
                        final String result = jo.getString("result");

                        if(result.equalsIgnoreCase("Y")) {
                            String room_idx = jo.getString("room_idx");
                            if(!room_idx.contains("R")) {
                                room_idx = "R" + room_idx;
                            }

                            Intent intent = new Intent(act, ChatAct.class);
                            intent.putExtra("roomIdx", room_idx);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent);

                            finish();
                        } else {
                            final String message = jo.getString("message");
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

        server.setTag("Chat Exist2");
        server.addParams("siteUrl", NetUrls.SITEURL);
        server.addParams("CONNECTCODE", "APP");
        server.addParams("_APP_MEM_IDX", UserPref.getMidx(act));

        server.addParams("dbControl", "getChatRoomNumber");
        server.addParams("guest_idx_ar", data.getIdx());
        server.addParams("user_idx", UserPref.getMidx(act));
        server.addParams("multi_is", "N");
        server.addParams("m_os", "android");
        server.execute(true, false);
    }

    private void setData() {
        // set name
        binding.tvName.setText(data.getName());

        // set id
        binding.tvId.setText(data.getId());

        // set profile image
        Glide.with(act)
                .load(NetUrls.DOMAIN + data.getProfile_img())
                .into(binding.ivProfile);

        // set background image
        Glide.with(act)
                .load(NetUrls.DOMAIN + data.getBackground_img())
                .into(binding.ivBackground);
    }

    private void setClickListener() {
        binding.ivProfile.setOnClickListener(this);
        binding.ivBackground.setOnClickListener(this);

        binding.flClose.setOnClickListener(this);
        binding.flShare.setOnClickListener(this);

        binding.flCharmGift.setOnClickListener(this);

        binding.llChatting.setOnClickListener(this);
        binding.llMoney.setOnClickListener(this);
        binding.llStory.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_profile:
                if (!StringUtil.isNull(data.getProfile_img())) {
                    Intent intent = new Intent(act, EnlargeAct.class);
                    intent.putExtra("imageUrl", NetUrls.DOMAIN + data.getProfile_img());
                    startActivity(intent);
                }
                break;

            case R.id.iv_background:
                if (!StringUtil.isNull(data.getBackground_img())) {
                    Intent intent = new Intent(act, EnlargeAct.class);
                    intent.putExtra("imageUrl",NetUrls.DOMAIN + data.getBackground_img());
                    startActivity(intent);
                }
                break;

            case R.id.fl_close:
                finish();
                break;

            case R.id.fl_share:
                Common.showToastDevelop(act);
                break;

            case R.id.fl_charm_gift:
                Common.showToastDevelop(act);
                break;

            case R.id.ll_chatting:
                setCheckChattingRoom();
                break;

            case R.id.ll_money:
                Common.showToastDevelop(act);
                break;

            case R.id.ll_story:
                Intent intent = new Intent(act, StoryAct.class);
                intent.putExtra("user", data);
                startActivity(intent);
                break;
        }
    }
}
