package com.peternaggschga.gwent.ui.dialogs.addcard;

import static org.valid4j.Assertive.require;

import android.widget.NumberPicker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.SortedMap;

/**
 * A ValuePicker displaying a resource String for each element in #selectableValues.
 * The resource String is retrieved from #displayIntegers by using the associated Integer as the String id in #getDisplayString().
 * @see #getDisplayString(Comparable)
 * @todo Add testing.
 */
class StringValuePicker<T extends Comparable<T>> extends ValuePicker<T> {
    /**
     * Constructor of a StringValuePicker wrapping the given NumberPicker and offering the given values.
     * Wrapper around #StringValuePicker(NumberPicker, SortedMap, Comparable) with null as defaultValue.
     *
     * @param picker           NumberPicker wrapped by the created StringValuePicker.
     * @param valueToStringRes SortedMap with all #selectableValues as keys and the respective #displayIntegers (String ids) as value.
     * @throws org.valid4j.errors.RequireViolation When valueToStringRes is empty.
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
     * @throws org.valid4j.errors.RequireViolation When valueToStringRes is empty
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
     * @throws org.valid4j.errors.RequireViolation When #displayIntegers does not contain a String id for the given value.
     */
    @Override
    @NonNull
    protected String getDisplayString(@NonNull T value) {
        Integer resId = getDisplayIntegers().get(value);
        require(resId != null);
        //noinspection DataFlowIssue
        return getContext().getString(resId);
    }
}
