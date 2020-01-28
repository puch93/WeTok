package kr.co.core.wetok.activity.rtc;

import android.app.Activity;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Vibrator;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
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
import kr.co.core.wetok.server.netUtil.NetUrls;
import kr.co.core.wetok.util.Common;
import kr.co.core.wetok.util.SSLConnect;
import kr.co.core.wetok.util.StringUtil;

public class VoiceReceiveAct extends BaseAct implements AppRTCClient.SignalingEvents,
        PeerConnectionClient.PeerConnectionEvents {
//        CallReceiveFragment.OnCallEvents {

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
    //    private CallReceiveFragment callReceiveFragment;
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
    private boolean callingStatus = false;
    Toast logToast;

    private boolean micEnabled = true;

    TextView callingStateText, callTime, profileNickname;
    LinearLayout muteBtn, disconnectButton01, disconnectButton02, connectButton, beforeArea, afterArea;
    TextView tv_mute;

    Vibrator vibrator = null;
    ImageView profileImage;
    public static Activity act;
    String receiveIdx;

    Intent intent;
    UserData userData;

    boolean isSelected = true;

    private final String BROADCAST_MESSAGE = "android.intent.action.PHONE_STATE";
    private BroadcastReceiver mReceiver = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Thread.setDefaultUncaughtExceptionHandler(new UnhandledExceptionHandler(this));

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        setContentView(R.layout.activity_voice_receive);


        SSLConnect ssl = new SSLConnect();
        ssl.postHttps("https://appr.tc", 1000, 1000);

        iceConnected = false;
        signalingParameters = null;

        // view 변수 선언
        callingStateText = (TextView) findViewById(R.id.tv_calling_state);
        callTime = (TextView) findViewById(R.id.tv_call_time);
        profileImage = (ImageView) findViewById(R.id.iv_profile);
        profileNickname = (TextView) findViewById(R.id.tv_nick);

        muteBtn = (LinearLayout) findViewById(R.id.ll_mute);
        tv_mute = (TextView) findViewById(R.id.tv_mute);

        disconnectButton01 = (LinearLayout) findViewById(R.id.ll_disconnect01);
        disconnectButton02 = (LinearLayout) findViewById(R.id.ll_disconnect02);
        connectButton  = (LinearLayout) findViewById(R.id.ll_connect);

        beforeArea = (LinearLayout) findViewById(R.id.ll_before_area);
        afterArea = (LinearLayout) findViewById(R.id.ll_after_area);

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


        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                vibrator.vibrate(
                        new long[]{1500, 1000}
                        , 0);
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

        // Add buttons click events.
        disconnectButton01.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (callingStatus) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            vibrator.cancel();
                        }
                    });

                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            onCallHangUp();
                        }
                    }, 1500);
                }
            }
        });
        disconnectButton02.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (callingStatus) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            vibrator.cancel();
                        }
                    });

                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            onCallHangUp();
                        }
                    }, 1500);
                }
            }
        });

        connectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (callingStatus) {
                    doReceiveVoiceTalk();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            vibrator.cancel();
                        }
                    });
                }
            }
        });

        // Create peer connection client.
        peerConnectionClient = new PeerConnectionClient();

        // Check for mandatory permissions.
        for (String permission : MANDATORY_PERMISSIONS) {
            if (checkCallingOrSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(act, "Permission " + permission + " is not granted", Toast.LENGTH_LONG).show();
                    }
                });
                result_cancel();
                finish();
                return;
            }
        }

        Uri roomUri = intent.getData();
        if (roomUri == null) {
            Log.e("CALL", "Didn't get any URL in intent!");
            result_cancel();
            finish();
            return;
        }


        // Get Intent parameters.
        String roomId = intent.getStringExtra(EXTRA_ROOMID);
        Log.d("CALL", "Room ID: " + roomId);
        if (roomId == null || roomId.length() == 0) {
            Log.e("CALL", "Incorrect room ID in intent!");
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
                this, peerConnectionParameters, VoiceReceiveAct.this);

        startCall();
    }

    public boolean onToggleMic() {
        if (peerConnectionClient != null) {
            micEnabled = !micEnabled;
            peerConnectionClient.setAudioEnabled(micEnabled);
        }
        return micEnabled;
    }


    private void startCall() {
        if (appRtcClient == null) {
            Log.e("CALL", "AppRTC client is not allocated for a call.");
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
        Log.d("CALL", "Starting the audio manager...");
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
        Log.d("CALL", "onAudioManagerDevicesChanged: " + availableDevices + ", "
                + "selected: " + device);
        // TODO(henrika): add callback handler.
    }

    private void disconnect() {
        callingStatus = false;

        if (act != null) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    vibrator.cancel();
                }
            });
        }

        activityRunning = false;

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

    private void result_cancel() {
        if (calledStartedTime != 0) {
            Intent resultIntent = new Intent(VoiceReceiveAct.this, ConnectActivity.class);
            resultIntent.putExtra("result_time", callTime.getText().toString());

            setResult(RESULT_CANCELED, resultIntent);
        } else {
            setResult(RESULT_CANCELED);
        }
    }

    private void result_ok() {
        if (calledStartedTime != 0) {
            Intent resultIntent = new Intent(VoiceReceiveAct.this, ConnectActivity.class);
            resultIntent.putExtra("result_time", callTime.getText().toString());

            setResult(RESULT_OK, resultIntent);
        } else {
            setResult(RESULT_OK);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        peerConnectionClient.startVideoSource();
        activityRunning = true;
    }

    @Override
    public void onStop() {
        super.onStop();
        activityRunning = false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver();
    }

    public void disconnectFromService() {
        disconnect();
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
                        disconnect();
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

    @Override
    protected void onDestroy() {
        unregisterReceiver();
        Thread.setDefaultUncaughtExceptionHandler(null);
        disconnect();
        activityRunning = false;
        super.onDestroy();
    }

    //    @Override
    public void onCallHangUp() {
        disconnect();
    }

    //    @Override
    public void onCallConnect() {
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                peerConnectionClient.createAnswer();
            }
        }, 1000);
    }

    // Should be called from UI thread
    private void callConnected() {
        final long delta = System.currentTimeMillis() - callStartedTimeMs;
        Log.i("CALL", "Call connected: delay=" + delta + "ms");
        if (peerConnectionClient == null || isError) {
            Log.w("CALL", "Call is connected in closed or error state");
            return;
        }
        peerConnectionClient.enableStatsEvents(true, STAT_CALLBACK_PERIOD);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                vibrator.cancel();
            }
        });

        callingStateText.setVisibility(View.GONE);

        // 시간 보이기
        callTime.setVisibility(View.VISIBLE);

        calledStartedTime = System.currentTimeMillis();

        beforeArea.setVisibility(View.GONE);
        afterArea.setVisibility(View.VISIBLE);
    }


    private void onConnectedToRoomInternal(final AppRTCClient.SignalingParameters params) {
        signalingParameters = params;
        callingStatus = true;

        peerConnectionClient.createPeerConnection(
                null, remoteRenderers, null, signalingParameters);

        if (signalingParameters.initiator) {
            peerConnectionClient.createOffer();
        } else {
            if (params.offerSdp != null) {
                peerConnectionClient.setRemoteDescription(params.offerSdp);
                // Create answer. Answer SDP will be sent to offering client in
                // PeerConnectionEvents.onLocalDescription event.
//                peerConnectionClient.createAnswer();
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
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                onConnectedToRoomInternal(params);
            }
        });
    }

    @Override
    public void onRemoteDescription(final SessionDescription sdp) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (peerConnectionClient == null) {
                    Log.e("CALL", "Received remote SDP for non-initilized peer connection.");
                    return;
                }
                peerConnectionClient.setRemoteDescription(sdp);
                if (!signalingParameters.initiator) {
                    // Create answer. Answer SDP will be sent to offering client in
                    // PeerConnectionEvents.onLocalDescription event.
//                    peerConnectionClient.createAnswer();
                }
            }
        });

    }

    @Override
    public void onRemoteIceCandidate(final IceCandidate candidate) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (peerConnectionClient == null) {
                    Log.e("CALL", "Received ICE candidate for a non-initialized peer connection.");
                    return;
                }
                peerConnectionClient.addRemoteIceCandidate(candidate);
            }
        });


    }

    @Override
    public void onRemoteIceCandidatesRemoved(final IceCandidate[] candidates) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (peerConnectionClient == null) {
                    Log.e("CALL", "Received ICE candidate removals for a non-initialized peer connection.");
                    return;
                }
                peerConnectionClient.removeRemoteIceCandidates(candidates);
            }
        });

    }

    @Override
    public void onChannelClose() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                disconnect();
            }
        });
    }

    @Override
    public void onChannelError(final String description) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(act, "통신에러가 발생하였습니다. 잠시 후 다시 시도해주세요.", Toast.LENGTH_SHORT).show();
            }
        });
        result_cancel();
        disconnect();
    }

    // -----Implementation of PeerConnectionClient.PeerConnectionEvents.---------
    // Send local peer connection SDP and ICE candidates to remote party.
    // All callbacks are invoked from peer connection client looper thread and
    // are routed to UI thread.
    @Override
    public void onLocalDescription(final SessionDescription sdp) {
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
                    Log.d("CALL", "Set video maximum bitrate: " + peerConnectionParameters.videoMaxBitrate);
                    peerConnectionClient.setVideoMaxBitrate(peerConnectionParameters.videoMaxBitrate);
                }
            }
        });

    }

    @Override
    public void onIceCandidate(final IceCandidate candidate) {
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
    }

    @Override
    public void onPeerConnectionStatsReady(final StatsReport[] reports) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (!isError && iceConnected) {
                    long originTime = System.currentTimeMillis() - calledStartedTime;
                    callTime.setText(getDate(originTime, "mm:ss"));
                }
            }
        });
    }

    @Override
    public void onPeerConnectionError(final String description) {
        reportError(description);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(act, "통신에러가 발생하였습니다. 잠시 후 다시 시도해주세요.", Toast.LENGTH_SHORT).show();
            }
        });
        disconnect();
        result_cancel();
    }

    public static String getDate(long milliSeconds, String dateFormat) {
        // Create a DateFormatter object for displaying date in specified format.
        SimpleDateFormat formatter = new SimpleDateFormat(dateFormat);

        // Create a calendar object that will convert the date and time value in milliseconds to date.
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(milliSeconds);
        return formatter.format(calendar.getTime());
    }

    private void reportError(final String description) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (!isError) {
                    isError = true;
                    disconnectWithErrorMessage(description);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(act, "통신에러가 발생하였습니다. 잠시 후 다시 시도해주세요.", Toast.LENGTH_SHORT).show();
                        }
                    });
                    disconnect();
                    result_cancel();
                }
            }
        });
    }


    private void disconnectWithErrorMessage(final String errorMessage) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(act, "통신에러가 발생하였습니다. 잠시 후 다시 시도해주세요.", Toast.LENGTH_SHORT).show();
            }
        });
        Log.e("Critical", "Critical error: " + errorMessage);
        if (commandLineRun || !activityRunning) {
            disconnect();
            result_cancel();
        }
    }

    @Override
    public void onBackPressed() {
        if (callingStatus) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    vibrator.cancel();
                }
            });

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
//                            result_cancel();
                    onCallHangUp();
                }
            }, 1500);
        }
//        super.onBackPressed();
//        onCallHangUp();
    }

    public void doReceiveVoiceTalk() {
        onCallConnect();
    }
}