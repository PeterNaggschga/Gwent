package com.peternaggschga.gwent.ui.dialogs.addcard.filter;

import androidx.annotation.NonNull;

import com.peternaggschga.gwent.data.Ability;
import com.peternaggschga.gwent.data.RowType;

import java.util.Set;

/**
 * Base decorator forwarding requests to the wrapped filter.
 */
abstract class FactionAbilityFilter implements AbilityFilter {
    /**
     * Wrapped filter supplying the baseline card options.
     */
    @NonNull
    protected final AbilityFilter base;

    /**
     * Row whose card options are being filtered.
     */
    @NonNull
    protected final RowType row;

    /**
     * Creates a decorator for the given baseline and row.
     *
     * @param row       Row shared with the wrapped filter.
     */
    FactionAbilityFilter(@NonNull RowType row) {
        this.base = new NeutralAbilityFilter(row);
        this.row = row;
    }

    @Override
    @NonNull
    public Set<Ability> getAbilities(boolean hero) {
        return base.getAbilities(hero);
    }

    @Override
    public int getSquadCount() {
        return base.getSquadCount();
    }

    @Override
    @NonNull
    public Set<Integer> getHeroDamageValues() {
        return base.getHeroDamageValues();
    }
}
