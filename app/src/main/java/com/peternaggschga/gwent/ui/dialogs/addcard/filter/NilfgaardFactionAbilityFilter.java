package com.peternaggschga.gwent.ui.dialogs.addcard.filter;

import androidx.annotation.NonNull;

import com.peternaggschga.gwent.data.Ability;
import com.peternaggschga.gwent.data.RowType;

import java.util.Set;

/**
 * Adds Nilfgaard card options to the neutral baseline.
 */
final class NilfgaardFactionAbilityFilter extends FactionAbilityFilter {
    /**
     * Creates a Nilfgaard decorator over the neutral-card baseline.
     *
     * @param row       Row shared with the wrapped filter.
     */
    NilfgaardFactionAbilityFilter(@NonNull RowType row) {
        super(row);
    }

    @Override
    @NonNull
    public Set<Ability> getAbilities(boolean hero) {
        Set<Ability> result = base.getAbilities(hero);
        result.add(Ability.NONE);
        if (!hero && row == RowType.MELEE) {
            result.add(Ability.BINDING);
        }
        return result;
    }

    @Override
    public int getSquadCount() {
        return row == RowType.MELEE ? base.getSquadCount() + 3 : base.getSquadCount();
    }

    @Override
    @NonNull
    public Set<Integer> getHeroDamageValues() {
        Set<Integer> result = base.getHeroDamageValues();
        result.add(10);
        return result;
    }
}
