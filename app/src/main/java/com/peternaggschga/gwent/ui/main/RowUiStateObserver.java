package com.peternaggschga.gwent.ui.main;

import android.widget.ImageView;
import android.widget.TextView;

import androidx.lifecycle.Observer;

import com.peternaggschga.gwent.R;
import com.peternaggschga.gwent.data.RowType;

public class RowUiStateObserver implements Observer<RowUiState> {
    private final RowType row;
    private final TextView damageView;
    private final ImageView weatherView;
    private final ImageView hornView;
    private final TextView unitView;

    public RowUiStateObserver(RowType row, TextView damageView, ImageView weatherView, ImageView hornView, TextView unitView) {
        this.row = row;
        this.damageView = damageView;
        this.weatherView = weatherView;
        this.hornView = hornView;
        this.unitView = unitView;
    }

    public void onChanged(RowUiState rowUiState) {
        damageView.setText(String.valueOf(rowUiState.getDamage()));
        if (rowUiState.isWeather()) {
            switch (row) {
                case MELEE:
                    ImageViewSwitchAnimator.animatedSwitch(weatherView, R.drawable.frost_weather)
                            .subscribe();
                    break;
                case RANGE:
                    ImageViewSwitchAnimator.animatedSwitch(weatherView, R.drawable.fog_weather)
                            .subscribe();
                    break;
                case SIEGE:
                    ImageViewSwitchAnimator.animatedSwitch(weatherView, R.drawable.rain_weather)
                            .subscribe();
                    break;
            }
        } else {
            ImageViewSwitchAnimator.animatedSwitch(weatherView, R.drawable.good_weather)
                    .subscribe();
        }
        ImageViewSwitchAnimator.animatedSwitch(hornView, rowUiState.isHorn() ? R.drawable.horn : R.drawable.horn_grey)
                .subscribe();
        unitView.setText(String.valueOf(rowUiState.getUnits()));
    }
}
