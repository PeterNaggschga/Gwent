package com.peternaggschga.gwent.ui.dialogs.addcard.filter;

import androidx.annotation.NonNull;

import com.peternaggschga.gwent.data.Ability;
import com.peternaggschga.gwent.data.RowType;

import java.util.Set;

/**
 * Adds Scoia'tael card options to the neutral baseline.
 */
final class ScoiataelFactionAbilityFilter extends FactionAbilityFilter {
    /**
     * Creates a Scoia'tael decorator over the neutral-card baseline.
     *
     * @param row       Row shared with the wrapped filter.
     */
    ScoiataelFactionAbilityFilter(@NonNull RowType row) {
        super(row);
    }

    @Override
    @NonNull
    public Set<Ability> getAbilities(boolean hero) {
        Set<Ability> result = base.getAbilities(hero);
        if (hero && row == RowType.MELEE) {
            result.add(Ability.MORAL_BOOST);
        }
        if (!hero && row == RowType.RANGE) {
            result.add(Ability.MORAL_BOOST);
        }
        if (!hero || row == RowType.RANGE) {
            result.add(Ability.NONE);
        }
        return result;
    }

    @Override
    @NonNull
    public Set<Integer> getHeroDamageValues() {
        Set<Integer> result = base.getHeroDamageValues();
        if (row == RowType.MELEE || row == RowType.RANGE) {
            result.add(10);
        }
        return result;
    }
}
