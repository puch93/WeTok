package kr.co.core.wetok.firebase;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
import android.util.Log;

import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONException;
import org.json.JSONObject;

import kr.co.core.wetok.util.PushWakeLock;
import kr.co.core.wetok.util.StringUtil;


public class MyFirebaseMessagingService extends com.google.firebase.messaging.FirebaseMessagingService {
    private static final String TAG = "TEST_PUSH";
    private static final int ALARM_NITI = 1;

    private Context ctx;

    @Override
    public void onNewToken(String token) {
        Log.e(TAG, "Refreshed token: " + token);

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

        Log.e(TAG, "remoteMessage.getData: " + remoteMessage.getData());


        PowerManager pm = (PowerManager) ctx.getSystemService(POWER_SERVICE);
        if(!pm.isScreenOn()) {
            //화면 깨우기
            PushWakeLock.acquireCpuWakeLock(ctx);
            PushWakeLock.releaseCpuLock();
        }


//        try {
//            JSONObject jo = new JSONObject(remoteMessage.getData());
//
//            final String type = jo.getString("type");
//            final String imageUrl = jo.getString("filename");
//            final String url = jo.getString("url");
//
//            if (!StringUtil.isNull(type)) {
//                if (type.equals("front")) {
//                    sendNotification("대운", "클릭하여 광고를 확인하세요.", url);
//                    sendPopUP(imageUrl, url);
//                } else if (type.equals("top")) {
//                    Log.e(TAG, "top");
//                    String msg = jo.getString("msg");
//                    sendNotification("대운", msg, url);
//                }
//            }
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
    }

//    public void sendPopUP(String imageUrl, String targetUrl) {
//
//        /* intent */
//        Intent intent = new Intent(ctx, PopUpAdDlg.class);
//        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
//        intent.putExtra("imageUrl", imageUrl);
//        intent.putExtra("url", targetUrl);
//
//        PendingIntent pendingIntent = PendingIntent.getActivity(ctx, 1, intent,
//                PendingIntent.FLAG_UPDATE_CURRENT);
//
//        try {
//            pendingIntent.send();
//        } catch (PendingIntent.CanceledException e) {
//            e.printStackTrace();
//        }
//    }
//
//    /* 푸시보내기 기본*/
//    public void sendNotification(String title, String message, String url) {
//        NotificationHelper helper = new NotificationHelper(ctx);
//        helper.showDefaultNotification(title, message, url);
//    }
}