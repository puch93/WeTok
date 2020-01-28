package kr.co.core.wetok.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.core.content.FileProvider;
import androidx.databinding.DataBindingUtil;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.media.MediaScannerConnection;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.PersistableBundle;
import android.os.PowerManager;
import android.os.Vibrator;
import android.provider.MediaStore;
import android.service.notification.StatusBarNotification;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.Formatter;
import android.text.style.StyleSpan;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.InputMethodManager;
import android.webkit.MimeTypeMap;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import kr.co.core.wetok.BuildConfig;
import kr.co.core.wetok.R;
import kr.co.core.wetok.activity.rtc.ConnectActivity;
import kr.co.core.wetok.adapter.ChattingAdapter;
import kr.co.core.wetok.adapter.ChattingPartAdapter;
import kr.co.core.wetok.data.ChattingData;
import kr.co.core.wetok.data.UserData;
import kr.co.core.wetok.databinding.ActivityChatBinding;
import kr.co.core.wetok.preference.SystemPref;
import kr.co.core.wetok.preference.UserPref;
import kr.co.core.wetok.server.JSONUrl;
import kr.co.core.wetok.server.ReqBasic;
import kr.co.core.wetok.server.netUtil.HttpResult;
import kr.co.core.wetok.server.netUtil.NetUrls;
import kr.co.core.wetok.util.Common;
import kr.co.core.wetok.util.OnKeyboardVisibilityListener;
import kr.co.core.wetok.util.RecordUtil;
import kr.co.core.wetok.util.SoftKeyboard;
import kr.co.core.wetok.util.StringUtil;
import kr.co.core.wetok.util.UriUtils;
import okhttp3.OkHttpClient;

public class ChatAct extends BaseAct implements View.OnClickListener {
    private static final int TYPE_ME_TEXT = 1;
    private static final int TYPE_ME_IMAGE = 3;
    private static final int TYPE_YOU_TEXT = 2;
    private static final int TYPE_YOU_IMAGE = 4;
    private static final int TYPE_DATE_LINE = 7;

    private static final int MORE_CAMERA_IMAGE = 1001;
    private static final int MORE_ALBUM_IMAGE = 1002;
    private static final int MORE_CAMERA_VIDEO = 1003;
    private static final int MORE_ALBUM_VIDEO = 1004;
    private static final int MORE_FILE = 1005;

    private static final int CROP_IMAGE = 1010;

    ActivityChatBinding binding;
    private Activity act;
    public static Activity real_act;
    private boolean exitState = false;

    private boolean bottomMenuOpenState = false;
    private boolean keyboardOpenState = false;
    private boolean toggleEditTextCheck = false;

    MediaPlayer player;

    private InputMethodManager imm;

    ActionBar actionBar;

    public String roomIdx;

    private ArrayList<ChattingData> list = new ArrayList<>();
    private ArrayList<UserData> list_part = new ArrayList<>();
    private ChattingAdapter adapter;
    private ChattingPartAdapter partAdapter;
    private Socket mSocket;
    private int all_count;

    /* image, video, file, record */
    private Uri photoUri;
    private Uri videoUri;

    private File downpath;
    private File outputFile;
    private static String recordFileName;
    private String mimeType;

    RecordUtil recordUtil;
    Vibrator vibrator;
    Timer timer;
    TimerTask adTask;

    long startTime;
    long currentTime;
    private float prevX;
    private float prevY;

    ProgressDialog progressDialog;
    ProgressDialog dialog;

    boolean isFirst = true;
    boolean isFirstSocket = true;
    boolean isDrawerOpen = false;
    public String menuCallState = "none";


    private int[] RESULT_ARRAY = {RTC_VOICE_CALL, RTC_VIDEO_CALL};
    private static final int RTC_VOICE_CALL = 0;
    private static final int RTC_VIDEO_CALL = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_chat, null);

        /* EditText 포커스될때 키보드가 UI 가리는 것 막음 */
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

        setLayout();
    }

    // SPOT
    private void setLayout() {
        imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        setActionBar();
        setClickListener();

        act = this;
        real_act = this;

        // audio setting
        setAudio();
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        // 룸인덱스 세팅
        roomIdx = getIntent().getStringExtra("roomIdx");
        if (StringUtil.isNull(roomIdx)) {
            Common.showToast(act, "해당 채팅방에 문제가 있습니다.\n관리자에게 문의 바랍니다.");
        } else {
            if (!roomIdx.contains("R"))
                roomIdx = "R" + roomIdx;
            Log.e(StringUtil.TAG, "roomIdx: " + roomIdx);
        }

        // 다이얼로그 세팅
        progressDialog = new ProgressDialog(this);
        dialog = new ProgressDialog(this);

        // 리사이클러뷰 세팅
        setRecyclerView();

        // 소켓연결
        setupSocketClient();

        // 드로어 리스너
        binding.dlChat.addDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(@NonNull View drawerView, float slideOffset) {

            }

            @Override
            public void onDrawerOpened(@NonNull View drawerView) {
                isDrawerOpen = true;
            }

            @Override
            public void onDrawerClosed(@NonNull View drawerView) {
                isDrawerOpen = false;
                menuCallState = "none";
            }

            @Override
            public void onDrawerStateChanged(int newState) {

            }
        });

        // 텍스트 change 리스너
        binding.etText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (
                        !StringUtil.isNull(binding.etText.getText().toString())
                ) {
                    if (!toggleEditTextCheck) {
                        toggleEditTextCheck = true;

                        binding.flMore01.animate()
                                .alpha(0f)
                                .setDuration(200)
                                .setListener(new AnimatorListenerAdapter() {
                                    @Override
                                    public void onAnimationEnd(Animator animation) {
                                        super.onAnimationEnd(animation);
                                        binding.flMore01.setVisibility(View.GONE);
                                    }
                                });

                        binding.flSend.setAlpha(0f);
                        binding.flSend.animate()
                                .alpha(1f)
                                .setDuration(200)
                                .setListener(new AnimatorListenerAdapter() {
                                    @Override
                                    public void onAnimationEnd(Animator animation) {
                                        super.onAnimationEnd(animation);
                                        binding.flSend.setVisibility(View.VISIBLE);
                                    }
                                });
                    }
                } else {
                    if (toggleEditTextCheck) {
                        toggleEditTextCheck = false;

                        binding.flSend.animate()
                                .alpha(0f)
                                .setDuration(200)
                                .setListener(new AnimatorListenerAdapter() {
                                    @Override
                                    public void onAnimationEnd(Animator animation) {
                                        super.onAnimationEnd(animation);
                                        Log.e(StringUtil.TAG, "onAnimationEnd: ");
                                        binding.flSend.setVisibility(View.GONE);
                                    }
                                });

                        binding.flMore01.setAlpha(0f);
                        binding.flMore01.animate()
                                .alpha(1f)
                                .setDuration(200)
                                .setListener(new AnimatorListenerAdapter() {
                                    @Override
                                    public void onAnimationEnd(Animator animation) {
                                        super.onAnimationEnd(animation);
                                        binding.flMore01.setVisibility(View.VISIBLE);
                                    }
                                });
                    }
                }
            }
        });

        setBottomMenu();

        setKeyboardVisibilityListener(null);
    }

    /* 녹음 관련 코드 */

    // 녹음 타이머
    public void setTimer() {
        startTime = System.currentTimeMillis();
        binding.llRecordArea.setVisibility(View.VISIBLE);

        timer = new Timer();
        adTask = new TimerTask() {
            @Override
            public void run() {
                currentTime = System.currentTimeMillis();

                long resultTime = currentTime - startTime;
                if ((resultTime) > 60000) {
                    cancelTimer();
                    Common.showToast(act, "최대 녹음시간은 1분입니다");
                    recordUtil.stopRecording();
                } else {
                    String resultTimeText = Common.converTimeSimpleLong(resultTime);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            binding.tvRecordTime.setText(resultTimeText);
                        }
                    });
                }
            }
        };
        timer.schedule(adTask, 0, 100);
    }

    public void cancelTimer() {
        if (adTask != null) {
            adTask.cancel();
            adTask = null;
        }

        if (timer != null) {
            timer.cancel();
            timer.purge();
            timer = null;
        }
    }

    private void setAudio() {
        recordUtil = new RecordUtil(act, new RecordUtil.RecordStateListener() {
            @Override
            public void afterStartRecord() {
                vibrator.vibrate(50);

                setTimer();
            }

            @Override
            public void afterStopRecord(String recordFile) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        binding.llRecordArea.setVisibility(View.GONE);

                        if (StringUtil.isNull(recordFile)) {
                            Common.showToast(act, "파일이 손상되었습니다");
                        } else {
                            doChatFileUpload(recordFile, "mic", "");
                        }
                    }
                });
            }

            @Override
            public void afterCancelRecord(boolean isNormal) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        binding.llRecordArea.setVisibility(View.GONE);

                        if (isNormal) {
                            binding.llRecordCancelArea.setVisibility(View.GONE);
                        } else {
                            binding.llRecordNoticeArea.setVisibility(View.VISIBLE);
                            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    binding.llRecordNoticeArea.setVisibility(View.GONE);
                                }
                            }, 400);
                        }
                    }
                });
            }
        }, null);

        binding.tvRecordBtn.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                binding.tvRecordBtn.setBackgroundResource(R.drawable.shape_rounded_record_on);
                            }
                        });

                        prevX = event.getRawX();
                        prevY = event.getRawY();

                        Log.e(StringUtil.TAG, "prevX: " + prevX + ", prevY: " + prevY);

                        recordUtil.startRecording();
                        return true;
                    case MotionEvent.ACTION_MOVE:
                        float rawY = event.getRawY();

                        if (rawY < prevY - 400) {
                            binding.llRecordArea.setVisibility(View.GONE);
                            binding.llRecordCancelArea.setVisibility(View.VISIBLE);
                        } else {
                            binding.llRecordArea.setVisibility(View.VISIBLE);
                            binding.llRecordCancelArea.setVisibility(View.GONE);
                        }
                        return true;
                    case MotionEvent.ACTION_UP:
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                binding.tvRecordBtn.setBackgroundResource(R.drawable.shape_rounded_record_off);
                            }
                        });

//                        float rawX = event.getRawX();
//                        float rawY = event.getRawY();
//                        Log.e(StringUtil.TAG, "rawX: " + rawX + ", rawY: " + rawY);

                        if (binding.llRecordCancelArea.getVisibility() == View.VISIBLE) {
                            cancelTimer();
                            recordUtil.cancelRecording(true);
                        } else {
                            if ((currentTime - startTime) < 1000) {
                                cancelTimer();
                                recordUtil.cancelRecording(false);
                            } else {
                                cancelTimer();
                                recordUtil.stopRecording();
                            }
                        }
                        return true;
                }
                return false;
            }
        });
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (!isFirst) {
            Log.e(StringUtil.TAG_SOCK, "onResume in");
            real_act = this;
            setupSocketClient();
        }

        isFirst = false;
    }

    @Override
    protected void onPause() {
        super.onPause();
        outRoom();
        real_act = null;
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (!exitState) {
            outRoom();
            real_act = null;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (!exitState) {
            outRoom();
            real_act = null;
        }
    }

    private void outRoom() {
        Log.i(StringUtil.TAG_SOCK, "roomExit, roomidx: " + roomIdx);

        JSONObject roomExit = new JSONObject();
        try {
            roomExit.put("user_idx", UserPref.getMidx(act));
            roomExit.put("room_idx", roomIdx);
            roomExit.put("site_idx", "2");
            mSocket.emit(JSONUrl.ROOMQUIT, roomExit);

            mSocket.disconnect();

            exitState = true;
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    /* 소켓 세팅 */
    private void setupSocketClient() {
        Log.i(StringUtil.TAG_SOCK, "setupSocketClient");

        try {
            TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
                public X509Certificate[] getAcceptedIssuers() {
                    X509Certificate[] myTrustedAnchors = new X509Certificate[0];
                    return myTrustedAnchors;
                }

                @Override
                public void checkClientTrusted(X509Certificate[] certs, String authType) {
                }

                @Override
                public void checkServerTrusted(X509Certificate[] certs, String authType) {
                }
            }};

            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new SecureRandom());

            OkHttpClient okHttpClient = new OkHttpClient.Builder()
                    .hostnameVerifier(new HostnameVerifier() {
                        @Override
                        public boolean verify(String hostname, SSLSession session) {
                            return true;
                        }
                    })
                    .sslSocketFactory(sc.getSocketFactory()).build();

            // default settings for all sockets
            IO.setDefaultOkHttpWebSocketFactory(okHttpClient);
            IO.setDefaultOkHttpCallFactory(okHttpClient);

            // set as an option
            IO.Options opts = new IO.Options();
            opts.callFactory = okHttpClient;
            opts.webSocketFactory = okHttpClient;

            mSocket = IO.socket(JSONUrl.CHATTING_ADDRESS);
            mSocket.on(Socket.EVENT_CONNECT, onConnect);
            mSocket.on(JSONUrl.CHATLIST, getChats);
            mSocket.on(JSONUrl.CHECK, getChats);
            mSocket.on(JSONUrl.ROOMREAD, getReads);
            mSocket.on(JSONUrl.CHATSEND, sendChat);
            mSocket.on(JSONUrl.DELETE, getChats);
            mSocket.on(JSONUrl.ROOMQUIT, roomExit);
            mSocket.on(JSONUrl.CHATRECIVE, chatRecive);
            mSocket.connect();
            System.out.println("socket setup!!! ");

        } catch (URISyntaxException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        } finally {
        }
    }

    // 소켓연결 함수
    private Emitter.Listener onConnect = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            System.out.println("socket onConnect !");
            Log.i(StringUtil.TAG_SOCK, "onConnect in");

            JSONObject roomData = new JSONObject();
            try {

                roomData.put("user_idx", UserPref.getMidx(act));
                roomData.put("room_idx", roomIdx);
                roomData.put("site_idx", JSONUrl.SITEIDX);

                mSocket.emit(JSONUrl.ROOMREAD, roomData);
                mSocket.emit(JSONUrl.CHECK, roomData);
            } catch (JSONException e) {
                e.printStackTrace();
            }


        }
    };

    // 현재 안쓰는 함수
    private Emitter.Listener getRoomIs = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Log.i(StringUtil.TAG_SOCK, "getRoomIs");

            JSONObject roomData = (JSONObject) args[0];

            //TODO 쪽지읽음처리
//            messageReadProcess();

            JSONObject readList = new JSONObject();
            try {
                readList.put("user_idx", UserPref.getMidx(act));
                readList.put("room_idx", roomIdx);
                readList.put("site_idx", JSONUrl.SITEIDX);
                mSocket.emit(JSONUrl.ROOMREAD, readList);

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

    // 채팅 리스트 가져오는 함수
    private Emitter.Listener getChats = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Log.i(StringUtil.TAG_SOCK, "getChats in");

            // 푸시 제거
            int push_id = Integer.parseInt(roomIdx.replace("R", ""));
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.cancel(push_id);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                StatusBarNotification[] showed_noti = notificationManager.getActiveNotifications();
                Log.e(StringUtil.TAG, "showed_noti: " + showed_noti.length);
            }

            JSONObject chatData = (JSONObject) args[0];
            Log.i(StringUtil.TAG_CHAT, "getChats chatData: " + chatData);

            try {
                // 채팅별 전체 카운트 가져오기
                JSONObject test = chatData.getJSONObject("roomUser");
                all_count = test.getInt("count");


                JSONArray chats = chatData.getJSONArray("chats");

                list = new ArrayList<ChattingData>();

                for (int i = 0; i < chats.length(); i++) {
                    JSONObject job = chats.getJSONObject(i);

                    Log.e(StringUtil.TAG_CHAT, "getChats chats(" + i + "): " + job);

                    String c_idx = job.getString("c_idx");
                    String c_user_idx = job.getString("c_user_idx");
                    String c_regdate = job.getString("c_regdate");
                    String c_read_cnt = job.getString("c_read_cnt");
                    String c_msg_type = job.getString("c_msg_type");
                    String c_limit_time = convertLimitTime(job.getString("c_expirationdate"));
                    String c_system_msg_is = job.getString("c_system_msg_is");
                    String c_flag_delete = StringUtil.getStr(job, "c_flag_delete");

                    String m_name = job.getString("m_nickname");
                    String m_photo = job.getString("m_profile");

                    int sub = job.getInt("in_cnt");
                    c_read_cnt = String.valueOf(all_count - sub);

                    Log.e(StringUtil.TAG_CHAT, "getChats c_read_member: " + job.getString("c_read_member"));
                    Log.e(StringUtil.TAG_CHAT, "getChats all_count: " + all_count + ", in_cnt: " + sub);
                    Log.e(StringUtil.TAG_CHAT, "getChats c_read_cnt: " + c_read_cnt);

                    if (c_system_msg_is.equalsIgnoreCase("Y")) {
                        c_msg_type = "system";
                    }

                    // file size 구하기
                    String file_size = null;
                    if (c_msg_type.equalsIgnoreCase("file")) {
                        try {

                            URL url = new URL(NetUrls.DOMAIN + job.getString("c_msg"));
                            URLConnection urlConnection = url.openConnection();
                            urlConnection.connect();
                            long size = (long) urlConnection.getContentLength();
                            file_size = Formatter.formatFileSize(act, size);
                            Log.e(StringUtil.TAG_CHAT, "file_size: " + file_size);

                        } catch (MalformedURLException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    // 메시지
                    String c_msg;
                    if (c_msg_type.equalsIgnoreCase("text")) {
                        c_msg = job.getString("c_msg");
                    } else {
                        c_msg = job.getString("c_msg");
                    }

                    // 음성메시지는 파일위치 / 시간으로 나눠 저장
                    if (c_msg_type.equalsIgnoreCase("mic")) {
                        String[] divide = c_msg.split("@@@");
                        c_msg = divide[0];
                        file_size = divide[1];
                    }


                    // 데이트라인 데이터 추가
                    String dateLine = convertTime(c_regdate, true);
                    String sendTime = convertTime(c_regdate, false);

                    // 데이트라인 확인 후 추가
                    if (i > 0) {
                        if (!list.get(list.size() - 1).getDateLine().equals(dateLine)) {
                            ChattingData data = new ChattingData(dateLine, "dateLine");
                            list.add(data);
                        }
                    } else {
                        ChattingData data = new ChattingData(dateLine, "dateLine");
                        list.add(data);
                    }


                    // 데이터 추가
                    final ChattingData data = new ChattingData(c_idx, c_msg, c_user_idx,
                            sendTime, dateLine,
                            c_read_cnt, c_msg_type, c_flag_delete, c_limit_time, m_name, m_photo, c_system_msg_is, file_size);

                    list.add(data);
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        adapter.setList(list);
                        if (isFirstSocket) {
                            binding.rcvChatting.scrollToPosition(adapter.getItemCount() - 1);
                            isFirstSocket = false;
                        }
                    }
                });
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

    // 채팅 실시간 받는 함수
    private Emitter.Listener chatRecive = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Log.i(StringUtil.TAG_SOCK, "chatRecive in");

            JSONObject roomData = (JSONObject) args[0];
            Log.e(StringUtil.TAG_CHAT, "roomData: " + roomData);
//            for (int k = 0; k < args.length; k++) {
//                Log.i(StringUtil.TAG_CHAT, "args[" + k + "]: " + args[k]);
//            }

            try {
                JSONArray chats = roomData.getJSONArray("chat");
                Log.i(StringUtil.TAG_CHAT, "chatRecive chat: " + chats);

                JSONObject job = chats.getJSONObject(0);
                String c_idx = job.getString("c_idx");
                String c_user_idx = job.getString("c_user_idx");
                String c_regdate = job.getString("c_regdate");
                String c_read_cnt = job.getString("c_read_cnt");
                String c_msg_type = job.getString("c_msg_type");
                String c_limit_time = convertLimitTime(job.getString("c_expirationdate"));
                String c_system_msg_is = job.getString("c_system_msg_is");
                String c_flag_delete = StringUtil.getStr(job, "c_flag_delete");

                String m_name = job.getString("m_nickname");
                String m_photo = job.getString("m_profile");

                if (job.has("in_cnt")) {
                    int sub = job.getInt("in_cnt");
                    c_read_cnt = String.valueOf(all_count - sub);
                    Log.e(StringUtil.TAG_CHAT, "chatRecive all_count: " + all_count + ", in_cnt: " + sub);
                    Log.e(StringUtil.TAG_CHAT, "chatRecive c_read_cnt: " + c_read_cnt);
                }

                if (c_system_msg_is.equalsIgnoreCase("Y")) {
                    c_msg_type = "system";
                }

                // file size 구하기
                String file_size = null;
                if (c_msg_type.equalsIgnoreCase("file")) {
                    try {

                        URL url = new URL(NetUrls.DOMAIN + job.getString("c_msg"));
                        URLConnection urlConnection = url.openConnection();
                        urlConnection.connect();
                        long size = (long) urlConnection.getContentLength();
                        file_size = Formatter.formatFileSize(act, size);
                        Log.e(StringUtil.TAG_CHAT, "file_size: " + file_size);

                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                // 메시지
                String c_msg;
                if (c_msg_type.equalsIgnoreCase("text")) {
                    c_msg = job.getString("c_msg");
                } else {
                    c_msg = job.getString("c_msg");
                }

                // 음성메시지는 파일위치 / 시간으로 나눠 저장
                if (c_msg_type.equalsIgnoreCase("mic")) {
                    String[] divide = c_msg.split("@@@");
                    c_msg = divide[0];
                    file_size = divide[1];
                }

                // 데이트라인 데이터 추가
                String dateLine = convertTime(c_regdate, true);
                String sendTime = convertTime(c_regdate, false);

                // 데이터 추가
                final ChattingData data = new ChattingData(c_idx, c_msg, c_user_idx,
                        sendTime, dateLine,
                        c_read_cnt, c_msg_type, c_flag_delete, c_limit_time, m_name, m_photo, c_system_msg_is, file_size);


                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (!c_user_idx.equalsIgnoreCase(UserPref.getMidx(act))) {
                            adapter.addItem(data);
                        } else {
                            if (data.getType().equalsIgnoreCase("text")) {
                                adapter.setItem(data);
                            } else {
                                adapter.addItem(data);
                            }
                        }

                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                binding.rcvChatting.scrollToPosition(adapter.getItemCount() - 1);
                            }
                        }, 300);
                    }
                });
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };


    // 상대정보 받아옴
    private Emitter.Listener getReads = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            JSONObject readData = (JSONObject) args[0];
            Log.i(StringUtil.TAG_SOCK, "getReads in");

            try {
                JSONObject jo_readData = readData.getJSONObject("roomUser");
                JSONArray ja = jo_readData.getJSONArray("users");

                list_part = new ArrayList<>();
                String names = "";

                for (int i = 0; i < ja.length(); i++) {
                    JSONObject jo = ja.getJSONObject(i);
                    if (!UserPref.getMidx(act).equals(jo.getString("m_idx"))) {
                        Log.e(StringUtil.TAG_CHAT, "getReads users(" + i + "): " + jo);

                        UserData user = new UserData();
                        user.setIdx(jo.getString("m_idx"));
                        user.setId(jo.getString("m_id"));
                        user.setPw(jo.getString("m_pass"));
                        user.setHp(jo.getString("m_hp"));
                        user.setIntro(jo.getString("m_intro"));
                        user.setName(jo.getString("m_nickname"));
                        user.setBirth(jo.getString("m_birthday"));
                        user.setProfile_img(jo.getString("m_profile"));
                        user.setBackground_img(jo.getString("m_background"));

                        list_part.add(user);

                        if (i != ja.length() - 1 &&
                                !ja.getJSONObject(ja.length() - 1).getString("m_idx").equalsIgnoreCase(UserPref.getMidx(act))) {
                            names += jo.getString("m_nickname") + ",";
                        } else {
                            names += jo.getString("m_nickname");
                        }
                    }
                }

                String finalNames = names;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        binding.tvTitle.setText(finalNames);

                        partAdapter = new ChattingPartAdapter(act, list_part);
                        binding.dlDrawer.rcvPart.setLayoutManager(new LinearLayoutManager(act));
                        partAdapter.setHasStableIds(false);
                        binding.dlDrawer.rcvPart.setHasFixedSize(true);
                        binding.dlDrawer.rcvPart.setAdapter(partAdapter);
                    }
                });

            } catch (JSONException e) {
                e.printStackTrace();
            }

            /* 읽음처리 함수 */
//            JSONObject roomData = new JSONObject();
//            try {
//                roomData.put("user_idx", UserPref.getMidx(act));
//                roomData.put("room_idx", roomIdx);
//                roomData.put("site_idx", JSONUrl.SITEIDX);
//
//                mSocket.emit(JSONUrl.CHECK, roomData);
//            } catch (JSONException e) {
//                e.printStackTrace();
//            }


            JSONObject chatList = new JSONObject();
            try {
                chatList.put("user_idx", UserPref.getMidx(act));
                chatList.put("room_idx", roomIdx);
                chatList.put("site_idx", "2");
                mSocket.emit(JSONUrl.CHATLIST, chatList);

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

    // 채팅 보내는 함수
    private Emitter.Listener sendChat = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Log.i(StringUtil.TAG_SOCK, "sendChat in");

            JSONObject readData = (JSONObject) args[0];
            System.out.println("Socket Send chat : " + readData);
        }
    };

    // 쓰는지 안쓰는지 몰겠음
    private Emitter.Listener roomExit = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Log.i(StringUtil.TAG_SOCK, "roomExit in");
            System.out.println("Socket exit  ");
        }
    };


    // 채팅별 만료시간 -> 문자열로 변환 (파일, 녹음)
    public String convertLimitTime(String original) {
        //아이템별 시간
        String time1 = original;
        SimpleDateFormat dateFormat1 = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", java.util.Locale.getDefault());
        Date date1 = null;
        try {
            date1 = dateFormat1.parse(time1);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        SimpleDateFormat dateFormat2 = new SimpleDateFormat("yyyy.MM.dd", java.util.Locale.getDefault());
        String time2 = dateFormat2.format(date1);
        return "~" + time2 + "까지";
    }

    // 데이트라인 or 채팅별 시간 -> 문자열로 변환
    private String convertTime(String origin, boolean isDateLine) {
        try {
            SimpleDateFormat dateFormat1 = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", java.util.Locale.getDefault());
            Date date = dateFormat1.parse(origin);

            SimpleDateFormat dateFormat;
            if (isDateLine) {
                //날짜별 구분
                dateFormat = new SimpleDateFormat(getString(R.string.chat_msg_date), java.util.Locale.getDefault());
                return dateFormat.format(date);
            } else {
                dateFormat = new SimpleDateFormat("a hh:mm", java.util.Locale.getDefault());
                return dateFormat.format(date);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }


    // 메시지별 삭제
    public void showDialogDelete(final String idx) {
        new AlertDialog.Builder(act)
                .setMessage("메시지를 삭제하시겠습니까?")
                .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        delete(idx);
                    }
                })
                .setNegativeButton("취소", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                }).show();
    }

    private void delete(String idx) {
        JSONObject chatSend = new JSONObject();
        try {
            chatSend.put("idx", idx);
            chatSend.put("room_idx", roomIdx);
            mSocket.emit(JSONUrl.DELETE, chatSend);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    // 파일 다운로드
    public void downloadFile(String Dpath) {
        progressDialog = new ProgressDialog(act);
        progressDialog.setMessage("다운로드중");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setIndeterminate(true);
        progressDialog.setCancelable(true);

        //final String fileURL = "http://webnautes.tistory.com/attachment/cfile4.uf@267BB53E58451C582BD045.avi";
        final String fileURL = Dpath;

        String fileName = fileURL.substring(fileURL.lastIndexOf('/') + 1, fileURL.length());
        downpath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        String extension = MimeTypeMap.getFileExtensionFromUrl(Dpath);
        final String mimeType = mimeTypeMap.getMimeTypeFromExtension(extension);
        //Log.d(HoUtils.TAG," 파일 형식 : "+extension+" 파일 : "+mimeType);
        outputFile = new File(downpath, fileName); //파일명까지 포함함 경로의 File 객체 생성
        if (outputFile.exists()) { //이미 다운로드 되어 있는 경우

            AlertDialog.Builder builder = new AlertDialog.Builder(act);
            builder.setTitle("파일 다운로드");
            builder.setMessage("이미 SD 카드에 존재합니다. 다시 다운로드 받을까요?");
            builder.setNegativeButton("아니오",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog,
                                            int which) {
                            Toast.makeText(getApplicationContext(), "기존 파일을 플레이합니다.", Toast.LENGTH_LONG).show();

                            //File file = new File(UriUtils.getPath(act,outputFile));
                            try {
                                Intent i = new Intent(Intent.ACTION_VIEW);
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                    i.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                    Uri contentUri = FileProvider.getUriForFile(act, "kr.co.core.wetok.provider", outputFile);
                                    i.setDataAndType(contentUri, mimeType);
                                    i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                    i.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                } else {
                                    i.setDataAndType(Uri.fromFile(outputFile), mimeType);
                                    i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                }
                                startActivity(i);
                            } catch (Exception e) {
                                e.printStackTrace();
                                Toast.makeText(act, "파일을 열 수 없습니다. 뷰어을 설치해 주세요.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
            builder.setPositiveButton("예",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            outputFile.delete(); //파일 삭제

                            final DownloadFilesTask downloadTask = new DownloadFilesTask(act);
                            downloadTask.execute(fileURL);

                            progressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                                @Override
                                public void onCancel(DialogInterface dialog) {
                                    downloadTask.cancel(true);
                                }
                            });
                        }
                    });
            builder.show();

        } else { //새로 다운로드 받는 경우
            final DownloadFilesTask downloadTask = new DownloadFilesTask(act);
            downloadTask.execute(fileURL);

            progressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    downloadTask.cancel(true);
                }
            });
        }
    }

    private void setKeyboardVisibilityListener(final OnKeyboardVisibilityListener onKeyboardVisibilityListener) {
        final View parentView = ((ViewGroup) findViewById(android.R.id.content)).getChildAt(0);
        parentView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {

            private boolean alreadyOpen;
            private final int defaultKeyboardHeightDP = 100;
            private final int EstimatedKeyboardDP = defaultKeyboardHeightDP + (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP ? 48 : 0);
            private final Rect rect = new Rect();

            @Override
            public void onGlobalLayout() {
                int estimatedKeyboardHeight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, EstimatedKeyboardDP, parentView.getResources().getDisplayMetrics());
                parentView.getWindowVisibleDisplayFrame(rect);
                int heightDiff = parentView.getRootView().getHeight() - (rect.bottom - rect.top);
                boolean isShown = heightDiff >= estimatedKeyboardHeight;

                if (isShown == alreadyOpen) {
                    Log.i("Keyboard state", "Ignoring global layout change...");
                    return;
                }
                alreadyOpen = isShown;

                keyboardOpenState = isShown;
                if (keyboardOpenState) {
                    closeBottomMenu();
                } else {

                }
            }
        });
    }

//    @Override
//    public void onVisibilityChanged(boolean visible) {
//        if(visible) {
//
//        } else {
//            closeBottomMenu();
//        }
//    }

    private class DownloadFilesTask extends AsyncTask<String, String, Long> {

        private Context context;
        private PowerManager.WakeLock mWakeLock;

        public DownloadFilesTask(Context context) {
            this.context = context;
        }


        //파일 다운로드를 시작하기 전에 프로그레스바를 화면에 보여줍니다.
        @Override
        protected void onPreExecute() { //2
            super.onPreExecute();

            //사용자가 다운로드 중 파워 버튼을 누르더라도 CPU가 잠들지 않도록 해서
            //다시 파워버튼 누르면 그동안 다운로드가 진행되고 있게 됩니다.
            PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, getClass().getName());
            mWakeLock.acquire();

            progressDialog.show();
        }

        //파일 다운로드를 진행합니다.
        @Override
        protected Long doInBackground(String... string_url) { //3
            int count;
            long FileSize = -1;
            InputStream input = null;
            OutputStream output = null;
            URLConnection connection = null;
            try {
                URL url = new URL(string_url[0]);
                connection = url.openConnection();
                connection.connect();
                //파일 크기를 가져옴
                FileSize = connection.getContentLength();
                //URL 주소로부터 파일다운로드하기 위한 input stream
                input = new BufferedInputStream(url.openStream(), 8192);
                downpath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
                String extension = MimeTypeMap.getFileExtensionFromUrl(string_url[0]);
                mimeType = mimeTypeMap.getMimeTypeFromExtension(extension);
                //Log.d(HoUtils.TAG," 파일 형식 : "+extension+" 파일 : "+mimeType);

                String fileName = string_url[0].substring(string_url[0].lastIndexOf('/') + 1, string_url[0].length());

                //outputFile= new File(downpath, fileName+"."+extension); //파일명까지 포함함 경로의 File 객체 생성
                outputFile = new File(downpath, fileName); //파일명까지 포함함 경로의 File 객체 생성

                /*downpath= Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                outputFile= new File(downpath, "Alight.avi"); //파일명까지 포함함 경로의 File 객체 생성*/

                // SD카드에 저장하기 위한 Output stream
                output = new FileOutputStream(outputFile);


                byte data[] = new byte[1024];
                long downloadedSize = 0;
                while ((count = input.read(data)) != -1) {
                    //사용자가 BACK 버튼 누르면 취소가능
                    if (isCancelled()) {
                        input.close();
                        return Long.valueOf(-1);
                    }

                    downloadedSize += count;

                    if (FileSize > 0) {
                        float per = ((float) downloadedSize / FileSize) * 100;
                        String str = "Downloaded " + downloadedSize + "KB / " + FileSize + "KB (" + (int) per + "%)";
                        publishProgress("" + (int) ((downloadedSize * 100) / FileSize), str);

                    }

                    //파일에 데이터를 기록합니다.
                    output.write(data, 0, count);
                }
                // Flush output
                output.flush();

                // Close streams
                output.close();
                input.close();


            } catch (Exception e) {
                Log.e("Error: ", e.getMessage());
            } finally {
                try {
                    if (output != null)
                        output.close();
                    if (input != null)
                        input.close();
                } catch (IOException ignored) {
                }

                mWakeLock.release();

            }
            return FileSize;
        }


        //다운로드 중 프로그레스바 업데이트
        @Override
        protected void onProgressUpdate(String... progress) { //4
            super.onProgressUpdate(progress);

            // if we get here, length is known, now set indeterminate to false
            progressDialog.setIndeterminate(false);
            progressDialog.setMax(100);
            progressDialog.setProgress(Integer.parseInt(progress[0]));
            progressDialog.setMessage(progress[1]);
        }

        //파일 다운로드 완료 후
        @Override
        protected void onPostExecute(Long size) { //5
            super.onPostExecute(size);

            progressDialog.dismiss();

            if (size > 0) {
                //Toast.makeText(getApplicationContext(), "다운로드 완료되었습니다. 파일 크기=" + size.toString(), Toast.LENGTH_LONG).show();
                Toast.makeText(getApplicationContext(), "다운로드 완료되었습니다.", Toast.LENGTH_LONG).show();

                Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                mediaScanIntent.setData(Uri.fromFile(outputFile));
                sendBroadcast(mediaScanIntent);

                try {
                    Intent i = new Intent(Intent.ACTION_VIEW);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        i.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        Uri contentUri = FileProvider.getUriForFile(act, "kr.co.core.wetok.provider", outputFile);
                        i.setDataAndType(contentUri, mimeType);
                        i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        i.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    } else {
                        i.setDataAndType(Uri.fromFile(outputFile), mimeType);
                        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    }
                    startActivity(i);
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(act, "파일을 열 수 없습니다. 뷰어을 설치해 주세요.", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(getApplicationContext(), "첨부된 파일이 없습니다", Toast.LENGTH_LONG).show();
            }
        }
    }


    // 채팅전송
    private void doChatSend(String msg, String type) {
        JSONObject chatSend = new JSONObject();

        try {
            chatSend.put("user_idx", UserPref.getMidx(act));
            chatSend.put("guest_idx", "3");
            chatSend.put("room_idx", roomIdx);
            chatSend.put("msg_type", type);
            chatSend.put("c_msg", msg);
            chatSend.put("site_idx", "2");
            chatSend.put("multi_is", "N");

            Log.i(StringUtil.TAG_CHAT, "chatSend: " + chatSend);
            mSocket.emit(JSONUrl.CHATSEND, chatSend);

            //php 푸시
            sendPush(msg, type);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    // 채팅 푸시 날리기
    private void sendPush(String msg, String type) {
        ReqBasic server = new ReqBasic(act, NetUrls.ADDRESS) {
            @Override
            public void onAfter(int resultCode, HttpResult resultData) {
                if (resultData.getResult() != null) {
                    try {
                        JSONObject jo = new JSONObject(resultData.getResult());
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Common.showToastNetwork(act);
                    }
                } else {
                    Common.showToastNetwork(act);

                }
            }
        };

        server.setTag("Send Push");
        server.addParams("siteUrl", NetUrls.SITEURL);
        server.addParams("CONNECTCODE", "APP");
        server.addParams("_APP_MEM_IDX", UserPref.getMidx(act));

        server.addParams("dbControl", "setChatPush");
        server.addParams("room_idx", roomIdx);
        server.addParams("msg_type", type);
        server.addParams("msg", msg);

        server.execute(true, false);
    }


    public void getFile() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        startActivityForResult(intent, MORE_FILE);
    }

    private void setRecyclerView() {
        adapter = new ChattingAdapter(act);

        binding.rcvChatting.setLayoutManager(new LinearLayoutManager(act));
        binding.rcvChatting.setAdapter(adapter);

        binding.rcvChatting.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                if (bottom < oldBottom) {
                    binding.rcvChatting.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            binding.rcvChatting.scrollToPosition(adapter.getItemCount() - 1);
                        }
                    }, 100);
                }
            }
        });
    }

    private void setClickListener() {
        binding.llTouch.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                closeBottomMenu();

                hideKeyboard();
                return false;
            }
        });

        binding.flRightBtn.setOnClickListener(this);
        binding.rcvChatting.setOnClickListener(this);
        binding.flRecord.setOnClickListener(this);

        binding.flMore02.setOnClickListener(this);
        binding.llAlbum.setOnClickListener(this);
        binding.llCamera.setOnClickListener(this);
        binding.llCallVideo.setOnClickListener(this);
        binding.llCallVoice.setOnClickListener(this);
        binding.llFile.setOnClickListener(this);

        binding.flBack.setOnClickListener(this);

        binding.dlDrawer.flLeave.setOnClickListener(this);
    }

    private void setActionBar() {
        setSupportActionBar(binding.toolbar);
        actionBar = getSupportActionBar();
        actionBar.setTitle(null);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeAsUpIndicator(R.drawable.wt_icon_back_wh_191022);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.appbar_actionbar_chat_room, menu);
        return super.onCreateOptionsMenu(menu);
    }

    private void showDialog() {
        LayoutInflater dialog = LayoutInflater.from(act);
        View dlgLayout = dialog.inflate(R.layout.dialog_chat, null);
        final Dialog dlgImgload = new Dialog(act);
        dlgImgload.setContentView(dlgLayout);

        dlgImgload.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dlgImgload.show();

        TextView tv_camera_image = (TextView) dlgLayout.findViewById(R.id.tv_camera_image);
        TextView tv_album_image = (TextView) dlgLayout.findViewById(R.id.tv_album_image);
        TextView tv_camera_video = (TextView) dlgLayout.findViewById(R.id.tv_camera_video);
        TextView tv_album_video = (TextView) dlgLayout.findViewById(R.id.tv_album_video);
        TextView tv_file = (TextView) dlgLayout.findViewById(R.id.tv_file);
        TextView tv_record = (TextView) dlgLayout.findViewById(R.id.tv_record);

        // 사진 촬영
        tv_camera_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                takeCameraImage();

                if (dlgImgload.isShowing()) {
                    dlgImgload.dismiss();
                }
            }
        });

        // 사진 앨범 추가
        tv_album_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (dlgImgload.isShowing()) {
                    dlgImgload.dismiss();
                }
            }
        });

        // 동영상 촬영
        tv_camera_video.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                takeCameraVideo();

                if (dlgImgload.isShowing()) {
                    dlgImgload.dismiss();
                }
            }
        });

        // 동영상 앨범 추가
        tv_album_video.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getAlbumVideo();

                if (dlgImgload.isShowing()) {
                    dlgImgload.dismiss();
                }
            }
        });

        // 첨부 파일
        tv_file.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getFile();

                if (dlgImgload.isShowing()) {
                    dlgImgload.dismiss();
                }
            }
        });

        // 음성 메시지
        tv_record.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.llBottomDefaultArea.setVisibility(View.GONE);
                binding.llBottomRecordArea.setVisibility(View.VISIBLE);

                if (dlgImgload.isShowing()) {
                    dlgImgload.dismiss();
                }
            }
        });
    }

    private void showDialogSub(boolean isCamera) {
        LayoutInflater dialog = LayoutInflater.from(act);
        View dlgLayout = dialog.inflate(R.layout.dialog_chat_sub, null);
        final Dialog dlgImgload = new Dialog(act);
        dlgImgload.setContentView(dlgLayout);

        dlgImgload.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dlgImgload.show();

        TextView tv_title = (TextView) dlgLayout.findViewById(R.id.tv_title);
        TextView tv_image = (TextView) dlgLayout.findViewById(R.id.tv_image);
        TextView tv_video = (TextView) dlgLayout.findViewById(R.id.tv_video);

        if (isCamera) {
            tv_title.setText("촬영하기");
        } else {
            tv_title.setText("앨범에서 가져오기");
        }

        // 사진 앨범 추가
        tv_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isCamera) {
                    takeCameraImage();
                } else {
                    getAlbumImage();
                }

                if (dlgImgload.isShowing()) {
                    dlgImgload.dismiss();
                }
            }
        });

        // 동영상 앨범 추가
        tv_video.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isCamera) {
                    takeCameraVideo();
                } else {
                    getAlbumVideo();
                }

                if (dlgImgload.isShowing()) {
                    dlgImgload.dismiss();
                }
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case android.R.id.home:
                finish();
                return true;

            case R.id.action_participant:
                binding.dlChat.openDrawer(Gravity.RIGHT);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void takeCameraImage() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File photoFile = null;
        try {
            photoFile = createImageFile();
        } catch (IOException e) {
            Common.showToast(act, "이미지 처리 오류! 다시 시도해주세요.");
            e.printStackTrace();
        }

        if (photoFile != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                photoUri = FileProvider.getUriForFile(this,
                        BuildConfig.APPLICATION_ID + ".provider", photoFile);
            } else {
                photoUri = Uri.fromFile(photoFile);
            }
            intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
            startActivityForResult(intent, MORE_CAMERA_IMAGE);
        }
    }

    private void takeCameraVideo() {
        Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, 50); // 초단위
        intent.putExtra(MediaStore.EXTRA_SIZE_LIMIT, (long) (1024 * 1024 * 15)); // 1024 * 1024 * n => n 메가
        intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 0);
        startActivityForResult(intent, MORE_CAMERA_VIDEO);
    }

    private void getAlbumImage() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setData(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*"); //이미지만 불러오기
        startActivityForResult(intent, MORE_ALBUM_IMAGE);
    }

    private void getAlbumVideo() {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_PICK);
        intent.setType("video/*");
        startActivityForResult(intent, MORE_ALBUM_VIDEO);
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("HHmmss").format(new Date());
        String imageFileName = "wetok" + timeStamp;
        File storageDir = new File(Environment.getExternalStorageDirectory() + "/WeTOK/");
//        File storageDir = new File(getExternalFilesDir(null) + "/WeTOK/");
        if (!storageDir.exists()) {
            storageDir.mkdirs();
        }

        File image = File.createTempFile(imageFileName, ".jpg", storageDir);
        return image;
    }

    private void cropImage() {
        Intent cropIntent = new Intent("com.android.camera.action.CROP");

        cropIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        cropIntent.setDataAndType(photoUri, "image/*");
        cropIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        cropIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

        // 파일 생성
        try {
            File albumFile = createImageFile();

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                photoUri = FileProvider.getUriForFile(act, "kr.co.core.wetok.provider", albumFile);
            } else {
                photoUri = Uri.fromFile(albumFile);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        cropIntent.putExtra("output", photoUri);

        // 여러 카메라어플중 기본앱 세팅
        List<ResolveInfo> list = getPackageManager().queryIntentActivities(cropIntent, 0);

        Intent i = new Intent(cropIntent);
        ResolveInfo res = list.get(0);

        i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        i.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

        grantUriPermission(res.activityInfo.packageName, photoUri,
                Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);

        i.setComponent(new ComponentName(res.activityInfo.packageName, res.activityInfo.name));
        startActivityForResult(i, CROP_IMAGE);
    }

    private void showProgressbar() {
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.setMessage("동영상 압축 진행중입니다..");
        dialog.show();
    }

    private void closeProgressbar() {
        if (dialog.isShowing()) {
            dialog.dismiss();
        }
    }

    private String setThumbnail(String mVideoFilePath) {
        Bitmap tempThumb = null;
        if (mVideoFilePath == null)
            return null;

        try {
            // 썸네일 추출후 리사이즈해서 다시 비트맵 생성
            String root = Environment.getExternalStorageDirectory().toString();
//                    Bitmap bitmap = ThumbnailUtils.createVideoThumbnail(mVideoFilePath, MediaStore.Video.Thumbnails.FULL_SCREEN_KIND);
            Bitmap bitmap = ThumbnailUtils.createVideoThumbnail(mVideoFilePath, MediaStore.Images.Thumbnails.MINI_KIND);
            Log.i("TEST_HOME", "rootPath: " + root + "/test/video_test.mp4");
            Bitmap thumb = ThumbnailUtils.extractThumbnail(bitmap, 360, 480);

            Log.i("TEST_HOME", "bitmap: " + bitmap);
            Log.i("TEST_HOME", "thumb: " + thumb);

            tempThumb = bitmap;

        } catch (Exception e) {
            e.printStackTrace();
        }

        //썸네일 저장 후 경로가지고 있기
        Date date = new Date();
        String filename = "/rec" + date.toString().replace(" ", "_").replace(":", "_") + ".png";

        File myDir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/JungGu/");
        if (!myDir.exists())
            myDir.mkdir();
//        mThumbFilePath = saveBitmapAsFile(tempThumb, myDir.getAbsolutePath() + filename);
        return saveBitmapAsFile(tempThumb, myDir.getAbsolutePath() + filename);
    }

    // 비트맵 -> 파일저장
    private String saveBitmapAsFile(Bitmap bitmap, String filepath) {
        File file = new File(filepath);
        OutputStream os = null;

        try {
            file.createNewFile();
            os = new FileOutputStream(file);

            bitmap.compress(Bitmap.CompressFormat.PNG, 100, os);

            os.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return file.getPath();
    }

    public String getVideoTime(String path) {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        if (Build.VERSION.SDK_INT >= 14) {
            retriever.setDataSource(path, new HashMap<String, String>());
        } else {
            retriever.setDataSource(path);
        }
        String time = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
        long timeInmillisec = Long.parseLong(time);
        String hms = String.format("%02d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(timeInmillisec) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(timeInmillisec)),
                TimeUnit.MILLISECONDS.toSeconds(timeInmillisec) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(timeInmillisec)));
        return hms;
    }

    private String getRecordTime(String filePath) {
        MediaPlayer player = new MediaPlayer();
        try {
            player.setDataSource(NetUrls.DOMAIN + filePath);
            player.prepare();
            return Common.converTimeSimpleLInt(player.getDuration());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;


        /* other */
//        MediaPlayer player2 = MediaPlayer.create(act, Uri.parse(NetUrls.DOMAIN + filePath));
//        return Common.converTimeSimpleLInt(player2.getDuration());

    }

    private void doChatFileUpload(final String path, final String type, final String thumbnail) {
        ReqBasic server = new ReqBasic(act, NetUrls.ADDRESS) {
            @Override
            public void onAfter(int resultCode, HttpResult resultData) {
                if (resultData.getResult() != null) {
                    try {
                        JSONObject jo = new JSONObject(resultData.getResult());
                        final String result = jo.getString("result");
                        final String message = jo.getString("message");

                        if (result.equalsIgnoreCase("Y")) {
                            final String file = jo.getString("file");

                            if (mSocket != null && mSocket.connected()) {
                                if (type.equalsIgnoreCase("movie")) {
                                    String file2 = jo.getString("file2");
                                    String file3 = getVideoTime(NetUrls.DOMAIN + file);
                                    doChatSend(file + "," + file2 + "," + file3, type);
                                } else {
                                    if (type.equalsIgnoreCase("mic")) {
                                        doChatSend(file + "@@@" + getRecordTime(file), type);
                                    } else {
                                        doChatSend(file, type);
                                    }
                                }
                            }

                            photoUri = null;


                        } else {
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


        server.setTag("File Upload");
        server.addParams("siteUrl", NetUrls.SITEURL);
        server.addParams("CONNECTCODE", "APP");
        server.addParams("_APP_MEM_IDX", UserPref.getMidx(act));


        File file = new File(path);
        if (type.equalsIgnoreCase("movie")) {
            server.addParams("dbControl", "setChattingPhotoUpload2");

            File thumb_file = new File(thumbnail);
            server.addFileParams("chattingImg", file);
            server.addFileParams("chattingImg2", thumb_file);
        } else {
            server.addParams("dbControl", "setChattingPhotoUpload");
            server.addFileParams("chattingImg", file);
        }

        server.execute(true, false);
    }

    // 방 나가기
    private void leaveRoom() {
        ReqBasic server = new ReqBasic(act, NetUrls.ADDRESS) {
            @Override
            public void onAfter(int resultCode, HttpResult resultData) {
                if (resultData.getResult() != null) {
                    try {
                        JSONObject jo = new JSONObject(resultData.getResult());

                        if (jo.getString("result").equalsIgnoreCase("Y")) {
                            finish();
                        } else {
                            String message = jo.getString("message");
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

        server.setTag("Leave Room");
        server.addParams("siteUrl", NetUrls.SITEURL);
        server.addParams("CONNECTCODE", "APP");
        server.addParams("_APP_MEM_IDX", UserPref.getMidx(act));

        server.addParams("dbControl", "setRoomOut");
        server.addParams("user_idx", UserPref.getMidx(act));
        server.addParams("ROOM_IDX", roomIdx);
        server.addParams("m_os", "android");
        server.execute(true, false);
    }

    //URI에서 실제경로 가져오기
    public String getRealPathFromURI(Context context, Uri contentUri) {
        Cursor cursor = null;
        try {
            String[] proj = {MediaStore.Images.Media.DATA};
            cursor = context.getContentResolver().query(contentUri, proj, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case MORE_CAMERA_IMAGE:
                    cropImage();
                    break;

                case MORE_ALBUM_IMAGE:
                    if (data == null) {
                        Common.showToast(act, getString(R.string.chat_msg_load_fail));
                        return;
                    }

                    photoUri = data.getData();
                    cropImage();
                    break;

                case CROP_IMAGE:
                    String mImagePath = photoUri.getPath();
                    Log.e(StringUtil.TAG, "mImagePath: " + mImagePath);

                    if (StringUtil.isNull(mImagePath)) {
                        Common.showToast(act, getString(R.string.chat_msg_fail));
                        break;
                    }

                    // 사진 크기변환
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inSampleSize = 2;
                    Bitmap bm = BitmapFactory.decodeFile(mImagePath, options);
                    Bitmap resize = null;
                    try {
                        File file = new File(mImagePath);
                        FileOutputStream out = new FileOutputStream(file);
                        int width = bm.getWidth();
                        int height = bm.getHeight();
                        Log.d(StringUtil.TAG_CHAT, "가로 : " + width + "세로 : " + height);
                        resize = Bitmap.createScaledBitmap(bm, width, height, true);
                        resize.compress(Bitmap.CompressFormat.JPEG, 100, out);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }

                    // 미디어 스캔
                    MediaScannerConnection.scanFile(act, new String[]{photoUri.getPath()}, null,
                            new MediaScannerConnection.OnScanCompletedListener() {
                                @Override
                                public void onScanCompleted(String path, Uri uri) {

                                }
                            });

                    doChatFileUpload(mImagePath, "photo", null);
                    photoUri = null;
                    break;

                case MORE_CAMERA_VIDEO:
                case MORE_ALBUM_VIDEO:
                    videoUri = data.getData();
                    String mVideoFilePath = getRealPathFromURI(getApplicationContext(), videoUri);
                    String mThumbFilePath;
                    Log.i("TEST_HOME", "videoUri: " + videoUri);
                    Log.i("TEST_HOME", "mVideoFilePath: " + mVideoFilePath);

                    //파일길이 확인 KB 반환
                    File file = new File(mVideoFilePath);
                    int file_size = Integer.parseInt(String.valueOf(file.length() / 1024));
                    Log.i("TEST_HOME", "file_size: " + file_size);

                    if (file_size > 15360) {// 15360 => 15MB
                        Common.showToast(act, "영상이 15MB를 초과합니다.");
                        break;
                    } else {
                        mThumbFilePath = setThumbnail(mVideoFilePath);
                        doChatFileUpload(mVideoFilePath, "movie", mThumbFilePath);
                    }

                    break;

                case MORE_FILE:
                    String filePath = UriUtils.getPath(act, data.getData());
                    File getFile = new File(filePath);
                    Log.e(StringUtil.TAG_CHAT, "파일 이름: " + getFile.getName());
                    if (getFile.length() > 10000000) {
                        Common.showToast(act, "파일 용량이 10MB가 넘습니다");
                        filePath = "";
                    } else {
                        doChatFileUpload(filePath, "file", null);
                    }
                    break;

                //TODO 통화 종료 후 -> 정상
                case RTC_VOICE_CALL:
                    Log.e(StringUtil.TAG, "success voice call in ChatAct");

                    String result_voice = "기간: ";
                    if (null != data) {
                        result_voice += data.getStringExtra("result_time");
                    }

                    final String cmp_result_voice = result_voice;
                    Timer timer = new Timer();
                    TimerTask task = new TimerTask() {
                        @Override
                        public void run() {
                            if (mSocket.connected()) {
                                doChatSend(cmp_result_voice, "call_voice");
                                timer.cancel();
                            }
                        }
                    };
                    timer.schedule(task, 500);
                    break;

                case RTC_VIDEO_CALL:
                    Log.e(StringUtil.TAG, "success video call in ChatAct");

                    String result_video = "기간: ";
                    if (null != data) {
                        result_video += data.getStringExtra("result_time");
                    }

                    final String cmp_result_video = result_video;
                    Timer timer2 = new Timer();
                    TimerTask task2 = new TimerTask() {
                        @Override
                        public void run() {
                            if (mSocket.connected()) {
                                doChatSend(cmp_result_video, "call_video");
                                timer2.cancel();
                            }
                        }
                    };
                    timer2.schedule(task2, 500);
                    break;
            }


        } else if (resultCode == Activity.RESULT_CANCELED) {
            switch (requestCode) {

                //TODO 통화 종료 후 -> 취소
                case RTC_VOICE_CALL:
                    Log.e(StringUtil.TAG, "cancel voice call in ChatAct");
                    Timer timer = new Timer();
                    TimerTask task = new TimerTask() {
                        @Override
                        public void run() {
                            if (mSocket.connected()) {
                                doChatSend("취소됨", "call_voice");
                                timer.cancel();
                            }
                        }
                    };
                    timer.schedule(task, 500);
                    break;

                case RTC_VIDEO_CALL:
                    Log.e(StringUtil.TAG, "cancel video call in ChatAct");
                    Timer timer2 = new Timer();
                    TimerTask task2 = new TimerTask() {
                        @Override
                        public void run() {
                            if (mSocket.connected()) {
                                doChatSend("취소됨", "call_video");
                                timer2.cancel();
                            }
                        }
                    };
                    timer2.schedule(task2, 500);
                    break;
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (isDrawerOpen) {
            binding.dlChat.closeDrawer(Gravity.RIGHT);
        } else if (bottomMenuOpenState) {
            closeBottomMenu();
        } else {
            super.onBackPressed();
        }
    }

    private void hideKeyboard() {
        if (keyboardOpenState) {
            imm.hideSoftInputFromWindow(binding.etText.getWindowToken(), 0);
        }
    }

    private void showKeyboard() {
        if (!keyboardOpenState) {
            imm.showSoftInput(binding.etText, 0);
        }
    }

    private void setBottomMenu() {
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

        /* bottom menu 길이 세팅 */
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) binding.llBottomMenuArea.getLayoutParams();
        params.height = SystemPref.getKeyBoardHeight(act); // 877
        binding.llBottomMenuArea.setLayoutParams(params);
    }


    private void openBottomMenu() {
        if (!bottomMenuOpenState) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
//                    Common.showToast(act, "open");
                    hideKeyboard();

                    /* default */
                    new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            binding.llBottomMenuArea.setVisibility(View.VISIBLE);
                        }
                    }, 100);

                    /* 1 */
//                    ValueAnimator anim = ValueAnimator.ofInt(SystemPref.getKeyBoardHeight(act), 0);
//                    anim
//                            .setDuration(300)
//                            .addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
//                                @Override
//                                public void onAnimationUpdate(ValueAnimator animation) {
//                                    Integer value = (Integer) animation.getAnimatedValue();
////                                    binding.llBottomMenuArea.getLayoutParams().height = value.intValue();
//                                    binding.llBottomMenuArea.setTranslationY(value.intValue());
//                                    binding.llBottomMenuArea.requestLayout();
//                                }
//                            });
//                    anim.start();
//
//                    binding.llBottomMenuArea.setVisibility(View.VISIBLE);


                    bottomMenuOpenState = true;
                }
            });
        }
    }

    private void closeBottomMenu() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
//                Common.showToast(act, "close");
                if (bottomMenuOpenState) {
                    /* default */
                    binding.llBottomMenuArea.setVisibility(View.GONE);


                    /* 1 */
//                    ValueAnimator anim = ValueAnimator.ofInt(0, SystemPref.getKeyBoardHeight(act));
//                    anim
//                            .setDuration(300)
//                            .addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
//                                @Override
//                                public void onAnimationUpdate(ValueAnimator animation) {
//                                    Integer value = (Integer) animation.getAnimatedValue();
////                                    binding.llBottomMenuArea.getLayoutParams().height = value.intValue();
//                                    binding.llBottomMenuArea.setTranslationY(value.intValue());
//                                    binding.llBottomMenuArea.requestLayout();
//                                }
//                            });
//                    anim.start();
//                    binding.llBottomMenuArea.setVisibility(View.GONE);

                    bottomMenuOpenState = false;
                }
            }
        });
    }

    public void closeDrawer() {
        if (isDrawerOpen) {
            binding.dlChat.closeDrawer(Gravity.RIGHT);
        }
    }


    // 클릭
    @Override
    public void onClick(View v) {
        Intent intent = null;

        switch (v.getId()) {
            case R.id.fl_back:
                binding.llBottomDefaultArea.setVisibility(View.VISIBLE);
                binding.llBottomRecordArea.setVisibility(View.GONE);

                binding.etText.setFocusableInTouchMode(true);
                binding.etText.requestFocus();
                showKeyboard();
                break;

            case R.id.rcv_chatting:
                if (bottomMenuOpenState) {
                    closeBottomMenu();
                }

                Common.showToast(act, "test");
                break;


            /* bottom menu */
            case R.id.fl_more02:
                if (bottomMenuOpenState) {
                    closeBottomMenu();
                } else {
                    openBottomMenu();
                }
                break;

            case R.id.fl_right_btn:
                if (!toggleEditTextCheck) {
                    // 더보기 버튼
                    if (bottomMenuOpenState) {
                        closeBottomMenu();
                    } else {
                        openBottomMenu();
                    }
                } else {
                    // 메시지 전송 버튼
                    if (StringUtil.isNull(binding.etText.getText().toString())) {
                        Common.showToast(act, "메시지를 입력하세요");
                        return;
                    }

                    String dateLine = new SimpleDateFormat(getString(R.string.chat_msg_date)).format(System.currentTimeMillis());
                    String sendTime = new SimpleDateFormat("a hh:mm").format((System.currentTimeMillis()));

                    // 데이트라인 확인 후 추가
                    if (list.size() != 0) {
                        if (!list.get(list.size() - 1).getDateLine().equals(dateLine)) {
                            ChattingData data = new ChattingData(dateLine, "dateLine");
                            list.add(data);
                        }
                    } else {
                        ChattingData data = new ChattingData(dateLine, "dateLine");
                        list.add(data);
                    }


                    String msg = binding.etText.getText().toString();
                    binding.etText.setText(null);
                    ChattingData data = new ChattingData("", msg, UserPref.getMidx(act),
                            sendTime, dateLine,
                            Integer.toString(list_part.size()), "text", "N", "", "", "", "N", "");
                    adapter.addItem(data);
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            binding.rcvChatting.scrollToPosition(adapter.getItemCount() - 1);
                        }
                    }, 200);

                    doChatSend(msg, "text");
                }
                break;

            case R.id.ll_album:
                showDialogSub(false);
                break;

            case R.id.ll_camera:
                showDialogSub(true);
                break;

            case R.id.ll_call_video:
                Common.showToastLong(act, "통화할 대상을 선택해주세요");
                binding.dlChat.openDrawer(Gravity.RIGHT);
                menuCallState = "video";
                break;

            case R.id.ll_call_voice:
                Common.showToastLong(act, "통화할 대상을 선택해주세요");
                binding.dlChat.openDrawer(Gravity.RIGHT);
                menuCallState = "voice";
                break;

            case R.id.ll_file:
                getFile();
                break;

            case R.id.fl_record:
                binding.llBottomDefaultArea.setVisibility(View.GONE);
                binding.llBottomRecordArea.setVisibility(View.VISIBLE);

                hideKeyboard();
                break;


            case R.id.fl_leave:
                new AlertDialog.Builder(act)
                        .setMessage("해당 방에서 나가시겠습니까?")
                        .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                leaveRoom();
                            }
                        })
                        .setNegativeButton("취소", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                            }
                        }).show();
                break;
        }
    }
}
