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
 * A Dialog class which is used for popups that are shown on top of the calling Activity.
 * The Dialog uses the layout specified in #layout with the #BACKGROUND color as the background.
 * The Dialog is cancelable when a #cancelViewId is specified (else it is #NO_CANCEL_VIEW).
 * The view specified by #cancelViewId may be clicked to dismiss the dialog.
 */
class OverlayDialog extends Dialog {
    /**
     * Integer used as #cancelViewId, when the Dialog shouldn't be cancelable.
     *
     * @see #cancelViewId
     */
    static final int NO_CANCEL_VIEW = -1;
    /**
     * ColorDrawable shown as the background of the Dialog (above the calling Activity).
     */
    @NonNull
    private static final ColorDrawable BACKGROUND = new ColorDrawable(Color.TRANSPARENT);
    /**
     * Integer referencing the layout shown by the Dialog.
     */
    @LayoutRes
    private final int layout;
    /**
     * Integer referencing a view that can be clicked to dismiss the Dialog.
     * Should be equal to #NO_CANCEL_VIEW, when the Dialog is not cancelable.
     * @see #NO_CANCEL_VIEW
     */
    @IdRes
    private final int cancelViewId;

    /**
     * Constructor of an OverlayDialog in the given Context, with the given layout and cancelViewId.
     * When cancelViewId is equal to #NO_CANCEL_VIEW, the Dialog is not cancelable;
     * otherwise it may be canceled by clicking on the referenced view.
     * @param context Context of the created OverlayDialog.
     * @param layout Integer referencing the layout shown by the created OverlayDialog.
     * @param cancelViewId Integer referencing the cancel view or #NO_CANCEL_VIEW.
     */
    OverlayDialog(@NonNull Context context, @LayoutRes int layout, @IdRes int cancelViewId) {
        super(context);
        this.layout = layout;
        this.cancelViewId = cancelViewId;
    }

    /**
     * Constructor of a non-cancelable OverlayDialog in the given Context and with the given layout.
     * Wrapper of #OverlayDialog(Context, int, int).
     * @see #OverlayDialog(Context, int, int)
     * @param context Context of the created OverlayDialog.
     * @param layout Integer referencing the layout shown by the created OverlayDialog.
     */
    OverlayDialog(@NonNull Context context, @LayoutRes int layout) {
        this(context, layout, NO_CANCEL_VIEW);
    }

    /**
     * Initializes #layout using #setContentView(int).
     * Sets View.OnClickListener canceling the dialog for the View referenced by #cancelViewId
     * if it is set.
     * Switches whether the Dialog is cancelable using #setCancelable().
     * @param savedInstanceState If this dialog is being reinitialized after
     *     the hosting activity was previously shut down, holds the result from
     *     the most recent call to {@link #onSaveInstanceState}, or null if this
     *     is the first time.
     * @see #setContentView(int)
     * @see #setCancelable(boolean)
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(layout);

        Window window = Objects.requireNonNull(getWindow());
        window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        window.setBackgroundDrawable(BACKGROUND);

        if (cancelViewId != NO_CANCEL_VIEW) {
            setCancelable(true);
            findViewById(cancelViewId).setOnClickListener(v -> cancel());
        } else {
            setCancelable(false);
        }
    }
}
