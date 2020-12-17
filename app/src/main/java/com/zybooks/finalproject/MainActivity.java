/*
Dakota Erdos
Travis Bussler
Final Project Fall 2020
Mobile Application Development
 */

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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private static final String LOG_TAG = "AudioRecordTest";
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 0;
    private static final int REQUEST_WRITE_PERMISSION = 1;

    private String fileName = null;
    // Lower Api Level Variables - Not currently being used
    // private File pubDocDir;
    // private File pubAudioDir;
    private File currentOutputFile;
    private boolean isFileCreated;
    private DateFormat dateFormatter;


    private MediaRecorder recorder = null;
    private MediaPlayer player = null;

    private Button btnRecord = null;
    private Button btnPlay = null;
    private Button btnDelete = null;

    private Spinner spinnerAudioSelect = null;
    private ArrayAdapter<String> adapter;
    private ArrayList<String> fileNames;

    private boolean mStartRecording = false;
    private boolean mStartPlaying = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dateFormatter = new SimpleDateFormat("yyyy-MM-dd-HH_mm_ss", Locale.US);

        spinnerAudioSelect = findViewById(R.id.SpinAudioSelect);

        File file = new File(getApplicationContext().getFilesDir().getAbsolutePath());
        File[] filesList = file.listFiles();

        fileNames = new ArrayList<>();

        assert filesList != null;
        if (filesList.length != 0) {
            for (File name : filesList) {
                fileNames.add(name.getName());
            }
        }

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, fileNames);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerAudioSelect.setAdapter(adapter);
        spinnerAudioSelect.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                fileName = parent.getItemAtPosition(position).toString();
                currentOutputFile = new File(getApplicationContext().getFilesDir(), fileName);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


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
                    if (currentOutputFile.exists()) {
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

        btnDelete = findViewById(R.id.btnDelete);
        btnDelete.setText("Delete Recording");
        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentOutputFile.delete();

                File file = new File(getApplicationContext().getFilesDir().getAbsolutePath());
                File[] filesList = file.listFiles();

                fileNames = new ArrayList<>();

                assert filesList != null;
                if (filesList.length != 0) {
                    for (File name : filesList) {
                        fileNames.add(name.getName());
                    }
                }

                UpdateList();
            }
        });
    }

    private void UpdateList() {
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, fileNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerAudioSelect.setAdapter(adapter);
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
            String state = Environment.getExternalStorageState();
            if (Environment.MEDIA_MOUNTED.equals(state)) {

                /*
                String folder = "AudioRecordings";

                pubDocDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);

                if (!pubDocDir.exists()) {
                    pubDocDir.mkdir();
                }

                pubAudioDir = new File(pubDocDir, folder);
                if (!pubAudioDir.exists()) {
                    pubAudioDir.mkdir();
                }
                */

                fileName = "AudioRecording_" + dateFormatter.format(Calendar.getInstance().getTime()) + ".mp4";

                currentOutputFile = new File(getApplicationContext().getFilesDir(), fileName);

                if (!currentOutputFile.exists()) {
                    try {
                        isFileCreated = currentOutputFile.createNewFile();
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
                recorder.setOutputFile(currentOutputFile);
                Log.d("startRecording()", "startRecording: " + currentOutputFile.getAbsolutePath());
                recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);

                try {
                    btnPlay.setClickable(false);
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
            btnPlay.setClickable(true);
            recorder.stop();
            recorder.release();
            recorder = null;
        } catch (Exception e) {
            Log.e("stopRecording()", "stopRecording: ", e);
        }

        fileNames.add(currentOutputFile.getName());
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, fileNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerAudioSelect.setAdapter(adapter);

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
            btnRecord.setClickable(false);
            player.setDataSource(currentOutputFile.getAbsolutePath());
            player.prepare();
            player.start();
        } catch (Exception e) {
            Log.e(LOG_TAG, "prepare() failed" + e.toString());

        }
    }

    private void stopPlaying() {
        btnRecord.setClickable(true);
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