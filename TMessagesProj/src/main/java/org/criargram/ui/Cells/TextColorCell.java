/*
 * This is the source code of Telegram for Android v. 5.x.x.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright Nikolai Kudashov, 2013-2018.
 */

package org.criargram.ui.Cells;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.annotation.Keep;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.TextView;

import org.criargram.messenger.AndroidUtilities;
import org.criargram.messenger.LocaleController;
import org.criargram.ui.ActionBar.Theme;
import org.criargram.ui.Components.LayoutHelper;

import java.util.ArrayList;

public class TextColorCell extends FrameLayout {

    private TextView textView;
    private boolean needDivider;
    private int currentColor;
    private float alpha = 1.0f;

    private static Paint colorPaint;

    public final static int colors[] = new int[] {0xfff04444, 0xffff8e01, 0xffffce1f, 0xff79d660, 0xff40edf6, 0xff46beff, 0xffd274f9, 0xffff4f96, 0xffbbbbbb};
    public final static int colorsToSave[] = new int[] {0xffff0000, 0xffff8e01, 0xffffff00, 0xff00ff00, 0xff00ffff, 0xff0000ff, 0xffd274f9, 0xffff00ff, 0xffffffff};

    public TextColorCell(Context context) {
        super(context);

        if (colorPaint == null) {
            colorPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        }

        textView = new TextView(context);
        textView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
        textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
        textView.setLines(1);
        textView.setMaxLines(1);
        textView.setSingleLine(true);
        textView.setGravity((LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT) | Gravity.CENTER_VERTICAL);
        addView(textView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT, (LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT) | Gravity.TOP, 21, 0, 21, 0));
    }

    @Keep
    @Override
    public void setAlpha(float value) {
        alpha = value;
        invalidate();
    }

    @Override
    public float getAlpha() {
        return alpha;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(50) + (needDivider ? 1 : 0), MeasureSpec.EXACTLY));
    }

    public void setTextAndColor(String text, int color, boolean divider) {
        textView.setText(text);
        needDivider = divider;
        currentColor = color;
        setWillNotDraw(!needDivider && currentColor == 0);
        invalidate();
    }

    public void setEnabled(boolean value, ArrayList<Animator> animators) {
        super.setEnabled(value);
        if (animators != null) {
            animators.add(ObjectAnimator.ofFloat(textView, "alpha", value ? 1.0f : 0.5f));
            animators.add(ObjectAnimator.ofFloat(this, "alpha", value ? 1.0f : 0.5f));
        } else {
            textView.setAlpha(value ? 1.0f : 0.5f);
            setAlpha(value ? 1.0f : 0.5f);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (needDivider) {
            canvas.drawLine(LocaleController.isRTL ? 0 : AndroidUtilities.dp(20), getMeasuredHeight() - 1, getMeasuredWidth() - (LocaleController.isRTL ? AndroidUtilities.dp(20) : 0), getMeasuredHeight() - 1, Theme.dividerPaint);
        }
        if (currentColor != 0) {
            colorPaint.setColor(currentColor);
            colorPaint.setAlpha((int) (255 * alpha));
            canvas.drawCircle(LocaleController.isRTL ? AndroidUtilities.dp(33) : getMeasuredWidth() - AndroidUtilities.dp(33), getMeasuredHeight() / 2, AndroidUtilities.dp(10), colorPaint);
        }
    }
}