/*
 * Clover - 4chan browser https://github.com/Floens/Clover/
 * Copyright (C) 2014  Floens
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.trashtier.chan.ui.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

public class CrossfadeView extends FrameLayout {
    private int fadeDuration = 200;

    private View viewOne;
    private View viewTwo;
    private boolean inited = false;
    private boolean viewOneSelected = true;

    public CrossfadeView(Context context) {
        super(context);
    }

    public CrossfadeView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CrossfadeView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        viewOne = getChildAt(0);
        viewTwo = getChildAt(1);
    }

    public void toggle(boolean viewOneSelected, boolean animated) {
        if (!inited || this.viewOneSelected != viewOneSelected) {
            this.viewOneSelected = viewOneSelected;
            doToggle(animated);
        }
    }

    public void toggle(boolean animated) {
        viewOneSelected = !viewOneSelected;
        doToggle(animated);
    }

    private void doToggle(boolean animated) {
        inited = true;
        if (animated) {
            if (viewOneSelected) {
                viewOne.setVisibility(View.VISIBLE);
                viewOne.animate().alpha(1f).setDuration(fadeDuration).setListener(null);
                viewTwo.animate().alpha(0f).setDuration(fadeDuration).setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        viewOne.setVisibility(View.VISIBLE);
                        viewTwo.setVisibility(View.GONE);
                    }
                });
            } else {
                viewTwo.setVisibility(View.VISIBLE);
                viewTwo.animate().alpha(1f).setDuration(fadeDuration).setListener(null);
                viewOne.animate().alpha(0f).setDuration(fadeDuration).setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        viewOne.setVisibility(View.GONE);
                        viewTwo.setVisibility(View.VISIBLE);
                    }
                });
            }
        } else {
            if (viewOneSelected) {
                viewOne.setVisibility(View.VISIBLE);
                viewOne.setAlpha(1f);
                viewTwo.setVisibility(View.GONE);
                viewTwo.setAlpha(0f);
            } else {
                viewOne.setVisibility(View.GONE);
                viewOne.setAlpha(0f);
                viewTwo.setVisibility(View.VISIBLE);
                viewTwo.setAlpha(1f);
            }
        }
    }
}
