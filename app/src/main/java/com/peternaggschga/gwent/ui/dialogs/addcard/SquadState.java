package com.peternaggschga.gwent.ui.dialogs.addcard;

import static com.peternaggschga.gwent.ui.dialogs.addcard.SquadManager.MAX_NR_SQUADS;

import androidx.annotation.IntRange;
import androidx.annotation.NonNull;

import com.peternaggschga.gwent.data.UnitEntity;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @todo Documentation
 * @todo Add testing.
 */
class SquadState {
    @IntRange(from = 1, to = MAX_NR_SQUADS)
    private final int squadNumber;

    @IntRange(from = 0)
    private final int squadMembers;

    @IntRange(from = 0)
    private final int memberBaseDamage;

    SquadState(@IntRange(from = 1, to = MAX_NR_SQUADS) int squadNumber,
               @IntRange(from = 0) int squadMembers, @IntRange(from = 0) int memberBaseDamage) {
        this.squadNumber = squadNumber;
        this.squadMembers = squadMembers;
        this.memberBaseDamage = memberBaseDamage;
    }

    @NonNull
    static SquadState getState(@IntRange(from = 1, to = MAX_NR_SQUADS) int squadNumber,
                               @NonNull List<UnitEntity> units) {
        units = units.stream()
                .filter(unit -> unit.getSquad() != null && unit.getSquad() == squadNumber)
                .collect(Collectors.toList());
        return new SquadState(squadNumber,
                units.size(),
                units.stream().findAny().map(UnitEntity::getDamage).orElse(5));
    }

    boolean hasMembers() {
        return squadMembers > 0;
    }

    @IntRange(from = 1, to = MAX_NR_SQUADS)
    int getSquadNumber() {
        return squadNumber;
    }

    @IntRange(from = 0)
    int getSquadMembers() {
        return squadMembers;
    }

    @IntRange(from = 0)
    int getMemberBaseDamage() {
        return memberBaseDamage;
    }
}
