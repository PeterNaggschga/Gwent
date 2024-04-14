package com.peternaggschga.gwent.ui.dialogs.cards;

import static androidx.recyclerview.widget.RecyclerView.NO_ID;
import static org.valid4j.Assertive.require;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.IntRange;
import androidx.annotation.NonNull;
import androidx.core.util.Consumer;
import androidx.recyclerview.widget.RecyclerView;

import com.peternaggschga.gwent.R;

import java.util.List;

/**
 * @todo Documentation
 */
class CardListAdapter extends RecyclerView.Adapter<CardListAdapter.CardViewHolder> {
    @NonNull
    private final Consumer<Integer> onCopy;
    @NonNull
    private final Consumer<Integer> onRemove;
    @NonNull
    private List<CardUiState> items;

    CardListAdapter(@NonNull List<CardUiState> items, @NonNull Consumer<Integer> onCopy,
                    @NonNull Consumer<Integer> onRemove) {
        super();
        this.items = items;
        this.onCopy = onCopy;
        this.onRemove = onRemove;
        setHasStableIds(true);
    }

    void setItems(@NonNull List<CardUiState> items) {
        this.items = items;
        //TODO make more sophisticated (ListAdapter)
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View cardView = LayoutInflater.from(parent.getContext()).inflate(R.layout.card, parent, false);
        return new CardViewHolder(cardView, onCopy, onRemove);
    }

    @Override
    public void onBindViewHolder(@NonNull CardViewHolder holder, @IntRange(from = 0) int position) {
        CardUiState item = items.get(position);
        holder.getDamageView().setText(item.getDamageString());
        holder.getDamageView().setBackgroundResource(item.getDamageBackgroundImageId());
        holder.getDamageView().setTextColor(item.getDamageTextColor());

        if (item.showAbility()) {
            holder.getAbilityView().setImageResource(item.getAbilityImage());
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

    @Override
    public int getItemCount() {
        return items.size();
    }

    @Override
    public long getItemId(@IntRange(from = 0) int position) {
        return items.get(position).getUnitId();
    }

    static class CardViewHolder extends RecyclerView.ViewHolder {
        private int itemId = (int) NO_ID;

        @NonNull
        private final TextView damageView;
        @NonNull
        private final ImageView abilityView;
        @NonNull
        private final TextView bindingView;

        CardViewHolder(@NonNull View itemView, @NonNull Consumer<Integer> onCopy, @NonNull Consumer<Integer> onRemove) {
            super(itemView);

            damageView = itemView.findViewById(R.id.damageView);
            abilityView = itemView.findViewById(R.id.abilityView);
            bindingView = itemView.findViewById(R.id.bindingView);

            itemView.findViewById(R.id.copyButton).setOnClickListener(v -> {
                require(itemId != NO_ID);
                onCopy.accept(itemId);
            });
            itemView.findViewById(R.id.deleteButton).setOnClickListener(v -> {
                // TODO: test if compatible with revenge units
                require(itemId != NO_ID);
                onRemove.accept(itemId);
            });
        }

        @NonNull
        TextView getDamageView() {
            return damageView;
        }

        @NonNull
        ImageView getAbilityView() {
            return abilityView;
        }

        @NonNull
        TextView getBindingView() {
            return bindingView;
        }

        void setItemId(int itemId) {
            this.itemId = itemId;
        }
    }
}
