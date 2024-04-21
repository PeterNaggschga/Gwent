package com.peternaggschga.gwent.ui.dialogs.addcard;

import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.ViewGroup;
import android.widget.NumberPicker;

import androidx.annotation.NonNull;

import com.peternaggschga.gwent.Ability;
import com.peternaggschga.gwent.GwentApplication;
import com.peternaggschga.gwent.R;
import com.peternaggschga.gwent.RowType;

import org.jetbrains.annotations.Contract;

import java.util.Arrays;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import io.reactivex.rxjava3.core.Completable;

/**
 * @todo Documentation
 */
class CardNumberPickerAdapter {
    @NonNull
    protected static final List<Ability> EPIC_UNIT_ABILITIES = Arrays.asList(Ability.NONE, Ability.HORN, Ability.MORAL_BOOST);

    @NonNull
    private final ValuePicker<Boolean> epicPicker;
    @NonNull
    private final DamageValuePicker damagePicker;
    @NonNull
    private final ValuePicker<Ability> abilityPicker;
    @NonNull
    private final NumberPicker squadPicker;
    @NonNull
    private final NumberPicker numberPicker;

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

    @NonNull
    @Contract(pure = true)
    static NumberPicker.OnValueChangeListener getDelayedOnValueChangeListener(@NonNull NumberPicker.OnValueChangeListener originalListener) {
        return (picker, oldVal, newVal) -> new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (newVal == picker.getValue()) {
                originalListener.onValueChange(picker, oldVal, newVal);
            }
        }, 500);
    }

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
