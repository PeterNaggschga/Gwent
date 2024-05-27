package com.peternaggschga.gwent.ui.dialogs.addcard;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.peternaggschga.gwent.GwentApplication;
import com.peternaggschga.gwent.R;
import com.peternaggschga.gwent.RowType;
import com.peternaggschga.gwent.data.UnitRepository;
import com.peternaggschga.gwent.ui.dialogs.OverlayDialog;
import com.peternaggschga.gwent.ui.dialogs.cards.ShowUnitsDialog;

import io.reactivex.rxjava3.disposables.CompositeDisposable;

/**
 * An OverlayDialog used to add new UnitEntity objects to a certain #row.
 */
public class AddCardDialog extends OverlayDialog {
    /**
     * RowType defining the row the new UnitEntity objects are added to.
     */
    @NonNull
    private final RowType row;

    /**
     * CompositeDisposable keeping track of all subscriptions to observables made by this class.
     * Is being disposed in an android.content.DialogInterface.OnDismissListener that is set in #AddCardDialog(ShowUnitsDialog)
     * and #AddCardDialog(Context, RowType).
     *
     * @see android.content.DialogInterface.OnDismissListener
     * @see #AddCardDialog(ShowUnitsDialog)
     * @see #AddCardDialog(Context, RowType)
     */
    @NonNull
    private final CompositeDisposable disposables = new CompositeDisposable();

    /**
     * CardNumberPickerAdapter managing the connection to the NumberPicker views of this Dialog.
     * Is lazily initialized in #onCreate().
     *
     * @see #onCreate(Bundle)
     * @see R.id#card_layout
     */
    @Nullable
    private CardNumberPickerAdapter pickerAdapter = null;

    /**
     * Constructor of an AddCardDialog called by the given ShowUnitsDialog.
     * Sets a Dialog.OnDismissListener disposing #disposables and showing the caller again.
     * Wrapper of #AddCardDialog(Context, RowType).
     * @see #AddCardDialog(Context, RowType)
     * @param caller ShowUnitsDialog that called this Dialog.
     */
    public AddCardDialog(@NonNull ShowUnitsDialog caller) {
        this(caller.getContext(), caller.getRow());

        setOnDismissListener(dialog -> {
            disposables.dispose();
            caller.show();
        });
    }

    /**
     * Constructor of an AddCardDialog in the given Context and for the given row.
     * Sets a Dialog.OnDismissListener disposing #disposables and showing the caller again.
     *
     * @param context Context this Dialog is shown in.
     * @param row     RowType defining which row the new UnitEntity objects are added to.
     */
    public AddCardDialog(@NonNull Context context, @NonNull RowType row) {
        super(context, R.layout.popup_add_card, R.id.popup_add_card_cancel_button);
        this.row = row;

        setOnDismissListener(dialog -> disposables.dispose());
    }

    /**
     * Initializes the #pickerAdapter
     * if not yet done so and sets View.OnClickListener for each button.
     *
     * @param savedInstanceState If this dialog is being reinitialized after
     *                           the hosting activity was previously shut down, holds the result from
     *                           the most recent call to {@link #onSaveInstanceState}, or null if this
     *                           is the first time.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (pickerAdapter == null) {
            disposables.add(
                    GwentApplication.getRepository(getContext())
                            .flatMap(UnitRepository::getUnits)
                            .map(SquadManager::new)
                            .map(squadManager ->
                                    new CardNumberPickerAdapter(findViewById(R.id.card_layout), squadManager))
                            .subscribe(cardNumberPickerAdapter -> pickerAdapter = cardNumberPickerAdapter)
            );
        }

        findViewById(R.id.popup_add_card_save_button).setOnClickListener(v -> {
            if (pickerAdapter != null) {
                disposables.add(pickerAdapter.addSelectedUnits(row).subscribe(this::dismiss));
            }
        });
    }
}
