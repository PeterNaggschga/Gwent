package com.peternaggschga.gwent.ui.dialogs.addcard.filter;

import androidx.annotation.NonNull;

import com.peternaggschga.gwent.data.Ability;
import com.peternaggschga.gwent.data.RowType;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * The neutral-card baseline used by every faction's filter.
 */
final class NeutralAbilityFilter implements AbilityFilter {
    /**
     * Row whose neutral-card options are being filtered.
     */
    @NonNull
    private final RowType row;

    /**
     * Creates the neutral-card baseline for a row.
     *
     * @param row Row whose neutral cards supply the available options.
     */
    NeutralAbilityFilter(@NonNull RowType row) {
        this.row = row;
    }

    @Override
    @NonNull
    public Set<Ability> getAbilities(boolean hero) {
        if (hero) {
            switch (row) {
                case MELEE:
                case RANGE:
                    return new HashSet<>(List.of(Ability.NONE));
                case SIEGE:
                    return new HashSet<>();
            }
        }
        Set<Ability> result = new HashSet<>(List.of(Ability.NONE));
        switch (row) {
            case MELEE:
                result.add(Ability.HORN);
                break;
            case RANGE:
                result.add(Ability.REVENGE);
                break;
        }
        return result;
    }

    @Override
    public int getSquadCount() {
        return 0;
    }

    @Override
    @NonNull
    public Set<Integer> getHeroDamageValues() {
        switch (row) {
            case MELEE:
                return new HashSet<>(List.of(0, 7, 15));
            case RANGE:
                return new HashSet<>(List.of(7));
            case SIEGE:
            default:
                return new HashSet<>();
        }
    }
}
