package kr.co.core.wetok.adapter;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import kr.co.core.wetok.R;
import kr.co.core.wetok.activity.ProfileOtherAct;
import kr.co.core.wetok.data.StoryData;
import kr.co.core.wetok.data.UserData;
import kr.co.core.wetok.preference.UserPref;
import kr.co.core.wetok.server.ReqBasic;
import kr.co.core.wetok.server.netUtil.HttpResult;
import kr.co.core.wetok.server.netUtil.NetUrls;
import kr.co.core.wetok.util.Common;
import kr.co.core.wetok.util.StringUtil;

public class StoryAdapter extends RecyclerView.Adapter<StoryAdapter.ViewHolder> {
    private Activity act;
    private ArrayList<StoryData> list;
    private UserData user;
    private boolean isMe;

    private AfterDelete afterDeleteListener;

    public StoryAdapter(Activity act, UserData user, boolean isMe, ArrayList<StoryData> list, AfterDelete afterDeleteListener) {
        this.act = act;
        this.user = user;
        this.list = list;
        this.isMe = isMe;

        this.afterDeleteListener = afterDeleteListener;
    }

    public interface AfterDelete {
        void afterDelete();
    }


    @NonNull
    @Override
    public StoryAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_story, parent, false);
        StoryAdapter.ViewHolder viewHolder = new StoryAdapter.ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull StoryAdapter.ViewHolder holder, int i) {
        final StoryData data = list.get(i);

        // set profile image
        Glide.with(act)
                .load(NetUrls.DOMAIN + user.getProfile_img())

                .into(holder.iv_profile);

        // set name
        holder.tv_name.setText(user.getName());

        // set text contents
        holder.tv_text.setText(data.getText());

        // reg date
        holder.tv_reg_date.setText(data.getRegDate());

        // set image contents
        if (!StringUtil.isNull(data.getImage())) {
            holder.iv_image.setVisibility(View.VISIBLE);
            Glide.with(act)
                    .load(NetUrls.DOMAIN + data.getImage())
                    .into(holder.iv_image);
        } else {
            holder.iv_image.setVisibility(View.GONE);
        }

        // delete button
        if (isMe) {
            holder.fl_delete.setVisibility(View.VISIBLE);
            holder.fl_delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showDialog(data.getIdx());
                }
            });
        } else {
            holder.fl_delete.setVisibility(View.GONE);
        }
    }

    private void showDialog(final String idx) {
        new AlertDialog.Builder(act)
                .setMessage("스토리를 삭제하시겠습니까?")
                .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        setStoryDelete(idx);
                    }
                })
                .setNegativeButton("취소", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                }).show();


//        final LayoutInflater layout = LayoutInflater.from(act);
//        View dialogView = layout.inflate(R.layout.dialog_warning, null);
//        final Dialog dialog = new Dialog(act);
//        dialog.setContentView(dialogView);
//
//        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
//        dialog.show();
//
//        TextView tv_confirm = (TextView) dialogView.findViewById(R.id.tv_confirm);
//        TextView tv_contents = (TextView) dialogView.findViewById(R.id.tv_contents);
//
//        tv_contents.setText("스토리를 삭제하시겠습니까?");
//        tv_confirm.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                setStoryDelete(idx);
//
//                if(dialog.isShowing()) {
//                    dialog.dismiss();
//                }
//
//            }
//        });
    }

    private void setStoryDelete(String idx) {
        ReqBasic server = new ReqBasic(act, NetUrls.ADDRESS) {
            @Override
            public void onAfter(int resultCode, HttpResult resultData) {
                if (resultData.getResult() != null) {
                    try {
                        JSONObject jo = new JSONObject(resultData.getResult());

                        if(jo.getString("result").equalsIgnoreCase("Y")) {
                            Common.showToast(act, "삭제가 완료되었습니다.");
                            afterDeleteListener.afterDelete();
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

        server.setTag("Story Delete");
        server.addParams("siteUrl", NetUrls.SITEURL);
        server.addParams("CONNECTCODE", "APP");
        server.addParams("_APP_MEM_IDX", UserPref.getMidx(act));

        server.addParams("dbControl", NetUrls.SET_STORY_DELETE);
        server.addParams("s_idx", idx);
        server.execute(true, false);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public void setList(ArrayList<StoryData> list) {
        this.list = list;
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        ImageView iv_profile, iv_image;
        TextView tv_name, tv_text, tv_reg_date;
        FrameLayout fl_delete;

        ViewHolder(@NonNull View view) {
            super(view);
            iv_profile = view.findViewById(R.id.iv_profile);
            iv_image = view.findViewById(R.id.iv_image);
            tv_name = view.findViewById(R.id.tv_name);
            tv_text = view.findViewById(R.id.tv_text);
            tv_reg_date = view.findViewById(R.id.tv_reg_date);
            fl_delete = view.findViewById(R.id.fl_delete);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                iv_profile.setClipToOutline(true);
            }
        }
    }
}
