package com.devlomi.recordview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import java.util.List;

public class WaveformView extends View {
    private List<Integer> amplitudes;
    private Paint playedPaint, unplayedPaint;
    private int progress = 0;

    public WaveformView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        playedPaint = new Paint();
        playedPaint.setColor(0xFF4CAF50);

        unplayedPaint = new Paint();
        unplayedPaint.setColor(0xFFB0BEC5);
    }

    public void setAmplitudes(List<Integer> amplitudes) {
        this.amplitudes = amplitudes;
        invalidate();
    }

    public void setProgress(int progress) {
        this.progress = progress;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (amplitudes == null || amplitudes.isEmpty()) return;

        int width = getWidth();
        int height = getHeight();
        int barWidth = width / amplitudes.size();

        for (int i = 0; i < amplitudes.size(); i++) {
            int ampHeight = (int) ((amplitudes.get(i) / 32768.0) * height);

            Paint paint = (i * 100 / amplitudes.size() <= progress) ? playedPaint : unplayedPaint;
            canvas.drawRect(i * barWidth, height - ampHeight, (i + 1) * barWidth, height, paint);
        }
    }
}
