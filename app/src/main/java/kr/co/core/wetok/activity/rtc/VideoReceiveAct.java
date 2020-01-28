package kr.co.core.wetok.activity.rtc;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Vibrator;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.corertc.coresdk.rtc.AppRTCAudioManager;
import com.corertc.coresdk.rtc.AppRTCClient;
import com.corertc.coresdk.rtc.DirectRTCClient;
import com.corertc.coresdk.rtc.PeerConnectionClient;
import com.corertc.coresdk.rtc.UnhandledExceptionHandler;
import com.corertc.coresdk.rtc.WebSocketRTCClient;

import org.webrtc.Camera1Enumerator;
import org.webrtc.Camera2Enumerator;
import org.webrtc.CameraEnumerator;
import org.webrtc.FileVideoCapturer;
import org.webrtc.IceCandidate;
import org.webrtc.Logging;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.RendererCommon;
import org.webrtc.SessionDescription;
import org.webrtc.StatsReport;
import org.webrtc.SurfaceViewRenderer;
import org.webrtc.VideoCapturer;
import org.webrtc.VideoFileRenderer;
import org.webrtc.VideoFrame;
import org.webrtc.VideoRenderer;
import org.webrtc.VideoSink;

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
import kr.co.core.wetok.server.netUtil.NetUrls;
import kr.co.core.wetok.util.StringUtil;

public class VideoReceiveAct extends BaseAct implements AppRTCClient.SignalingEvents,
        PeerConnectionClient.PeerConnectionEvents {

    private static final String TAG = "CallActivity";

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

    private static final int CAPTURE_PERMISSION_REQUEST_CODE = 1;
    private static final int STAT_CALLBACK_PERIOD = 1000;

    private static class ProxyRenderer implements VideoRenderer.Callbacks {
        private VideoRenderer.Callbacks target;

        @Override
        synchronized public void renderFrame(VideoRenderer.I420Frame frame) {
            if (target == null) {
                VideoRenderer.renderFrameDone(frame);
                return;
            }
            target.renderFrame(frame);
        }

        synchronized public void setTarget(VideoRenderer.Callbacks target) {
            this.target = target;
        }
    }

    private static class ProxyVideoSink implements VideoSink {
        private VideoSink target;

        @Override
        synchronized public void onFrame(VideoFrame frame) {
            if (target == null) {
                return;
            }
            target.onFrame(frame);
        }

        synchronized public void setTarget(VideoSink target) {
            this.target = target;
        }
    }

    private final ProxyRenderer remoteProxyRenderer = new ProxyRenderer();
    private final ProxyVideoSink localProxyVideoSink = new ProxyVideoSink();
    private PeerConnectionClient peerConnectionClient = null;
    private AppRTCClient appRtcClient;
    private AppRTCClient.SignalingParameters signalingParameters;
    private AppRTCAudioManager audioManager = null;
    private SurfaceViewRenderer pipRenderer;
    private SurfaceViewRenderer fullscreenRenderer;
    private SurfaceViewRenderer toggleRenderer;

    private VideoFileRenderer videoFileRenderer;
    private final List<VideoRenderer.Callbacks> remoteRenderers = new ArrayList<>();
    private boolean commandLineRun;
    private AppRTCClient.RoomConnectionParameters roomConnectionParameters;
    private PeerConnectionClient.PeerConnectionParameters peerConnectionParameters;
    private boolean iceConnected;
    private long callStartedTimeMs = 0;
    private boolean micEnabled = true;
    private boolean isSwappedFeeds;

    TextView profileNickname, callingStateText, callTime;
    ImageView profileImage;
    LinearLayout disconnectButton01, disconnectButton02, convertCameraButton, muteBtn, profileArea, connectButton;
    LinearLayout beforeArea, afterArea;
    TextView tv_mute;

    public static Activity act;
    UserData userData;


    Vibrator vibrator = null;
    private long calledStartedTime = 0;
    public boolean isToggleMirror = false;

    private static final int POINT_GIFT = 4;

    private final String BROADCAST_MESSAGE = "android.intent.action.PHONE_STATE";
    private BroadcastReceiver mReceiver = null;

    boolean isSelected = true;
    boolean callingState = false;

    Intent intent;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Thread.setDefaultUncaughtExceptionHandler(new UnhandledExceptionHandler(this));

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        getWindow().getDecorView().setSystemUiVisibility(getSystemUiVisibility());
        setContentView(R.layout.activity_video_receive);

        act = this;
//        SSLConnect ssl = new SSLConnect();
//        ssl.postHttps("https://appr.tc", 1000, 1000);

        iceConnected = false;
        signalingParameters = null;

        pipRenderer = findViewById(R.id.pip_video_view);
        fullscreenRenderer = findViewById(R.id.fullscreen_video_view);

        profileNickname = (TextView) findViewById(R.id.tv_nick);
        profileImage = (ImageView) findViewById(R.id.iv_profile);

        disconnectButton01 = (LinearLayout) findViewById(R.id.ll_disconnect01);
        disconnectButton02 = (LinearLayout) findViewById(R.id.ll_disconnect02);
        profileArea = (LinearLayout) findViewById(R.id.ll_profile_area);
        connectButton = (LinearLayout) findViewById(R.id.ll_connect);
        beforeArea = (LinearLayout) findViewById(R.id.ll_before_area);
        afterArea = (LinearLayout) findViewById(R.id.ll_after_area);
        convertCameraButton = (LinearLayout) findViewById(R.id.ll_convert_camera);
        muteBtn = (LinearLayout) findViewById(R.id.ll_mute);
        tv_mute = (TextView) findViewById(R.id.tv_mute);
        callingStateText = (TextView) findViewById(R.id.tv_calling_state);
        callTime = (TextView) findViewById(R.id.tv_call_time);

        intent = getIntent();
        userData = (UserData) intent.getSerializableExtra("userData");

        // 상대 데이터 세팅
        profileNickname.setText(userData.getName());
        Glide.with(act)
                .load(NetUrls.DOMAIN + userData.getProfile_img())
                .transform(new RoundedCorners(27))
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(profileImage);


        // 진동끄기
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                vibrator.vibrate(
                        new long[]{1500, 1000}
                        , 0);
            }
        });


        // 카메라 전환 버튼
        convertCameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onCameraSwitch();
            }
        });

        // 통화끊기
        disconnectButton01.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isSelected) {
                    isSelected = false;
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
            public void onClick(View v) {
                if (isSelected) {
                    isSelected = false;
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            onCallHangUp();
                        }
                    }, 1500);
                }
            }
        });

        // 통화 받기
        connectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!callingState) {
                    callingState = true;
                    onCallConnect();
                }
            }
        });

        //음소거
        muteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean enabled = onToggleMic();
                muteBtn.setAlpha(enabled ? 1.0f : 0.3f);
                tv_mute.setSelected(!enabled);
            }
        });


        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggleCallControlFragmentVisibility();
            }
        };

        // Swap feeds on pip view click.
        pipRenderer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setSwappedFeeds(!isSwappedFeeds);
            }
        });

        fullscreenRenderer.setOnClickListener(listener);


        remoteRenderers.add(remoteProxyRenderer);

        final Intent intent = getIntent();

        // Create peer connection client.
        peerConnectionClient = new PeerConnectionClient();


        // Create video renderers.
        pipRenderer.init(peerConnectionClient.getRenderContext(), null);
        pipRenderer.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FIT);

        fullscreenRenderer.init(peerConnectionClient.getRenderContext(), null);
        fullscreenRenderer.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FILL);

        pipRenderer.setZOrderMediaOverlay(true);
        pipRenderer.setEnableHardwareScaler(true /* enabled */);
        fullscreenRenderer.setEnableHardwareScaler(true /* enabled */);
        // Start with local feed in fullscreen and swap it to the pip when the call is connected.
        setSwappedFeeds(true /* isSwappedFeeds */);


        Uri roomUri = intent.getData();
        String roomId = intent.getStringExtra(EXTRA_ROOMID);
        Log.i(StringUtil.TAG_RTC, "roomId(VideoReceive): " + roomId);


        boolean loopback = intent.getBooleanExtra(EXTRA_LOOPBACK, false);
        boolean tracing = intent.getBooleanExtra(EXTRA_TRACING, false);

        int videoWidth = intent.getIntExtra(EXTRA_VIDEO_WIDTH, 0);
        int videoHeight = intent.getIntExtra(EXTRA_VIDEO_HEIGHT, 0);

        PeerConnectionClient.DataChannelParameters dataChannelParameters = null;
        if (intent.getBooleanExtra(EXTRA_DATA_CHANNEL_ENABLED, false)) {
            dataChannelParameters = new PeerConnectionClient.DataChannelParameters(intent.getBooleanExtra(EXTRA_ORDERED, true),
                    intent.getIntExtra(EXTRA_MAX_RETRANSMITS_MS, -1),
                    intent.getIntExtra(EXTRA_MAX_RETRANSMITS, -1), intent.getStringExtra(EXTRA_PROTOCOL),
                    intent.getBooleanExtra(EXTRA_NEGOTIATED, false), intent.getIntExtra(EXTRA_ID, -1));
        }
        peerConnectionParameters =
                new PeerConnectionClient.PeerConnectionParameters(intent.getBooleanExtra(EXTRA_VIDEO_CALL, true), loopback,
                        tracing, videoWidth, videoHeight, intent.getIntExtra(EXTRA_VIDEO_FPS, 0),
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
                this, peerConnectionParameters, VideoReceiveAct.this);

        startCall();
    }


    @Override
    public void onBackPressed() {
        if (isSelected) {
            Log.i(StringUtil.TAG_RTC, "액티비티 호출");
            isSelected = false;
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    onCallHangUp();
                }
            }, 1500);
        }
    }



    public void onCameraSwitch() {
        if (peerConnectionClient != null) {
            peerConnectionClient.switchCamera();
            toggleRenderer.setMirror(isToggleMirror);
            isToggleMirror = !isToggleMirror;
        }
    }

    private void startCall() {
        if (appRtcClient == null) {
            Log.e(TAG, "AppRTC client is not allocated for a call.");
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
        Log.d(TAG, "Starting the audio manager...");
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


    @TargetApi(19)
    private static int getSystemUiVisibility() {
        int flags = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            flags |= View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        }
        return flags;
    }

    private boolean useCamera2() {
        return Camera2Enumerator.isSupported(this) && getIntent().getBooleanExtra(EXTRA_CAMERA2, true);
    }

    private boolean captureToTexture() {
        return getIntent().getBooleanExtra(EXTRA_CAPTURETOTEXTURE_ENABLED, false);
    }

    private VideoCapturer createCameraCapturer(CameraEnumerator enumerator) {
        final String[] deviceNames = enumerator.getDeviceNames();
        // First, try to find front facing camera
        for (String deviceName : deviceNames) {
            if (enumerator.isFrontFacing(deviceName)) {
                Logging.d(TAG, "Creating front facing camera capturer.");
                VideoCapturer videoCapturer = enumerator.createCapturer(deviceName, null);

                if (videoCapturer != null) {
                    return videoCapturer;
                }
            }
        }

        // Front facing camera not found, try something else
        Logging.d(TAG, "Looking for other cameras.");
        for (String deviceName : deviceNames) {
            if (!enumerator.isFrontFacing(deviceName)) {
                Logging.d(TAG, "Creating other camera capturer.");
                VideoCapturer videoCapturer = enumerator.createCapturer(deviceName, null);

                if (videoCapturer != null) {
                    return videoCapturer;
                }
            }
        }
        return null;
    }

    @Override
    public void onStop() {
        super.onStop();
        if (peerConnectionClient != null) {
            peerConnectionClient.stopVideoSource();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if (peerConnectionClient != null) {
            peerConnectionClient.startVideoSource();
        }
    }

    @Override
    protected void onDestroy() {
        Thread.setDefaultUncaughtExceptionHandler(null);
        if (act != null) {
            disconnect();
        }

        super.onDestroy();
        unregisterReceiver();
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

        mReceiver = new BroadcastReceiver() {
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
        act.registerReceiver(mReceiver, theFilter);

    }

    private void unregisterReceiver() {
        if (mReceiver != null) {
            Log.i("TEST_HOME", "unregisterReceiver");
            unregisterReceiver(mReceiver);
            mReceiver = null;
        }
    }

    public void onCallHangUp() {
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                disconnect();
            }
        }, 500);
    }

    public void onCallConnect() {
        act.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                vibrator.cancel();
            }
        });

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                peerConnectionClient.createAnswer();
            }
        }, 1000);
    }

    public boolean onToggleMic() {
        if (peerConnectionClient != null) {
            micEnabled = !micEnabled;
            peerConnectionClient.setAudioEnabled(micEnabled);
        }
        return micEnabled;
    }

    private void toggleCallControlFragmentVisibility() {
        if (!iceConnected) {
            return;
        }
    }

    // Should be called from UI thread
    private void callConnected() {
        final long delta = System.currentTimeMillis() - callStartedTimeMs;
        Log.i(TAG, "Call connected: delay=" + delta + "ms");
        if (peerConnectionClient == null) {
            Log.w(TAG, "Call is connected in closed or error state");
            return;
        }
        // Enable statistics callback.
        peerConnectionClient.enableStatsEvents(true, STAT_CALLBACK_PERIOD);
        setSwappedFeeds(false /* isSwappedFeeds */);

        callingStateText.setVisibility(View.GONE);

        //시간 세팅
        calledStartedTime = System.currentTimeMillis();
        callTime.setVisibility(View.VISIBLE);

        // 레이아웃 전환
        profileArea.setVisibility(View.GONE);
        beforeArea.setVisibility(View.GONE);
        afterArea.setVisibility(View.VISIBLE);
    }

    // This method is called when the audio manager reports audio device change,
    // e.g. from wired headset to speakerphone.
    private void onAudioManagerDevicesChanged(
            final AppRTCAudioManager.AudioDevice device, final Set<AppRTCAudioManager.AudioDevice> availableDevices) {
        Log.d(TAG, "onAudioManagerDevicesChanged: " + availableDevices + ", "
                + "selected: " + device);
        // TODO(henrika): add callback handler.
    }

    // Disconnect from remote resources, dispose of local resources, and exit.
    private void disconnect() {
        if (act != null) {
            act.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    vibrator.cancel();
                }
            });
        }

        remoteProxyRenderer.setTarget(null);
        localProxyVideoSink.setTarget(null);
        if (appRtcClient != null) {
            appRtcClient.disconnectFromRoom();
            appRtcClient = null;
        }
        if (pipRenderer != null) {
            pipRenderer.release();
            pipRenderer = null;
        }
        if (videoFileRenderer != null) {
            videoFileRenderer.release();
            videoFileRenderer = null;
        }
        if (fullscreenRenderer != null) {
            fullscreenRenderer.release();
            fullscreenRenderer = null;
        }
        if (peerConnectionClient != null) {
            peerConnectionClient.close();
            peerConnectionClient = null;
        }
        if (audioManager != null) {
            audioManager.stop();
            audioManager = null;
        }
        if (iceConnected) {
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
        }, 500);


        Log.d(TAG, "disconnect: ");
    }

    private void result_cancel() {
        if (calledStartedTime != 0) {
            long result_time_long = (System.currentTimeMillis() - calledStartedTime) / 1000;
            double result_multiply = (double) result_time_long / 30;
            String result_point = String.valueOf((int) (result_multiply * 115));
            String result_time = String.valueOf(result_time_long);

            Intent resultIntent = new Intent(VideoReceiveAct.this, ConnectActivity.class);
            resultIntent.putExtra("result_time", result_time);
            resultIntent.putExtra("result_point", result_point);

            setResult(RESULT_CANCELED, resultIntent);
        } else {
            setResult(RESULT_CANCELED);
        }
    }

    private void result_ok() {
        if (calledStartedTime != 0) {
            long result_time_long = (System.currentTimeMillis() - calledStartedTime) / 1000;
            double result_multiply = (double) result_time_long / 30;
            String result_point = String.valueOf((int) (result_multiply * 115));
            String result_time = String.valueOf(result_time_long);

            Intent resultIntent = new Intent(VideoReceiveAct.this, ConnectActivity.class);
            resultIntent.putExtra("result_time", result_time);
            resultIntent.putExtra("result_point", result_point);

            setResult(RESULT_OK, resultIntent);
        } else {
            setResult(RESULT_OK);
        }
    }

    private VideoCapturer createVideoCapturer() {
        final VideoCapturer videoCapturer;
        String videoFileAsCamera = getIntent().getStringExtra(EXTRA_VIDEO_FILE_AS_CAMERA);
        if (videoFileAsCamera != null) {
            videoCapturer = new FileVideoCapturer(videoFileAsCamera);
        } else if (useCamera2()) {
            if (!captureToTexture()) {
                disconnect();
                return null;
            }

            Logging.d(TAG, "Creating capturer using camera2 API.");
            videoCapturer = createCameraCapturer(new Camera2Enumerator(this));
        } else {
            Logging.d(TAG, "Creating capturer using camera1 API.");
            videoCapturer = createCameraCapturer(new Camera1Enumerator(captureToTexture()));
        }
        if (videoCapturer == null) {
            disconnect();
            return null;
        }
        return videoCapturer;
    }

    private void setSwappedFeeds(boolean isSwappedFeeds) {
        Logging.d(TAG, "setSwappedFeeds: " + isSwappedFeeds);
        this.isSwappedFeeds = isSwappedFeeds;
        localProxyVideoSink.setTarget(isSwappedFeeds ? fullscreenRenderer : pipRenderer);
        remoteProxyRenderer.setTarget(isSwappedFeeds ? pipRenderer : fullscreenRenderer);
        fullscreenRenderer.setMirror(isSwappedFeeds);
        pipRenderer.setMirror(!isSwappedFeeds);

        toggleRenderer = isSwappedFeeds ? fullscreenRenderer : pipRenderer;
//        fullscreenRenderer.setMirror(false);
//        pipRenderer.setMirror(false);
    }

    // -----Implementation of AppRTCClient.AppRTCSignalingEvents ---------------
    // All callbacks are invoked from websocket signaling looper thread and
    // are routed to UI thread.
    private void onConnectedToRoomInternal(final AppRTCClient.SignalingParameters params) {
        final long delta = System.currentTimeMillis() - callStartedTimeMs;

        signalingParameters = params;
        VideoCapturer videoCapturer = null;

        if (peerConnectionParameters.videoCallEnabled) {
            videoCapturer = createVideoCapturer();
        }

        peerConnectionClient.createPeerConnection(
                localProxyVideoSink, remoteRenderers, videoCapturer, signalingParameters);

        if (signalingParameters.initiator) {
            // Create offer. Offer SDP will be sent to answering client in
            // PeerConnectionEvents.onLocalDescription event.
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
                    Log.e(TAG, "Received remote SDP for non-initilized peer connection.");
                    return;
                }
                peerConnectionClient.setRemoteDescription(sdp);
                if (!signalingParameters.initiator) {
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
                    Log.e(TAG, "Received ICE candidate for a non-initialized peer connection.");
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
                    Log.e(TAG, "Received ICE candidate removals for a non-initialized peer connection.");
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
        disconnect();
    }

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
                    Log.d(TAG, "Set video maximum bitrate: " + peerConnectionParameters.videoMaxBitrate);
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
        final long delta = System.currentTimeMillis() - callStartedTimeMs;
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
    public void disconnectFromService() {
        disconnect();
    }

    @Override
    public void onPeerConnectionClosed() {
    }

    @Override
    public void onPeerConnectionStatsReady(final StatsReport[] reports) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (iceConnected) {
                    if (act != null) {
                        long originTime = System.currentTimeMillis() - calledStartedTime;
                        callTime.setText(getDate(originTime, "mm:ss"));
                    }
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
        disconnect();
    }
}