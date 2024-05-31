package com.peternaggschga.gwent.ui.dialogs.addcard;

import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.ViewGroup;
import android.widget.NumberPicker;

import androidx.annotation.NonNull;

import com.peternaggschga.gwent.GwentApplication;
import com.peternaggschga.gwent.R;
import com.peternaggschga.gwent.RowType;
import com.peternaggschga.gwent.data.Ability;

import org.jetbrains.annotations.Contract;

import java.util.Arrays;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import io.reactivex.rxjava3.core.Completable;

/**
 * A helper class responsible for initializing the NumberPicker views of an AddCardDialog
 * (in #CardNumperPickerAdapter())
 * and adding UnitEntity objects with the selected attributes when #addSelectedUnits() is called.
 * @see AddCardDialog
 */
class CardNumberPickerAdapter {
    /**
     * List of Ability values that epic units can have.
     */
    @NonNull
    private static final List<Ability> EPIC_UNIT_ABILITIES = Arrays.asList(Ability.NONE, Ability.HORN, Ability.MORAL_BOOST);

    /**
     * ValuePicker used to decide the value of UnitEntity#epic.
     */
    @NonNull
    private final ValuePicker<Boolean> epicPicker;

    /**
     * DamageValuePicker used to decide the value of UnitEntity#damage.
     */
    @NonNull
    private final DamageValuePicker damagePicker;

    /**
     * ValuePicker used to decide the value of UnitEntity#ability.
     *
     * @see #squadPicker
     */
    @NonNull
    private final ValuePicker<Ability> abilityPicker;

    /**
     * NumberPicker used to decide the value of UnitEntity#squad.
     * Only visible if the value of #abilityPicker is set to Ability#BINDING.
     * @see #abilityPicker
     */
    @NonNull
    private final NumberPicker squadPicker;

    /**
     * NumberPicker used to decide the number of UnitEntity objects that are inserted.
     */
    @NonNull
    private final NumberPicker numberPicker;

    /**
     * Constructor of a CardNumberPickerAdapter
     * managing the NumberPicker views in the given ViewGroup using the given SquadManager.
     * Sets value bounds and NumberPicker.OnValueChangedListener for the pickers in the ViewGroup.
     * The ViewGroup must be the ConstraintLayout with the id R.id#card_layout from popup_add_card.xml.
     * @see R.id#card_layout
     * @param pickerGroup ViewGroup containing the managed NumberPicker views.
     * @param squadManager SquadManager containing up-to-date SquadState.
     */
    CardNumberPickerAdapter(@NonNull ViewGroup pickerGroup, @NonNull SquadManager squadManager) {
        SortedMap<Boolean, Integer> epicStringResources = new TreeMap<>();
        epicStringResources.put(false, R.string.add_picker_epic_normal);
        epicStringResources.put(true, R.string.add_picker_epic_epic);
        epicPicker = new StringValuePicker<>(pickerGroup.findViewById(R.id.popup_add_card_epic_picker),
                epicStringResources,
                false);

        damagePicker = new DamageValuePicker(pickerGroup.findViewById(R.id.popup_add_card_dmg_picker));

        SortedMap<Ability, Integer> abilityStringResources = new TreeMap<>();
        abilityStringResources.put(Ability.NONE, R.string.add_picker_ability_default);
        abilityStringResources.put(Ability.HORN, R.string.add_picker_ability_horn);
        abilityStringResources.put(Ability.REVENGE, R.string.add_picker_ability_revenge);
        abilityStringResources.put(Ability.BINDING, R.string.add_picker_ability_binding);
        abilityStringResources.put(Ability.MORAL_BOOST, R.string.add_picker_ability_moralBoost);
        abilityPicker = new StringValuePicker<>(pickerGroup.findViewById(R.id.popup_add_card_ability_picker),
                abilityStringResources,
                Ability.NONE);

        squadPicker = pickerGroup.findViewById(R.id.popup_add_card_binding_picker);
        squadPicker.setMinValue(1);
        squadPicker.setMaxValue(SquadManager.MAX_NR_SQUADS);

        numberPicker = pickerGroup.findViewById(R.id.popup_add_card_number_picker);
        numberPicker.setMinValue(1);
        numberPicker.setMaxValue(10);

        epicPicker.setOnValueChangedListener((picker, oldVal, newVal) -> {
            damagePicker.setEpicValues(newVal);
            abilityPicker.setSelectableValues(newVal ? EPIC_UNIT_ABILITIES : Arrays.asList(Ability.values()));
        });

        abilityPicker.setOnValueChangedListener((picker, oldVal, newVal) -> {
            if (newVal == Ability.BINDING) {
                squadPicker.setVisibility(View.VISIBLE);
                squadPicker.setValue(squadManager.getFirstSquadWithMembers());
                squadManager.onSquadChanged(squadManager.getFirstSquadWithMembers(), damagePicker);
            } else {
                squadPicker.setVisibility(View.GONE);
            }
        });

        squadPicker.setOnValueChangedListener(
                getDelayedOnValueChangeListener((picker, oldVal, newVal) -> {
                    if (newVal == picker.getValue()) {
                        squadManager.onSquadChanged(newVal, damagePicker);
                    }
                })
        );
    }

    /**
     * Creates a NumberPicker.OnValueChangeListener that only executes the given NumberPicker.OnValueChangeListener after 500 ms
     * if the value has not changed.
     * @see NumberPicker.OnValueChangeListener
     * @param originalListener NumberPicker.OnValueChangeListener that is called when the value does not change.
     * @return A NumberPicker.OnValueChangeListener with delayed execution.
     */
    @NonNull
    @Contract(pure = true)
    static NumberPicker.OnValueChangeListener getDelayedOnValueChangeListener(@NonNull NumberPicker.OnValueChangeListener originalListener) {
        return (picker, oldVal, newVal) -> new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (newVal == picker.getValue()) {
                originalListener.onValueChange(picker, oldVal, newVal);
            }
        }, 500);
    }

    /**
     * Adds new UnitEntity objects the attributes selected by the managed pickers.
     * @param row RowType defining to which row the units are added.
     * @return A Completable tracking operation status.
     */
    @NonNull
    Completable addSelectedUnits(@NonNull RowType row) {
        return GwentApplication.getRepository(numberPicker.getContext())
                .flatMapCompletable(repository ->
                        repository.insertUnit(epicPicker.getValue(),
                                damagePicker.getValue(),
                                abilityPicker.getValue(),
                                squadPicker.getVisibility() == View.VISIBLE ? squadPicker.getValue() : null,
                                row,
                                numberPicker.getValue()));
    }
}
