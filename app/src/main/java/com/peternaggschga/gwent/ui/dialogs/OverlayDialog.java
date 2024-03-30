package com.peternaggschga.gwent.ui.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.ViewGroup;
import android.view.Window;

import androidx.annotation.IdRes;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;

import java.util.Objects;

/**
 * @todo Documentation
 */
class OverlayDialog extends Dialog {
    @NonNull
    private static final ColorDrawable BACKGROUND = new ColorDrawable(Color.TRANSPARENT);
    private static final int NO_CANCEL_VIEW = -1;
    @LayoutRes
    private final int layout;
    @IdRes
    private final int cancelViewId;

    OverlayDialog(@NonNull Context context, @LayoutRes int layout, @IdRes int cancelViewId) {
        super(context);
        this.layout = layout;
        this.cancelViewId = cancelViewId;
    }

    OverlayDialog(@NonNull Context context, @LayoutRes int layout) {
        this(context, layout, NO_CANCEL_VIEW);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(layout);

        Window window = Objects.requireNonNull(getWindow());
        window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        window.setBackgroundDrawable(BACKGROUND);

        setCancelable(cancelViewId != NO_CANCEL_VIEW);
        findViewById(cancelViewId).setOnClickListener(v -> cancel());
    }
}
