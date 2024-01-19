package com.peternaggschga.gwent;

import android.widget.ImageView;
import android.widget.TextView;

import androidx.lifecycle.Observer;

import com.peternaggschga.gwent.data.RowType;
import com.peternaggschga.gwent.ui.RowUiState;

public class RowObserver implements Observer<RowUiState> {
    private final RowType row;
    private final TextView damageView;
    private final ImageView weatherView;
    private final ImageView hornView;
    private final TextView unitView;

    public RowObserver(RowType row, TextView damageView, ImageView weatherView, ImageView hornView, TextView unitView) {
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
                    weatherView.setImageResource(R.drawable.frost_weather); // TODO: animate
                    break;
                case RANGE:
                    weatherView.setImageResource(R.drawable.fog_weather); // TODO: animate
                    break;
                case SIEGE:
                    weatherView.setImageResource(R.drawable.rain_weather); // TODO: animate
                    break;
            }
        } else {
            weatherView.setImageResource(R.drawable.good_weather); // TODO: animate
        }
        hornView.setImageResource(rowUiState.isHorn() ? R.drawable.horn : R.drawable.horn_grey); // TODO: animate
        unitView.setText(String.valueOf(rowUiState.getUnits()));
    }
}
