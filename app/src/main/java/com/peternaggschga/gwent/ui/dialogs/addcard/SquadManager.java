package com.peternaggschga.gwent.ui.dialogs.addcard;


import android.content.Context;
import android.widget.Toast;

import androidx.annotation.IntRange;
import androidx.annotation.NonNull;

import com.peternaggschga.gwent.R;
import com.peternaggschga.gwent.data.Ability;
import com.peternaggschga.gwent.data.UnitEntity;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Supplies squad membership information and base damage to the CardNumberPickerAdapter.
 * @see CardNumberPickerAdapter
 */
public class SquadManager {
    /**
     * Integer constant defining how many different squads there may be per row.
     */
    public static final int MAX_NR_SQUADS = UnitEntity.BERSERKER_SQUAD - 1;

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
     * Returns the first occupied squad among the selectable squads, or 1 if all are empty.
     *
     * @param squadCount Number of squads selectable for the current faction and row.
     * @return An occupied selectable squad number, or 1.
     * @throws IllegalArgumentException If squadCount is outside the selectable squad range.
     */
    int getFirstSquadWithMembers(@IntRange(from = 1, to = MAX_NR_SQUADS) int squadCount) {
        if (squadCount < 1 || squadCount > MAX_NR_SQUADS) {
            throw new IllegalArgumentException("SquadCount must be in [1, " + MAX_NR_SQUADS + "].");
        }
        return Arrays.stream(states)
                .limit(squadCount)
                .filter(SquadState::hasMembers)
                .findFirst()
                .map(SquadState::getSquadNumber)
                .orElse(1);
    }

    /**
     * Shows squad membership information without modifying a user's damage selection.
     * Can be debounced independently of immediate selection updates.
     *
     * @param squadNumber Selected squad number.
     * @param context Context used to display the Toast.
     * @throws IllegalArgumentException If squadNumber is outside the selectable squad range.
     */
    void showSquadInfo(@IntRange(from = 1, to = MAX_NR_SQUADS) int squadNumber, @NonNull Context context) {
        SquadState squad = getSquadState(squadNumber);
        Toast.makeText(context,
                context.getResources().getQuantityString(R.plurals.popUp_add_card_binding_count,
                        squad.getSquadMembers(), squad.getSquadNumber(), squad.getSquadMembers()),
                Toast.LENGTH_SHORT).show();
    }

    /**
     * Immediately applies an occupied squad's base damage without showing a Toast.
     *
     * @param squadNumber Selected squad number.
     * @param picker      Damage picker updated if the squad has members.
     * @throws IllegalArgumentException If squadNumber is outside the selectable squad range.
     */
    void setSquadDamage(@IntRange(from = 1, to = MAX_NR_SQUADS) int squadNumber,
                        @NonNull DamageValuePicker picker) {
        SquadState squad = getSquadState(squadNumber);
        if (squad.hasMembers()) {
            picker.setValue(squad.getMemberBaseDamage());
        }
    }

    /**
     * Validates a squad number and returns its stored state.
     *
     * @param squadNumber Squad number to look up.
     * @return State of the selected squad.
     * @throws IllegalArgumentException If squadNumber is outside the selectable squad range.
     */
    @NonNull
    private SquadState getSquadState(int squadNumber) {
        if (squadNumber < 1 || squadNumber > MAX_NR_SQUADS) {
            throw new IllegalArgumentException("SquadNumber must be in [1, " + MAX_NR_SQUADS + "] but is " + squadNumber + ".");
        }
        return states[squadNumber - 1];
    }
}
