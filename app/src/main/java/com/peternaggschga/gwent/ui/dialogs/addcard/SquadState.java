package com.peternaggschga.gwent.ui.dialogs.addcard;

import static com.peternaggschga.gwent.ui.dialogs.addcard.SquadManager.MAX_NR_SQUADS;

import androidx.annotation.IntRange;
import androidx.annotation.NonNull;

import com.peternaggschga.gwent.data.UnitEntity;

import java.util.List;
import java.util.stream.Collectors;

/**
 * A data class encapsulating information about the squad defined by #squadNumber, i.e.,
 * how many #squadMembers are there and what is the #memberBaseDamage of units in this squad.
 * @see SquadManager
 */
class SquadState {
    /**
     * Integer containing the number of the represented squad.
     * Equivalent to UnitEntity#squad of the units in the squad.
     *
     * @see #getSquadNumber()
     */
    @IntRange(from = 1, to = MAX_NR_SQUADS)
    private final int squadNumber;

    /**
     * Integer representing the number of members in the squad.
     * @see #getSquadMembers()
     */
    @IntRange(from = 0)
    private final int squadMembers;

    /**
     * Integer containing the base damage of members of this squad.
     * If members have different UnitEntity#damage values, it is undefined which of them is picked.
     * If #squadMembers is 0, #memberBaseDamage defaults to 5.
     * @see #getMemberBaseDamage()
     */
    @IntRange(from = 0)
    private final int memberBaseDamage;

    /**
     * Constructor of a SquadState with the given #squadNumber, #squadMembers, and #memberBaseDamage.
     * Should only be used by #getState().
     *
     * @param squadNumber      Integer containing the number of the represented squad.
     * @param squadMembers     Integer representing the number of members in the squad.
     * @param memberBaseDamage Integer containing the base damage of members of this squad.
     * @see #getState(int, List)
     * @throws org.valid4j.errors.RequireViolation When one of the parameters doesn't meet its IntRange constraint.
     */
    private SquadState(@IntRange(from = 1, to = MAX_NR_SQUADS) int squadNumber,
               @IntRange(from = 0) int squadMembers, @IntRange(from = 0) int memberBaseDamage) {
        // TODO: assert 1 <= squadNumber && squadNumber <= MAX_NR_SQUADS);
        // TODO: assert squadMembers >= 0);
        // TODO: assert memberBaseDamage >= 0);
        this.squadNumber = squadNumber;
        this.squadMembers = squadMembers;
        this.memberBaseDamage = memberBaseDamage;
    }

    /**
     * Creates a new SquadState representing the squad with the given #squadNumber.
     * Information for #squadMembers and #memberBaseDamage is retrieved from the given List of UnitEntity objects.
     * @param squadNumber Integer containing the number of the represented squad.
     * @param units List of UnitEntity objects used to count squad-members.
     * @return A SquadState object that is newly created from the given List of units.
     * @throws org.valid4j.errors.RequireViolation When the given squad number is not between 1 and #MAX_NR_SQUADS.
     */
    @NonNull
    static SquadState getState(@IntRange(from = 1, to = MAX_NR_SQUADS) int squadNumber,
                               @NonNull List<UnitEntity> units) {
        // TODO: assert 1 <= squadNumber && squadNumber <= MAX_NR_SQUADS);
        units = units.stream()
                .filter(unit -> unit.getSquad() != null && unit.getSquad() == squadNumber)
                .collect(Collectors.toList());
        return new SquadState(squadNumber,
                units.size(),
                units.stream().findAny().map(UnitEntity::getDamage).orElse(5));
    }

    /**
     * Returns whether the represented squad has members, i.e., whether #squadMembers is greater than 0.
     * @return A Boolean defining whether the represented squad has members or not.
     */
    boolean hasMembers() {
        return squadMembers > 0;
    }

    /**
     * Returns the number of the represented squad.
     * @see #squadNumber
     * @return An Integer defining which squad is represented.
     */
    @IntRange(from = 1, to = MAX_NR_SQUADS)
    int getSquadNumber() {
        return squadNumber;
    }

    /**
     * Returns the number of members in the represented squad.
     * @see #squadMembers
     * @return An Integer defining how many members are in the represented squad.
     */
    @IntRange(from = 0)
    int getSquadMembers() {
        return squadMembers;
    }

    /**
     * Returns the base damage of members in the represented squad.
     * @see #memberBaseDamage
     * @return An Integer defining the base damage of units in the represented squad.
     */
    @IntRange(from = 0)
    int getMemberBaseDamage() {
        return memberBaseDamage;
    }
}
