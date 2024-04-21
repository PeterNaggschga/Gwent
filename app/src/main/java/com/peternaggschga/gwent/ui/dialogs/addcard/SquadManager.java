package com.peternaggschga.gwent.ui.dialogs.addcard;

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
 * @todo Documentation
 * @todo Add testing.
 */
class SquadManager {
    public static final int MAX_NR_SQUADS = 3;
    @NonNull
    private final SquadState[] states = new SquadState[MAX_NR_SQUADS];

    SquadManager(@NonNull List<UnitEntity> units) {
        units = units.stream()
                .filter(unit -> unit.getAbility() == Ability.BINDING)
                .collect(Collectors.toList());
        for (int i = 0; i < MAX_NR_SQUADS; i++) {
            states[i] = SquadState.getState(i + 1, units);
        }
    }

    int getFirstSquadWithMembers() {
        return Arrays.stream(states)
                .filter(SquadState::hasMembers)
                .findFirst()
                .map(SquadState::getSquadNumber)
                .orElse(1);
    }

    void onSquadChanged(@IntRange(from = 1, to = 3) int newVal, @NonNull DamageValuePicker picker) {
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
