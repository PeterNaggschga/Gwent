package com.peternaggschga.gwent.ui.main;

import android.widget.ImageButton;
import android.widget.TextView;

import androidx.lifecycle.Observer;

import com.peternaggschga.gwent.R;

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
        ImageViewSwitchAnimator.animatedSwitch(resetButton, menuUiState.isReset() ? R.drawable.icon_reset : R.drawable.icon_reset_grey)
                .subscribe();
        weatherButton.setClickable(menuUiState.isWeather());
        ImageViewSwitchAnimator.animatedSwitch(weatherButton, menuUiState.isWeather() ? R.drawable.icon_weather : R.drawable.icon_weather_grey)
                .subscribe();
        burnButton.setClickable(menuUiState.isBurn());
        ImageViewSwitchAnimator.animatedSwitch(burnButton, menuUiState.isBurn() ? R.drawable.icon_burn : R.drawable.icon_burn_grey)
                .subscribe();
    }
}
