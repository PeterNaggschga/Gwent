package com.peternaggschga.gwent.ui.main;

import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;

import io.reactivex.rxjava3.core.Completable;

/**
 * A class providing functionality for changing
 * the resource shown by an ImageView using a fading animation.
 *
 * @todo Remove res/anim
 * @see #animatedSwitch(ImageView, int)
 */
public class ImageViewSwitchAnimator {
    /**
     * Returns a copy of the given template which is one layer above it.
     * @param template ImageView that is copied.
     * @return An ImageView similar to template but one layer in front of it.
     */
    @NonNull
    private static ImageView getOverlayView(@NonNull ImageView template) {
        ImageView result = new ImageView(template.getContext());
        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) template.getLayoutParams();
        result.setLayoutParams(new ConstraintLayout.LayoutParams(params.width, params.height));
        result.setImageDrawable(template.getDrawable());
        result.setScaleType(template.getScaleType());
        result.setX(template.getX());
        result.setY(template.getY());
        result.setTranslationZ(template.getTranslationZ() + 1);
        return result;
    }

    /**
     * Changes the resource of the given ImageView in an animation to the given resource id.
     * Animation is realized by creating a new ImageView on top of the given ImageView
     * which is then faded out using the ``fade_out`` animation provided by Android.
     * The operation is complete when the animation ends.
     * @see android.R.anim#fade_out
     * @param view ImageView, whose resource is to be changed.
     * @param resId Integer representing the drawable resource that the given view should show.
     * @return A Completable tracking operation status.
     */
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
