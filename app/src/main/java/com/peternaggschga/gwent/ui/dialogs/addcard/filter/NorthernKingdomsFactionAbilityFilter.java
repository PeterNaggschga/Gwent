package com.peternaggschga.gwent.ui.dialogs.addcard.filter;

import androidx.annotation.NonNull;

import com.peternaggschga.gwent.data.Ability;
import com.peternaggschga.gwent.data.RowType;

import java.util.List;
import java.util.Set;

/**
 * Adds Northern Kingdoms card options to the neutral baseline.
 */
final class NorthernKingdomsFactionAbilityFilter extends FactionAbilityFilter {
    /**
     * Creates a Northern Kingdoms decorator over the neutral-card baseline.
     *
     * @param row       Row shared with the wrapped filter.
     */
    NorthernKingdomsFactionAbilityFilter(@NonNull RowType row) {
        super(row);
    }

    @Override
    @NonNull
    public Set<Ability> getAbilities(boolean hero) {
        Set<Ability> result = base.getAbilities(hero);
        if (!hero) {
            result.addAll(List.of(Ability.BINDING, Ability.MORAL_BOOST));
        }
        if (!hero && row == RowType.SIEGE) {
            result.add(Ability.NONE);
        }
        return result;
    }

    @Override
    public int getSquadCount() {
        switch (row) {
            case MELEE:
                return base.getSquadCount() + 2;
            case RANGE:
            case SIEGE:
                return base.getSquadCount() + 1;
            default:
                return base.getSquadCount();
        }
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
