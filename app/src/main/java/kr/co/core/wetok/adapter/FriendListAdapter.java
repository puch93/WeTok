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
import com.bumptech.glide.load.resource.bitmap.CircleCrop;

import java.util.ArrayList;

import kr.co.core.wetok.R;
import kr.co.core.wetok.activity.ProfileMeAct;
import kr.co.core.wetok.activity.ProfileOtherAct;
import kr.co.core.wetok.data.UserData;
import kr.co.core.wetok.preference.UserPref;
import kr.co.core.wetok.server.netUtil.NetUrls;
import kr.co.core.wetok.util.StringUtil;

public class FriendListAdapter extends RecyclerView.Adapter<FriendListAdapter.ViewHolder> {
    private static final int FROM_PROFILE = 1002;

    private Activity act;
    private ArrayList<UserData> list;
    private Fragment fragment;

    public FriendListAdapter(Activity act, ArrayList<UserData> list, Fragment fragment) {
        this.act = act;
        this.list = list;
        this.fragment = fragment;
    }

    @NonNull
    @Override
    public FriendListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_main, parent, false);
        FriendListAdapter.ViewHolder viewHolder = new FriendListAdapter.ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull FriendListAdapter.ViewHolder holder, int i) {
        final UserData data = list.get(i);

        holder.tv_name.setText(data.getName());
        holder.tv_intro.setText(data.getIntro());

        Glide.with(act)
                .load(NetUrls.DOMAIN + data.getProfile_img())

                .into(holder.iv_profile);

        if(UserPref.getMidx(act).equalsIgnoreCase(data.getIdx())) {
            holder.ll_all_area.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    fragment.startActivityForResult(new Intent(act, ProfileMeAct.class), FROM_PROFILE);
                }
            });
        } else {
            holder.ll_all_area.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(act, ProfileOtherAct.class);
                    intent.putExtra("user", data);
                    act.startActivity(intent);
                }
            });
        }
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
        LinearLayout ll_all_area;

        ViewHolder(@NonNull View view) {
            super(view);
            iv_profile = (ImageView) view.findViewById(R.id.iv_profile);
            tv_name = view.findViewById(R.id.tv_name);
            tv_intro = view.findViewById(R.id.tv_intro);
            ll_all_area = view.findViewById(R.id.ll_all_area);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                iv_profile.setClipToOutline(true);
            }
        }
    }
}
