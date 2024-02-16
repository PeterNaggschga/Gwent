package com.peternaggschga.gwent.ui.dialogs;

import static com.peternaggschga.gwent.ui.main.FactionSwitchListener.THEME_MONSTER;
import static com.peternaggschga.gwent.ui.main.FactionSwitchListener.THEME_NILFGAARD;
import static com.peternaggschga.gwent.ui.main.FactionSwitchListener.THEME_NORTHERN_KINGDOMS;
import static com.peternaggschga.gwent.ui.main.FactionSwitchListener.THEME_SCOIATAEL;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import androidx.annotation.IntRange;
import androidx.annotation.NonNull;

import com.peternaggschga.gwent.R;

import java.util.Objects;

/**
 * A Dialog class used to change the faction design.
 */
public class ChangeFactionDialog extends Dialog {
    /**
     * Constructor of a ChangeFactionDialog
     * that calls the given Callback when one theme is selected.
     *
     * @param context  Context in which this Dialog is run.
     * @param callback Callback that is called when a theme is selected.
     */
    public ChangeFactionDialog(@NonNull Context context, @NonNull Callback callback) {
        super(context);

        setContentView(R.layout.popup_faction);

        Window window = Objects.requireNonNull(getWindow());
        window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        findViewById(R.id.factionBackground).setOnClickListener(v -> cancel());

        findViewById(R.id.monsterCardView).setOnClickListener(getOnThemeClickListener(callback, THEME_MONSTER));
        findViewById(R.id.monsterButton).setOnClickListener(getOnThemeClickListener(callback, THEME_MONSTER));

        findViewById(R.id.nilfgaardCardView).setOnClickListener(getOnThemeClickListener(callback, THEME_NILFGAARD));
        findViewById(R.id.nilfgaardButton).setOnClickListener(getOnThemeClickListener(callback, THEME_NILFGAARD));

        findViewById(R.id.northernKingdomsCardView).setOnClickListener(getOnThemeClickListener(callback, THEME_NORTHERN_KINGDOMS));
        findViewById(R.id.northernKingdomsButton).setOnClickListener(getOnThemeClickListener(callback, THEME_NORTHERN_KINGDOMS));

        findViewById(R.id.scoiataelCardView).setOnClickListener(getOnThemeClickListener(callback, THEME_SCOIATAEL));
        findViewById(R.id.scoiataelButton).setOnClickListener(getOnThemeClickListener(callback, THEME_SCOIATAEL));

        setCancelable(true);
    }

    /**
     * Returns a View.OnclickListener instance that calls #cancel()
     * and uses the given Callback to propagate the selected theme.
     *
     * @param callback Callback used to propagate the selected theme.
     * @param theme    Integer representing the selected theme.
     * @return A View.OnClickListener handling theme input.
     * @see #cancel()
     * @see Callback#onThemeSelect(int)
     */
    @NonNull
    private View.OnClickListener getOnThemeClickListener(@NonNull Callback callback,
                                                         @IntRange(from = THEME_MONSTER, to = THEME_SCOIATAEL) int theme) {
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
