package com.peternaggschga.gwent.domain.cases;

import android.content.Context;
import android.view.View;
import android.widget.CheckBox;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import com.peternaggschga.gwent.R;

/**
 * An adapter class adapting AlertDialog.Builder to provide an interface
 * for creating an AlertDialog asking the user whether he really wants to reset.
 */
class ResetAlertDialogBuilderAdapter {
    /**
     * AlertDialog.Builder that is adapted by this class.
     */
    @NonNull
    private final AlertDialog.Builder adapteeBuilder;

    /**
     * Callback used to propagate the user's answer to the built AlertDialog back to its creator.
     */
    @NonNull
    private final Callback dialogCallback;

    /**
     * Constructor of a ResetAlertDialogBuilderAdapter.
     * The given Callback is called when the user responds to the built AlertDialog.
     *
     * @param context        Context of the built AlertDialog.
     * @param dialogCallback Callback used to propagate the user's answer to the creator.
     */
    ResetAlertDialogBuilderAdapter(@NonNull Context context, @NonNull Callback dialogCallback) {
        this.adapteeBuilder = new AlertDialog.Builder(context)
                .setIconAttribute(android.R.attr.alertDialogIcon)
                .setTitle(R.string.alertDialog_reset_title)
                .setOnCancelListener(dialog -> dialogCallback.reset(false))
                .setNegativeButton(R.string.alertDialog_reset_negative, (dialog, which) -> dialog.cancel());
        this.dialogCallback = dialogCallback;
    }

    /**
     * Creates an AlertDialog with the arguments supplied to this builder.
     * Basically just calls AlertDialog.Builder#create() on #adapteeBuilder.
     *
     * @see AlertDialog.Builder#create()
     */
    @NonNull
    AlertDialog create() {
        return adapteeBuilder.create();
    }

    /**
     * Changes the shown message of the AlertDialog
     * and whether it is cancelable depending on the trigger of the dialog.
     *
     * @param trigger {@link ResetDialogUseCase.Trigger} defining what triggered the reset.
     * @return The ResetAlertDialogBuilderAdapter with the changed trigger.
     */
    @NonNull
    ResetAlertDialogBuilderAdapter setTrigger(@NonNull ResetDialogUseCase.Trigger trigger) {
        adapteeBuilder.setMessage((trigger != ResetDialogUseCase.Trigger.FACTION_SWITCH) ?
                        R.string.alertDialog_reset_msg_default :
                        R.string.alertDialog_reset_msg_faction_switch)
                .setCancelable(trigger != ResetDialogUseCase.Trigger.FACTION_SWITCH);
        return this;
    }

    /**
     * Changes the positive button callback depending on whether it is a monsterDialog or not.
     * When it is a monsterDialog,
     * a checkbox is shown defining whether the perk of monster faction should be activated.
     *
     * @param monsterDialog Boolean defining whether a monsterDialog should be shown.
     * @return The ResetAlertDialogBuilderAdapter with the changed trigger.
     */
    @NonNull
    ResetAlertDialogBuilderAdapter setMonsterDialog(boolean monsterDialog) {
        if (monsterDialog) {
            View checkBoxView = View.inflate(adapteeBuilder.getContext(), R.layout.alertdialog_checkbox, null);
            adapteeBuilder.setView(checkBoxView)
                    .setPositiveButton(R.string.alertDialog_reset_positive, (dialog, which) -> {
                        CheckBox checkBox = checkBoxView.findViewById(R.id.alertDialog_checkbox);
                        dialogCallback.reset(true, checkBox.isChecked());
                    });
        } else {
            adapteeBuilder.setPositiveButton(R.string.alertDialog_reset_positive, ((dialog, which) -> dialogCallback.reset(true)));
        }
        return this;
    }

    /**
     * An interface defining functions to propagate the user's decision back to the creator.
     */
    interface Callback {
        /**
         * Called when the user makes a decision.
         * When the AlertDialog is a monster dialog, #reset(boolean) can be used.
         *
         * @param resetDecision Boolean defining whether the user has confirmed the reset.
         * @param keepUnit      Boolean defining whether a random unit should be kept when resetting.
         * @see #reset(boolean)
         */
        void reset(boolean resetDecision, boolean keepUnit);

        /**
         * Wrapper for #reset(boolean, boolean).
         * Can be used when the AlertDialog is not a monster dialog
         * since it calls #reset(boolean, boolean) without keeping a unit.
         *
         * @param resetDecision Boolean defining whether the user has confirmed the reset.
         * @see #reset(boolean, boolean)
         */
        default void reset(boolean resetDecision) {
            reset(resetDecision, false);
        }
    }
}
