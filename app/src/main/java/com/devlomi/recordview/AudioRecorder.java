package com.devlomi.recordview;

import android.media.MediaRecorder;
import java.io.IOException;

public class AudioRecorder {
    private MediaRecorder mediaRecorder;
    private String filePath;
    private boolean isRecording = false;
    private boolean isPaused = false;
    private long pausedTime = 0;

    // Initialize MediaRecorder
    private void initMediaRecorder() {
        mediaRecorder = new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
    }

    // Start the recording
    public void start(String filePath) throws IOException {
        if (mediaRecorder == null) {
            initMediaRecorder();
        }

        this.filePath = filePath;
        mediaRecorder.setOutputFile(filePath);
        mediaRecorder.prepare();
        mediaRecorder.start();
        isRecording = true;
        isPaused = false;
    }

    // Stop the recording
    public void stop() {
        try {
            if (mediaRecorder != null && isRecording) {
                mediaRecorder.stop();
                destroyMediaRecorder();
                isRecording = false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Pause the recording
    public void pause() {
        if (mediaRecorder != null && isRecording) {
            // Save the current time when paused
            pausedTime = System.currentTimeMillis();
            mediaRecorder.stop();
            isPaused = true;
        }
    }

    // Resume the recording from the last pause point
    public void resume() throws IOException {
        if (mediaRecorder == null) {
            initMediaRecorder();
        }

        if (isPaused) {
            mediaRecorder.setOutputFile(filePath); // Output to the same file
            mediaRecorder.prepare();
            mediaRecorder.start();
            isRecording = true;
            isPaused = false;
        }
    }

    // Destroy the MediaRecorder instance
    private void destroyMediaRecorder() {
        if (mediaRecorder != null) {
            mediaRecorder.release();
            mediaRecorder = null;
        }
    }

    // Get the current amplitude
    public int getMaxAmplitude() {
        if (mediaRecorder != null) {
            try {
                return mediaRecorder.getMaxAmplitude();
            } catch (IllegalStateException e) {
                e.printStackTrace();
            }
        }
        return 0;
    }

    public boolean isRecording() {
        return isRecording;
    }

    public boolean isPaused() {
        return isPaused;
    }

    public long getPausedTime() {
        return pausedTime;
    }
}
