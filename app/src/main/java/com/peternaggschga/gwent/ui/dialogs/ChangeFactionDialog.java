package com.peternaggschga.gwent.ui.dialogs;

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
import com.peternaggschga.gwent.ui.main.FactionSwitchListener;

import java.util.Objects;

public class ChangeFactionDialog extends Dialog {
    public ChangeFactionDialog(@NonNull Context context, @NonNull Callback callback) {
        super(context);

        setContentView(R.layout.popup_faction);

        Window window = Objects.requireNonNull(getWindow());
        window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        findViewById(R.id.factionBackground).setOnClickListener(v -> cancel());

        findViewById(R.id.monsterCardView).setOnClickListener(getOnThemeClickListener(callback, FactionSwitchListener.THEME_MONSTER));
        findViewById(R.id.monsterButton).setOnClickListener(getOnThemeClickListener(callback, FactionSwitchListener.THEME_MONSTER));

        findViewById(R.id.nilfgaardCardView).setOnClickListener(getOnThemeClickListener(callback, FactionSwitchListener.THEME_NILFGAARD));
        findViewById(R.id.nilfgaardButton).setOnClickListener(getOnThemeClickListener(callback, FactionSwitchListener.THEME_NILFGAARD));

        findViewById(R.id.northernKingdomsCardView).setOnClickListener(getOnThemeClickListener(callback, FactionSwitchListener.THEME_NORTHERN_KINGDOMS));
        findViewById(R.id.northernKingdomsButton).setOnClickListener(getOnThemeClickListener(callback, FactionSwitchListener.THEME_NORTHERN_KINGDOMS));

        findViewById(R.id.scoiataelCardView).setOnClickListener(getOnThemeClickListener(callback, FactionSwitchListener.THEME_SCOIATAEL));
        findViewById(R.id.scoiataelButton).setOnClickListener(getOnThemeClickListener(callback, FactionSwitchListener.THEME_SCOIATAEL));

        setCancelable(true);
    }

    @NonNull
    private View.OnClickListener getOnThemeClickListener(@NonNull Callback callback,
                                                         @IntRange(from = 0, to = 3) int theme) {
        return v -> {
            cancel();
            callback.onThemeSelect(theme);
        };
    }

    public interface Callback {
        void onThemeSelect(@IntRange(from = 0, to = 3) int theme);
    }
}
