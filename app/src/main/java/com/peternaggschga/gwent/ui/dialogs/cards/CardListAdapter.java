package com.peternaggschga.gwent.ui.dialogs.cards;

import static androidx.recyclerview.widget.RecyclerView.NO_ID;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.IntRange;
import androidx.annotation.NonNull;
import androidx.core.util.Consumer;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.peternaggschga.gwent.R;

/**
 * A ListAdapter used to provide CardListAdapter.CardViewHolder objects
 * created from CardUiState objects to a RecyclerView.
 * @see CardUiState
 * @see CardListAdapter.CardViewHolder
 * @see ListAdapter
 * @see RecyclerView
 */
class CardListAdapter extends ListAdapter<CardUiState, CardListAdapter.CardViewHolder> {
    /**
     * Consumer that is used in #onCreateViewHolder(ViewGroup, int)
     * as CardListAdapter.CardViewHolder#onCopy.
     * Is called
     * when the user clicks on the copy-button of an element with the UnitEntity#id of the represented UnitEntity.
     *
     * @see #onCreateViewHolder(ViewGroup, int)
     * @see CardListAdapter.CardViewHolder#onCopy
     */
    @NonNull
    private final Consumer<Integer> onCopy;

    /**
     * Consumer that is used in #onCreateViewHolder(ViewGroup, int)
     * as CardListAdapter.CardViewHolder#onRemove.
     * Is called
     * when the user clicks on the delete-button of an element with the UnitEntity#id of the represented UnitEntity.
     * @see #onCreateViewHolder(ViewGroup, int)
     * @see CardListAdapter.CardViewHolder#onRemove
     */
    @NonNull
    private final Consumer<Integer> onRemove;

    /**
     * Constructor of a CardListAdapter with the given #onCopy and #onRemove callbacks.
     * Calls super-constructor ListAdapter#ListAdapter(DiffUtil.ItemCallback)
     * with CardUiState#DIFF_CALLBACK.
     * Also calls #setHasStableIds() since #getItemId() returns the stable UnitEntity#id.
     * @see ListAdapter#ListAdapter(DiffUtil.ItemCallback)
     * @see CardUiState#DIFF_CALLBACK
     * @see #setHasStableIds(boolean)
     * @param onCopy Consumer that is called with the UnitEntity#id,
     *               when the copy-button of the representing card is clicked.
     * @param onRemove Consumer that is called with the UnitEntity#id,
     *                 when the delete-button of the representing card is clicked.
     */
    CardListAdapter(@NonNull Consumer<Integer> onCopy, @NonNull Consumer<Integer> onRemove) {
        super(CardUiState.DIFF_CALLBACK);
        this.onCopy = onCopy;
        this.onRemove = onRemove;
        setHasStableIds(true);
    }

    /**
     * Creates a new CardViewHolder managing a new card view that is inflated from the given parent.
     * @param parent The ViewGroup into which the new View will be added after it is bound to
     *               an adapter position.
     * @param viewType The view type of the new View.
     * @return A CardViewHolder object for a new card layout and with #onCopy and #onRemove callbacks.
     */
    @NonNull
    @Override
    public CardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View cardView = LayoutInflater.from(parent.getContext()).inflate(R.layout.card, parent, false);
        return new CardViewHolder(cardView, onCopy, onRemove);
    }

    /**
     * Changes the views of CardViewHolder to represent the item at the given position.
     * @param holder The CardViewHolder which should be updated to represent the contents of the
     *        item at the given position in the data set.
     * @param position The position of the item within the adapter's data set.
     */
    @Override
    public void onBindViewHolder(@NonNull CardViewHolder holder, @IntRange(from = 0) int position) {
        CardUiState item = getItem(position);
        holder.getDamageView().setText(item.getDamageString());
        holder.getDamageView().setBackgroundResource(item.getDamageBackgroundImageId());
        holder.getDamageView().setTextColor(item.getDamageTextColor());

        if (item.showAbility()) {
            holder.getAbilityView().setImageResource(item.getAbilityImageId());
            holder.getAbilityView().setVisibility(View.VISIBLE);
        } else {
            holder.getAbilityView().setVisibility(View.GONE);
        }

        if (item.showSquad()) {
            holder.getBindingView().setText(item.getSquadString());
            holder.getBindingView().setVisibility(View.VISIBLE);
        } else {
            holder.getBindingView().setVisibility(View.GONE);
        }

        holder.setItemId(item.getUnitId());
    }

    /**
     * Returns the UnitEntity#id of the UnitEntity represented at the given position.
     * @param position Adapter position to query.
     * @return A Long referencing the UnitEntity#id of the UnitEntity represented at the given position.
     */
    @Override
    public long getItemId(@IntRange(from = 0) int position) {
        return getItem(position).getUnitId();
    }

    /**
     * A RecyclerView.ViewHolder class managing a card view
     * that shows a representation of the UnitEntity referenced by #itemId.
     * @see RecyclerView.ViewHolder
     */
    static class CardViewHolder extends RecyclerView.ViewHolder {
        /**
         * TextView showing the (de-)buffed damage of the represented UnitEntity.
         * @see #getDamageView()
         */
        @NonNull
        private final TextView damageView;
        /**
         * ImageView showing an image of the UnitEntity#ability of the represented UnitEntity.
         * If UnitEntity#ability is Ability#NONE, this view's visibility should be View#GONE.
         * @see #getAbilityView()
         */
        @NonNull
        private final ImageView abilityView;
        /**
         * TextView showing the UnitEntity#squad
         * of the represented UnitEntity if UnitEntity#squad is Ability#BINDING.
         * If UnitEntity#ability is not Ability#BINDING, this view's visibility should be View#GONE.
         * @see #getBindingView()
         */
        @NonNull
        private final TextView bindingView;
        /**
         * Integer referencing the UnitEntity#id of the represented UnitEntity.
         * Is initialized with #NO_ID and therefore must be set using #setItemId()
         * in #onBindViewHolder(CardViewHolder, int).
         *
         * @see #setItemId(int)
         * @see #onBindViewHolder(CardViewHolder, int)
         */
        private int itemId = (int) NO_ID;

        /**
         * Constructor of a CardViewHolder for the given View initializing #damageView,
         * #abilityView, and #bindingView.
         * Sets View.OnClickListener on copy-button and delete-button
         * calling the given onCopy and onRemove callbacks with #unitId.
         *
         * @param itemView View with the card layout.
         * @param onCopy   Consumer that is called with #unitId, when the copy-button of the given View is clicked.
         * @param onRemove Consumer that is called with #unitId, when the delete-button of the given View is clicked.
         */
        CardViewHolder(@NonNull View itemView,
                       @NonNull Consumer<Integer> onCopy, @NonNull Consumer<Integer> onRemove) {
            super(itemView);

            damageView = itemView.findViewById(R.id.damageView);
            abilityView = itemView.findViewById(R.id.abilityView);
            bindingView = itemView.findViewById(R.id.bindingView);

            itemView.findViewById(R.id.copyButton).setOnClickListener(v -> {
                // TODO: assert itemId > NO_ID);
                onCopy.accept(itemId);
            });
            itemView.findViewById(R.id.deleteButton).setOnClickListener(v -> {
                // TODO: assert itemId > NO_ID);
                onRemove.accept(itemId);
            });
        }

        /**
         * Returns the #damageView of the managed View.
         * @return A TextView showing the damage of the represented UnitEntity.
         * @see #damageView
         */
        @NonNull
        TextView getDamageView() {
            return damageView;
        }

        /**
         * Returns the #abilityView of the managed View.
         * @return An ImageView showing the UnitEntity#ability of the represented UnitEntity.
         * @see #abilityView
         */
        @NonNull
        ImageView getAbilityView() {
            return abilityView;
        }

        /**
         * Returns the #bindingView of the managed View.
         * @return A TextView showing the UnitEntity#squad of the represented UnitEntity.
         * @see #bindingView
         */
        @NonNull
        TextView getBindingView() {
            return bindingView;
        }

        /**
         * Sets the #unitId of this ViewHolder.
         *
         * @param itemId Integer referencing the UnitEntity#id of the represented UnitEntity.
         * @throws org.valid4j.errors.RequireViolation When itemid is negative.
         * @see #itemId
         */
        void setItemId(int itemId) {
            // TODO: assert itemId != NO_ID);
            this.itemId = itemId;
        }
    }
}
