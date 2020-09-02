package kr.co.core.wetok.adapter;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;


import net.igenius.customcheckbox.CustomCheckBox;

import java.util.ArrayList;

import kr.co.core.wetok.R;
import kr.co.core.wetok.data.CheckUserData;
import kr.co.core.wetok.server.netUtil.NetUrls;


public class ChattingAddAdapter extends RecyclerView.Adapter<ChattingAddAdapter.ViewHolder>{
    private ArrayList<CheckUserData> list;
    Activity act;

    public ChattingAddAdapter(Activity act, ArrayList<CheckUserData> list) {
        this.act = act;
        this.list = list;
    }

    @NonNull
    @Override
    public ChattingAddAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_add, parent, false);
        ChattingAddAdapter.ViewHolder viewHolder = new ChattingAddAdapter.ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ChattingAddAdapter.ViewHolder holder, int i) {
        final CheckUserData data = list.get(i);

        holder.tv_name.setText(data.getName());
        holder.tv_intro.setText(data.getIntro());

        Glide.with(act)
                .load(NetUrls.DOMAIN + data.getProfile_img())
                .into(holder.iv_profile);

        holder.ll_all_area.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                holder.cb_select.setChecked(!data.isChecked(), true);
            }
        });

        holder.cb_select.setChecked(data.isChecked());

        holder.cb_select.setOnCheckedChangeListener(new CustomCheckBox.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CustomCheckBox checkBox, boolean isChecked) {
                data.setChecked(isChecked);
            }
        });
    }

    public Object getItem(int position) {
        return list.get(position);
    }

    public void setList(ArrayList<CheckUserData> list) {
        this.list = list;
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        ImageView iv_profile;
        TextView tv_name, tv_intro;
        LinearLayout ll_all_area;
        CustomCheckBox cb_select;

        ViewHolder(@NonNull View view) {
            super(view);
            iv_profile = (ImageView) view.findViewById(R.id.iv_profile);
            tv_name = view.findViewById(R.id.tv_name);
            tv_intro = view.findViewById(R.id.tv_intro);
            ll_all_area = view.findViewById(R.id.ll_all_area);
            cb_select = view.findViewById(R.id.cb_select);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                iv_profile.setClipToOutline(true);
            }
        }
    }

    public ArrayList<CheckUserData> getAllData() {
        return list;
    }
}
