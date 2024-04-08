package com.peternaggschga.gwent.ui.dialogs.cards;

import static androidx.recyclerview.widget.RecyclerView.NO_ID;
import static com.peternaggschga.gwent.Ability.BINDING;
import static com.peternaggschga.gwent.Ability.NONE;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.annotation.IntRange;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.peternaggschga.gwent.GwentApplication;
import com.peternaggschga.gwent.R;
import com.peternaggschga.gwent.RowType;
import com.peternaggschga.gwent.data.Observer;
import com.peternaggschga.gwent.data.UnitEntity;
import com.peternaggschga.gwent.data.UnitRepository;
import com.peternaggschga.gwent.domain.cases.DamageCalculatorUseCase;
import com.peternaggschga.gwent.domain.cases.RemoveUnitsUseCase;
import com.peternaggschga.gwent.domain.damage.DamageCalculator;

import java.util.List;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;

/**
 * @todo Documentation
 */
class CardListAdapter extends RecyclerView.Adapter<CardListAdapter.CardViewHolder> implements Observer {
    @NonNull
    private final UnitRepository repository;
    @NonNull
    private final RowType row;
    @ColorInt
    private final int defaultColor;
    @ColorInt
    private final int buffColor;
    @ColorInt
    private final int debuffColor;
    @NonNull
    private final RemoveItemCallback removeItemCallback;
    @NonNull
    private List<UnitEntity> items;
    @NonNull
    private DamageCalculator damageCalculator;

    private CardListAdapter(@NonNull UnitRepository repository, @NonNull RowType row,
                            @NonNull List<UnitEntity> items, @NonNull DamageCalculator damageCalculator,
                            @ColorInt int defaultColor, @ColorInt int buffColor,
                            @ColorInt int debuffColor, @NonNull RemoveItemCallback removeItemCallback) {
        super();
        this.repository = repository;
        this.row = row;
        this.items = items;
        this.damageCalculator = damageCalculator;
        this.defaultColor = defaultColor;
        this.buffColor = buffColor;
        this.debuffColor = debuffColor;
        this.removeItemCallback = removeItemCallback;
        setHasStableIds(true);
    }

    @NonNull
    static Single<CardListAdapter> getAdapter(@NonNull Context context, @NonNull RowType row) {
        int defaultColor = context.getColor(R.color.color_damage_textColor);
        int buffColor = context.getColor(R.color.color_damage_textColor_buffed);
        int debuffColor = context.getColor(R.color.color_damage_textColor_debuffed);

        return GwentApplication.getRepository(context)
                .flatMap(repository -> {
                    RemoveItemCallback callback = new RemoveItemCallback() {
                        @NonNull
                        @Override
                        public Completable removeItem(int id) {
                            return RemoveUnitsUseCase.remove(context, repository, id);
                        }
                    };
                    return DamageCalculatorUseCase.getDamageCalculator(repository, row)
                            .zipWith(repository.getUnits(), (damageCalculator, unitEntities) ->
                                    new CardListAdapter(repository,
                                            row,
                                            unitEntities,
                                            damageCalculator,
                                            defaultColor,
                                            buffColor,
                                            debuffColor,
                                            callback))
                            .doOnSuccess(repository::registerObserver);
                });
    }

    @NonNull
    @Override
    public CardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View cardView = LayoutInflater.from(parent.getContext()).inflate(R.layout.card, parent, false);
        return new CardViewHolder(cardView);
    }

    @Override
    public void onBindViewHolder(@NonNull CardViewHolder holder, @IntRange(from = 0) int position) {
        UnitEntity item = items.get(position);

        if (item.isEpic()) {
            holder.getDamageView().setText("");
            switch (item.calculateDamage(damageCalculator)) {
                case 0:
                default:
                    holder.getDamageView().setBackgroundResource(R.drawable.icon_epic_damage_0);
                    break;
                case 7:
                    holder.getDamageView().setBackgroundResource(R.drawable.icon_epic_damage_7);
                    break;
                case 8:
                    holder.getDamageView().setBackgroundResource(R.drawable.icon_epic_damage_8);
                    break;
                case 10:
                    holder.getDamageView().setBackgroundResource(R.drawable.icon_epic_damage_10);
                    break;
                case 11:
                    holder.getDamageView().setBackgroundResource(R.drawable.icon_epic_damage_11);
                    break;
                case 15:
                    holder.getDamageView().setBackgroundResource(R.drawable.icon_epic_damage_15);
                    break;
            }
        } else {
            holder.getDamageView().setBackgroundResource(R.drawable.icon_damage_background);
            holder.getDamageView().setText(String.valueOf(item.calculateDamage(damageCalculator)));
            switch (item.isBuffed(damageCalculator)) {
                default:
                case DEFAULT:
                    holder.getDamageView().setTextColor(defaultColor);
                    break;
                case BUFFED:
                    holder.getDamageView().setTextColor(buffColor);
                    break;
                case DEBUFFED:
                    holder.getDamageView().setTextColor(debuffColor);
            }
        }

        if (item.getAbility() == NONE) {
            holder.getAbilityView().setVisibility(View.GONE);
            holder.getBindingView().setVisibility(View.GONE);
        } else if (item.getAbility() == BINDING) {
            holder.getAbilityView().setImageResource(R.drawable.icon_binding);
            holder.getAbilityView().setVisibility(View.VISIBLE);
            holder.getBindingView().setText(String.valueOf(item.getSquad()));
            holder.getBindingView().setVisibility(View.VISIBLE);
        } else {
            switch (item.getAbility()) {
                case HORN:
                    holder.getAbilityView().setImageResource(R.drawable.icon_horn);
                    break;
                case REVENGE:
                    holder.getAbilityView().setImageResource(R.drawable.icon_revenge);
                    break;
                case MORAL_BOOST:
                    holder.getAbilityView().setImageResource(R.drawable.icon_moral_boost);
            }
            holder.getAbilityView().setVisibility(View.VISIBLE);
            holder.getBindingView().setVisibility(View.GONE);
        }

        holder.setItemId(item.getId());
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    @Override
    public long getItemId(@IntRange(from = 0) int position) {
        return items.get(position).getId();
    }

    @NonNull
    @Override
    public Completable update() {
        return DamageCalculatorUseCase.getDamageCalculator(repository, row)
                .doOnSuccess(damageCalculator -> CardListAdapter.this.damageCalculator = damageCalculator)
                .ignoreElement()
                .andThen(repository.getUnits(row))
                .doOnSuccess(unitEntities -> items = unitEntities)
                .ignoreElement();
    }

    private interface RemoveItemCallback {
        @NonNull
        Completable removeItem(int id);
    }

    class CardViewHolder extends RecyclerView.ViewHolder {
        private int itemId = (int) NO_ID;

        @NonNull
        private final TextView damageView;
        @NonNull
        private final ImageView abilityView;
        @NonNull
        private final TextView bindingView;

        CardViewHolder(@NonNull View itemView) {
            super(itemView);

            damageView = itemView.findViewById(R.id.damageView);
            abilityView = itemView.findViewById(R.id.abilityView);
            bindingView = itemView.findViewById(R.id.bindingView);

            itemView.findViewById(R.id.copyButton).setOnClickListener(v -> {
                // noinspection CheckResult, ResultOfMethodCallIgnored
                repository.copy(itemId)
                        .subscribe(() -> {
                            // TODO use ListAdapter for changes
                            notifyItemInserted(items.size() - 1);
                            notifyItemRangeChanged(0, items.size() - 2);
                        });
            });
            itemView.findViewById(R.id.deleteButton).setOnClickListener(v -> {
                // TODO: test if compatible with revenge units
                int itemsBeforeRemoval = items.size();
                int oldPosition = getAdapterPosition();

                // noinspection CheckResult, ResultOfMethodCallIgnored
                removeItemCallback.removeItem(itemId)
                        .subscribe(() -> {
                            boolean sizeChanged = itemsBeforeRemoval != items.size();
                            if (sizeChanged) {
                                notifyItemRemoved(oldPosition);
                                notifyItemRangeChanged(0, items.size() - 1);
                            } else {
                                // TODO: test if moved item is automatically updated
                                notifyItemMoved(oldPosition, items.size() - 1);
                            }
                        });
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
