package com.peternaggschga.gwent.ui.dialogs.addcard;


import static com.peternaggschga.gwent.data.UnitEntity.EPIC_DAMAGE_VALUES;
import static com.peternaggschga.gwent.data.UnitEntity.NON_EPIC_DAMAGE_VALUES_UPPER_BOUND;

import android.widget.NumberPicker;

import androidx.annotation.IntRange;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * A ValuePicker used for selection of damage values for UnitEntity#damage.
 * Allows for switching between epic and normal damage values using #setEpicValues().
 */
class DamageValuePicker extends ValuePicker<Integer> {
    /**
     * Boolean defining whether or not this DamageValuePicker shows epic damage values.
     * @see #setEpicValues(boolean)
     */
    private boolean epicValues = false;

    /**
     * Constructor of a DamageValuePicker wrapping the given NumberPicker.
     * Calls #setEpicValues() to initialize the NumberPicker with non-epic damage values.
     * @see ValuePicker#ValuePicker(NumberPicker, SortedMap)
     * @see #setEpicValues(boolean)
     * @param picker NumberPicker wrapped by the created DamageValuePicker.
     */
    DamageValuePicker(@NonNull NumberPicker picker) {
        super(picker, Arrays.stream(EPIC_DAMAGE_VALUES).collect(
                TreeMap::new,
                (map, integer) -> map.put(integer, integer),
                Map::putAll)
        );
        setEpicValues(false);
    }

    /**
     * Returns a localized String representing the given value when #epicValues is false or otherwise the corresponding epic damage value.
     *
     * @param value Value that should be represented as a String.
     * @return A localized String representing the value.
     * @throws IllegalStateException When #epicValues is true and #displayIntegers does not contain a mapping for the given value.
     */
    @Override
    @NonNull
    protected String getDisplayString(@NonNull Integer value) {
        if (epicValues && !getDisplayIntegers().containsKey(value)) {
            throw new IllegalStateException("When epicValues is true, displayIntegers must contain the given key but does not contain " + value + ".");
        }
        return epicValues ? String.valueOf(getDisplayIntegers().get(value)) : String.valueOf(value);
    }

    /**
     * Sets the picker to the given value.
     * If #epicValues is true, ValuePicker#setValue() is called.
     * Otherwise the #picker is simply set to the given value.
     *
     * @param value Value that the picker is set to.
     * @throws IllegalStateException When #epicValues is false and the given value is not in [0,20].
     * @see ValuePicker#setValue(Comparable)
     */
    @Override
    void setValue(@NonNull @IntRange(from = 0, to = NON_EPIC_DAMAGE_VALUES_UPPER_BOUND) Integer value) {
        if (epicValues) {
            super.setValue(value);
        } else if (value < 0 || value > NON_EPIC_DAMAGE_VALUES_UPPER_BOUND) {
            throw new IllegalStateException("Value must be in [0, " + NON_EPIC_DAMAGE_VALUES_UPPER_BOUND + "] but is " + value + ".");
        } else {
            getPicker().setValue(value);
        }
    }

    /**
     * Returns the currently selected value.
     * @return A value that is selected in #picker.
     */
    @NonNull
    @Override
    Integer getValue() {
        return epicValues ? super.getValue() : getPicker().getValue();
    }

    /**
     * Switches the #selectableValues between non-epic and epic damage values.
     * Sets #epicValues.
     * @see #epicValues
     * @param epicValues Boolean defining whether or not epic values are shown.
     */
    void setEpicValues(boolean epicValues) {
        this.epicValues = epicValues;

        if (epicValues) {
            super.setSelectableValues(Arrays.asList(EPIC_DAMAGE_VALUES), EPIC_DAMAGE_VALUES[3]);
            return;
        }
        getSelectableValues().clear();
        getSelectableValues().addAll(IntStream.rangeClosed(0, NON_EPIC_DAMAGE_VALUES_UPPER_BOUND).boxed().collect(Collectors.toList()));
        getPicker().setDisplayedValues(null);
        getPicker().setMaxValue(NON_EPIC_DAMAGE_VALUES_UPPER_BOUND);
        getPicker().setValue(5);
    }

    /**
     * Does nothing. Overrides parent since #selectableValues should only be modified by #setEpicValues().
     * @param values Collection of the new selectable values.
     * @param defaultValue Value that is shown in the beginning.
     *                     If null, then the first value defined by the Comparable interface is used.
     */
    @Override
    void setSelectableValues(@NonNull Collection<Integer> values, @Nullable Integer defaultValue) {
    }
}
