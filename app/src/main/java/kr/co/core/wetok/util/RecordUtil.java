package kr.co.core.wetok.util;

import android.app.Activity;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.CountDownTimer;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.IOException;

public class RecordUtil {
    private static final String TAG = "TEST_RECORED";
    private static int MAX_RECORD_TIME = 6000000;

    private MediaPlayer player;
    private MediaRecorder recorder;
    private Activity act;

    private String recordFile;

    private int position = 0;

    RecordStateListener recordListener;
    MediaStateListener mediaStateListener;

    private String mediaState = "default";

    private int startTime;
    private int endTime;
    private CountDownTimer timer;


    public interface MediaStateListener {
        void afterStart();

        void afterPause();

        void afterResume();

        void afterStop();

        void afterClose();

        void synchronizeTime(String time);
    }

    public interface RecordStateListener {
        void afterStartRecord();

        void afterStopRecord(String recordFile);

        void afterCancelRecord(boolean isNormal);
    }

    public RecordUtil(Activity act, RecordStateListener recordListener, MediaStateListener mediaStateListener) {
        this.act = act;
        this.recordListener = recordListener;
        this.mediaStateListener = mediaStateListener;

        File sdcard = Environment.getExternalStorageDirectory();
        File file = new File(sdcard, "recorded.mp4");
        recordFile = file.getAbsolutePath();
    }

    /* 녹음 */
    public void startRecording() {
        recorder = new MediaRecorder();
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        recorder.setOutputFile(recordFile);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        try {
            recorder.prepare();
            recorder.start();

            recordListener.afterStartRecord();
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "prepare() failed in record");
        }
    }

    public void stopRecording() {
        if (null != recorder) {
            recorder.stop();
            recorder.release();
            recorder = null;

            recordListener.afterStopRecord(recordFile);
        }
    }

    public void cancelRecording(boolean isNormal) {
        if (null != recorder) {

            recorder.release();
            recorder = null;

            recordListener.afterCancelRecord(isNormal);
        }
    }


    /* 녹음 재생 */
    public void startPlaying(String filePath) {
        player = new MediaPlayer();
        player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                closePlaying();
            }
        });

        try {
            player.setDataSource(filePath);
            player.prepare();
            player.start();

//            startTime = 0;
//            endTime = player.getDuration();
//            setTimer();

            mediaState = "playing";
            mediaStateListener.afterStart();

            Log.e(TAG, "audio play -> start");
        } catch (IOException e) {
            Log.e(TAG, "prepare() failed in play");
        }
    }

    public void pausePlaying() {
        if (player != null) {
            position = player.getCurrentPosition();
            player.pause();
//            startTime = position;
//            cancelTimer();

            mediaState = "pause";
            mediaStateListener.afterPause();
            Log.e(TAG, "audio play -> pause");
        }
    }

    public void resumePlaying() {
        if (player != null && !player.isPlaying()) {
            player.seekTo(position);
            player.start();
//            setTimer();

            mediaState = "resume";
            mediaStateListener.afterResume();

            Log.e(TAG, "audio play -> resume");
        }
    }


    public void stopPlaying() {
        if (player != null && player.isPlaying()) {
            player.stop();

            mediaState = "stop";
            mediaStateListener.afterStop();
            Log.e(TAG, "audio play -> stop");
        }
    }

    public void closePlaying() {
        if (player != null) {
            player.release();
            player = null;

            mediaState = "close";
            mediaStateListener.afterClose();
            Log.e(TAG, "audio play -> close");
        }
    }

    public String getMediaState() {
        return mediaState;
    }

//    private void setTimer() {
//        timer = new CountDownTimer(endTime - startTime, 100) {
//            @Override
//            public void onTick(long millisUntilFinished) {
//                Log.e(TAG, "onTick: " + millisUntilFinished);
//                startTime = (int) millisUntilFinished;
//
//                mediaStateListener.synchronizeTime(Common.converTimeSimpleLong(millisUntilFinished));
//            }
//
//            @Override
//            public void onFinish() {
//                mediaStateListener.synchronizeTime(Common.converTimeSimpleLong(endTime));
//            }
//        }.start();
//    }

//    private void cancelTimer() {
//        timer.cancel();
//    }
}
