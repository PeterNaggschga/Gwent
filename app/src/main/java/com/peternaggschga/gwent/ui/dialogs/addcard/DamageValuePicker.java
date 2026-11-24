package com.peternaggschga.gwent.ui.dialogs.addcard;


import static com.peternaggschga.gwent.data.UnitEntity.EPIC_DAMAGE_VALUES;
import static com.peternaggschga.gwent.data.UnitEntity.NON_EPIC_DAMAGE_VALUES_UPPER_BOUND;

import android.widget.NumberPicker;

import androidx.annotation.IntRange;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * A ValuePicker used for selection of damage values for UnitEntity.damage.
 * Allows for switching between epic and normal damage values using .setEpicValues().
 */
class DamageValuePicker extends ValuePicker<Integer> {
    /**
     * Boolean defining whether or not this DamageValuePicker shows epic damage values.
     * @see .setEpicValues(boolean)
     */
    private boolean epicValues = false;

    /**
     * Preferred hero damage when the current selection is unavailable and this value is offered.
     */
    private static final int DEFAULT_HERO_DAMAGE = 10;

    /**
     * Fallback damage when the current selection is outside the normal damage range.
     */
    private static final int DEFAULT_NORMAL_DAMAGE = 5;

    /**
     * Sorted, distinct hero damage values copied from the faction filter at construction.
     * Empty when no heroes are available for the edited row.
     */
    @NonNull
    private final List<Integer> heroDamageValues;

    /**
     * Constructor of a DamageValuePicker wrapping the given NumberPicker.
     * Initializes the NumberPicker with normal damage values and a default damage of 5.
     * @see ValuePicker.ValuePicker(NumberPicker, SortedMap)
     * @see .setEpicValues(boolean)
     * @param picker NumberPicker wrapped by the created DamageValuePicker.
     * @param heroDamageValues Hero damage values supplied by the AbilityFilter; may be empty.
     * @throws IllegalArgumentException If heroDamageValues contains an unknown hero damage value.
     */
    DamageValuePicker(@NonNull NumberPicker picker, @NonNull Collection<Integer> heroDamageValues) {
        super(picker, EPIC_DAMAGE_VALUES.stream().collect(
                TreeMap::new,
                (map, integer) -> map.put(integer, integer),
                Map::putAll)
        );
        if (!EPIC_DAMAGE_VALUES.containsAll(heroDamageValues)) {
            throw new IllegalArgumentException("Hero damage values must be a subset of EPIC_DAMAGE_VALUES.");
        }
        this.heroDamageValues = heroDamageValues.stream().distinct().sorted().collect(Collectors.toList());
        setNormalValues(5);
    }

    /**
     * Returns a localized String representing the given value when .epicValues is false or otherwise the corresponding epic damage value.
     *
     * @param value Value that should be represented as a String.
     * @return A localized String representing the value.
     * @throws IllegalStateException When .epicValues is true and .displayIntegers does not contain a mapping for the given value.
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
     * If .epicValues is true, ValuePicker.setValue() is called.
     * Otherwise the .picker is simply set to the given value.
     *
     * @param value Value that the picker is set to.
     * @throws IllegalStateException When .epicValues is false and the given value is not in [0,20].
     * @see ValuePicker.setValue(Comparable)
     */
    @Override
    void setValue(@NonNull @IntRange(from = 0, to = NON_EPIC_DAMAGE_VALUES_UPPER_BOUND) Integer value) {
        if (epicValues) {
            super.setValue(value);
        } else if (value < 0 || value > NON_EPIC_DAMAGE_VALUES_UPPER_BOUND) {
            throw new IllegalStateException("Value must be in [0, " + NON_EPIC_DAMAGE_VALUES_UPPER_BOUND + "] but is " + value + ".");
        } else {
            getPicker().setValue(value);
            publishValue(value);
        }
    }

    /**
     * Returns the currently selected value.
     * @return A value that is selected in .picker.
     */
    @NonNull
    @Override
    Integer getValue() {
        return epicValues ? super.getValue() : getPicker().getValue();
    }

    /**
     * Configures normal damage bounds and publishes only the final selection.
     *
     * @param selected Normal damage value to select.
     */
    private void setNormalValues(int selected) {
        epicValues = false;
        getSelectableValues().clear();
        getSelectableValues().addAll(IntStream.rangeClosed(0, NON_EPIC_DAMAGE_VALUES_UPPER_BOUND).boxed().collect(Collectors.toList()));
        getPicker().setDisplayedValues(null);
        getPicker().setMaxValue(NON_EPIC_DAMAGE_VALUES_UPPER_BOUND);
        getPicker().setValue(selected);
        publishValue(selected);
    }

    /**
     * Switches between normal damage and the faction's filtered hero damage values.
     * Preserves the selected damage when valid; otherwise prefers 10 for heroes
     * or the lowest available hero damage. Normal damage defaults to 5 when invalid.
     * Uses the hero damage values provided at construction.
     *
     * @param epicValues Whether hero damage values should be shown.
     * @throws IllegalStateException If hero mode is requested when no hero damage values are available.
     */
    void setEpicValues(boolean epicValues) {
        int previous = getValue();
        if (!epicValues) {
            setNormalValues(previous >= 0 && previous <= NON_EPIC_DAMAGE_VALUES_UPPER_BOUND ? previous : DEFAULT_NORMAL_DAMAGE);
            return;
        }
        if (heroDamageValues.isEmpty()) {
            throw new IllegalStateException("No hero damage values are available.");
        }
        int selected = heroDamageValues.contains(previous) ? previous
                : heroDamageValues.contains(DEFAULT_HERO_DAMAGE) ? DEFAULT_HERO_DAMAGE : heroDamageValues.get(0);
        this.epicValues = true;
        super.setSelectableValues(heroDamageValues, selected);
    }

    /**
     * Throws UnsupportedOperationException, since selectable values should only be modified by .setEpicValues().
     * @param values Collection of the new selectable values.
     * @param defaultValue Value that is shown in the beginning.
     *                     If null, then the first value defined by the Comparable interface is used.
     * @see DamageValuePicker.setEpicValues(boolean)
     * @throws UnsupportedOperationException On each call.
     */
    @Override
    void setSelectableValues(@NonNull Collection<Integer> values, @Nullable Integer defaultValue) {
        throw new UnsupportedOperationException("DamageValuePicker values must only be changed using setEpicValues()");
    }
}
