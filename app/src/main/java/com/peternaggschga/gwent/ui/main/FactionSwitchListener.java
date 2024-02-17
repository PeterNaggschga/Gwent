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

/**
 * A class implementing SharedPreference.OnSharedPreferenceChangeListener for the #THEME_PREFERENCE_KEY SharedPreference.
 * When this Preference is updated,
 * the theme is switched in an animated way using ImageViewSwitchAnimator.
 *
 * @see ImageViewSwitchAnimator
 */
public class FactionSwitchListener implements SharedPreferences.OnSharedPreferenceChangeListener {
    /**
     * String constant defining the key of the theme SharedPreference.
     */
    @NonNull
    public static final String THEME_PREFERENCE_KEY = "theme";

    /**
     * Integer constant representing the Monster theme.
     */
    public static final int THEME_MONSTER = 0;

    /**
     * Integer constant representing the Nilfgaard theme.
     */
    public static final int THEME_NILFGAARD = 1;

    /**
     * Integer constant representing the Northern Kingdoms theme.
     */
    public static final int THEME_NORTHERN_KINGDOMS = 2;

    /**
     * Integer constant representing the Scoia'tael theme.
     */
    public static final int THEME_SCOIATAEL = 3;

    /**
     * List of ImageView objects that show a colored ball.
     * @see R.drawable#ball_red
     * @see R.drawable#ball_grey
     * @see R.drawable#ball_blue
     * @see R.drawable#ball_green
     */
    @NonNull
    private final List<ImageView> ballViews;

    /**
     * List of ImageView objects that show a card back.
     * @see R.drawable#card_monster_landscape_free
     * @see R.drawable#card_nilfgaard_landscape_free
     * @see R.drawable#card_northern_kingdoms_landscape_free
     * @see R.drawable#card_scoiatael_landscape_free
     */
    @NonNull
    private final List<ImageView> cardViews;

    /**
     * List of TextView objects that show the number of units in a certain color.
     * @see R.color#color_text_monster
     * @see R.color#color_text_nilfgaard
     * @see R.color#color_text_northern_kingdoms
     * @see R.color#color_text_scoiatael
     */
    @NonNull
    private final List<TextView> unitViews;

    /**
     * ImageButton showing the logo of the current faction.
     * @see R.drawable#icon_round_monster
     * @see R.drawable#icon_round_nilfgaard
     * @see R.drawable#icon_round_northern_kingdoms
     * @see R.drawable#icon_round_scoiatael
     */
    @NonNull
    private final ImageButton factionButton;

    /**
     * Constructor of a FactionSwitchListener
     * updating the given View objects when #onSharedPreferenceChanged() is called.
     * Should only be used by factory method #getListener().
     * @see #getListener(Window)
     * @param ballViews List of ImageView objects showing a colored ball.
     * @param cardViews List of ImageView objects showing the backside of a card.
     * @param unitViews List of TextView objects showing the number of units.
     * @param factionButton ImageButton that shows the current faction logo.
     */
    private FactionSwitchListener(@NonNull List<ImageView> ballViews, @NonNull List<ImageView> cardViews,
                                  @NonNull List<TextView> unitViews, @NonNull ImageButton factionButton) {
        this.ballViews = ballViews;
        this.cardViews = cardViews;
        this.unitViews = unitViews;
        this.factionButton = factionButton;
    }

    /**
     * Returns a new FactionSwitchListener instance for the given Window.
     * Factory method of FactionSwitchListener.
     * @param mainWindow Window, that is updated by the new FactionSwitchListener.
     * @return A FactionSwitchListener instance for the given Window.
     */
    @NonNull
    @Contract("_ -> new")
    public static FactionSwitchListener getListener(@NonNull Window mainWindow) {
        List<ImageView> ballViews = new ArrayList<>(4);
        List<ImageView> cardViews = new ArrayList<>(3);
        List<TextView> unitViews = new ArrayList<>(3);

        for (int id : new int[]{R.id.firstRow, R.id.secondRow, R.id.thirdRow}) {
            ConstraintLayout rowLayout = mainWindow.findViewById(id);

            ballViews.add(rowLayout.findViewById(R.id.pointBall));
            cardViews.add(rowLayout.findViewById(R.id.cardsImage));
            unitViews.add(rowLayout.findViewById(R.id.cardCountView));
        }

        ballViews.add(mainWindow.findViewById(R.id.overallPointBall));

        return new FactionSwitchListener(ballViews, cardViews, unitViews, mainWindow.findViewById(R.id.factionButton));
    }

    /**
     * Called when a shared preference is changed, added, or removed.
     * Only handles changes when the given key is equal to #THEME_PREFERENCE_KEY.
     * Updates the View objects in #ballViews,
     * #cardViews, #unitViews, and #factionButton using ImageViewSwitchAnimator.
     * @see ImageViewSwitchAnimator
     * @param sharedPreferences SharedPreferences that received the change.
     * @param key               String representing the key of the preference that was changed, added, or removed.
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
