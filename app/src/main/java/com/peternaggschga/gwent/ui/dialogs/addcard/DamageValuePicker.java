package com.peternaggschga.gwent.ui.dialogs.addcard;

import android.widget.NumberPicker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Collection;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @todo Documentation
 * @todo Add testing.
 */
class DamageValuePicker extends ValuePicker<Integer> {
    @NonNull
    private static final Integer[] EPIC_DAMAGE_VALUES = {0, 7, 8, 10, 11, 15};
    private boolean epicValues = false;

    DamageValuePicker(@NonNull NumberPicker picker) {
        super(picker, getEpicDamageValues());
        setEpicValues(false);
    }

    @NonNull
    private static SortedMap<Integer, Integer> getEpicDamageValues() {
        SortedMap<Integer, Integer> epicDamageValues = new TreeMap<>();
        int i = 0;
        for (Integer damage : EPIC_DAMAGE_VALUES) {
            epicDamageValues.put(i++, damage);
        }
        return epicDamageValues;
    }

    @Override
    @NonNull
    protected String getDisplayString(@NonNull Integer value) {
        return epicValues ? String.valueOf(displayIntegers.get(value)) : String.valueOf(value);
    }

    @NonNull
    @Override
    Integer getValue() {
        return epicValues ? super.getValue() : picker.getValue();
    }

    void setEpicValues(boolean epicValues) {
        this.epicValues = epicValues;

        if (epicValues) {
            super.setSelectableValues(IntStream.range(0, EPIC_DAMAGE_VALUES.length)
                            .boxed()
                            .collect(Collectors.toList()),
                    3);
            return;
        }
        selectableValues.clear();
        selectableValues.addAll(IntStream.rangeClosed(0, 20).boxed().collect(Collectors.toList()));
        picker.setDisplayedValues(null);
        picker.setMaxValue(20);
        picker.setValue(5);
    }

    @Override
    void setSelectableValues(@NonNull Collection<Integer> values, @Nullable Integer defaultValue) {
    }
}
