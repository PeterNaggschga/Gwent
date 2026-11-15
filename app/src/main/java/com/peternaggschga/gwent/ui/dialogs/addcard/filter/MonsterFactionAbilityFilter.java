package com.peternaggschga.gwent.ui.dialogs.addcard.filter;

import androidx.annotation.NonNull;

import com.peternaggschga.gwent.data.Ability;
import com.peternaggschga.gwent.data.RowType;

import java.util.List;
import java.util.Set;

/**
 * Adds Monster card options to the neutral baseline.
 */
final class MonsterFactionAbilityFilter extends FactionAbilityFilter {
    /**
     * Creates a Monster decorator over the neutral-card baseline.
     *
     * @param row       Row shared with the wrapped filter.
     */
    MonsterFactionAbilityFilter(@NonNull RowType row) {
        super(row);
    }

    @Override
    @NonNull
    public Set<Ability> getAbilities(boolean hero) {
        Set<Ability> result = base.getAbilities(hero);
        if (!hero) {
            result.add(Ability.NONE);
            return result;
        }
        switch (row) {
            case RANGE:
                result.add(Ability.MORAL_BOOST);
            case MELEE:
                result.add(Ability.NONE);
                break;
        }
        return result;
    }

    @Override
    @NonNull
    public Set<Integer> getHeroDamageValues() {
        Set<Integer> result = base.getHeroDamageValues();
        if (row != RowType.SIEGE) {
            result.addAll(List.of(8, 10));
        }
        return result;
    }
}
