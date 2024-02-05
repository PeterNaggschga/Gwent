package com.peternaggschga.gwent.ui.main;

import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.DrawableRes;
import androidx.lifecycle.Observer;

import com.peternaggschga.gwent.R;
import com.peternaggschga.gwent.data.RowType;

public class RowUiStateObserver implements Observer<RowUiState> {
    private final TextView damageView;
    private final ImageView weatherView;
    private final int weatherResource;
    private final ImageView hornView;
    private final TextView unitView;

    private RowUiStateObserver(TextView damageView, ImageView weatherView, @DrawableRes int weatherResource, ImageView hornView, TextView unitView) {
        this.damageView = damageView;
        this.weatherView = weatherView;
        this.weatherResource = weatherResource;
        this.hornView = hornView;
        this.unitView = unitView;
    }

    public static RowUiStateObserver getObserver(RowType row, TextView damageView, ImageView weatherView, ImageView hornView, TextView unitView) {
        int weatherResource;
        switch (row) {
            case MELEE:
                weatherResource = R.drawable.frost_weather;
                break;
            case RANGE:
                weatherResource = R.drawable.fog_weather;
                break;
            case SIEGE:
                weatherResource = R.drawable.rain_weather;
                break;
            default:
                weatherResource = R.drawable.good_weather;
        }
        return new RowUiStateObserver(damageView, weatherView, weatherResource, hornView, unitView);
    }

    public void onChanged(RowUiState rowUiState) {
        damageView.setText(String.valueOf(rowUiState.getDamage()));
        ImageViewSwitchAnimator.animatedSwitch(weatherView, rowUiState.isWeather() ? weatherResource : R.drawable.good_weather)
                .subscribe();
        ImageViewSwitchAnimator.animatedSwitch(hornView, rowUiState.isHorn() ? R.drawable.horn : R.drawable.horn_grey)
                .subscribe();
        unitView.setText(String.valueOf(rowUiState.getUnits()));
    }
}
