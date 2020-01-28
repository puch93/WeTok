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
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;

import java.util.ArrayList;

import kr.co.core.wetok.R;
import kr.co.core.wetok.activity.ChatAct;
import kr.co.core.wetok.activity.EnlargeAct;
import kr.co.core.wetok.data.ChattingListData;
import kr.co.core.wetok.data.UserData;
import kr.co.core.wetok.server.netUtil.NetUrls;
import kr.co.core.wetok.util.StringUtil;

public class ChattingListAdapter extends RecyclerView.Adapter<ChattingListAdapter.ViewHolder> {
    private Activity act;
    private ArrayList<ChattingListData> list;

    private static final int TYPE_SINGLE = 101;
    private static final int TYPE_MULTI = 102;

    public ChattingListAdapter(Activity act, ArrayList<ChattingListData> list) {
        this.act = act;
        this.list = list;
    }

    @NonNull
    @Override
    public ChattingListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        switch (viewType) {
            case TYPE_SINGLE:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat, parent, false);
                return new ViewHolder1(view);

            case TYPE_MULTI:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_group, parent, false);
                return new ViewHolder2(view);

            default:
                return null;
        }
    }

    @Override
    public int getItemViewType(int position) {
        ChattingListData data = list.get(position);

        if (data.getUserArray().size() == 1) {
            return TYPE_SINGLE;
        } else if (data.getUserArray().size() > 1) {
            return TYPE_MULTI;
        } else {
            return super.getItemViewType(position);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull ChattingListAdapter.ViewHolder holder, int i) {
        final ChattingListData data = list.get(i);
        int viewType = getItemViewType(i);

        if(viewType == TYPE_SINGLE) {
            ViewHolder1 holder1 = (ViewHolder1) holder;
            final UserData userData = data.getUserArray().get(0);

            // set other name
            holder1.tv_name.setText(userData.getName());
            list.get(i).setUserNames(userData.getName());

            // set other profile
            if (!StringUtil.isNull(userData.getProfile_img())) {
                Glide.with(act)
                        .load(NetUrls.DOMAIN + userData.getProfile_img())
                        .into(holder1.iv_profile);
            }

            // set list data text
            holder1.tv_contents.setText(data.getText());

            // set list data send time
            holder1.tv_send_time.setText(data.getSendTime());

            // set list read count
            if (data.getReadCount() == null || data.getReadCount().length() < 1 || data.getReadCount().equals("0")) {
                holder1.tv_read_count.setVisibility(View.INVISIBLE);
            } else {
                holder1.tv_read_count.setVisibility(View.VISIBLE);
                holder1.tv_read_count.setText(data.getReadCount());
            }


            // click listener
            holder1.ll_all_area.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(act, ChatAct.class);
                    intent.putExtra("roomIdx", data.getRoomIdx());
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    act.startActivity(intent);
                }
            });
        } else {
            ViewHolder2 holder2 = (ViewHolder2) holder;

            ArrayList<UserData> userArray = data.getUserArray();
            int count = userArray.size();
            if (count > 4)
                count = 4;

            initProfImages(holder2);

            switch (count) {
                case 2:
                    holder2.iv_prof_02.setVisibility(View.VISIBLE);
                    holder2.tv_prof_padding_top.setVisibility(View.VISIBLE);

                    for (int k = 0; k < 2; k++) {
                        if (!StringUtil.isNull(userArray.get(k).getProfile_img())) {
                            Glide.with(act)
                                    .load(NetUrls.DOMAIN + (userArray.get(k).getProfile_img()))
                                    .transform(new RoundedCorners(27))
                                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                                    .into(holder2.images.get(k));
                        } else {
                            Glide.with(act)
                                    .load(R.drawable.wt_profile156_def_191126)
                                    .transform(new RoundedCorners(27))
                                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                                    .into(holder2.images.get(k));
                        }
                    }
                    break;

                case 3:
                    holder2.iv_prof_03.setVisibility(View.VISIBLE);
                    holder2.iv_prof_04.setVisibility(View.VISIBLE);
                    holder2.tv_prof_padding_bottom.setVisibility(View.VISIBLE);

                    for (int k = 0; k < 3; k++) {
                        if (k == 0) {
                            if (!StringUtil.isNull(userArray.get(k).getProfile_img())) {
                                Glide.with(act)
                                        .load(NetUrls.DOMAIN + (userArray.get(k).getProfile_img()))
                                        .transform(new RoundedCorners(27))
                                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                                        .into(holder2.images.get(k));
                            } else {
                                Glide.with(act)
                                        .load(R.drawable.wt_profile156_def_191126)
                                        .transform(new RoundedCorners(27))
                                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                                        .into(holder2.images.get(k));
                            }
                        } else {
                            if (!StringUtil.isNull(userArray.get(k).getProfile_img())) {
                                Glide.with(act)
                                        .load(NetUrls.DOMAIN + (userArray.get(k).getProfile_img()))
                                        .transform(new RoundedCorners(27))
                                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                                        .into(holder2.images.get(k+1));
                            } else {
                                Glide.with(act)
                                        .load(R.drawable.wt_profile156_def_191126)
                                        .transform(new RoundedCorners(27))
                                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                                        .into(holder2.images.get(k+1));
                            }
                        }
                    }
                    break;
                default:
                    holder2.iv_prof_02.setVisibility(View.VISIBLE);
                    holder2.iv_prof_03.setVisibility(View.VISIBLE);
                    holder2.iv_prof_04.setVisibility(View.VISIBLE);
                    holder2.tv_prof_padding_top.setVisibility(View.VISIBLE);
                    holder2.tv_prof_padding_bottom.setVisibility(View.VISIBLE);

                    for (int k = 0; k < 4; k++) {
                        if (!StringUtil.isNull(userArray.get(k).getProfile_img())) {
                            Glide.with(act)
                                    .load(NetUrls.DOMAIN + (userArray.get(k).getProfile_img()))
                                    .transform(new RoundedCorners(27))
                                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                                    .into(holder2.images.get(k));
                        } else {
                            Glide.with(act)
                                    .load(R.drawable.wt_profile156_def_191126)
                                    .transform(new RoundedCorners(27))
                                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                                    .into(holder2.images.get(k));
                        }
                    }
                    break;
            }



            // set users name
            holder2.tv_name.setText(data.getUserNames());

            // set list data text
            holder2.tv_contents.setText(data.getText());

            // set list data send time
            holder2.tv_send_time.setText(data.getSendTime());

            // set list read count
            if (data.getReadCount() == null || data.getReadCount().length() < 1 || data.getReadCount().equals("0")) {
                holder2.tv_read_count.setVisibility(View.INVISIBLE);
            } else {
                holder2.tv_read_count.setVisibility(View.VISIBLE);
                holder2.tv_read_count.setText(data.getReadCount());
            }


            // click listener
            holder2.ll_all_area.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(act, ChatAct.class);
                    intent.putExtra("roomIdx", data.getRoomIdx());
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    act.startActivity(intent);
                }
            });
        }
    }

    private void initProfImages(ViewHolder2 holder) {
        for (int i = 1; i < holder.images.size(); i++) {
            holder.images.get(i).setVisibility(View.GONE);
        }

        holder.tv_prof_padding_top.setVisibility(View.GONE);
        holder.tv_prof_padding_bottom.setVisibility(View.GONE);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public void setList(ArrayList<ChattingListData> list) {
        this.list = list;
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        ViewHolder(@NonNull View view) {
            super(view);
        }
    }

    class ViewHolder1 extends ViewHolder {
        ImageView iv_profile;
        TextView tv_name, tv_contents, tv_read_count, tv_send_time;
        LinearLayout ll_all_area;

        ViewHolder1(@NonNull View view) {
            super(view);
            iv_profile = view.findViewById(R.id.iv_profile);

            tv_name = view.findViewById(R.id.tv_name);
            tv_contents = view.findViewById(R.id.tv_contents);
            tv_read_count = view.findViewById(R.id.tv_read_count);
            tv_send_time = view.findViewById(R.id.tv_send_time);

            ll_all_area = view.findViewById(R.id.ll_all_area);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                iv_profile.setClipToOutline(true);
            }
        }
    }

    class ViewHolder2 extends ViewHolder {
        TextView tv_name, tv_contents, tv_read_count, tv_send_time;
        LinearLayout ll_all_area;

        ImageView iv_prof_01, iv_prof_02, iv_prof_03, iv_prof_04;
        TextView tv_prof_padding_top, tv_prof_padding_bottom;

        ArrayList<ImageView> images;

        ViewHolder2(@NonNull View view) {
            super(view);
            tv_name = view.findViewById(R.id.tv_name);
            tv_contents = view.findViewById(R.id.tv_contents);
            tv_read_count = view.findViewById(R.id.tv_read_count);
            tv_send_time = view.findViewById(R.id.tv_send_time);

            ll_all_area = view.findViewById(R.id.ll_all_area);

            iv_prof_01 = (ImageView) view.findViewById(R.id.iv_prof_01);
            iv_prof_02 = (ImageView) view.findViewById(R.id.iv_prof_02);
            iv_prof_03 = (ImageView) view.findViewById(R.id.iv_prof_03);
            iv_prof_04 = (ImageView) view.findViewById(R.id.iv_prof_04);

            tv_prof_padding_top = view.findViewById(R.id.tv_prof_padding_top);
            tv_prof_padding_bottom = view.findViewById(R.id.tv_prof_padding_bottom);

            images = new ArrayList<>();
            images.add(iv_prof_01);
            images.add(iv_prof_02);
            images.add(iv_prof_03);
            images.add(iv_prof_04);
        }
    }
}
