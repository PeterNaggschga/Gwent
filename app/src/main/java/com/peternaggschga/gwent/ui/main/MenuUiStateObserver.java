package com.peternaggschga.gwent.ui.main;

import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.lifecycle.Observer;

import com.peternaggschga.gwent.R;

/**
 * An observer class responsible for updating the menu views when notified,
 * i.e., when a new MenuUiState is produced by MenuUpdateObserver.
 *
 * @see MenuUiState
 * @see MenuUpdateObserver
 */
public class MenuUiStateObserver implements Observer<MenuUiState> {
    /**
     * A TextView showing the user the summed-up damage of all units, i.e., MenuUiState#damage.
     */
    @NonNull
    private final TextView damageView;

    /**
     * An ImageButton responsible for resetting the whole board,
     * i.e., clearing weather and commander's horn and deleting all units.
     * Is not clickable and gray when MenuUiState#reset is ``false``.
     */
    @NonNull
    private final ImageButton resetButton;

    /**
     * An ImageButton responsible for clearing all weather effects.
     * Is not clickable and gray when MenuUiState#weather is ``false``.
     */
    @NonNull
    private final ImageButton weatherButton;

    /**
     * An ImageButton responsible for deleting the units with the highest damage.
     * Is not clickable and gray when MenuUiState#burn is ``false``.
     */
    @NonNull
    private final ImageButton burnButton;

    /**
     * Constructor of a MenuUiStateObserver updating the given views when #onChanged() is called.
     *
     * @param damageView    TextView showing the summed-up damage of all units.
     * @param resetButton   ImageButton used to reset the whole game board.
     * @param weatherButton ImageButton used to reset the weather debuff in all rows.
     * @param burnButton    ImageButton used to delete the strongest units from the game board.
     */
    public MenuUiStateObserver(@NonNull TextView damageView, @NonNull ImageButton resetButton,
                               @NonNull ImageButton weatherButton, @NonNull ImageButton burnButton) {
        this.damageView = damageView;
        this.resetButton = resetButton;
        this.weatherButton = weatherButton;
        this.burnButton = burnButton;
    }

    /**
     * Changes value in #damageView as well as appearance and clickable status of #resetButton,
     * #weatherButton, and #burnButton.
     * Appearance changes are animated using ImageViewSwitchAnimator.
     *
     * @param menuUiState MenuUiState representing the updated state of the menu.
     * @see ImageViewSwitchAnimator
     */
    @Override
    public void onChanged(@NonNull MenuUiState menuUiState) {
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
