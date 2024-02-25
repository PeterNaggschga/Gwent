package com.peternaggschga.gwent.domain.cases;

import static com.peternaggschga.gwent.domain.cases.ResetDialogUseCase.TRIGGER_BUTTON_CLICK;
import static com.peternaggschga.gwent.domain.cases.ResetDialogUseCase.TRIGGER_FACTION_SWITCH;
import static org.valid4j.Assertive.require;

import android.content.Context;
import android.view.View;
import android.widget.CheckBox;

import androidx.annotation.IntRange;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import com.peternaggschga.gwent.R;

class ResetAlertDialogBuilderAdapter {
    @NonNull
    private final AlertDialog.Builder adapteeBuilder;
    @NonNull
    private final Callback dialogCallback;
    @IntRange(from = TRIGGER_BUTTON_CLICK, to = TRIGGER_FACTION_SWITCH)
    private int trigger = TRIGGER_BUTTON_CLICK;
    private boolean monsterDialog = false;

    public ResetAlertDialogBuilderAdapter(@NonNull Context context, @NonNull Callback dialogCallback) {
        this.adapteeBuilder = new AlertDialog.Builder(context);
        this.dialogCallback = dialogCallback;
    }

    @NonNull
    public AlertDialog create() {
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
        return adapteeBuilder
                .setIconAttribute(android.R.attr.alertDialogIcon)
                .setTitle(R.string.alertDialog_reset_title)
                .setMessage((trigger != TRIGGER_FACTION_SWITCH) ?
                        R.string.alertDialog_reset_msg_default :
                        R.string.alertDialog_reset_msg_faction_switch)
                .setCancelable(trigger != TRIGGER_FACTION_SWITCH)
                .setOnCancelListener(dialog -> dialogCallback.reset(false))
                .setNegativeButton(R.string.alertDialog_reset_negative, (dialog, which) -> dialog.cancel())
                .create();
    }

    @IntRange(from = TRIGGER_BUTTON_CLICK, to = TRIGGER_FACTION_SWITCH)
    public int getTrigger() {
        return trigger;
    }

    @NonNull
    public ResetAlertDialogBuilderAdapter setTrigger(@IntRange(from = TRIGGER_BUTTON_CLICK, to = TRIGGER_FACTION_SWITCH) int trigger) {
        require(TRIGGER_BUTTON_CLICK <= trigger && trigger <= TRIGGER_FACTION_SWITCH);
        this.trigger = trigger;
        return this;
    }

    public boolean isMonsterDialog() {
        return monsterDialog;
    }

    @NonNull
    public ResetAlertDialogBuilderAdapter setMonsterDialog(boolean monsterDialog) {
        this.monsterDialog = monsterDialog;
        return this;
    }

    interface Callback {
        void reset(boolean resetDecision, boolean keepUnit);

        default void reset(boolean resetDecision) {
            reset(resetDecision, false);
        }
    }
}
