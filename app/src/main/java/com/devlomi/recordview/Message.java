package com.devlomi.recordview;

import java.io.File;

public class Message {

    public enum Type {
        TEXT,
        AUDIO
    }

    private String text;
    private File audioFile;
    private long audioDuration;
    private Type type;

    // Constructor for text message
    public Message(String text, Type type) {
        this.text = text;
        this.type = type;
    }

    // Constructor for audio message
    public Message(File audioFile, Type type, long audioDuration) {
        this.audioFile = audioFile;
        this.type = type;
        this.audioDuration = audioDuration;
    }

    public String getText() {
        return text;
    }

    public File getAudioFile() {
        return audioFile;
    }

    public long getAudioDuration() {
        return audioDuration;
    }

    public Type getType() {
        return type;
    }
}
