package com.peternaggschga.gwent.ui.main;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.os.Build;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.peternaggschga.gwent.R;

import org.jetbrains.annotations.Contract;

import java.util.ArrayList;
import java.util.List;

public class FactionSwitchListener implements SharedPreferences.OnSharedPreferenceChangeListener {
    public static final String THEME_PREFERENCE_KEY = "theme";
    public static final int THEME_MONSTER = 0;
    public static final int THEME_NILFGAARD = 1;
    public static final int THEME_NORTHERN_KINGDOMS = 2;
    public static final int THEME_SCOIATAEL = 3;

    @NonNull
    private final List<ImageView> ballViews;
    @NonNull
    private final List<ImageView> cardViews;
    @NonNull
    private final List<TextView> unitViews;
    @NonNull
    private final ImageButton factionButton;

    private FactionSwitchListener(@NonNull List<ImageView> ballViews, @NonNull List<ImageView> cardViews,
                                  @NonNull List<TextView> unitViews, @NonNull ImageButton factionButton) {
        this.ballViews = ballViews;
        this.cardViews = cardViews;
        this.unitViews = unitViews;
        this.factionButton = factionButton;
    }

    @NonNull
    @Contract("_ -> new")
    public static FactionSwitchListener getFactionSwitchObserver(@NonNull Window mainWindow) {
        List<ImageView> ballViews = new ArrayList<>(4);
        List<ImageView> cardViews = new ArrayList<>(3);
        List<TextView> unitViews = new ArrayList<>(3);

        int[] rowIds = {R.id.firstRow, R.id.secondRow, R.id.thirdRow};
        for (int id : rowIds) {
            ConstraintLayout rowLayout = mainWindow.findViewById(id);

            ballViews.add(rowLayout.findViewById(R.id.pointBall));
            cardViews.add(rowLayout.findViewById(R.id.cardsImage));
            unitViews.add(rowLayout.findViewById(R.id.cardCountView));
        }

        ballViews.add(mainWindow.findViewById(R.id.overallPointBall));

        return new FactionSwitchListener(ballViews, cardViews, unitViews, mainWindow.findViewById(R.id.factionButton));
    }

    /**
     * Called when a shared preference is changed, added, or removed. This
     * may be called even if a preference is set to its existing value.
     *
     * <p>This callback will be run on your main thread.
     *
     * <p><em>Note: This callback will not be triggered when preferences are cleared
     * via {@link Editor#clear()}, unless targeting {@link Build.VERSION_CODES#R}
     * on devices running OS versions {@link Build.VERSION_CODES#R Android R}
     * or later.</em>
     *
     * @param sharedPreferences The {@link SharedPreferences} that received the change.
     * @param key               The key of the preference that was changed, added, or removed. Apps targeting
     *                          {@link Build.VERSION_CODES#R} on devices running OS versions
     *                          {@link Build.VERSION_CODES#R Android R} or later, will receive
     *                          a {@code null} value when preferences are cleared.
     */
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, @Nullable String key) {
        if (!THEME_PREFERENCE_KEY.equals(key)) {
            return;
        }

        Context context = factionButton.getContext();

        int theme;
        switch (sharedPreferences.getInt(key, THEME_SCOIATAEL)) {
            case THEME_MONSTER:
                theme = R.style.MonsterTheme;
                break;
            case THEME_NILFGAARD:
                theme = R.style.NilfgaardTheme;
                break;
            case THEME_NORTHERN_KINGDOMS:
                theme = R.style.NorthernKingdomsTheme;
                break;
            case THEME_SCOIATAEL:
            default:
                theme = R.style.ScoiataelTheme;
        }

        int ballImageRes;
        int cardImageRes;
        int unitNumberTextColor;
        int factionButtonImageRes;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            try (TypedArray attributeValues = context.obtainStyledAttributes(theme, R.styleable.theme)) {
                ballImageRes = attributeValues.getResourceId(R.styleable.theme_point_ball_mipmap, R.drawable.ball_green);
                cardImageRes = attributeValues.getResourceId(R.styleable.theme_card_view_mipmap, R.drawable.card_scoiatael_landscape_free);
                unitNumberTextColor = attributeValues.getColor(R.styleable.theme_colorPrimary, context.getColor(R.color.color_text_scoiatael));
                factionButtonImageRes = attributeValues.getResourceId(R.styleable.theme_android_alertDialogIcon, R.drawable.icon_round_scoiatael);
            }
        } else {
            TypedArray attributeValues = context.obtainStyledAttributes(theme, R.styleable.theme);
            ballImageRes = attributeValues.getResourceId(R.styleable.theme_point_ball_mipmap, R.drawable.ball_green);
            cardImageRes = attributeValues.getResourceId(R.styleable.theme_card_view_mipmap, R.drawable.card_scoiatael_landscape_free);
            unitNumberTextColor = attributeValues.getColor(R.styleable.theme_colorPrimary, context.getColor(R.color.color_text_scoiatael));
            factionButtonImageRes = attributeValues.getResourceId(R.styleable.theme_android_alertDialogIcon, R.drawable.icon_round_scoiatael);
            attributeValues.recycle();
        }

        ballViews.forEach(view -> ImageViewSwitchAnimator.animatedSwitch(view, ballImageRes).subscribe());
        cardViews.forEach(view -> ImageViewSwitchAnimator.animatedSwitch(view, cardImageRes).subscribe());
        unitViews.forEach(textView -> textView.setTextColor(unitNumberTextColor));
        ImageViewSwitchAnimator.animatedSwitch(factionButton, factionButtonImageRes).subscribe();
    }
}
