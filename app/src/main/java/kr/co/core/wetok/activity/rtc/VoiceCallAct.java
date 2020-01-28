package kr.co.core.wetok.activity.rtc;

import android.app.Activity;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.corertc.coresdk.rtc.AppRTCAudioManager;
import com.corertc.coresdk.rtc.AppRTCClient;
import com.corertc.coresdk.rtc.DirectRTCClient;
import com.corertc.coresdk.rtc.PeerConnectionClient;
import com.corertc.coresdk.rtc.UnhandledExceptionHandler;
import com.corertc.coresdk.rtc.WebSocketRTCClient;

import org.json.JSONException;
import org.json.JSONObject;
import org.webrtc.IceCandidate;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.SessionDescription;
import org.webrtc.StatsReport;
import org.webrtc.VideoRenderer;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import kr.co.core.wetok.R;
import kr.co.core.wetok.activity.BaseAct;
import kr.co.core.wetok.data.UserData;
import kr.co.core.wetok.preference.UserPref;
import kr.co.core.wetok.server.ReqBasic;
import kr.co.core.wetok.server.netUtil.HttpResult;
import kr.co.core.wetok.server.netUtil.NetUrls;
import kr.co.core.wetok.util.Common;
import kr.co.core.wetok.util.SSLConnect;
import kr.co.core.wetok.util.StringUtil;

public class VoiceCallAct extends BaseAct implements AppRTCClient.SignalingEvents,
        PeerConnectionClient.PeerConnectionEvents {

    private static final String TAG = "VoiceCallAct";

    static {
        try {
            System.loadLibrary("c++_shared");
            System.loadLibrary("boringssl.cr");
            System.loadLibrary("protobuf_lite.cr");
        } catch (UnsatisfiedLinkError e) {
        }
    }

    public static final String EXTRA_ROOMID = "ROOMID";
    public static final String EXTRA_URLPARAMETERS = "URLPARAMETERS";
    public static final String EXTRA_LOOPBACK = "LOOPBACK";
    public static final String EXTRA_VIDEO_CALL = "VIDEO_CALL";
    public static final String EXTRA_CAMERA2 = "CAMERA2";
    public static final String EXTRA_VIDEO_WIDTH = "VIDEO_WIDTH";
    public static final String EXTRA_VIDEO_HEIGHT = "VIDEO_HEIGHT";
    public static final String EXTRA_VIDEO_FPS = "VIDEO_FPS";
    public static final String EXTRA_VIDEO_CAPTUREQUALITYSLIDER_ENABLED = "VIDEO_CAPTUREQUALITYSLIDER";

    public static final String EXTRA_VIDEO_BITRATE = "VIDEO_BITRATE";
    public static final String EXTRA_VIDEOCODEC = "VIDEOCODEC";
    public static final String EXTRA_HWCODEC_ENABLED = "HWCODEC";
    public static final String EXTRA_CAPTURETOTEXTURE_ENABLED = "CAPTURETOTEXTURE";
    public static final String EXTRA_FLEXFEC_ENABLED = "FLEXFEC";
    public static final String EXTRA_AUDIO_BITRATE = "AUDIO_BITRATE";
    public static final String EXTRA_AUDIOCODEC = "AUDIOCODEC";
    public static final String EXTRA_NOAUDIOPROCESSING_ENABLED = "NOAUDIOPROCESSING";
    public static final String EXTRA_AECDUMP_ENABLED = "AECDUMP";
    public static final String EXTRA_OPENSLES_ENABLED = "OPENSLES";
    public static final String EXTRA_DISABLE_BUILT_IN_AEC = "DISABLE_BUILT_IN_AEC";
    public static final String EXTRA_DISABLE_BUILT_IN_AGC = "DISABLE_BUILT_IN_AGC";
    public static final String EXTRA_DISABLE_BUILT_IN_NS = "DISABLE_BUILT_IN_NS";
    public static final String EXTRA_ENABLE_LEVEL_CONTROL = "ENABLE_LEVEL_CONTROL";
    public static final String EXTRA_DISABLE_WEBRTC_AGC_AND_HPF = "DISABLE_WEBRTC_GAIN_CONTROL";
    public static final String EXTRA_DISPLAY_HUD = "DISPLAY_HUD";

    public static final String EXTRA_TRACING = "TRACING";
    public static final String EXTRA_CMDLINE = "CMDLINE";
    public static final String EXTRA_RUNTIME = "RUNTIME";
    public static final String EXTRA_VIDEO_FILE_AS_CAMERA = "VIDEO_FILE_AS_CAMERA";
    public static final String EXTRA_DATA_CHANNEL_ENABLED = "DATA_CHANNEL_ENABLED";
    public static final String EXTRA_ORDERED = "ORDERED";
    public static final String EXTRA_MAX_RETRANSMITS_MS = "MAX_RETRANSMITS_MS";
    public static final String EXTRA_MAX_RETRANSMITS = "MAX_RETRANSMITS";
    public static final String EXTRA_PROTOCOL = "PROTOCOL";
    public static final String EXTRA_NEGOTIATED = "NEGOTIATED";
    public static final String EXTRA_ID = "ID";
    public static final String RECEIVE_IDX = "RECEIVE_IDX";

    private static final String[] MANDATORY_PERMISSIONS = {"android.permission.MODIFY_AUDIO_SETTINGS",
            "android.permission.RECORD_AUDIO", "android.permission.INTERNET"};

    private static final int STAT_CALLBACK_PERIOD = 1000;

    private final List<VideoRenderer.Callbacks> remoteRenderers = new ArrayList<>();


    private boolean iceConnected;
    private AppRTCClient.SignalingParameters signalingParameters;
    //    private CallFragment callFragment;
    private PeerConnectionClient peerConnectionClient = null;
    private PeerConnectionClient.PeerConnectionParameters peerConnectionParameters;
    private AppRTCClient.RoomConnectionParameters roomConnectionParameters;
    private boolean commandLineRun;
    private AppRTCClient appRtcClient;
    private long callStartedTimeMs = 0;
    private long calledStartedTime = 0;
    private AppRTCAudioManager audioManager = null;

    private boolean isError;
    private boolean activityRunning;
    Toast logToast;

    private boolean micEnabled = true;

    TextView callTime, profileNickname, callingStateText;
    ImageView profileImage;
    LinearLayout disconnectButton, muteBtn;
    TextView tv_mute;

    public static Activity act;
    String receive_idx;
    MediaPlayer callSound;
    boolean soundStatus = false;
    Intent intent;

    boolean callingState = true;

    String yidx, roomId;
    UserData userData;

    boolean isDisconnectButtonPressed = false;

    private final String BROADCAST_MESSAGE = "android.intent.action.PHONE_STATE";
    private BroadcastReceiver mReceiver = null;

    private Timer timer;

    private String resultTime = "00:00";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Thread.setDefaultUncaughtExceptionHandler(new UnhandledExceptionHandler(this));

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        setContentView(R.layout.activity_voice_call);

        SSLConnect ssl = new SSLConnect();
        ssl.postHttps("https://appr.tc", 1000, 1000);

        iceConnected = false;
        signalingParameters = null;

        // view 변수 선언
        callingStateText = (TextView) findViewById(R.id.tv_calling_state);
        callTime = (TextView) findViewById(R.id.tv_call_time);
        profileImage = (ImageView) findViewById(R.id.iv_profile);
        profileNickname = (TextView) findViewById(R.id.tv_nick);
        disconnectButton = (LinearLayout) findViewById(R.id.ll_disconnect);
        muteBtn = (LinearLayout) findViewById(R.id.ll_mute);
        tv_mute = (TextView) findViewById(R.id.tv_mute);

        act = this;

        intent = getIntent();
        userData = (UserData) intent.getSerializableExtra("userData");

        // 상대 데이터 세팅
        profileNickname.setText(userData.getName());
        Glide.with(act)
                .load(NetUrls.DOMAIN + userData.getProfile_img())
                .transform(new RoundedCorners(27))
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(profileImage);

        // 콜링 사운드 세팅
        callSound = MediaPlayer.create(act, R.raw.call_sound);
        callSound.setLooping(true);
        callSound.start();

        soundStatus = true;

        // 통화종료버튼 세팅
        disconnectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isDisconnectButtonPressed) {
                    isDisconnectButtonPressed = true;
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            onCallHangUp();
                        }
                    }, 500);
                }
            }
        });

        // 음소거 버튼 세팅
        muteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean enabled = onToggleMic();
                muteBtn.setAlpha(enabled ? 1.0f : 0.3f);
                tv_mute.setSelected(!enabled);
            }
        });

        // Create peer connection client.
        peerConnectionClient = new PeerConnectionClient();

        // Check for mandatory permissions.
        for (String permission : MANDATORY_PERMISSIONS) {
            if (checkCallingOrSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission " + permission + " is not granted", Toast.LENGTH_LONG).show();
                result_cancel();
                finish();
                return;
            }
        }

        Uri roomUri = intent.getData();
        if (roomUri == null) {
            Log.e(StringUtil.TAG_RTC, "Didn't get any URL in intent!");
            result_cancel();
            finish();
            return;
        }

        // Get Intent parameters.
        roomId = intent.getStringExtra(EXTRA_ROOMID);
        receive_idx = intent.getStringExtra(RECEIVE_IDX);
        Log.d(StringUtil.TAG_RTC, "Room ID: " + roomId);
        if (roomId == null || roomId.length() == 0) {
            Log.e(StringUtil.TAG_RTC, "Incorrect room ID in intent!");
            result_cancel();
            finish();
            return;
        }

        boolean loopback = intent.getBooleanExtra(EXTRA_LOOPBACK, false);
        boolean tracing = intent.getBooleanExtra(EXTRA_TRACING, false);

        PeerConnectionClient.DataChannelParameters dataChannelParameters = null;
        if (intent.getBooleanExtra(EXTRA_DATA_CHANNEL_ENABLED, false)) {
            dataChannelParameters = new PeerConnectionClient.DataChannelParameters(intent.getBooleanExtra(EXTRA_ORDERED, true),
                    intent.getIntExtra(EXTRA_MAX_RETRANSMITS_MS, -1),
                    intent.getIntExtra(EXTRA_MAX_RETRANSMITS, -1), intent.getStringExtra(EXTRA_PROTOCOL),
                    intent.getBooleanExtra(EXTRA_NEGOTIATED, false), intent.getIntExtra(EXTRA_ID, -1));
        }
        peerConnectionParameters =
                new PeerConnectionClient.PeerConnectionParameters(intent.getBooleanExtra(EXTRA_VIDEO_CALL, true), loopback,
                        tracing, 0, 0, intent.getIntExtra(EXTRA_VIDEO_FPS, 0),
                        intent.getIntExtra(EXTRA_VIDEO_BITRATE, 0), intent.getStringExtra(EXTRA_VIDEOCODEC),
                        intent.getBooleanExtra(EXTRA_HWCODEC_ENABLED, true),
                        intent.getBooleanExtra(EXTRA_FLEXFEC_ENABLED, false),
                        intent.getIntExtra(EXTRA_AUDIO_BITRATE, 0), intent.getStringExtra(EXTRA_AUDIOCODEC),
                        intent.getBooleanExtra(EXTRA_NOAUDIOPROCESSING_ENABLED, false),
                        intent.getBooleanExtra(EXTRA_AECDUMP_ENABLED, false),
                        intent.getBooleanExtra(EXTRA_OPENSLES_ENABLED, false),
                        intent.getBooleanExtra(EXTRA_DISABLE_BUILT_IN_AEC, false),
                        intent.getBooleanExtra(EXTRA_DISABLE_BUILT_IN_AGC, false),
                        intent.getBooleanExtra(EXTRA_DISABLE_BUILT_IN_NS, false),
                        intent.getBooleanExtra(EXTRA_ENABLE_LEVEL_CONTROL, false),
                        intent.getBooleanExtra(EXTRA_DISABLE_WEBRTC_AGC_AND_HPF, false), dataChannelParameters);
        commandLineRun = intent.getBooleanExtra(EXTRA_CMDLINE, false);
        int runTimeMs = intent.getIntExtra(EXTRA_RUNTIME, 0);

        // Create connection client. Use DirectRTCClient if room name is an IP otherwise use the
        // standard WebSocketRTCClient.
        if (loopback || !DirectRTCClient.IP_PATTERN.matcher(roomId).matches()) {
            appRtcClient = new WebSocketRTCClient(this);
        } else {
            Log.i(TAG, "Using DirectRTCClient because room name looks like an IP.");
            appRtcClient = new DirectRTCClient(this);
        }
        // Create connection parameters.
        String urlParameters = intent.getStringExtra(EXTRA_URLPARAMETERS);
        roomConnectionParameters =
                new AppRTCClient.RoomConnectionParameters(roomUri.toString(), roomId, loopback, urlParameters);

        // For command line execution run connection for <runTimeMs> and exit.
        if (commandLineRun && runTimeMs > 0) {
            (new Handler()).postDelayed(new Runnable() {
                @Override
                public void run() {
                    disconnect();
                }
            }, runTimeMs);
        }

        if (loopback) {
            PeerConnectionFactory.Options options = new PeerConnectionFactory.Options();
            options.networkIgnoreMask = 0;
            peerConnectionClient.setPeerConnectionFactoryOptions(options);
        }

        peerConnectionClient.createPeerConnectionFactory(
                this, peerConnectionParameters, VoiceCallAct.this);

        startCall();

        callOtherUser(userData.getIdx());

        timer = new Timer();
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        callingStateText.setText("응답 대기중");
                    }
                }, 0);

                new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        callingStateText.setText("응답 대기중.");
                    }
                }, 500);

                new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        callingStateText.setText("응답 대기중..");
                    }
                }, 1000);

                new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        callingStateText.setText("응답 대기중...");
                    }
                }, 1500);
            }
        };

        timer.schedule(timerTask, 0, 2000);
    }

    public boolean onToggleMic() {
        if (peerConnectionClient != null) {
            micEnabled = !micEnabled;
            peerConnectionClient.setAudioEnabled(micEnabled);
        }
        return micEnabled;
    }

    private void sendDisconnectPush(String yidx) {
        ReqBasic server = new ReqBasic(act, NetUrls.ADDRESS) {
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
                        Common.showToastNetwork(act);
                    }
                } else {
                    Common.showToastNetwork(act);
                }
            }
        };

        server.setTag("Disconnect Push");
        server.addParams("siteUrl", NetUrls.SITEURL);
        server.addParams("CONNECTCODE", "APP");
        server.addParams("_APP_MEM_IDX", UserPref.getMidx(act));

        server.addParams("dbControl", "getVideoChattingENDSend");
        server.addParams("guest_idx", yidx);
        server.addParams("msg", "call");
        server.execute(true, false);
    }

    public void disconnectFromService() {
        disconnect();
    }


    private void callOtherUser(String yidx) {
        ReqBasic server = new ReqBasic(act, NetUrls.ADDRESS) {
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
//                        Common.showToastNetwork(act);
                    }
                } else {
//                    Common.showToastNetwork(act);
                }
            }
        };

        server.setTag("Call Other (Voice)");
        server.addParams("siteUrl", NetUrls.SITEURL);
        server.addParams("CONNECTCODE", "APP");
        server.addParams("_APP_MEM_IDX", UserPref.getMidx(act));

        server.addParams("dbControl", "getVideoChattingIDSend");
        server.addParams("guest_idx", yidx);
        server.addParams("_ID", roomId);
        server.addParams("_CLASS", "음성");
        server.execute(true, false);
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver();
    }

    private void registerReceiver() {
        if (mReceiver != null) return;

        final IntentFilter theFilter = new IntentFilter();
        theFilter.addAction(BROADCAST_MESSAGE);

        this.mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                TelephonyManager tm = (TelephonyManager) context.getSystemService(Service.TELEPHONY_SERVICE);

                switch (tm.getCallState()) {
                    //통화중
                    case TelephonyManager.CALL_STATE_OFFHOOK:
                        onCallHangUp();
                        break;
                }
            }
        };
        this.registerReceiver(this.mReceiver, theFilter);

    }

    private void unregisterReceiver() {
        if (mReceiver != null) {
            this.unregisterReceiver(mReceiver);
            mReceiver = null;
        }
    }

    private void startCall() {
        if (appRtcClient == null) {
            Log.e(StringUtil.TAG_RTC, "AppRTC client is not allocated for a call.");
            return;
        }
        callStartedTimeMs = System.currentTimeMillis();

        // Start room connection.
        appRtcClient.connectToRoom(roomConnectionParameters);
        // Create and audio manager that will take care of audio routing,
        // audio modes, audio device enumeration etc.
        audioManager = AppRTCAudioManager.create(getApplicationContext());

        // Store existing audio settings and change audio mode to
        // MODE_IN_COMMUNICATION for best possible VoIP performance.
        Log.d(StringUtil.TAG_RTC, "Starting the audio manager...");
        audioManager.start(new AppRTCAudioManager.AudioManagerEvents() {
            // This method will be called each time the number of available audio
            // devices has changed.
            @Override
            public void onAudioDeviceChanged(
                    AppRTCAudioManager.AudioDevice audioDevice, Set<AppRTCAudioManager.AudioDevice> availableAudioDevices) {
                onAudioManagerDevicesChanged(audioDevice, availableAudioDevices);
            }
        });
    }

    private void onAudioManagerDevicesChanged(
            final AppRTCAudioManager.AudioDevice device, final Set<AppRTCAudioManager.AudioDevice> availableDevices) {
        Log.d(StringUtil.TAG_RTC, "onAudioManagerDevicesChanged: " + availableDevices + ", "
                + "selected: " + device);
        // TODO(henrika): add callback handler.

        Log.i(StringUtil.TAG_RTC, "onAudioManagerDevicesChanged");
    }

    private void disconnect() {
        Log.i(StringUtil.TAG_RTC, "disconnect");

        activityRunning = false;
        if (soundStatus) {
            callSound.stop();
            callSound.release();
            soundStatus = false;
        }

        if (appRtcClient != null) {
            appRtcClient.disconnectFromRoom();
            appRtcClient = null;
        }

        if (peerConnectionClient != null) {
            peerConnectionClient.close();
            peerConnectionClient = null;
        }
        if (audioManager != null) {
            audioManager.stop();
            audioManager = null;
        }

        if (iceConnected && !isError) {
            result_ok();
        } else {
            result_cancel();
        }

        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                finish();
                act = null;
            }
        }, 1000);
    }

    @Override
    public void onStart() {
        Log.i(StringUtil.TAG_RTC, "onStart");
        super.onStart();
        peerConnectionClient.startVideoSource();
        activityRunning = true;
    }

    private void result_cancel() {
        setResult(RESULT_CANCELED);

//        if (calledStartedTime != 0) {
//
//            Intent resultIntent = new Intent(VoiceCallAct.this, ConnectActivity.class);
//            resultIntent.putExtra("result_time", callTime.getText().toString());
//            resultIntent.putExtra("type", "voice_call");
//
//            setResult(RESULT_CANCELED, resultIntent);
//        } else {
//            setResult(RESULT_CANCELED);
//        }
    }

    private void result_ok() {
        Intent resultIntent = new Intent(VoiceCallAct.this, ConnectActivity.class);
        resultIntent.putExtra("result_time", callTime.getText().toString());

        setResult(RESULT_OK, resultIntent);

//        if (calledStartedTime != 0) {
//            Intent resultIntent = new Intent(VoiceCallAct.this, ConnectActivity.class);
//            resultIntent.putExtra("result_time", callTime.getText().toString());
//
//            setResult(RESULT_OK, resultIntent);
//        } else {
//            setResult(RESULT_OK);
//        }
    }

    @Override
    public void onStop() {
        super.onStop();
        activityRunning = false;
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver();

        Thread.setDefaultUncaughtExceptionHandler(null);
        disconnect();
        activityRunning = false;
        if (soundStatus) {
            callSound.stop();
            callSound.release();
            soundStatus = false;
        }

        super.onDestroy();
    }

    public void onCallHangUp() {
        Log.i(StringUtil.TAG_RTC, "onCallHangUp");
        if (callingState)
            sendDisconnectPush(userData.getIdx());

        disconnect();
    }


    // Should be called from UI thread
    private void callConnected() {
        Log.i(StringUtil.TAG_RTC, "callConnected");
        final long delta = System.currentTimeMillis() - callStartedTimeMs;
        Log.i(StringUtil.TAG_RTC, "Call connected: delay=" + delta + "ms");
        if (peerConnectionClient == null || isError) {
            Log.w(StringUtil.TAG_RTC, "Call is connected in closed or error state");
            return;
        }
        peerConnectionClient.enableStatsEvents(true, STAT_CALLBACK_PERIOD);

        // 응답대기중 글자 삭제
        if(timer != null) {
            timer.cancel();
        }
        callingStateText.setVisibility(View.GONE);

        // 시간 보이기
        callTime.setVisibility(View.VISIBLE);

        // 음소거 버튼 켜기
        muteBtn.setVisibility(View.VISIBLE);

        // 소리 끄기
        if (soundStatus) {
            callSound.stop();
            callSound.release();
            soundStatus = false;
        }

        calledStartedTime = System.currentTimeMillis();
    }


    private void onConnectedToRoomInternal(final AppRTCClient.SignalingParameters params) {
        Log.i(StringUtil.TAG_RTC, "onConnectedToRoomInternal");
        signalingParameters = params;

        peerConnectionClient.createPeerConnection(
                null, remoteRenderers, null, signalingParameters);

        if (signalingParameters.initiator) {
            peerConnectionClient.createOffer();
        } else {
            if (params.offerSdp != null) {
                peerConnectionClient.setRemoteDescription(params.offerSdp);
                // Create answer. Answer SDP will be sent to offering client in
                // PeerConnectionEvents.onLocalDescription event.
                peerConnectionClient.createAnswer();
            }
            if (params.iceCandidates != null) {
                // Add remote ICE candidates from room.
                for (IceCandidate iceCandidate : params.iceCandidates) {
                    peerConnectionClient.addRemoteIceCandidate(iceCandidate);
                }
            }
        }
    }

    @Override
    public void onConnectedToRoom(final AppRTCClient.SignalingParameters params) {
        Log.i(StringUtil.TAG_RTC, "onConnectedToRoom");
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                onConnectedToRoomInternal(params);
            }
        });
    }

    @Override
    public void onRemoteDescription(final SessionDescription sdp) {
        Log.i(StringUtil.TAG_RTC, "onRemoteDescription");
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (peerConnectionClient == null) {
                    Log.e(StringUtil.TAG_RTC, "Received remote SDP for non-initilized peer connection.");
                    return;
                }
                peerConnectionClient.setRemoteDescription(sdp);
                if (!signalingParameters.initiator) {
                    // Create answer. Answer SDP will be sent to offering client in
                    // PeerConnectionEvents.onLocalDescription event.
                    peerConnectionClient.createAnswer();
                }
            }
        });
    }

    @Override
    public void onRemoteIceCandidate(final IceCandidate candidate) {
        Log.i(StringUtil.TAG_RTC, "onRemoteIceCandidate");
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (peerConnectionClient == null) {
                    Log.e(StringUtil.TAG_RTC, "Received ICE candidate for a non-initialized peer connection.");
                    return;
                }
                peerConnectionClient.addRemoteIceCandidate(candidate);
            }
        });


    }

    @Override
    public void onRemoteIceCandidatesRemoved(final IceCandidate[] candidates) {
        Log.i(StringUtil.TAG_RTC, "onRemoteIceCandidatesRemoved");
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (peerConnectionClient == null) {
                    Log.e(StringUtil.TAG_RTC, "Received ICE candidate removals for a non-initialized peer connection.");
                    return;
                }
                peerConnectionClient.removeRemoteIceCandidates(candidates);
            }
        });

    }

    @Override
    public void onChannelClose() {
        Log.i(StringUtil.TAG_RTC, "onChannelClose");
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                disconnect();
            }
        });
    }

    @Override
    public void onChannelError(final String description) {
        Log.i(StringUtil.TAG_RTC, "onChannelError");
        Toast.makeText(act, "통신에러가 발생하였습니다. 잠시 후 다시 시도해주세요.", Toast.LENGTH_SHORT).show();
        disconnect();
        result_cancel();
    }

    // -----Implementation of PeerConnectionClient.PeerConnectionEvents.---------
    // Send local peer connection SDP and ICE candidates to remote party.
    // All callbacks are invoked from peer connection client looper thread and
    // are routed to UI thread.
    @Override
    public void onLocalDescription(final SessionDescription sdp) {
        Log.i(StringUtil.TAG_RTC, "onLocalDescription");
        final long delta = System.currentTimeMillis() - callStartedTimeMs;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (appRtcClient != null) {
                    if (signalingParameters.initiator) {
                        appRtcClient.sendOfferSdp(sdp);
                    } else {
                        appRtcClient.sendAnswerSdp(sdp);
                    }
                }
                if (peerConnectionParameters.videoMaxBitrate > 0) {
                    Log.d(StringUtil.TAG_RTC, "Set video maximum bitrate: " + peerConnectionParameters.videoMaxBitrate);
                    peerConnectionClient.setVideoMaxBitrate(peerConnectionParameters.videoMaxBitrate);
                }
            }
        });

    }

    @Override
    public void onIceCandidate(final IceCandidate candidate) {
        Log.i(StringUtil.TAG_RTC, "onIceCandidate");
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (appRtcClient != null) {
                    appRtcClient.sendLocalIceCandidate(candidate);
                }
            }
        });
    }

    @Override
    public void onIceCandidatesRemoved(final IceCandidate[] candidates) {
        Log.i(StringUtil.TAG_RTC, "onIceCandidatesRemoved");
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (appRtcClient != null) {
                    appRtcClient.sendLocalIceCandidateRemovals(candidates);
                }
            }
        });
    }

    @Override
    public void onIceConnected() {
        Log.i(StringUtil.TAG_RTC, "onIceConnected");
        final long delta = System.currentTimeMillis() - callStartedTimeMs;
        callingState = false;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                iceConnected = true;
                callConnected();
            }
        });
    }

    @Override
    public void onIceDisconnected() {
        Log.i(StringUtil.TAG_RTC, "onIceDisconnected");
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                iceConnected = false;
                disconnect();
            }
        });
    }

    @Override
    public void onPeerConnectionClosed() {
        Log.i(StringUtil.TAG_RTC, "onPeerConnectionClosed");
    }


    @Override
    public void onPeerConnectionStatsReady(final StatsReport[] reports) {
        Log.i(StringUtil.TAG_RTC, "onPeerConnectionStatsReady");
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (!isError && iceConnected) {
                    long reusltTime = System.currentTimeMillis() - calledStartedTime;
                    resultTime = getDate(reusltTime, "mm:ss");
                    callTime.setText(resultTime);
                }
            }
        });
    }

    public static String getDate(long milliSeconds, String dateFormat) {
        Log.i(StringUtil.TAG_RTC, "getDate");
        // Create a DateFormatter object for displaying date in specified format.
        SimpleDateFormat formatter = new SimpleDateFormat(dateFormat);

        // Create a calendar object that will convert the date and time value in milliseconds to date.
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(milliSeconds);
        return formatter.format(calendar.getTime());
    }

    @Override
    public void onPeerConnectionError(final String description) {
        Log.i(StringUtil.TAG_RTC, "onPeerConnectionError");
        reportError(description);
        Toast.makeText(act, "통신에러가 발생하였습니다. 잠시 후 다시 시도해주세요.", Toast.LENGTH_SHORT).show();
        disconnect();
        result_cancel();
    }


    private void reportError(final String description) {
        Log.i(StringUtil.TAG_RTC, "reportError");
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (!isError) {
                    isError = true;
                    disconnectWithErrorMessage(description);
                    Toast.makeText(act, "통신에러가 발생하였습니다. 잠시 후 다시 시도해주세요.", Toast.LENGTH_SHORT).show();
                    result_cancel();
                    disconnect();
                }
            }
        });
    }

    private void disconnectWithErrorMessage(final String errorMessage) {
        Log.i(StringUtil.TAG_RTC, "disconnectWithErrorMessage");
        Log.e("Critical", "Critical error: " + errorMessage);
        Toast.makeText(act, "통신에러가 발생하였습니다. 잠시 후 다시 시도해주세요.", Toast.LENGTH_SHORT).show();
        if (commandLineRun || !activityRunning) {
            disconnect();
            result_cancel();
        }
    }

    @Override
    public void onBackPressed() {
        if (!isDisconnectButtonPressed) {
            isDisconnectButtonPressed = true;
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    onCallHangUp();
                }
            }, 500);
        }
    }

}
