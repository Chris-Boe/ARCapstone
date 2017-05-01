package plu.capstone.UI;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextPaint;
import android.text.style.ClickableSpan;
import android.text.style.ReplacementSpan;
import android.view.View;

/**
 * Created by playt on 4/30/2017.
 */



public class ButtonSpan extends ReplacementSpan {

    private static final float PADDING = 50.0f;
    private RectF rect;
    private int backgroundColor;
    private int foregroundColor;

    public ButtonSpan(int bg, int fg) {
        this.rect = new RectF();
        this.backgroundColor = bg;
        this.foregroundColor = fg;
    }

    @Override
    public int getSize(@NonNull Paint paint, CharSequence text, @IntRange(from = 0) int start, @IntRange(from = 0) int end, @Nullable Paint.FontMetricsInt fm) {
       return Math.round(paint.measureText(text, start, end) + PADDING);
    }

    @Override
    public void draw(@NonNull Canvas canvas, CharSequence text, @IntRange(from = 0) int start, @IntRange(from = 0) int end, float x, int top, int y, int bottom, @NonNull Paint paint) {
        // Background
        rect.set(x, top, x + paint.measureText(text, start, end) + PADDING, bottom);
        paint.setColor(backgroundColor);
        canvas.drawRect(rect, paint);

        // Text
        paint.setColor(foregroundColor);
        int xPos = Math.round(x + (PADDING / 2));
        int yPos = (int) ((canvas.getHeight() / 2) - ((paint.descent() + paint.ascent()) / 2)) ;
        canvas.drawText(text, start, end, xPos, yPos, paint);
    }
}

