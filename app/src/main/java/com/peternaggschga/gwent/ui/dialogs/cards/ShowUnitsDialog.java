package com.peternaggschga.gwent.ui.dialogs.cards;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.peternaggschga.gwent.GwentApplication;
import com.peternaggschga.gwent.R;
import com.peternaggschga.gwent.RowType;
import com.peternaggschga.gwent.domain.cases.RemoveUnitsUseCase;
import com.peternaggschga.gwent.ui.dialogs.OverlayDialog;

import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.disposables.Disposable;

/**
 * @todo Documentation
 */
public class ShowUnitsDialog extends OverlayDialog {
    @NonNull
    private final CardListAdapter cardListAdapter;
    @NonNull
    private final Disposable updateSubscription;

    private ShowUnitsDialog(@NonNull Context context, @NonNull CardListAdapter cardListAdapter,
                            @NonNull Disposable updateSubscription) {
        super(context, R.layout.popup_cards);
        this.cardListAdapter = cardListAdapter;
        this.updateSubscription = updateSubscription;
    }

    @NonNull
    public static Single<ShowUnitsDialog> getDialog(@NonNull Context context, @NonNull RowType row) {
        return GwentApplication.getRepository(context)
                .flatMap(repository -> repository.isWeather(row)
                        .zipWith(repository.isHorn(row), (weather, horn) ->
                                new CardUiStateFactory(context, weather, horn))
                        .map(factory -> {
                            CardListAdapter adapter = new CardListAdapter(
                                    id -> repository.copy(id).subscribe(),
                                    id -> RemoveUnitsUseCase.remove(context, repository, id).subscribe()
                            );
                            Disposable updateSubscription = repository.getUnitsFlowable(row)
                                    .subscribe(units ->
                                            adapter.submitList(factory.createCardUiState(units)));
                            return new ShowUnitsDialog(context, adapter, updateSubscription);
                        })
                );
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayoutManager llm = new LinearLayoutManager(getContext());
        llm.setOrientation(RecyclerView.HORIZONTAL);
        RecyclerView recyclerView = findViewById(R.id.cardsList);
        recyclerView.setLayoutManager(llm);
        recyclerView.setAdapter(cardListAdapter);

        findViewById(R.id.popup_cards_add_button).setOnClickListener(v -> {
            dismiss();
            // TODO
        });

        findViewById(R.id.popup_cards_cancel_button).setOnClickListener(v -> dismiss());

        setOnDismissListener(dialog -> updateSubscription.dispose());
    }
}
