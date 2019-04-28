package com.ctchat.sample.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

public class AudioVirtualizerView extends View {
    private byte[] mBytes = null;
    private Paint mForePaint = new Paint();
    private float[] mPoints;
    private Rect mRect = new Rect();
    private int mVisualizerSpectrumNum = 10;

    public AudioVirtualizerView(Context context, AttributeSet attributeSet)
    {
        super(context, attributeSet);
        this.mForePaint.setAntiAlias(true);
        this.mForePaint.setColor(Color.GRAY);
    }

    protected void onDraw(Canvas canvas)
    {
        super.onDraw(canvas);
        if (this.mBytes == null) {
            return;
        }
        if ((this.mPoints == null) || (this.mPoints.length < this.mBytes.length * 4)) {
            this.mPoints = new float[this.mBytes.length * 4];
        }
        this.mRect.set(0, 0, getWidth(), getHeight());

        int boundWidth = this.mRect.width() / this.mVisualizerSpectrumNum;
        int boundHeight = this.mRect.height();

        for (int i = 0; i < this.mVisualizerSpectrumNum; i++){
            if (this.mBytes[i] < 0) {
                this.mBytes[i] = Byte.MAX_VALUE;
            }
            int posx = boundWidth * i + boundWidth / 2;
            this.mPoints[(i * 4)] = posx;
            this.mPoints[(i * 4 + 1)] = boundHeight;
            this.mPoints[(i * 4 + 2)] = posx;
            this.mPoints[(i * 4 + 3)] = (boundHeight - this.mBytes[i]);
        }

        canvas.drawLines(this.mPoints, this.mForePaint);
    }

    public void setSpectrumNum(int num)
    {
        this.mVisualizerSpectrumNum = num;
    }

    public void updateVisualizer(byte[] bytes)
    {
        this.mBytes = bytes;
        this.mForePaint.setStrokeWidth(getWidth() / (this.mVisualizerSpectrumNum * 2));
        invalidate();
    }
}
