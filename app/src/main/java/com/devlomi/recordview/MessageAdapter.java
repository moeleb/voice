package com.devlomi.recordview;

import android.media.MediaPlayer;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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

    public static class AudioViewHolder extends RecyclerView.ViewHolder {

        private Button playAudioButton;
        private TextView audioDurationText;
        private ProgressBar progressBar;
        private MediaPlayer mediaPlayer;
        private boolean isPlaying = false;
        private boolean isPaused = false;
        private File currentAudioFile;
        private Handler handler = new Handler();

        public AudioViewHolder(View itemView) {
            super(itemView);
            playAudioButton = itemView.findViewById(R.id.play_audio_button);
            audioDurationText = itemView.findViewById(R.id.audio_duration);
            progressBar = itemView.findViewById(R.id.progress_bar);
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

            // Reset UI initially
            updateButtonIcon(false);
            progressBar.setVisibility(View.GONE);  // Ensure progress bar is hidden initially
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

            progressBar.setVisibility(View.VISIBLE);  // Show progress bar when playing
            updateButtonIcon(true);

            // Start progress bar update
            updateProgressBar();
        }

        private void pauseAudio() {
            if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                mediaPlayer.pause();
                isPlaying = false;
                isPaused = true;
                updateButtonIcon(false);
                progressBar.setVisibility(View.GONE);  // Hide progress bar when paused
            }
        }

        private void stopAudio() {
            if (mediaPlayer != null) {
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.stop();
                }
                mediaPlayer.release();
                mediaPlayer = null;
            }

            isPlaying = false;
            isPaused = false;
            progressBar.setVisibility(View.GONE);  // Hide progress bar when stopped
            updateButtonIcon(false);
        }

        private void updateButtonIcon(boolean playing) {
            if (playing) {
                playAudioButton.setBackgroundResource(R.drawable.pause); // your pause drawable
            } else {
                playAudioButton.setBackgroundResource(R.drawable.play); // your play drawable
            }
        }

        private void updateProgressBar() {
            if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                final int currentPosition = mediaPlayer.getCurrentPosition();
                progressBar.setProgress((int) ((currentPosition / (float) mediaPlayer.getDuration()) * 100));
                audioDurationText.setText(getFormattedDuration(currentPosition));

                handler.postDelayed(this::updateProgressBar, 1000); // Update progress every second
            }
        }

        public void releaseMediaPlayer() {
            stopAudio();
        }

        private String getFormattedDuration(long durationInMillis) {
            int seconds = (int) (durationInMillis / 1000);
            int minutes = seconds / 60;
            seconds = seconds % 60;
            return String.format("%02d:%02d", minutes, seconds);
        }
    }
}
