package com.peternaggschga.gwent.ui.introduction;

import static org.valid4j.Assertive.require;

import android.view.Window;
import android.widget.ImageView;

import androidx.annotation.IntRange;
import androidx.annotation.NonNull;

import com.peternaggschga.gwent.R;

/**
 * @todo Documentation
 */
class IndicatorManager {
    @NonNull
    private final ImageView[] indicators = new ImageView[5];

    @IntRange(from = 0, to = 4)
    private int currentView = 0;

    IndicatorManager(@NonNull Window window) {
        indicators[0] = window.findViewById(R.id.introduction_indicator_0);
        indicators[1] = window.findViewById(R.id.introduction_indicator_1);
        indicators[2] = window.findViewById(R.id.introduction_indicator_2);
        indicators[3] = window.findViewById(R.id.introduction_indicator_3);
        indicators[4] = window.findViewById(R.id.introduction_indicator_4);
    }

    void updateIndicators(@IntRange(from = 0, to = 4) int position) {
        require(0 <= position && position <= 4);
        indicators[currentView].setImageResource(R.drawable.indicator_unselected);
        indicators[position].setImageResource(R.drawable.indicator_selected);
        currentView = position;
    }
}
