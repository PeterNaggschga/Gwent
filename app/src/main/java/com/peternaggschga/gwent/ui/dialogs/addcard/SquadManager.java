package com.peternaggschga.gwent.ui.dialogs.addcard;

import static org.valid4j.Assertive.require;

import android.content.Context;
import android.widget.Toast;

import androidx.annotation.IntRange;
import androidx.annotation.NonNull;

import com.peternaggschga.gwent.Ability;
import com.peternaggschga.gwent.R;
import com.peternaggschga.gwent.data.UnitEntity;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A class used by the CardNumberPickerAdapter to determine the first squad with members
 * or set the default damage according to the squads base damage.
 * @see CardNumberPickerAdapter
 */
class SquadManager {
    /**
     * Integer constant defining how many different squads there may be per row.
     */
    public static final int MAX_NR_SQUADS = 3;

    /**
     * Array of SquadState objects containing one SquadState for each possible squad.
     */
    @NonNull
    private final SquadState[] states = new SquadState[MAX_NR_SQUADS];

    /**
     * Constructor of a new SquadManager storing information on the squads of the given units.
     *
     * @param units List of UnitEntity objects that is used to fetch squad information.
     */
    SquadManager(@NonNull List<UnitEntity> units) {
        units = units.stream()
                .filter(unit -> unit.getAbility() == Ability.BINDING)
                .collect(Collectors.toList());
        for (int i = 0; i < MAX_NR_SQUADS; i++) {
            states[i] = SquadState.getState(i + 1, units);
        }
    }

    /**
     * Returns the lowest squad number referring to a squad that has members.
     * If no squad has members, i.e., there are no units with the Ability#BINDING ability, 1 is returned.
     * @return An Integer referencing to the first squad with members.
     */
    int getFirstSquadWithMembers() {
        return Arrays.stream(states)
                .filter(SquadState::hasMembers)
                .findFirst()
                .map(SquadState::getSquadNumber)
                .orElse(1);
    }

    /**
     * Shows a Toast with information about the selected squad
     * and sets the given picker to the SquadState#memberBaseDamage of said squad.
     *
     * @param newVal Integer representing the newly selected squad.
     * @param picker DamageValuePicker that is updated.
     * @throws org.valid4j.errors.RequireViolation When newVal is not between 1 and #MAX_NR_SQUADS.
     * @see SquadState#getMemberBaseDamage()
     */
    void onSquadChanged(@IntRange(from = 1, to = MAX_NR_SQUADS) int newVal, @NonNull DamageValuePicker picker) {
        require(1 <= newVal && newVal <= MAX_NR_SQUADS);
        SquadState squad = states[newVal - 1];

        Context context = picker.getContext();
        Toast.makeText(context,
                context.getString(R.string.popUp_add_card_binding_count, squad.getSquadNumber(), squad.getSquadMembers()),
                Toast.LENGTH_SHORT).show();

        if (squad.hasMembers()) {
            picker.setValue(squad.getMemberBaseDamage());
        }
    }
}
