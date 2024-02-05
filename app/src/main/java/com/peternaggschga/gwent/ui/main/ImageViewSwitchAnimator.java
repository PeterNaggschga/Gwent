package com.peternaggschga.gwent.ui.main;

import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;

import io.reactivex.rxjava3.core.Completable;

public class ImageViewSwitchAnimator {
    @NonNull
    private static ImageView getOverlayView(@NonNull ImageView template) {
        ImageView result = new ImageView(template.getContext());
        result.setLayoutParams(new ViewGroup.LayoutParams(template.getWidth(), template.getHeight()));
        result.setImageDrawable(template.getDrawable());
        result.setScaleType(template.getScaleType());
        result.setX(template.getX());
        result.setY(template.getY());
        result.setTranslationZ(template.getTranslationZ() + 1);
        return result;
    }

    @NonNull
    public static Completable animatedSwitch(@NonNull ImageView view, @DrawableRes int resId) {
        return Completable.create(emitter -> {
            Animation fadeAnimation = AnimationUtils.loadAnimation(view.getContext(), android.R.anim.fade_out);
            ImageView foregroundView = getOverlayView(view);
            ViewGroup parent = (ViewGroup) view.getParent();

            fadeAnimation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                    view.setImageResource(resId);
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    parent.removeView(foregroundView);
                    emitter.onComplete();
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                }
            });

            parent.addView(foregroundView);
            foregroundView.startAnimation(fadeAnimation);
        });
    }
}
