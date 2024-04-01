package com.peternaggschga.gwent.ui.dialogs;

import static com.peternaggschga.gwent.ui.main.FactionSwitchListener.THEME_MONSTER;
import static com.peternaggschga.gwent.ui.main.FactionSwitchListener.THEME_NILFGAARD;
import static com.peternaggschga.gwent.ui.main.FactionSwitchListener.THEME_NORTHERN_KINGDOMS;
import static com.peternaggschga.gwent.ui.main.FactionSwitchListener.THEME_SCOIATAEL;

import android.content.Context;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.IntRange;
import androidx.annotation.NonNull;

import com.peternaggschga.gwent.R;

/**
 * An OverlayDialog class used to change the faction design.
 */
public class ChangeFactionDialog extends OverlayDialog {
    /**
     * Callback that is called when a theme is selected.
     */
    private final Callback callback;

    /**
     * Constructor of a ChangeFactionDialog
     * that calls the given Callback when one theme is selected.
     *
     * @param context  Context in which this Dialog is run.
     * @param callback Callback that is called when a theme is selected.
     */
    public ChangeFactionDialog(@NonNull Context context, @NonNull Callback callback) {
        super(context, R.layout.popup_faction, R.id.factionBackground);
        this.callback = callback;
    }

    /**
     * Initializes layout and sets listeners for each view.
     *
     * @param savedInstanceState If this dialog is being reinitialized after
     *                           the hosting activity was previously shut down, holds the result from
     *                           the most recent call to {@link #onSaveInstanceState}, or null if this
     *                           is the first time.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        findViewById(R.id.monsterCardView).setOnClickListener(getOnThemeClickListener(THEME_MONSTER));
        findViewById(R.id.monsterButton).setOnClickListener(getOnThemeClickListener(THEME_MONSTER));

        findViewById(R.id.nilfgaardCardView).setOnClickListener(getOnThemeClickListener(THEME_NILFGAARD));
        findViewById(R.id.nilfgaardButton).setOnClickListener(getOnThemeClickListener(THEME_NILFGAARD));

        findViewById(R.id.northernKingdomsCardView).setOnClickListener(getOnThemeClickListener(THEME_NORTHERN_KINGDOMS));
        findViewById(R.id.northernKingdomsButton).setOnClickListener(getOnThemeClickListener(THEME_NORTHERN_KINGDOMS));

        findViewById(R.id.scoiataelCardView).setOnClickListener(getOnThemeClickListener(THEME_SCOIATAEL));
        findViewById(R.id.scoiataelButton).setOnClickListener(getOnThemeClickListener(THEME_SCOIATAEL));
    }

    /**
     * Returns a View.OnclickListener instance that calls #cancel()
     * and uses #callback to propagate the selected theme.
     *
     * @param theme Integer representing the selected theme.
     * @return A View.OnClickListener handling theme input.
     * @see #cancel()
     * @see Callback#onThemeSelect(int)
     */
    @NonNull
    private View.OnClickListener getOnThemeClickListener(@IntRange(from = THEME_MONSTER, to = THEME_SCOIATAEL) int theme) {
        return v -> {
            cancel();
            callback.onThemeSelect(theme);
        };
    }

    /**
     * An interface
     * used for propagating a selected theme back to the creator of a ChangeFactionDialog.
     */
    public interface Callback {
        /**
         * Callback being called when a theme is selected in the respective ChangeFactionDialog.
         *
         * @param theme Integer representing the selected theme.
         */
        void onThemeSelect(@IntRange(from = THEME_MONSTER, to = THEME_SCOIATAEL) int theme);
    }
}
