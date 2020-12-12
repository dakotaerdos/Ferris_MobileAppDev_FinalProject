package com.zybooks.finalproject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private static final String LOG_TAG = "AudioRecordTest";
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 0;
    private static final int REQUEST_WRITE_PERMISSION = 1;

    private String fileName = null;
    private File pubDocDir;
    private File pubAudioDir;
    private File outputFile;
    private boolean isFileCreated;

    private MediaRecorder recorder = null;
    private MediaPlayer player = null;

    private Button btnRecord = null;
    private Button btnPlay = null;

    boolean mStartRecording = false;
    boolean mStartPlaying = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fileName = "/testAudioRecord.mp4";

        btnRecord = findViewById(R.id.btnRecord);
        btnRecord.setText(R.string.start_recording);
        btnRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mStartRecording = !mStartRecording;

                if (mStartRecording) {
                    btnRecord.setText(R.string.stop_recording);
                } else {
                    btnRecord.setText(R.string.start_recording);
                    btnPlay.setClickable(true);
                }

                onRecord(mStartRecording);

            }
        });

        btnPlay = findViewById(R.id.btnPlay);
        btnPlay.setText(R.string.start_playing);
        btnPlay.setClickable(false);
        btnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if (outputFile.exists()) {
                        mStartPlaying = !mStartPlaying;

                        if (mStartPlaying) {
                            btnPlay.setText(R.string.stop_playing);
                        } else {
                            btnPlay.setText(R.string.start_playing);
                        }

                        onPlay(mStartPlaying);
                    }
                } catch (NullPointerException ex) {
                    Log.e("btnPlay.OnClick()", "onClick: File Error", ex);
                }
            }
        });
    }

    private boolean hasWriteFilePermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_WRITE_PERMISSION);
            return false;
        }
        return true;
    }

    private boolean hasRecordPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.RECORD_AUDIO}, REQUEST_RECORD_AUDIO_PERMISSION);
            return false;
        }
        return true;
    }

    private boolean hasFile() {
        isFileCreated = false;
        if (hasWriteFilePermission()) {
            String folder = "AudioRecordings";
            String state = Environment.getExternalStorageState();
            if (Environment.MEDIA_MOUNTED.equals(state)) {

                /*pubDocDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);

                if (!pubDocDir.exists()) {
                    pubDocDir.mkdir();
                }

                pubAudioDir = new File(pubDocDir, folder);
                if (!pubAudioDir.exists()) {
                    pubAudioDir.mkdir();
                }*/

                outputFile = new File(getApplicationContext().getFilesDir(), fileName);

                if (!outputFile.exists()) {
                    try {
                        isFileCreated = outputFile.createNewFile();
                    } catch (IOException e) {
                        Log.e("hasFile()", "IO Exception Occurred");
                    } catch (SecurityException e) {
                        Log.e("hasFile()", "Security Exception Occurred");
                    }
                } else {
                    isFileCreated = true;
                }
            }
        }
        return isFileCreated;
    }

    private void onRecord(boolean start) {
        if (start) {
            startRecording();
        } else {
            stopRecording();
        }
    }

    private void startRecording() {
        if (hasRecordPermission()) {
            if (hasFile()) {
                recorder = new MediaRecorder();
                recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
                recorder.setOutputFile(outputFile);
                Log.d("startRecording()", "startRecording: " + outputFile.getAbsolutePath());
                recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);

                try {
                    recorder.prepare();
                    recorder.start();

                } catch (Exception e) {
                    Log.e(LOG_TAG, "prepare() failed");
                    Log.e(LOG_TAG, e.toString());
                }
            }
        }
    }

    private void stopRecording() {
        try {
            recorder.stop();
            recorder.release();
            recorder = null;
        } catch (Exception e) {
            Log.e("stopRecording()", "stopRecording: ", e);
        }

        // TODO: Add code to change file name here on stop
    }

    private void onPlay(boolean start) {
        if (start) {
            startPlaying();
        } else {
            stopPlaying();
        }
    }

    private void startPlaying() {
        player = new MediaPlayer();
        try {
            player.setDataSource(outputFile.getAbsolutePath());
            player.prepare();
            player.start();
        } catch (Exception e) {
            Log.e(LOG_TAG, "prepare() failed");
        }
    }

    private void stopPlaying() {
        player.release();
        player = null;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_RECORD_AUDIO_PERMISSION:
                if (grantResults.length > 0) {
                    boolean permissionToRecord = grantResults[0] == PackageManager.PERMISSION_GRANTED;

                    if (permissionToRecord) {
                        Toast.makeText(getApplicationContext(), "Permission Granted", Toast.LENGTH_LONG).show();
                        Log.d("onRequestPermissionResult", "onRequestPermissionsResult: Record Permission Granted");
                    } else {
                        Toast.makeText(getApplicationContext(), "Permission Denied", Toast.LENGTH_LONG).show();
                        Log.d("onRequestPermissionResult", "onRequestPermissionsResult: Record Permission Denied");
                    }
                }
                break;
            case REQUEST_WRITE_PERMISSION:
                if (grantResults.length > 1) {
                    boolean permissionToWrite = grantResults[1] == PackageManager.PERMISSION_GRANTED;

                    if (permissionToWrite) {
                        Toast.makeText(getApplicationContext(), "Write Permission Granted", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(getApplicationContext(), "Write Permission Denied", Toast.LENGTH_LONG).show();
                    }
                }
        }
    }
}