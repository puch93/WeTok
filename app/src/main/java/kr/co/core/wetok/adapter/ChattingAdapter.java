package kr.co.core.wetok.adapter;

import android.app.Activity;
import android.content.Intent;
import android.media.Image;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.CountDownTimer;
import android.text.format.Formatter;
import android.util.Log;
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
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.DrawableImageViewTarget;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import io.socket.client.On;
import kr.co.core.wetok.R;
import kr.co.core.wetok.activity.ChatAct;
import kr.co.core.wetok.activity.EnlargeAct;
import kr.co.core.wetok.data.ChattingData;
import kr.co.core.wetok.data.UserData;
import kr.co.core.wetok.preference.UserPref;
import kr.co.core.wetok.server.JSONUrl;
import kr.co.core.wetok.server.netUtil.NetUrls;
import kr.co.core.wetok.util.Common;
import kr.co.core.wetok.util.RecordUtil;
import kr.co.core.wetok.util.StringUtil;

public class ChattingAdapter extends RecyclerView.Adapter<ChattingAdapter.ViewHolder> {
    private static final int TYPE_ME_TEXT = 1;
    private static final int TYPE_ME_IMAGE = 3;
    private static final int TYPE_ME_VIDEO = 5;
    private static final int TYPE_ME_FILE = 7;
    private static final int TYPE_ME_RECORD = 9;
    private static final int TYPE_ME_CALL_VOICE = 11;
    private static final int TYPE_ME_CALL_VIDEO = 13;

    private static final int TYPE_YOU_TEXT = 2;
    private static final int TYPE_YOU_IMAGE = 4;
    private static final int TYPE_YOU_VIDEO = 6;
    private static final int TYPE_YOU_FILE = 8;
    private static final int TYPE_YOU_RECORD = 10;
    private static final int TYPE_YOU_CALL_VOICE = 12;
    private static final int TYPE_YOU_CALL_VIDEO = 14;

    private static final int TYPE_SYSTEM = 99;

    private static final int TYPE_DATE_LINE = 100;

    private Activity act;
    private ArrayList<ChattingData> list;

    private int current_idx = -1;
    private int previous_idx = -1;
    private RecordUtil recordUtilMe;

    public ChattingAdapter(Activity act) {
        this.act = act;

        // set record util for play
        recordUtilMe = new RecordUtil(act, null, new RecordUtil.MediaStateListener() {
            @Override
            public void afterStart() {
                list.get(current_idx).setSelected(true);
//                notifyItemChanged(current_idx);
                notifyDataSetChanged();
            }

            @Override
            public void afterPause() {
                list.get(current_idx).setSelected(false);
//                notifyItemChanged(current_idx);
                notifyDataSetChanged();
            }

            @Override
            public void afterResume() {
                list.get(current_idx).setSelected(true);
//                notifyItemChanged(current_idx);
                notifyDataSetChanged();
            }

            @Override
            public void afterStop() {

            }

            @Override
            public void afterClose() {
                if (current_idx != -1) {
                    list.get(current_idx).setSelected(false);
//                    notifyItemChanged(current_idx);
                    notifyDataSetChanged();
                }
            }

            @Override
            public void synchronizeTime(String time) {
            }
        });
    }

    public void addItem(ChattingData item) {
        if (list == null) {
            list = new ArrayList<>();
        }
        list.add(item);
        notifyDataSetChanged();
    }

    public void setItem(ChattingData item) {
        list.set(list.size() - 1, item);
        notifyDataSetChanged();
    }

    public void setList(ArrayList<ChattingData> list) {
        this.list = list;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ChattingAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        switch (viewType) {
            case TYPE_ME_TEXT:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_me, parent, false);
                return new ViewHolder1(view);
            case TYPE_YOU_TEXT:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_other, parent, false);
                return new ViewHolder2(view);

            case TYPE_ME_IMAGE:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_me_image, parent, false);
                return new ViewHolder3(view);
            case TYPE_YOU_IMAGE:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_other_image, parent, false);
                return new ViewHolder4(view);

            case TYPE_ME_VIDEO:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_me_video, parent, false);
                return new ViewHolder5(view);
            case TYPE_YOU_VIDEO:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_other_video, parent, false);
                return new ViewHolder6(view);

            case TYPE_ME_FILE:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_me_file, parent, false);
                return new ViewHolder7(view);
            case TYPE_YOU_FILE:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_other_file, parent, false);
                return new ViewHolder8(view);

            case TYPE_ME_RECORD:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_me_record, parent, false);
                return new ViewHolder9(view);
            case TYPE_YOU_RECORD:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_other_record, parent, false);
                return new ViewHolder10(view);

            case TYPE_ME_CALL_VOICE:
            case TYPE_ME_CALL_VIDEO:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_me_call, parent, false);
                return new ViewHolder11(view);
            case TYPE_YOU_CALL_VOICE:
            case TYPE_YOU_CALL_VIDEO:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_other_call, parent, false);
                return new ViewHolder12(view);

            case TYPE_SYSTEM:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_system, parent, false);
                return new ViewHolder99(view);

            case TYPE_DATE_LINE:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_date, parent, false);
                return new ViewHolder100(view);

            default:
                return null;
        }
    }

    @Override
    public int getItemViewType(int position) {
        ChattingData data = list.get(position);
        String type = data.getType();

        String midx = UserPref.getMidx(act);

        switch (type) {
            case "text":
                if (data.getUser_idx().equalsIgnoreCase(midx))
                    return TYPE_ME_TEXT;
                else
                    return TYPE_YOU_TEXT;

            case "photo":
                if (data.getUser_idx().equalsIgnoreCase(midx))
                    return TYPE_ME_IMAGE;
                else
                    return TYPE_YOU_IMAGE;

            case "movie":
                if (data.getUser_idx().equalsIgnoreCase(midx))
                    return TYPE_ME_VIDEO;
                else
                    return TYPE_YOU_VIDEO;


            case "file":
                if (data.getUser_idx().equalsIgnoreCase(midx))
                    return TYPE_ME_FILE;
                else
                    return TYPE_YOU_FILE;

            case "mic":
                if (data.getUser_idx().equalsIgnoreCase(midx))
                    return TYPE_ME_RECORD;
                else
                    return TYPE_YOU_RECORD;

            case "call_voice":
                if (data.getUser_idx().equalsIgnoreCase(midx))
                    return TYPE_ME_CALL_VOICE;
                else
                    return TYPE_YOU_CALL_VOICE;

            case "call_video":
                if (data.getUser_idx().equalsIgnoreCase(midx))
                    return TYPE_ME_CALL_VIDEO;
                else
                    return TYPE_YOU_CALL_VIDEO;

            case "system":
                return TYPE_SYSTEM;

            case "dateLine":
                return TYPE_DATE_LINE;

            default:
                return super.getItemViewType(position);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull ChattingAdapter.ViewHolder viewHolder, int i) {
        final ChattingData data = list.get(i);
        int viewType = getItemViewType(i);

        switch (viewType) {
            case TYPE_ME_TEXT:
                ViewHolder1 holder1 = (ViewHolder1) viewHolder;

                // set text contents
                holder1.tv_text.setText(data.getMsg());

                // set send time
                holder1.tv_send_time.setText(data.getSendTime());

                // set is read
                holder1.tv_is_read.setText(data.getIsRead());
                if (!StringUtil.isNull(data.getIsRead()) && data.getIsRead().equals("0")) {
                    holder1.tv_is_read.setVisibility(View.GONE);
                } else {
                    holder1.tv_is_read.setVisibility(View.VISIBLE);
                }

                // set long click listener
                holder1.tv_text.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        ((ChatAct) act).showDialogDelete(data.getIdx());
                        return true;
                    }
                });
                break;

            case TYPE_YOU_TEXT:
                ViewHolder2 holder2 = (ViewHolder2) viewHolder;

                // set other data
                setOtherData(holder2, data.getU_name(), data.getU_image());

                // set text contents
                holder2.tv_text.setText(data.getMsg());

                // set send time
                holder2.tv_send_time.setText(data.getSendTime());

                // set is read
                holder2.tv_is_read.setText(data.getIsRead());
                if (!StringUtil.isNull(data.getIsRead()) && data.getIsRead().equals("0")) {
                    holder2.tv_is_read.setVisibility(View.GONE);
                } else {
                    holder2.tv_is_read.setVisibility(View.VISIBLE);
                }
                break;

            case TYPE_ME_IMAGE:
                ViewHolder3 holder3 = (ViewHolder3) viewHolder;

                // set image contents
                Glide.with(act)
                        .load(NetUrls.DOMAIN + data.getMsg())
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(holder3.iv_send_image);

                // set send time
                holder3.tv_send_time.setText(data.getSendTime());

                // set is read
                holder3.tv_is_read.setText(data.getIsRead());
                if (!StringUtil.isNull(data.getIsRead()) && data.getIsRead().equals("0")) {
                    holder3.tv_is_read.setVisibility(View.GONE);
                } else {
                    holder3.tv_is_read.setVisibility(View.VISIBLE);
                }

                // set long click listener
                holder3.iv_send_image.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        ((ChatAct) act).showDialogDelete(data.getIdx());
                        return true;
                    }
                });

                // set image enlarge
                holder3.iv_send_image.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        enlargeImage(data.getMsg());
                    }
                });
                break;

            case TYPE_YOU_IMAGE:
                ViewHolder4 holder4 = (ViewHolder4) viewHolder;

                // set other data
                setOtherData(holder4, data.getU_name(), data.getU_image());

                // set image contents
                RequestOptions options_you = RequestOptions.bitmapTransform(new RoundedCorners(30));

                Glide.with(act)
                        .load(NetUrls.DOMAIN + data.getMsg())
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(holder4.iv_send_image);

                // set send time
                holder4.tv_send_time.setText(data.getSendTime());

                // set is read
                holder4.tv_is_read.setText(data.getIsRead());
                if (!StringUtil.isNull(data.getIsRead()) && data.getIsRead().equals("0")) {
                    holder4.tv_is_read.setVisibility(View.GONE);
                } else {
                    holder4.tv_is_read.setVisibility(View.VISIBLE);
                }

                // set image enlarge
                holder4.iv_send_image.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        enlargeImage(data.getMsg());
                    }
                });
                break;

            case TYPE_ME_VIDEO:
                ViewHolder5 holder5 = (ViewHolder5) viewHolder;

                // set thumbnail image
                String[] all_me = data.getMsg().split(",");
                String video_me = all_me[0];
                String thumbnail_me = all_me[1];
                String time_me = all_me[2];

                Glide.with(act)
                        .load(NetUrls.DOMAIN + thumbnail_me)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(holder5.iv_thumbnail);


                // set video play time
                holder5.tv_play_time.setText(time_me);

                // set send time
                holder5.tv_send_time.setText(data.getSendTime());

                // set is read
                holder5.tv_is_read.setText(data.getIsRead());
                if (!StringUtil.isNull(data.getIsRead()) && data.getIsRead().equals("0")) {
                    holder5.tv_is_read.setVisibility(View.GONE);
                } else {
                    holder5.tv_is_read.setVisibility(View.VISIBLE);
                }

                // set long click listener
                holder5.fl_video_area.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        ((ChatAct) act).showDialogDelete(data.getIdx());
                        return true;
                    }
                });

                // set video click listener
                holder5.fl_video_area.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        Uri uri = Uri.parse(NetUrls.DOMAIN + video_me);
                        intent.setDataAndType(uri, "video/*");
                        if (intent.resolveActivity(act.getPackageManager()) != null) {
                            act.startActivity(intent);
                        }
                    }
                });
                break;

            case TYPE_YOU_VIDEO:
                ViewHolder6 holder6 = (ViewHolder6) viewHolder;

                // set other data
                setOtherData(holder6, data.getU_name(), data.getU_image());

                // set thumbnail image
                String[] all_you = data.getMsg().split(",");
                String video_you = all_you[0];
                String thumbnail_you = all_you[1];
                String time_you = all_you[2];

                Glide.with(act)
                        .load(NetUrls.DOMAIN + thumbnail_you)
                        .into(holder6.iv_thumbnail);

                // set video play time
                holder6.tv_play_time.setText(time_you);

                // set send time
                holder6.tv_send_time.setText(data.getSendTime());

                // set is read
                holder6.tv_is_read.setText(data.getIsRead());
                if (!StringUtil.isNull(data.getIsRead()) && data.getIsRead().equals("0")) {
                    holder6.tv_is_read.setVisibility(View.GONE);
                } else {
                    holder6.tv_is_read.setVisibility(View.VISIBLE);
                }

                // set video click listener
                holder6.fl_video_area.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        Uri uri = Uri.parse(NetUrls.DOMAIN + video_you);
                        intent.setDataAndType(uri, "video/*");
                        if (intent.resolveActivity(act.getPackageManager()) != null) {
                            act.startActivity(intent);
                        }
                    }
                });
                break;

            case TYPE_ME_FILE:
                ViewHolder7 holder7 = (ViewHolder7) viewHolder;

                // set send time
                holder7.tv_send_time.setText(data.getSendTime());

                // set is read
                holder7.tv_is_read.setText(data.getIsRead());
                if (!StringUtil.isNull(data.getIsRead()) && data.getIsRead().equals("0")) {
                    holder7.tv_is_read.setVisibility(View.GONE);
                } else {
                    holder7.tv_is_read.setVisibility(View.VISIBLE);
                }

                // set file info
                File file_me = new File(data.getMsg());
                holder7.tv_file_byte.setText(data.getFile_size());
                holder7.tv_file_name.setText(file_me.getName());
                holder7.tv_file_expiration.setText(data.getLimit_time());

                // set click listener
                holder7.ll_all_area.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ((ChatAct) ChatAct.real_act).downloadFile(NetUrls.DOMAIN + data.getMsg());
                    }
                });

                // set long click listener
                holder7.ll_all_area.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        ((ChatAct) act).showDialogDelete(data.getIdx());
                        return true;
                    }
                });
                break;

            case TYPE_YOU_FILE:
                ViewHolder8 holder8 = (ViewHolder8) viewHolder;

                // set other data
                setOtherData(holder8, data.getU_name(), data.getU_image());

                // set send time
                holder8.tv_send_time.setText(data.getSendTime());

                // set is read
                holder8.tv_is_read.setText(data.getIsRead());
                if (!StringUtil.isNull(data.getIsRead()) && data.getIsRead().equals("0")) {
                    holder8.tv_is_read.setVisibility(View.GONE);
                } else {
                    holder8.tv_is_read.setVisibility(View.VISIBLE);
                }

                // set file info
                File file_you = new File(data.getMsg());
                holder8.tv_file_byte.setText(data.getFile_size());
                holder8.tv_file_name.setText(file_you.getName());
                holder8.tv_file_expiration.setText(data.getLimit_time());

                // set click listener
                holder8.ll_all_area.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ((ChatAct) ChatAct.real_act).downloadFile(NetUrls.DOMAIN + data.getMsg());
                    }
                });
                break;


            case TYPE_ME_RECORD:
                ViewHolder9 holder9 = (ViewHolder9) viewHolder;

                holder9.tv_record_time.setText(data.getFile_size());


                if (data.isSelected()) {
                    Glide.with(act)
                            .asGif()
                            .load(R.raw.wifi01)
                            .into(holder9.iv_record_image);
                } else {
                    Glide.with(act)
                            .load(R.drawable.wifi01_off)
                            .into(holder9.iv_record_image);
                }

                // set click listener
                holder9.ll_all_area.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String state = "default";
                        if (current_idx == i) {
                            state = recordUtilMe.getMediaState();
                        }

                        switch (state) {
                            case "playing":
                            case "resume":
                                recordUtilMe.pausePlaying();
                                break;
                            case "pause":
                                recordUtilMe.resumePlaying();
                                break;
                            default:
                                recordUtilMe.closePlaying();

                                previous_idx = current_idx;
                                current_idx = i;

                                recordUtilMe.startPlaying(NetUrls.DOMAIN + data.getMsg());
                                break;
                        }
                    }
                });

                /* other info set */

                // set send time
                holder9.tv_send_time.setText(data.getSendTime());

                // set is read
                holder9.tv_is_read.setText(data.getIsRead());
                if (!StringUtil.isNull(data.getIsRead()) && data.getIsRead().equals("0")) {
                    holder9.tv_is_read.setVisibility(View.GONE);
                } else {
                    holder9.tv_is_read.setVisibility(View.VISIBLE);
                }

                // set long click listener
                holder9.ll_all_area.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        ((ChatAct) act).showDialogDelete(data.getIdx());
                        return true;
                    }
                });
                break;

            case TYPE_YOU_RECORD:
                ViewHolder10 holder10 = (ViewHolder10) viewHolder;

                holder10.tv_record_time.setText(data.getFile_size());

                if (data.isSelected()) {
                    Glide.with(act)
                            .asGif()
                            .load(R.raw.wifi01)
                            .into(holder10.iv_record_image);
                } else {
                    Glide.with(act)
                            .load(R.drawable.wifi01_off)
                            .into(holder10.iv_record_image);
                }

                // set click listener
                holder10.ll_all_area.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String state = "default";
                        if (current_idx == i) {
                            state = recordUtilMe.getMediaState();
                        }

                        switch (state) {
                            case "playing":
                            case "resume":
                                recordUtilMe.pausePlaying();
                                break;
                            case "pause":
                                recordUtilMe.resumePlaying();
                                break;
                            default:
                                recordUtilMe.closePlaying();

                                previous_idx = current_idx;
                                current_idx = i;

                                recordUtilMe.startPlaying(NetUrls.DOMAIN + data.getMsg());
                                break;
                        }
                    }
                });


                /* set other info */

                // set other data
                setOtherData(holder10, data.getU_name(), data.getU_image());

                // set send time
                holder10.tv_send_time.setText(data.getSendTime());

                // set is read
                holder10.tv_is_read.setText(data.getIsRead());
                if (!StringUtil.isNull(data.getIsRead()) && data.getIsRead().equals("0")) {
                    holder10.tv_is_read.setVisibility(View.GONE);
                } else {
                    holder10.tv_is_read.setVisibility(View.VISIBLE);
                }
                break;


            case TYPE_ME_CALL_VOICE:
            case TYPE_ME_CALL_VIDEO:
                ViewHolder11 holder11 = (ViewHolder11) viewHolder;

                // set call state
                if(viewType == TYPE_ME_CALL_VOICE) {
                    Glide.with(act).load(R.drawable.voice_call).into(holder11.iv_call_state);
                } else {
                    Glide.with(act).load(R.drawable.video_call).into(holder11.iv_call_state);
                }
                holder11.tv_text.setText(data.getMsg());

                // set send time
                holder11.tv_send_time.setText(data.getSendTime());

                // set is read
                holder11.tv_is_read.setText(data.getIsRead());
                if (!StringUtil.isNull(data.getIsRead()) && data.getIsRead().equals("0")) {
                    holder11.tv_is_read.setVisibility(View.GONE);
                } else {
                    holder11.tv_is_read.setVisibility(View.VISIBLE);
                }

                // set long click listener
                holder11.tv_text.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        ((ChatAct) act).showDialogDelete(data.getIdx());
                        return true;
                    }
                });
                break;

            case TYPE_YOU_CALL_VOICE:
            case TYPE_YOU_CALL_VIDEO:
                ViewHolder12 holder12 = (ViewHolder12) viewHolder;

                // set other data
                setOtherData(holder12, data.getU_name(), data.getU_image());

                // set call state
                if(viewType == TYPE_YOU_CALL_VOICE) {
                    Glide.with(act).load(R.drawable.voice_call).into(holder12.iv_call_state);
                } else {
                    Glide.with(act).load(R.drawable.video_call).into(holder12.iv_call_state);
                }

                holder12.tv_text.setText(data.getMsg());

                // set send time
                holder12.tv_send_time.setText(data.getSendTime());

                // set is read
                holder12.tv_is_read.setText(data.getIsRead());
                if (!StringUtil.isNull(data.getIsRead()) && data.getIsRead().equals("0")) {
                    holder12.tv_is_read.setVisibility(View.GONE);
                } else {
                    holder12.tv_is_read.setVisibility(View.VISIBLE);
                }
                break;

            case TYPE_SYSTEM:
                ViewHolder99 holder99 = (ViewHolder99) viewHolder;

                // set date
                holder99.tv_system.setText(data.getMsg());
                break;

            case TYPE_DATE_LINE:
                ViewHolder100 holder100 = (ViewHolder100) viewHolder;

                // set date
                holder100.tv_date.setText(data.getDateLine());
                break;
        }

    }

    private void enlargeImage(String imageUrl) {
        Intent intent = new Intent(act, EnlargeAct.class);
        intent.putExtra("imageUrl", NetUrls.DOMAIN + imageUrl);
        act.startActivity(intent);
    }

    private void setOtherData(ViewHolder holder, String name, String imageUrl) {
        if (holder instanceof ViewHolder2) {
            ViewHolder2 holder2 = (ViewHolder2) holder;

            // set name
            holder2.tv_name.setText(name);

            // set profile image
            Glide.with(act)
                    .load(NetUrls.DOMAIN + imageUrl)
                    .transform(new RoundedCorners(45))
                    .into(holder2.iv_profile);

            // set profile image enlarge
            holder2.iv_profile.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!StringUtil.isNull(imageUrl))
                        enlargeImage(imageUrl);
                }
            });

        } else if (holder instanceof ViewHolder4) {
            ViewHolder4 holder4 = (ViewHolder4) holder;

            // set name
            holder4.tv_name.setText(name);

            // set profile image
            Glide.with(act)
                    .load(NetUrls.DOMAIN + imageUrl)
                    .transform(new RoundedCorners(45))
                    .into(holder4.iv_profile);

            // set profile image enlarge
            holder4.iv_profile.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!StringUtil.isNull(imageUrl))
                        enlargeImage(imageUrl);
                }
            });

        } else if (holder instanceof ViewHolder6) {
            ViewHolder6 holder6 = (ViewHolder6) holder;

            // set name
            holder6.tv_name.setText(name);

            // set profile image
            Glide.with(act)
                    .load(NetUrls.DOMAIN + imageUrl)
                    .transform(new RoundedCorners(45))
                    .into(holder6.iv_profile);

            // set profile image enlarge
            holder6.iv_profile.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!StringUtil.isNull(imageUrl))
                        enlargeImage(imageUrl);
                }
            });

        } else if (holder instanceof ViewHolder8) {
            ViewHolder8 holder8 = (ViewHolder8) holder;

            // set name
            holder8.tv_name.setText(name);

            // set profile image
            Glide.with(act)
                    .load(NetUrls.DOMAIN + imageUrl)
                    .transform(new RoundedCorners(45))
                    .into(holder8.iv_profile);

            // set profile image enlarge
            holder8.iv_profile.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!StringUtil.isNull(imageUrl))
                        enlargeImage(imageUrl);
                }
            });
        } else if (holder instanceof ViewHolder10){
            ViewHolder10 holder10 = (ViewHolder10) holder;

            // set name
            holder10.tv_name.setText(name);

            // set profile image
            Glide.with(act)
                    .load(NetUrls.DOMAIN + imageUrl)
                    .transform(new RoundedCorners(45))
                    .into(holder10.iv_profile);

            // set profile image enlarge
            holder10.iv_profile.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!StringUtil.isNull(imageUrl))
                        enlargeImage(imageUrl);
                }
            });
        } else {
            ViewHolder12 holder12 = (ViewHolder12) holder;

            // set name
            holder12.tv_name.setText(name);

            // set profile image
            Glide.with(act)
                    .load(NetUrls.DOMAIN + imageUrl)
                    .transform(new RoundedCorners(45))
                    .into(holder12.iv_profile);

            // set profile image enlarge
            holder12.iv_profile.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!StringUtil.isNull(imageUrl))
                        enlargeImage(imageUrl);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return list == null ? 0 : list.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        ViewHolder(@NonNull View view) {
            super(view);
        }
    }

    class ViewHolder1 extends ViewHolder {
        TextView tv_text, tv_send_time, tv_is_read;

        ViewHolder1(@NonNull View view) {
            super(view);
            tv_is_read = view.findViewById(R.id.tv_is_read);
            tv_text = view.findViewById(R.id.tv_text);
            tv_send_time = view.findViewById(R.id.tv_send_time);
        }
    }

    class ViewHolder2 extends ViewHolder {
        TextView tv_text, tv_send_time, tv_name, tv_is_read;
        ImageView iv_profile;

        ViewHolder2(@NonNull View view) {
            super(view);
            tv_text = view.findViewById(R.id.tv_text);
            tv_send_time = view.findViewById(R.id.tv_send_time);
            tv_name = view.findViewById(R.id.tv_name);
            tv_is_read = view.findViewById(R.id.tv_is_read);
            iv_profile = (ImageView) view.findViewById(R.id.iv_profile);

        }
    }

    class ViewHolder3 extends ViewHolder {
        TextView tv_send_time, tv_is_read;
        ImageView iv_send_image;

        ViewHolder3(@NonNull View view) {
            super(view);
            tv_is_read = view.findViewById(R.id.tv_is_read);
            tv_send_time = view.findViewById(R.id.tv_send_time);
            iv_send_image = (ImageView) view.findViewById(R.id.iv_send_image);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                iv_send_image.setClipToOutline(true);
            }
        }
    }

    class ViewHolder4 extends ViewHolder {
        TextView tv_send_time, tv_name, tv_is_read;
        ImageView iv_profile, iv_send_image;

        ViewHolder4(@NonNull View view) {
            super(view);
            tv_send_time = view.findViewById(R.id.tv_send_time);
            tv_name = view.findViewById(R.id.tv_name);
            iv_profile = (ImageView) view.findViewById(R.id.iv_profile);
            iv_send_image = (ImageView) view.findViewById(R.id.iv_send_image);
            tv_is_read = view.findViewById(R.id.tv_is_read);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                iv_send_image.setClipToOutline(true);
            }
        }
    }

    class ViewHolder5 extends ViewHolder {
        TextView tv_send_time, tv_is_read, tv_play_time;
        ImageView iv_thumbnail;
        FrameLayout fl_video_area;

        ViewHolder5(@NonNull View view) {
            super(view);
            tv_is_read = view.findViewById(R.id.tv_is_read);
            tv_play_time = view.findViewById(R.id.tv_play_time);
            tv_send_time = view.findViewById(R.id.tv_send_time);
            iv_thumbnail = view.findViewById(R.id.iv_thumbnail);
            fl_video_area = view.findViewById(R.id.fl_video_area);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                iv_thumbnail.setClipToOutline(true);
            }
        }
    }

    class ViewHolder6 extends ViewHolder {
        TextView tv_send_time, tv_name, tv_is_read, tv_play_time;
        ImageView iv_profile, iv_thumbnail;
        FrameLayout fl_video_area;

        ViewHolder6(@NonNull View view) {
            super(view);
            tv_send_time = view.findViewById(R.id.tv_send_time);
            tv_play_time = view.findViewById(R.id.tv_play_time);
            tv_name = view.findViewById(R.id.tv_name);
            iv_profile = view.findViewById(R.id.iv_profile);
            iv_thumbnail = view.findViewById(R.id.iv_thumbnail);
            tv_is_read = view.findViewById(R.id.tv_is_read);
            fl_video_area = view.findViewById(R.id.fl_video_area);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                iv_thumbnail.setClipToOutline(true);
            }
        }
    }

    class ViewHolder7 extends ViewHolder {
        TextView tv_file_name, tv_file_expiration, tv_file_byte, tv_send_time, tv_is_read;
        LinearLayout ll_all_area;

        ViewHolder7(@NonNull View view) {
            super(view);
            tv_is_read = view.findViewById(R.id.tv_is_read);
            tv_send_time = view.findViewById(R.id.tv_send_time);

            tv_file_name = view.findViewById(R.id.tv_file_name);
            tv_file_expiration = view.findViewById(R.id.tv_file_expiration);
            tv_file_byte = view.findViewById(R.id.tv_file_byte);
            ll_all_area = view.findViewById(R.id.ll_all_area);
        }
    }

    class ViewHolder8 extends ViewHolder {
        TextView tv_file_name, tv_file_expiration, tv_file_byte, tv_send_time, tv_name, tv_is_read;
        ImageView iv_profile;
        LinearLayout ll_all_area;

        ViewHolder8(@NonNull View view) {
            super(view);
            tv_send_time = view.findViewById(R.id.tv_send_time);
            tv_name = view.findViewById(R.id.tv_name);
            tv_is_read = view.findViewById(R.id.tv_is_read);
            iv_profile = (ImageView) view.findViewById(R.id.iv_profile);

            tv_file_name = view.findViewById(R.id.tv_file_name);
            tv_file_expiration = view.findViewById(R.id.tv_file_expiration);
            tv_file_byte = view.findViewById(R.id.tv_file_byte);
            ll_all_area = view.findViewById(R.id.ll_all_area);

        }
    }

    class ViewHolder9 extends ViewHolder {
        TextView tv_record_time, tv_send_time, tv_is_read;
        ImageView iv_record_image;
        LinearLayout ll_all_area;

        ViewHolder9(@NonNull View view) {
            super(view);
            tv_is_read = view.findViewById(R.id.tv_is_read);
            tv_send_time = view.findViewById(R.id.tv_send_time);

            iv_record_image = (ImageView) view.findViewById(R.id.iv_record_image);

            tv_record_time = view.findViewById(R.id.tv_record_time);
            ll_all_area = view.findViewById(R.id.ll_all_area);
        }
    }

    class ViewHolder10 extends ViewHolder {
        TextView tv_record_time, tv_send_time, tv_name, tv_is_read;
        ImageView iv_profile, iv_record_image;
        LinearLayout ll_all_area;

        ViewHolder10(@NonNull View view) {
            super(view);
            tv_send_time = view.findViewById(R.id.tv_send_time);
            tv_name = view.findViewById(R.id.tv_name);
            tv_is_read = view.findViewById(R.id.tv_is_read);
            iv_profile = (ImageView) view.findViewById(R.id.iv_profile);

            iv_record_image = (ImageView) view.findViewById(R.id.iv_record_image);

            tv_record_time = view.findViewById(R.id.tv_record_time);
            ll_all_area = view.findViewById(R.id.ll_all_area);

        }
    }

    class ViewHolder11 extends ViewHolder {
        TextView tv_text, tv_send_time, tv_is_read;
        ImageView iv_call_state;

        ViewHolder11(@NonNull View view) {
            super(view);
            tv_is_read = view.findViewById(R.id.tv_is_read);
            tv_text = view.findViewById(R.id.tv_text);
            tv_send_time = view.findViewById(R.id.tv_send_time);
            iv_call_state = view.findViewById(R.id.iv_call_state);
        }
    }

    class ViewHolder12 extends ViewHolder {
        TextView tv_text, tv_send_time, tv_name, tv_is_read;
        ImageView iv_profile;
        ImageView iv_call_state;

        ViewHolder12(@NonNull View view) {
            super(view);
            tv_text = view.findViewById(R.id.tv_text);
            tv_send_time = view.findViewById(R.id.tv_send_time);
            tv_name = view.findViewById(R.id.tv_name);
            tv_is_read = view.findViewById(R.id.tv_is_read);
            iv_profile = (ImageView) view.findViewById(R.id.iv_profile);
            iv_call_state = view.findViewById(R.id.iv_call_state);
        }
    }

    class ViewHolder99 extends ViewHolder {
        TextView tv_system;

        ViewHolder99(@NonNull View view) {
            super(view);
            tv_system = view.findViewById(R.id.tv_system);
        }
    }

    class ViewHolder100 extends ViewHolder {
        TextView tv_date;

        ViewHolder100(@NonNull View view) {
            super(view);
            tv_date = view.findViewById(R.id.tv_date);
        }
    }
}
