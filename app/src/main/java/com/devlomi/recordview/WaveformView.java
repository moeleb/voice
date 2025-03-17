package com.devlomi.recordview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

public class WaveformView extends View {
    private Paint paint;
    private float[] amplitudes = new float[100];

    public WaveformView(Context context, AttributeSet attrs) {
        super(context, attrs);
        paint = new Paint();
        paint.setColor(0xFF00FF00);
        paint.setStrokeWidth(5f);
    }

    public void updateWave(float amplitude) {
        System.arraycopy(amplitudes, 1, amplitudes, 0, amplitudes.length - 1);
        amplitudes[amplitudes.length - 1] = amplitude;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float width = getWidth();
        float height = getHeight();
        float centerY = height / 2;
        float spacing = width / (amplitudes.length + 1);

        for (int i = 0; i < amplitudes.length; i++) {
            float x = i * spacing;
            float y = centerY - amplitudes[i] * centerY;
            canvas.drawLine(x, centerY, x, y, paint);
        }
    }
}
