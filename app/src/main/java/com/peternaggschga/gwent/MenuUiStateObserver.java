package com.peternaggschga.gwent;

import android.widget.ImageButton;
import android.widget.TextView;

import androidx.lifecycle.Observer;

import com.peternaggschga.gwent.ui.MenuUiState;

public class MenuUiStateObserver implements Observer<MenuUiState> {
    private final TextView damageView;
    private final ImageButton resetButton;
    private final ImageButton weatherButton;
    private final ImageButton burnButton;

    public MenuUiStateObserver(TextView damageView, ImageButton resetButton, ImageButton weatherButton, ImageButton burnButton) {
        this.damageView = damageView;
        this.resetButton = resetButton;
        this.weatherButton = weatherButton;
        this.burnButton = burnButton;
    }

    @Override
    public void onChanged(MenuUiState menuUiState) {
        damageView.setText(String.valueOf(menuUiState.getDamage()));
        resetButton.setClickable(menuUiState.isReset());
        resetButton.setImageResource(menuUiState.isReset() ? R.drawable.icon_reset : R.drawable.icon_reset_grey); // TODO: animate
        weatherButton.setClickable(menuUiState.isWeather());
        weatherButton.setImageResource(menuUiState.isWeather() ? R.drawable.icon_weather : R.drawable.icon_weather_grey); // TODO: animate
        burnButton.setClickable(menuUiState.isBurn());
        burnButton.setImageResource(menuUiState.isBurn() ? R.drawable.icon_burn : R.drawable.icon_burn_grey); // TODO: animate
    }
}
