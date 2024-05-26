package com.peternaggschga.gwent.ui.dialogs.cards;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.peternaggschga.gwent.GwentApplication;
import com.peternaggschga.gwent.R;
import com.peternaggschga.gwent.RowType;
import com.peternaggschga.gwent.domain.cases.RemoveUnitsUseCase;
import com.peternaggschga.gwent.ui.dialogs.OverlayDialog;
import com.peternaggschga.gwent.ui.dialogs.addcard.AddCardDialog;

import java.util.Objects;

import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;

/**
 * An OverlayDialog used to list the units of a certain row and enabling the user to copy, add,
 * or delete new units.
 * @see CardListAdapter
 */
public class ShowUnitsDialog extends OverlayDialog {
    /**
     * RowType defining which row is represented by this Dialog.
     *
     * @see #getRow()
     */
    @NonNull
    private final RowType row;

    /**
     * CardListAdapter offering a list of CardUiState objects to the UI.
     */
    @NonNull
    private final CardListAdapter cardListAdapter;

    /**
     * CompositeDisposable keeping track of all subscriptions to observables made by this class.
     * Is being disposed in an android.content.DialogInterface.OnDismissListener that is set in #onCreate().
     *
     * @see android.content.DialogInterface.OnDismissListener
     * @see #onCreate(Bundle)
     */
    @NonNull
    private final CompositeDisposable disposables = new CompositeDisposable();

    /**
     * Constructor of a ShowUnitsDialog shown in the given Context,
     * with the given CardListAdapter and update subscription.
     *
     * @param context            Context this Dialog is shown in.
     * @param row                RowType defining which row all shown units belong to.
     * @param cardListAdapter    CardListAdapter providing an always up-to-date list of CardUiState objects for a certain row.
     * @param updateSubscription Disposable managing the running updates of the given CardListAdapter.
     *                           When disposed, the adapter is not updated anymore.
     */
    private ShowUnitsDialog(@NonNull Context context, @NonNull RowType row,
                            @NonNull CardListAdapter cardListAdapter, @NonNull Disposable updateSubscription) {
        super(context, R.layout.popup_cards);
        this.row = row;
        this.cardListAdapter = cardListAdapter;
        disposables.add(updateSubscription);
    }

    /**
     * Creates a new ShowUnitsDialog in the given Context and for the given row.
     * @param context Context the Dialog is shown in.
     * @param row RowType defining the row that is represented by this Dialog.
     * @return A Single emitting the created ShowUnitsDialog.
     */
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
                                    .map(factory::createCardUiState)
                                    .subscribe(adapter::submitList);
                            return new ShowUnitsDialog(context, row, adapter, updateSubscription);
                        })
                );
    }

    /**
     * Initializes the RecyclerView by connecting it to the #cardListAdapter
     * and sets View.OnClickListener for each button.
     * Also registers a RecyclerView.AdapterDataObserver responsible for scrolling to the end of the RecyclerView
     * whenever an item is being inserted.
     * @todo Initialize RecyclerView only once.
     * @see CardListAdapter#registerAdapterDataObserver(RecyclerView.AdapterDataObserver)
     * @param savedInstanceState If this dialog is being reinitialized after
     *                           the hosting activity was previously shut down, holds the result from
     *                           the most recent call to {@link #onSaveInstanceState}, or null if this
     *                           is the first time.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayoutManager llm = new LinearLayoutManager(getContext());
        llm.setOrientation(RecyclerView.HORIZONTAL);
        RecyclerView recyclerView = findViewById(R.id.cards_list);
        ((DefaultItemAnimator) Objects.requireNonNull(recyclerView.getItemAnimator())).setSupportsChangeAnimations(false);
        recyclerView.setLayoutManager(llm);
        recyclerView.setAdapter(cardListAdapter);

        cardListAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            /** Smoothly scrolls to the last item in the RecyclerView
             * to show the user that an item has been inserted.
             * @see RecyclerView#smoothScrollToPosition(int)
             * @param positionStart Integer defining the first position from where new items are inserted.
             * @param itemCount Integer defining how many items have been inserted.
             */
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                int scrollPosition = positionStart + itemCount - 1;
                if (scrollPosition >= 0) {
                    recyclerView.smoothScrollToPosition(scrollPosition);
                }
            }
        });

        findViewById(R.id.popup_cards_add_button).setOnClickListener(v -> {
            hide();
            new AddCardDialog(ShowUnitsDialog.this).show();
        });

        findViewById(R.id.popup_cards_cancel_button).setOnClickListener(v -> dismiss());

        setOnDismissListener(dialog -> disposables.dispose());
    }

    /**
     * Returns the row this ShowUnitsDialog is representing.
     *
     * @return A RowType defining the represented row.
     * @see #row
     */
    @NonNull
    public RowType getRow() {
        return row;
    }
}
