package com.peternaggschga.gwent.ui.introduction;

import android.view.Window;
import android.widget.ImageView;

import androidx.annotation.IntRange;
import androidx.annotation.NonNull;

import com.peternaggschga.gwent.R;

/**
 * A class managing the indicator views shown in the bottom bar of the IntroductionActivity.
 * The managed views show the user how many pages there are and which one they are on.
 */
class IndicatorManager {
    /**
     * Array of ImageViews that contains the indicator views.
     */
    @NonNull
    private final ImageView[] indicators = new ImageView[5];

    /**
     * Integer referencing the position, i.e. the index in indicators,
     * of the view that is currently shown as selected.
     * Is set in updateIndicators(int).
     *
     * @see .updateIndicators(int)
     */
    @IntRange(from = 0, to = 4)
    private int currentView = 0;

    /**
     * Constructor of an IndicatorManager for the given Window.
     * Initializes values in indicators.
     * @param window Window containing the indicator views.
     * @see Window.findViewById(int)
     * @see R.id.introduction_indicator_0
     * @see R.id.introduction_indicator_1
     * @see R.id.introduction_indicator_2
     * @see R.id.introduction_indicator_3
     * @see R.id.introduction_indicator_4
     */
    IndicatorManager(@NonNull Window window) {
        indicators[0] = window.findViewById(R.id.introduction_indicator_0);
        indicators[1] = window.findViewById(R.id.introduction_indicator_1);
        indicators[2] = window.findViewById(R.id.introduction_indicator_2);
        indicators[3] = window.findViewById(R.id.introduction_indicator_3);
        indicators[4] = window.findViewById(R.id.introduction_indicator_4);
    }

    /**
     * Updates the indicator views according to the given new position.
     * Sets the currentView to R.drawableindicator_unselected
     * and the view at the new position to R.drawableindicator_selected.
     * Saves the given position in currentView.
     * @param position Integer referencing the page represented by the managed indicators.
     * @throws ArrayIndexOutOfBoundsException When position is not in [0, 4].
     */
    void updateIndicators(@IntRange(from = 0, to = 4) int position) {
        indicators[currentView].setImageResource(R.drawable.indicator_unselected);
        indicators[position].setImageResource(R.drawable.indicator_selected);
        currentView = position;
    }
}
