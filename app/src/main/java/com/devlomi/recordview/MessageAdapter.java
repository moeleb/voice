package com.devlomi.recordview;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.media.MediaPlayer;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_TEXT = 0;
    private static final int TYPE_AUDIO = 1;

    private List<Message> messages;

    public MessageAdapter(List<Message> messages) {
        this.messages = messages;
    }

    @Override
    public int getItemViewType(int position) {
        return messages.get(position).getType() == Message.Type.AUDIO ? TYPE_AUDIO : TYPE_TEXT;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == TYPE_TEXT) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_text_message, parent, false);
            return new TextViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_audio_message, parent, false);
            return new AudioViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        Message message = messages.get(position);

        if (holder instanceof TextViewHolder) {
            ((TextViewHolder) holder).bind(message.getText());
        } else if (holder instanceof AudioViewHolder) {
            ((AudioViewHolder) holder).bind(message.getAudioFile(), message.getAudioDuration());
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    public void updateAmplitude(int amplitude) {


    }

    public static class TextViewHolder extends RecyclerView.ViewHolder {
        private TextView messageText;

        public TextViewHolder(View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.message_text);
        }

        public void bind(String message) {
            messageText.setText(message);
        }
    }

    public class AudioViewHolder extends RecyclerView.ViewHolder {
        private Button playAudioButton;
        private TextView audioDurationText;
        private LinearLayout amplitudeLayout;
        private MediaPlayer mediaPlayer;
        private boolean isPlaying = false;
        private boolean isPaused = false;
        private File currentAudioFile;
        private Handler handler = new Handler();
        private static final int NUM_BARS = 30; // Number of bars to display
        private int progress = 0;
        private int[] amplitudeValues = new int[NUM_BARS]; // Array to store amplitude values

        public AudioViewHolder(View itemView) {
            super(itemView);
            playAudioButton = itemView.findViewById(R.id.play_audio_button);
            audioDurationText = itemView.findViewById(R.id.audio_duration);
            amplitudeLayout = itemView.findViewById(R.id.amplitude_layout);

            // Initialize the bars
            for (int i = 0; i < NUM_BARS; i++) {
                View bar = new View(itemView.getContext());
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, 20); // Adjust height as needed
                params.weight = 1;
                bar.setLayoutParams(params);
                bar.setBackgroundColor(itemView.getContext().getResources().getColor(R.color.white)); // Initial color (color1)
                amplitudeLayout.addView(bar);
            }
        }

        public void bind(File audioFile, long audioDuration) {
            currentAudioFile = audioFile;
            audioDurationText.setText(getFormattedDuration(audioDuration));

            playAudioButton.setOnClickListener(v -> {
                if (isPlaying) {
                    pauseAudio();
                } else {
                    playAudio(currentAudioFile);
                }
            });

            updateButtonIcon(false);
        }

        private void playAudio(File audioFile) {
            if (audioFile == null || !audioFile.exists()) {
                Toast.makeText(itemView.getContext(), "Audio file not found", Toast.LENGTH_SHORT).show();
                return;
            }

            if (mediaPlayer == null) {
                mediaPlayer = new MediaPlayer();
                try {
                    mediaPlayer.setDataSource(audioFile.getPath());
                    mediaPlayer.prepare();
                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(itemView.getContext(), "Failed to play audio", Toast.LENGTH_SHORT).show();
                    return;
                }

                mediaPlayer.setOnCompletionListener(mp -> stopAudio());
            }

            if (isPaused) {
                mediaPlayer.start();
            } else {
                mediaPlayer.start();
            }

            isPlaying = true;
            isPaused = false;

            updateButtonIcon(true);
            updateBars();
        }

        private void updateBars() {
            if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                // Calculate progress and set amplitude values
                final int progress = (int) ((mediaPlayer.getCurrentPosition() * 100) / mediaPlayer.getDuration());
                setAmplitudeValues(progress);

                // Update the bars every 100ms
                handler.postDelayed(this::updateBars, 100);
            }
        }

        private void setAmplitudeValues(int progress) {
            // Calculate the number of filled bars based on the progress
            int filledBars = (int) ((progress / 100.0) * NUM_BARS);

            for (int i = 0; i < NUM_BARS; i++) {
                amplitudeValues[i] = (i < filledBars) ? 1 : 0; // 1 for filled, 0 for empty
            }

            updateBarColors(filledBars);
        }

        private void updateBarColors(int progress) {
            // Calculate how many bars should be filled based on progress
            int filledBars = (int) ((progress / 100.0) * NUM_BARS);

            // Update each bar based on the filledBars count
            for (int i = 0; i < NUM_BARS; i++) {
                View bar = amplitudeLayout.getChildAt(i);

                if (i < filledBars) {
                    // Change color to red for filled bars
                    bar.setBackgroundColor(itemView.getContext().getResources().getColor(R.color.red));
                } else {
                    // Keep the bar white if it hasn't been filled
                    bar.setBackgroundColor(itemView.getContext().getResources().getColor(R.color.white));
                }
            }
        }


        private void updateButtonIcon(boolean playing) {
            if (playing) {
                playAudioButton.setBackgroundResource(R.drawable.pause);
            } else {
                playAudioButton.setBackgroundResource(R.drawable.play);
            }
        }

        private String getFormattedDuration(long durationInMillis) {
            int seconds = (int) (durationInMillis / 1000) % 60;
            int minutes = (int) ((durationInMillis / 1000) / 60) % 60;
            return String.format("%02d:%02d", minutes, seconds);
        }

        private void stopAudio() {
            if (mediaPlayer != null) {
                mediaPlayer.pause();
                mediaPlayer.seekTo(0);
                isPlaying = false;
                updateButtonIcon(false);
            }
        }

        private void pauseAudio() {
            if (mediaPlayer != null) {
                mediaPlayer.pause();
                isPlaying = false;
                isPaused = true;
                updateButtonIcon(false);
            }
        }
    }
}