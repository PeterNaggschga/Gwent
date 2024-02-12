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

import java.util.Objects;

public class ChangeFactionDialog extends Dialog {
    public ChangeFactionDialog(@NonNull Context context, @NonNull Callback callback) {
        super(context);

        setContentView(R.layout.popup_faction);

        Window window = Objects.requireNonNull(getWindow());
        window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        findViewById(R.id.factionBackground).setOnClickListener(v -> cancel());

        int monsterTheme = context.getResources().getInteger(R.integer.theme_id_monster);
        findViewById(R.id.monsterCardView).setOnClickListener(getOnThemeClickListener(callback, monsterTheme));
        findViewById(R.id.monsterButton).setOnClickListener(getOnThemeClickListener(callback, monsterTheme));

        int nilfgaardTheme = context.getResources().getInteger(R.integer.theme_id_nilfgaard);
        findViewById(R.id.nilfgaardCardView).setOnClickListener(getOnThemeClickListener(callback, nilfgaardTheme));
        findViewById(R.id.nilfgaardButton).setOnClickListener(getOnThemeClickListener(callback, nilfgaardTheme));

        int northernKingdomsTheme = context.getResources().getInteger(R.integer.theme_id_northern_kingdoms);
        findViewById(R.id.northernKingdomsCardView).setOnClickListener(getOnThemeClickListener(callback, northernKingdomsTheme));
        findViewById(R.id.northernKingdomsButton).setOnClickListener(getOnThemeClickListener(callback, northernKingdomsTheme));

        int scoiataelTheme = context.getResources().getInteger(R.integer.theme_id_scoiatael);
        findViewById(R.id.scoiataelCardView).setOnClickListener(getOnThemeClickListener(callback, scoiataelTheme));
        findViewById(R.id.scoiataelButton).setOnClickListener(getOnThemeClickListener(callback, scoiataelTheme));

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
