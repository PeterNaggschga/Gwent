package com.peternaggschga.gwent.ui.dialogs.addcard;



import android.widget.NumberPicker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.SortedMap;

/**
 * A ValuePicker displaying a resource String for each element in #selectableValues.
 * The resource String is retrieved from #displayIntegers by using the associated Integer as the String id in #getDisplayString().
 * @see #getDisplayString(Comparable)
 */
class StringValuePicker<T extends Comparable<T>> extends ValuePicker<T> {
    /**
     * Constructor of a StringValuePicker wrapping the given NumberPicker and offering the given values.
     * Wrapper around #StringValuePicker(NumberPicker, SortedMap, Comparable) with null as defaultValue.
     *
     * @param picker           NumberPicker wrapped by the created StringValuePicker.
     * @param valueToStringRes SortedMap with all #selectableValues as keys and the respective #displayIntegers (String ids) as value.
     * @throws IllegalArgumentException When valueToStringRes is empty.
     * @see #StringValuePicker(NumberPicker, SortedMap, Comparable)
     */
    @SuppressWarnings("unused")
    StringValuePicker(@NonNull NumberPicker picker, @NonNull SortedMap<T, Integer> valueToStringRes) {
        this(picker, valueToStringRes, null);
    }

    /**
     * Constructor of a StringValuePicker wrapping the given NumberPicker,
     * offering the given values and showing the given defaultValue.
     * Matches super-constructor #ValuePicker(NumberPicker, SortedMap, Comparable).
     *
     * @see #StringValuePicker(NumberPicker, SortedMap)
     * @param picker NumberPicker wrapped by the created StringValuePicker.
     * @param valueToStringRes SortedMap with all #selectableValues as keys and the respective #displayIntegers (String ids) as value.
     * @param defaultValue Value that is shown in the beginning.
     *                     If null, then the first value defined by the Comparable interface is used.
     * @throws IllegalArgumentException When valueToStringRes is empty.
     * or when defaultValue is not null but not contained in valueToStringRes.
     */
    StringValuePicker(@NonNull NumberPicker picker, @NonNull SortedMap<T, Integer> valueToStringRes, @Nullable T defaultValue) {
        super(picker, valueToStringRes, defaultValue);
    }

    /**
     * Returns a localized String representing the given value by looking up the respective String id in #displayIntegers.
     *
     * @param value Value that should be represented as a String.
     * @return A localized String representing the value.
     * @throws IllegalStateException When #displayIntegers does not contain a String id for the given value.
     */
    @Override
    @NonNull
    protected String getDisplayString(@NonNull T value) {
        Integer resId;
        if ((resId = getDisplayIntegers().get(value)) == null) {
            throw new IllegalStateException("Value must be key in displayIntegers but is " + value + ".");
        }
        return getContext().getString(resId);
    }
}
