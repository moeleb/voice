package com.devlomi.recordview;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;

import static com.devlomi.recordview.AppConfig.amplitudeArrayList;

import com.visualizer.amplitude.AudioRecordView;

import android.Manifest;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.devlomi.record_view.OnRecordClickListener;
import com.devlomi.record_view.OnRecordListener;
import com.devlomi.record_view.RecordButton;
import com.devlomi.record_view.RecordPermissionHandler;
import com.devlomi.record_view.RecordView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private MessageAdapter messageAdapter;
    private List<Message> messages = new ArrayList<>();
    private EditText editTextMessage;
    private RecordView recordView;
    private RecordButton recordButton;
    private ImageButton sendButton;
    private File recordFile;
    private AudioRecorder audioRecorder;
    private AudioRecordView audioRecordView;
    private android.os.Handler amplitudeHandler = new android.os.Handler();
    private Runnable updateAmplitudeTask;
    private boolean isRecording = false;
    private WaveformView waveformView;

    // New variable to store the current amplitude
    private int currentAmplitude = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.messagesRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        messageAdapter = new MessageAdapter(messages);
        recyclerView.setAdapter(messageAdapter);

        editTextMessage = findViewById(R.id.edit_text_message);
        recordView = findViewById(R.id.record_view);
        recordButton = findViewById(R.id.record_button);
        sendButton = findViewById(R.id.send_button);
        audioRecordView = findViewById(R.id.audioRecordView);
        audioRecorder = new AudioRecorder();

        setupRecordView();
        setupEditText();

        sendButton.setOnClickListener(v -> sendTextMessage());
    }

    private void setupRecordView() {
        recordView.setLockEnabled(true);
        recordView.setRecordLockImageView(findViewById(R.id.record_lock));
        recordButton.setRecordView(recordView);

        recordButton.setOnRecordClickListener(v -> {
            Toast.makeText(MainActivity.this, "RECORD BUTTON CLICKED", Toast.LENGTH_SHORT).show();
            Log.d("RecordButton", "RECORD BUTTON CLICKED");
        });

        recordView.setOnRecordListener(new OnRecordListener() {
            @Override
            public void onStart() {
                isRecording = true;
                recordFile = new File(getFilesDir(), UUID.randomUUID().toString() + ".3gp");
                try {
                    audioRecorder.start(recordFile.getPath());

                    if (audioRecordView != null) {
                        audioRecordView.setVisibility(View.VISIBLE);
                        startUpdatingAmplitude();
                    } else {
                        Log.e("MainActivity", "AudioRecordView is null");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Log.d("RecordView", "onStart");
            }

            @Override
            public void onCancel() {
                isRecording = false;
                stopRecording(true);
                audioRecordView.setVisibility(View.GONE);
            }

            @Override
            public void onFinish(long recordTime, boolean limitReached) {
                isRecording = false;
                stopRecording(false);
                audioRecordView.setVisibility(View.GONE);
                audioRecordView.recreate();

                File savedFile = saveRecordingToDirectory(recordFile);
                long duration = getAudioDuration(savedFile);

                if (savedFile.exists()) {
                    messages.add(new Message(savedFile, Message.Type.AUDIO, duration));
                    messageAdapter.notifyDataSetChanged();

                    Toast.makeText(MainActivity.this, "Recorded: " + savedFile.getPath(), Toast.LENGTH_SHORT).show();
                    Log.d("RecordingFinish", "File saved at: " + savedFile.getPath());
                } else {
                    Toast.makeText(MainActivity.this, "Failed to save audio!", Toast.LENGTH_SHORT).show();
                    Log.e("RecordingFinish", "File not found after saving!");
                }
            }

            @Override
            public void onLessThanSecond() {
                stopRecording(true);
                audioRecordView.setVisibility(View.GONE);
            }

            @Override
            public void onLock() {
                Log.d("RecordView", "onLock");
            }
        });

        recordView.setRecordPermissionHandler(() -> {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return true;
            boolean granted = ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PERMISSION_GRANTED;
            if (!granted) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, 0);
            }
            return granted;
        });
    }


    private void startUpdatingAmplitude() {
        updateAmplitudeTask = new Runnable() {
            @Override
            public void run() {
                if (audioRecorder != null && isRecording) {
                    currentAmplitude = audioRecorder.getMaxAmplitude();
                    amplitudeArrayList.add(currentAmplitude);
                    Log.d("Amplitude", "Current amplitude: " + amplitudeArrayList);

                    audioRecordView.update(currentAmplitude);

//                    if (messageAdapter != null) {
//                        messageAdapter.updateAmplitude(currentAmplitude);
//                    }

                    amplitudeHandler.postDelayed(this, 100);
                }
            }
        };
        amplitudeHandler.post(updateAmplitudeTask);
    }

    private void setupEditText() {
        editTextMessage.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                boolean isTyping = s.length() > 0;
                sendButton.setVisibility(isTyping ? View.VISIBLE : View.GONE);
                recordButton.setVisibility(isTyping ? View.GONE : View.VISIBLE);
            }
        });
    }

    private void sendTextMessage() {
        String text = editTextMessage.getText().toString().trim();
        if (!text.isEmpty()) {
            messages.add(new Message(text, Message.Type.TEXT));
            messageAdapter.notifyDataSetChanged();
            editTextMessage.setText("");
        }
    }

    private void stopRecording(boolean deleteFile) {
        audioRecorder.stop();
        audioRecordView.recreate();

        if (recordFile != null && recordFile.exists() && deleteFile) {
            recordFile.delete();
            Log.d("RecordingFinish", "Recording canceled and file deleted");
        }
    }

    private File saveRecordingToDirectory(File sourceFile) {
        File destDir = getRecordingDirectory();
        File destFile = new File(destDir, UUID.randomUUID().toString() + ".3gp");

        if (!sourceFile.exists()) {
            Log.e("RecordingFinish", "Source file does not exist!");
            return sourceFile;
        }

        boolean moved = sourceFile.renameTo(destFile);
        if (!moved) {
            try (FileInputStream in = new FileInputStream(sourceFile);
                 FileOutputStream out = new FileOutputStream(destFile)) {

                byte[] buffer = new byte[1024];
                int len;
                while ((len = in.read(buffer)) > 0) {
                    out.write(buffer, 0, len);
                }
                sourceFile.delete();
                Log.d("RecordingFinish", "File copied to: " + destFile.getPath());

            } catch (IOException e) {
                Log.e("RecordingFinish", "Failed to copy file: " + e.getMessage());
            }
        } else {
            Log.d("RecordingFinish", "File moved to: " + destFile.getPath());
        }

        return destFile;
    }

    private File getRecordingDirectory() {
        File dir = new File(getExternalFilesDir(null), "Test Records");
        if (!dir.exists()) dir.mkdirs();
        return dir;
    }

    private long getAudioDuration(File audioFile) {
        long duration = 0;
        try {
            MediaPlayer player = new MediaPlayer();
            player.setDataSource(audioFile.getPath());
            player.prepare();
            duration = player.getDuration();
            player.release();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return duration;
    }

    public int getCurrentAmplitude() {
        return currentAmplitude;
    }
}