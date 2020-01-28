package kr.co.core.wetok.adapter;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

import kr.co.core.wetok.R;
import kr.co.core.wetok.activity.ChatAct;
import kr.co.core.wetok.activity.ProfileMeAct;
import kr.co.core.wetok.activity.ProfileOtherAct;
import kr.co.core.wetok.activity.rtc.ConnectActivity;
import kr.co.core.wetok.data.UserData;
import kr.co.core.wetok.preference.UserPref;
import kr.co.core.wetok.server.netUtil.NetUrls;
import kr.co.core.wetok.util.Common;

public class ChattingPartAdapter extends RecyclerView.Adapter<ChattingPartAdapter.ViewHolder> {
    private static final int FROM_PROFILE = 1002;

    private Activity act;
    private ArrayList<UserData> list;

    private static final int RTC_VOICE_CALL = 0;
    private static final int RTC_VIDEO_CALL = 2;

    public ChattingPartAdapter(Activity act, ArrayList<UserData> list) {
        this.act = act;
        this.list = list;
    }

    @NonNull
    @Override
    public ChattingPartAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_part, parent, false);
        ChattingPartAdapter.ViewHolder viewHolder = new ChattingPartAdapter.ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ChattingPartAdapter.ViewHolder holder, int i) {
        final UserData data = list.get(i);

        holder.ll_part_area.setVisibility(View.GONE);
        holder.ll_all_area.setVisibility(View.VISIBLE);

        holder.tv_name.setText(data.getName());
        holder.tv_intro.setText(data.getIntro());

        Glide.with(act)
                .load(NetUrls.DOMAIN + data.getProfile_img())

                .into(holder.iv_profile);

        if (UserPref.getMidx(act).equalsIgnoreCase(data.getIdx())) {
            holder.ll_all_area.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    act.startActivity(new Intent(act, ProfileMeAct.class));
                }
            });
        } else {
            holder.ll_all_area.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = null;
                    switch (((ChatAct) act).menuCallState) {
                        case "voice":
                            intent = new Intent(act, ConnectActivity.class);
                            intent.putExtra("mode", "voice");
                            intent.putExtra("type", "call");
                            intent.putExtra("userData", data);
                            act.startActivityForResult(intent, RTC_VOICE_CALL);
                            break;

                        case "video":
                            intent = new Intent(act, ConnectActivity.class);
                            intent.putExtra("mode", "video");
                            intent.putExtra("type", "call");
                            intent.putExtra("userData", data);
                            act.startActivityForResult(intent, RTC_VIDEO_CALL);
                            break;

                        default:
                            intent = new Intent(act, ProfileOtherAct.class);
                            intent.putExtra("user", data);
                            act.startActivity(intent);
                            break;
                    }

                    ((ChatAct) act).menuCallState = "none";
                    ((ChatAct) act).closeDrawer();
                }
            });
        }

//        if (i == list.size() - 1) {
//            holder.ll_part_area.setVisibility(View.VISIBLE);
//            holder.ll_all_area.setVisibility(View.GONE);
//
//            holder.ll_part_area.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    Common.showToast(act, "대화상대 초대");
//                }
//            });
//        } else {
//            holder.ll_part_area.setVisibility(View.GONE);
//            holder.ll_all_area.setVisibility(View.VISIBLE);
//
//            holder.tv_name.setText(data.getName());
//            holder.tv_intro.setText(data.getIntro());
//
//            Glide.with(act)
//                    .load(NetUrls.DOMAIN + data.getProfile_img())
//
//                    .into(holder.iv_profile);
//
//            if (UserPref.getMidx(act).equalsIgnoreCase(data.getIdx())) {
//                holder.ll_all_area.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        act.startActivity(new Intent(act, ProfileMeAct.class));
//                    }
//                });
//            } else {
//                holder.ll_all_area.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        Intent intent = new Intent(act, ProfileOtherAct.class);
//                        intent.putExtra("user", data);
//                        act.startActivity(intent);
//                    }
//                });
//            }
//        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public void setList(ArrayList<UserData> list) {
        this.list = list;
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        ImageView iv_profile;
        TextView tv_name, tv_intro;
        LinearLayout ll_all_area, ll_part_area;

        ViewHolder(@NonNull View view) {
            super(view);
            iv_profile = (ImageView) view.findViewById(R.id.iv_profile);
            tv_name = view.findViewById(R.id.tv_name);
            tv_intro = view.findViewById(R.id.tv_intro);
            ll_all_area = view.findViewById(R.id.ll_all_area);
            ll_part_area = view.findViewById(R.id.ll_part_area);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                iv_profile.setClipToOutline(true);
            }
        }
    }
}
