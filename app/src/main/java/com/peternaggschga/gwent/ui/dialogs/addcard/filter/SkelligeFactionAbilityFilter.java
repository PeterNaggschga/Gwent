package com.peternaggschga.gwent.ui.dialogs.addcard.filter;

import androidx.annotation.NonNull;

import com.peternaggschga.gwent.data.Ability;
import com.peternaggschga.gwent.data.RowType;

import java.util.List;
import java.util.Set;

/**
 * Adds Skellige card options to the neutral baseline.
 */
final class SkelligeFactionAbilityFilter extends FactionAbilityFilter {
    /**
     * Creates a Skellige decorator over the neutral-card baseline.
     *
     * @param row       Row shared with the wrapped filter.
     */
    SkelligeFactionAbilityFilter(@NonNull RowType row) {
        super(row);
    }

    @Override
    @NonNull
    public Set<Ability> getAbilities(boolean hero) {
        Set<Ability> result = base.getAbilities(hero);
        if (hero) {
            switch (row) {
                case RANGE:
                    result.add(Ability.MARDROEME);
                case MELEE:
                    result.add(Ability.NONE);
                    break;
            }
            return result;
        }
        result.add(Ability.NONE);
        switch (row) {
            case MELEE:
                result.add(Ability.BINDING);
            case RANGE:
                result.addAll(List.of(Ability.MORAL_BOOST, Ability.BERSERKER, Ability.REVENGE));
                break;
            case SIEGE:
                result.addAll(List.of(Ability.BINDING, Ability.HORN));
                break;
        }
        return result;
    }

    @Override
    public int getSquadCount() {
        switch (row) {
            case MELEE:
                return base.getSquadCount() + 2;
            case SIEGE:
                return base.getSquadCount() + 1;
            case RANGE:
            default:
                return base.getSquadCount();
        }
    }

    @Override
    @NonNull
    public Set<Integer> getHeroDamageValues() {
        Set<Integer> result = base.getHeroDamageValues();
        switch (row) {
            case MELEE:
                result.add(10);
                break;
            case RANGE:
                result.addAll(List.of(8, 10));
                break;
        }
        return result;
    }
}
