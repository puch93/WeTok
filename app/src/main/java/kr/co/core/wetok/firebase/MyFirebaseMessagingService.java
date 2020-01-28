package kr.co.core.wetok.firebase;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.PowerManager;
import android.util.Log;
import android.view.View;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import kr.co.core.wetok.R;
import kr.co.core.wetok.activity.ChatAct;
import kr.co.core.wetok.activity.MainAct;
import kr.co.core.wetok.activity.rtc.ConnectActivity;
import kr.co.core.wetok.activity.rtc.VideoCallAct;
import kr.co.core.wetok.activity.rtc.VideoReceiveAct;
import kr.co.core.wetok.activity.rtc.VoiceCallAct;
import kr.co.core.wetok.activity.rtc.VoiceReceiveAct;
import kr.co.core.wetok.data.UserData;
import kr.co.core.wetok.dialog.PopUpAdDlg;
import kr.co.core.wetok.preference.UserPref;
import kr.co.core.wetok.server.ReqBasic;
import kr.co.core.wetok.server.netUtil.HttpResult;
import kr.co.core.wetok.server.netUtil.NetUrls;
import kr.co.core.wetok.util.Common;
import kr.co.core.wetok.util.PushWakeLock;
import kr.co.core.wetok.util.StringUtil;


public class MyFirebaseMessagingService extends com.google.firebase.messaging.FirebaseMessagingService {

    private static final int ALARM_NITI = 1;

    private Context ctx;

    Timer timer;
    TimerTask adTask;
    public static boolean disconnectState = true;

    private long currentTime;
    private long lastTime;
    private final long limitTime = 10000;

    String msg;

    @Override
    public void onNewToken(String token) {
        Log.e(StringUtil.TAG_PUSH, "Refreshed token: " + token);

        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // Instance ID token to your app server.
        sendRegistrationToServer(token);
    }

    private void sendRegistrationToServer(String token) {
        // TODO: Implement this method to send token to your app server.
    }


    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        ctx = this;

        Log.e(StringUtil.TAG_PUSH, "remoteMessage.getData: " + remoteMessage.getData());


        PowerManager pm = (PowerManager) ctx.getSystemService(POWER_SERVICE);
        if (!pm.isScreenOn()) {
            //화면 깨우기
            PushWakeLock.acquireCpuWakeLock(ctx);
            PushWakeLock.releaseCpuLock();
        }

        try {
            JSONObject jo = new JSONObject(remoteMessage.getData());

            String type = jo.getString("type");

            if (!StringUtil.isNull(type)) {
                if (type.equalsIgnoreCase("top")) {
                    // top 노티 일때

                    String url = jo.getString("url");
                    String msg = jo.getString("msg");

                    sendNotification("WeTOK", msg, url);

                } else if (type.equalsIgnoreCase("front")) {
                    // popup 노티 일때

                    String imageUrl = jo.getString("filename");
                    String url = jo.getString("url");

                    sendNotification("WeTOK", getString(R.string.firebase_popup_click), url);
                    sendPopup(imageUrl, url);

                } else if (type.equalsIgnoreCase("chat")) {
                    // 채팅 노티 일때
                    Log.e(StringUtil.TAG_PUSH, "chat in");

                    // set room idx
                    JSONArray ja = new JSONArray(remoteMessage.getData().get("guest_info"));
                    String names = "";
                    String roomIdx = "";
                    boolean isMe = false;
                    for (int i = 0; i < ja.length(); i++) {
                        JSONObject jsonObject = ja.getJSONObject(i);
                        if (jsonObject.getString("m_idx").equalsIgnoreCase(UserPref.getMidx(ctx))) {
                            isMe = true;
                            roomIdx = jsonObject.getString("cr_room_idx");
                        } else {
                            names += jsonObject.getString("m_nickname") + ",";
                        }
                    }

                    // set other info
                    JSONObject job = new JSONObject(remoteMessage.getData().get("user_info"));
                    names += job.getString("m_nickname");

                    /* 내가 로그인된 정보와 받는사람 정보 일치한지 idx 검사 & 내 idx null 인지 검사*/
                    if (!StringUtil.isNull(UserPref.getMidx(ctx)) && isMe) {
                        Log.e(StringUtil.TAG_PUSH, "정상");

                        // set message
                        String contents = jo.getString("body");

                        // roomIdx 가 다르거나, 채팅 액티비티가 null 일 경우에만 전송
                        if (!roomIdx.contains("R")) {
                            roomIdx = "R" + roomIdx;
                        }

                        if (ChatAct.real_act == null || !((ChatAct) ChatAct.real_act).roomIdx.equalsIgnoreCase(roomIdx)) {
                            // 메인 안읽은 갯수 갱신
                            if (MainAct.act != null) {
                                ((MainAct) MainAct.act).getReadCount();
                                ((MainAct) MainAct.act).checkFragment();
                            }

                            sendChatNotification(names, contents, roomIdx);
                        }
                    } else {
                        Log.e(StringUtil.TAG_PUSH, "내 인덱스가 null 이거나, 받는 정보 틀림");
                    }


                    // 음성/영상 통화 수신
                } else if(type.equalsIgnoreCase("idsend")) {
                    if (checkCallState()) {
                        cancelTimer();

                        JSONObject job = new JSONObject(String.valueOf(jo.get("memberInfo")));

                        UserData userData = new UserData();
                        userData.setIdx(job.getString("m_idx"));
                        userData.setId(job.getString("m_id"));
                        userData.setPw(job.getString("m_pass"));
                        userData.setHp(job.getString("m_hp"));
                        userData.setIntro(job.getString("m_intro"));
                        userData.setName(job.getString("m_nickname"));
                        userData.setBirth(job.getString("m_birthday"));
                        userData.setProfile_img(job.getString("m_profile"));
                        userData.setBackground_img(job.getString("m_background"));

                        String type_call = jo.getString("CLASS");
                        if (type_call.equalsIgnoreCase("음성")) {
                            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    Intent intent = new Intent(getApplicationContext(), ConnectActivity.class);
                                    intent.putExtra("mode", "voice");
                                    intent.putExtra("type", "receive");
                                    intent.putExtra("roomId", remoteMessage.getData().get("ID"));
                                    intent.putExtra("userData", userData);

                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                    PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
                                    try {
                                        pendingIntent.send();
                                    } catch (PendingIntent.CanceledException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }, 4000);
                        } else {
                            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                                @Override
                                public void run() {

                                    Intent intent = new Intent(getApplicationContext(), ConnectActivity.class);
                                    intent.putExtra("mode", "video");
                                    intent.putExtra("type", "receive");
                                    intent.putExtra("roomId", remoteMessage.getData().get("ID"));
                                    intent.putExtra("userData", userData);

                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                    PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
                                    try {
                                        pendingIntent.send();
                                    } catch (PendingIntent.CanceledException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }, 4000);
                        }
                    } else {
                        JSONObject job = new JSONObject(String.valueOf(jo.get("memberInfo")));
                        String yidx = job.getString("m_idx");
                        sendDisconnectPush(yidx);
                    }

                    // 통화종료
                } else if(type.equalsIgnoreCase("disconnect")) {
                    msg = jo.getString("msg");

                    timer = new Timer();
                    lastTime = System.currentTimeMillis();
                    checkDisconnect();
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void sendDisconnectPush(String yidx) {
        ReqBasic server = new ReqBasic(ctx, NetUrls.ADDRESS) {
            @Override
            public void onAfter(int resultCode, HttpResult resultData) {
                if (resultData.getResult() != null) {
                    try {
                        JSONObject jo = new JSONObject(resultData.getResult());

                        if(jo.getString("result").equalsIgnoreCase("Y")) {

                        } else {

                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                }
            }
        };

        server.setTag("Disconnect Push");
        server.addParams("siteUrl", NetUrls.SITEURL);
        server.addParams("CONNECTCODE", "APP");
        server.addParams("_APP_MEM_IDX", UserPref.getMidx(ctx));

        server.addParams("dbControl", "getVideoChattingENDSend");
        server.addParams("guest_idx", yidx);
        server.addParams("msg", "상대가 통화중 입니다");
        server.execute(true, false);
    }

    //통화종료 타이머실행
    public void checkDisconnect() {
        adTask = new TimerTask() {
            @Override
            public void run() {
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        if (disconnectState) {
                            checkDisconnectState();
                        }

                        currentTime = System.currentTimeMillis();
                        if ((currentTime - lastTime) > limitTime) {
                            cancelTimer();
                            if (!msg.equals("call")) {
                                ((MainAct) MainAct.act).showPushToast(msg);
                            }
                        }

                        Log.i("TEST_TIME", "currentTime: " + currentTime);
                        Log.i("TEST_TIME", "currentTime - lastTime: " + (currentTime - lastTime));
                    }
                });
            }
        };
        timer.schedule(adTask, 0, 500);
    }

    private void checkDisconnectState() {
        if (msg.equals("call")) {
            VoiceReceiveAct act_call_receive = (VoiceReceiveAct) VoiceReceiveAct.act;
            VideoReceiveAct act_video_receive = (VideoReceiveAct) VideoReceiveAct.act;

            if (act_call_receive != null) {
                disconnectState = false;
                cancelTimer();
                act_call_receive.disconnectFromService();

                Log.i("TEST_TIME", "act_call_receive");
            } else if (act_video_receive != null) {
                disconnectState = false;
                cancelTimer();
                act_video_receive.disconnectFromService();

                Log.i("TEST_TIME", "act_video_receive");
            }
        } else {
            VoiceCallAct act_call = (VoiceCallAct) VoiceCallAct.act;
            VideoCallAct act_video = (VideoCallAct) VideoCallAct.act;

            if (act_call != null) {
                cancelTimer();
                act_call.disconnectFromService();
                if (!msg.equals("call")) {
                    ((MainAct) MainAct.act).showPushToast(msg);
                }

                Log.i("TEST_TIME", "act_call");
            } else if (act_video != null) {
                cancelTimer();
                act_video.disconnectFromService();
                if (!msg.equals("call")) {
                    ((MainAct) MainAct.act).showPushToast(msg);
                }

                Log.i("TEST_TIME", "act_video");
            }
        }
    }

    private void cancelTimer() {
        Log.i("TEST_TIME", "cancelTimer");
        if (adTask != null) {
            adTask.cancel();
            adTask = null;
        }

        if (timer != null) {
            timer.cancel();
            timer.purge();
            timer = null;
        }

        disconnectState = true;
    }

    private boolean checkCallState() {
        VoiceReceiveAct act_call_receive = (VoiceReceiveAct) VoiceReceiveAct.act;
        VideoReceiveAct act_video_receive = (VideoReceiveAct) VideoReceiveAct.act;
        VoiceCallAct act_call = (VoiceCallAct) VoiceCallAct.act;
        VideoCallAct act_video = (VideoCallAct) VideoCallAct.act;


        if (act_call != null || act_video != null || act_call_receive != null || act_video_receive != null) {
            return false;
        } else {
            return true;
        }
    }

    private void setChatNotiAllCount() {
        ReqBasic server = new ReqBasic(ctx, NetUrls.ADDRESS) {
            @Override
            public void onAfter(int resultCode, HttpResult resultData) {
                if (resultData.getResult() != null) {
                    try {
                        JSONObject jo = new JSONObject(resultData.getResult());

                        final String result = jo.getString("result");
                        final String read_count = jo.getString("sum");

                        if (result.equalsIgnoreCase("Y")) {
                            setBadge(Integer.parseInt(read_count));
                        } else {
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                }
            }
        };

        server.setTag("Chat All Count");
        server.addParams("siteUrl", NetUrls.SITEURL);
        server.addParams("CONNECTCODE", "APP");
        server.addParams("_APP_MEM_IDX", UserPref.getMidx(ctx));

        server.addParams("dbControl", "getNotReadAllSum");
        server.addParams("multi_is", "N");
        server.execute(true, false);
    }

    private void setBadge(int value) {
        Log.e(StringUtil.TAG, "setBadge in push, badge_count: " + value);
        Intent intent = new Intent("android.intent.action.BADGE_COUNT_UPDATE");
        intent.putExtra("badge_count_package_name", ctx.getPackageName());
        intent.putExtra("badge_count_class_name", getLauncherClassName(ctx));
        intent.putExtra("badge_count", value);
        sendBroadcast(intent);
    }

    public static String

    getLauncherClassName(Context context) {
        PackageManager pm = context.getPackageManager();
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> resolveInfos = pm.queryIntentActivities(intent, 0);
        for (ResolveInfo resolveInfo : resolveInfos) {
            String pkgName = resolveInfo.activityInfo.applicationInfo.packageName;
            if(pkgName.equalsIgnoreCase(context.getPackageName())) {
                String className = resolveInfo.activityInfo.name; return className;
            }
        }
        return null;
    }

    // 채팅 노티
    public void sendChatNotification(String title, String message, String roomIdx) {
        setChatNotiAllCount();

        String GROUP_KEY_WORK_EMAIL = "com.android.example.WORK_EMAIL";
        int SUMMARY_ID = 0;


        Log.e(StringUtil.TAG_PUSH, "sendChatNotification in");
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);

        //If on Oreo then notification required a notification channel.
        // create channel
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("default", "Default", NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription("위톡 알림설정");
            channel.setImportance(NotificationManager.IMPORTANCE_HIGH);

            notificationManager.createNotificationChannel(channel);
        }

        // content intent
        Intent intent = new Intent(ctx, ChatAct.class);
        intent.putExtra("roomIdx", roomIdx);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(ctx, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);

        // delete intent
        Intent intent_delete = new Intent(this, MyBroadcastReceiver.class);
        PendingIntent pendingIntent_delete = PendingIntent.getBroadcast(this.getApplicationContext(), 0, intent_delete, 0);


        // create notification
        Notification notification_chat = new NotificationCompat.Builder(ctx, "default")
                .setContentTitle(title)
                .setContentText(message)
                .setAutoCancel(true)
                .setSmallIcon(R.drawable.app_icon_lite)
                .setGroup(GROUP_KEY_WORK_EMAIL)
                .setContentIntent(pendingIntent)
                .setDeleteIntent(pendingIntent_delete)
                .build();

        Notification summaryNotification =
                new NotificationCompat.Builder(ctx, "default")
                        .setContentTitle("title")
                        //set content text to support devices running API level < 24
                        .setContentText("content text")
                        .setSmallIcon(R.drawable.app_icon_lite)
                        //build summary info into InboxStyle template
//                        .setStyle(new NotificationCompat.InboxStyle()
//                                .addLine("Alex Faarborg  Check this out")
//                                .addLine("Jeff Chang    Launch Party")
//                                .setBigContentTitle("2 new messages")
//                                .setSummaryText("janedoe@example.com"))
                        //specify which group this notification belongs to
                        .setGroup(GROUP_KEY_WORK_EMAIL)
                        //set this notification as the summary for the group
                        .setGroupSummary(true)
                        .build();

        // notify
        int push_id = Integer.parseInt(roomIdx.replace("R", ""));
        notificationManager.notify(push_id, notification_chat);
//        notificationManager.notify(SUMMARY_ID, summaryNotification);
    }

    // 상단 노티
    public void sendNotification(String title, String message, String url) {
        NotificationManager notificationManager = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);

        //If on Oreo then notification required a notification channel.
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("default", "Default", NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(ctx, "default")
                .setContentTitle(title)
                .setContentText(message)
                .setAutoCancel(true)
                .setSmallIcon(R.drawable.app_icon_lite);


        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(ctx, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        builder.setContentIntent(pendingIntent);

        Notification notification = builder.build();
        notification.flags = notification.flags | notification.FLAG_AUTO_CANCEL;

        notificationManager.notify(1, notification);
    }

    // 팝업 노티
    public void sendPopup(String imageUrl, String targetUrl) {
        /* intent */
        Intent intent = new Intent(ctx, PopUpAdDlg.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
        intent.putExtra("imageUrl", imageUrl);
        intent.putExtra("url", targetUrl);

        PendingIntent pendingIntent = PendingIntent.getActivity(ctx, 1, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        try {
            pendingIntent.send();
        } catch (PendingIntent.CanceledException e) {
            e.printStackTrace();
        }
    }
}