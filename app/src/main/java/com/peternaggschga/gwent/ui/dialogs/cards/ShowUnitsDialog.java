package com.peternaggschga.gwent.ui.dialogs.cards;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.peternaggschga.gwent.GwentApplication;
import com.peternaggschga.gwent.R;
import com.peternaggschga.gwent.RowType;
import com.peternaggschga.gwent.ui.dialogs.OverlayDialog;

/**
 * @todo Documentation
 */
public class ShowUnitsDialog extends OverlayDialog {
    @NonNull
    private final RowType row;
    private CardListAdapter cardListAdapter = null;

    public ShowUnitsDialog(@NonNull Context context, @NonNull RowType row) {
        super(context, R.layout.popup_cards);
        this.row = row;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // noinspection CheckResult, ResultOfMethodCallIgnored
        CardListAdapter.getAdapter(getContext(), row)
                .subscribe(cardListAdapter -> {
                    ShowUnitsDialog.this.cardListAdapter = cardListAdapter;
                    LinearLayoutManager llm = new LinearLayoutManager(getContext());
                    llm.setOrientation(RecyclerView.HORIZONTAL);
                    RecyclerView recyclerView = findViewById(R.id.cardsList);
                    recyclerView.setLayoutManager(llm);
                    recyclerView.setAdapter(cardListAdapter);
                });

        findViewById(R.id.popup_cards_add_button).setOnClickListener(v -> {
            dismiss();
            // TODO
        });

        findViewById(R.id.popup_cards_cancel_button).setOnClickListener(v -> dismiss());

        setOnDismissListener(dialog -> {
            // noinspection CheckResult, ResultOfMethodCallIgnored
            GwentApplication.getRepository(getContext()).subscribe(repository ->
                    repository.unregisterObserver(cardListAdapter));
        });
    }
}
