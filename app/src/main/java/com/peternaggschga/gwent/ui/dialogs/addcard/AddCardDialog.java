package com.peternaggschga.gwent.ui.dialogs.addcard;

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
 * @todo Documentation
 */
public class AddCardDialog extends OverlayDialog {
    @NonNull
    private final RowType row;
    @NonNull
    private final CompositeDisposable disposables = new CompositeDisposable();
    @Nullable
    private CardNumberPickerAdapter pickerAdapter = null;

    /**
     * Constructor of an AddCardDialog in the given Context.
     *
     * @param context Context this Dialog is shown in.
     * @param caller
     */
    public AddCardDialog(@NonNull ShowUnitsDialog caller) {
        super(caller.getContext(), R.layout.popup_add_card);
        this.row = caller.getRow();

        setOnDismissListener(dialog -> {
            disposables.dispose();
            caller.show();
        });
    }

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

        findViewById(R.id.popup_add_card_cancel_button).setOnClickListener(v -> dismiss());
    }
}
