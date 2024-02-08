package com.peternaggschga.gwent.ui.main;

import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.lifecycle.Observer;

import com.peternaggschga.gwent.R;
import com.peternaggschga.gwent.data.RowType;

/**
 * An observer class responsible for updating the views of the row defined in #row when notified,
 * i.e., when a new RowUiState is produced by RowStateUseCase.
 *
 * @see com.peternaggschga.gwent.domain.cases.RowStateUseCase
 * @see RowUiState
 */
public class RowUiStateObserver implements Observer<RowUiState> {
    /**
     * A TextView showing the user the summed-up damage of all units in this row,
     * i.e., RowUiState#damage.
     */
    @NonNull
    private final TextView damageView;

    /**
     * An ImageView responsible for showing the current state of the weather debuff in this row.
     * Displays the resource in #weatherResource when RowUiState#weather is ``true``.
     *
     * @see #weatherResource
     */
    @NonNull
    private final ImageView weatherView;

    /**
     * An Integer representing the drawable resource shown by #weatherView
     * when the weather debuff is active, i.e., when RowUiState#weather is ``true``.
     *
     * @see #weatherView
     */
    @DrawableRes
    private final int weatherResource;

    /**
     * An ImageView responsible for showing the current state of the commander's horn buff in this row.
     * Is gray when RowUiState#horn is ``false``.
     *
     * @see #weatherResource
     */
    @NonNull
    private final ImageView hornView;

    /**
     * A TextView showing the number of units in this row, i.e., RowUiState#units.
     */
    @NonNull
    private final TextView unitView;

    /**
     * Constructor of a RowUiStateObserver updating the given views when #onChanged() is called.
     * Should only be called by #getObserver().
     *
     * @param damageView      TextView showing the summed-up damage of all units in the observed row.
     * @param weatherView     ImageView showing the current state of the weather debuff of the observed row.
     * @param weatherResource Integer representing the drawable resource shown when the weather debuff is active.
     * @param hornView        ImageView showing the current state of the commander's horn buff of the observed row.
     * @param unitView        TextView showing the number of units in the observed row.
     * @see #getObserver(RowType, TextView, ImageView, ImageView, TextView)
     */
    private RowUiStateObserver(@NonNull TextView damageView, @NonNull ImageView weatherView,
                               @DrawableRes int weatherResource, @NonNull ImageView hornView,
                               @NonNull TextView unitView) {
        this.damageView = damageView;
        this.weatherView = weatherView;
        this.weatherResource = weatherResource;
        this.hornView = hornView;
        this.unitView = unitView;
    }

    /**
     * Returns a new RowUiStateObserver for the given row updating the given views.
     * Factory method for RowUiStateObserver.
     * #weatherResource is defined according to the given RowType.
     *
     * @param row         RowType defining which row is being observed.
     * @param damageView  TextView showing the summed-up damage of all units in the observed row.
     * @param weatherView ImageView showing the current state of the weather debuff of the observed row.
     * @param hornView    ImageView showing the current state of the commander's horn buff of the observed row.
     * @param unitView    TextView showing the number of units in the observed row.
     * @return A RowUiStateObserver updating the given views.
     * @see #RowUiStateObserver(TextView, ImageView, int, ImageView, TextView)
     */
    @NonNull
    public static RowUiStateObserver getObserver(@NonNull RowType row, @NonNull TextView damageView,
                                                 @NonNull ImageView weatherView, @NonNull ImageView hornView,
                                                 @NonNull TextView unitView) {
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

    /**
     * Changes values in #damageView and #unitView as well as the image in #weatherView and #hornView.
     * Image switches are animated using ImageViewSwitchAnimator.
     *
     * @param rowUiState RowUiState representing the updated state of the row.
     * @see ImageViewSwitchAnimator
     */
    @Override
    public void onChanged(@NonNull RowUiState rowUiState) {
        damageView.setText(String.valueOf(rowUiState.getDamage()));
        ImageViewSwitchAnimator.animatedSwitch(weatherView, rowUiState.isWeather() ? weatherResource : R.drawable.good_weather)
                .subscribe();
        ImageViewSwitchAnimator.animatedSwitch(hornView, rowUiState.isHorn() ? R.drawable.horn : R.drawable.horn_grey)
                .subscribe();
        unitView.setText(String.valueOf(rowUiState.getUnits()));
    }
}
