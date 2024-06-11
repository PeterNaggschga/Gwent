package com.peternaggschga.gwent.ui.dialogs.addcard;



import android.content.Context;
import android.widget.NumberPicker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.UiContext;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;

/**
 * A generic abstract wrapper class around NumberPicker
 * used to select values of arbitrary types extending Comparable.
 * <p>
 * Must be subclassed to provide the displayed String values using #getDisplayString().
 * The default case, which is implemented in StringValuePicker,
 * is that #displayIntegers maps the #selectableValues to a String resource id.
 * @see NumberPicker
 * @see StringValuePicker
 */
abstract class ValuePicker<T extends Comparable<T>> {
    /**
     * NumberPicker that is wrapped by this class, used to select the values.
     *
     * @see #getValue()
     * @see #setValue(Comparable)
     * @see #getPicker()
     */
    @NonNull
    private final NumberPicker picker;

    /**
     * Map mapping each of the #selectableValues to an Integer that may be used
     * to select the displayed value Strings in implementations of #getDisplayString().
     * @see #getDisplayIntegers()
     */
    @NonNull
    private final Map<T, Integer> displayIntegers;

    /**
     * List of values that are selectable using this ValuePicker.
     * @see #setSelectableValues(Collection)
     * @see #setSelectableValues(Collection, Comparable)
     * @see #getSelectableValues()
     */
    @NonNull
    private final List<T> selectableValues;

    /**
     * Constructor of a ValuePicker wrapping the given NumberPicker and offering the given values.
     * Wrapper around #ValuePicker(NumberPicker, SortedMap, Comparable) with null as defaultValue.
     * @see #ValuePicker(NumberPicker, SortedMap, Comparable)
     * @param picker NumberPicker wrapped by the created ValuePicker.
     * @param valueToStringRes SortedMap with all #selectableValues as keys and the respective #displayIntegers as value.
     * @throws org.valid4j.errors.RequireViolation When valueToStringRes is empty.
     */
    ValuePicker(@NonNull NumberPicker picker, @NonNull SortedMap<T, Integer> valueToStringRes) {
        this(picker, valueToStringRes, null);
    }

    /**
     * Constructor of a ValuePicker wrapping the given NumberPicker,
     * offering the given values and showing the given defaultValue.
     *
     * @see #ValuePicker(NumberPicker, SortedMap)
     * @param picker NumberPicker wrapped by the created ValuePicker.
     * @param valueToStringRes SortedMap with all #selectableValues as keys and the respective #displayIntegers as value.
     * @param defaultValue Value that is shown in the beginning.
     *                     If null, then the first value defined by the Comparable interface is used.
     * @throws org.valid4j.errors.RequireViolation When valueToStringRes is empty
     * or when defaultValue is not null but not contained in valueToStringRes.
     */
    ValuePicker(@NonNull NumberPicker picker, @NonNull SortedMap<T, Integer> valueToStringRes,
                @Nullable T defaultValue) {
        // TODO: assert !valueToStringRes.isEmpty());
        // TODO: assert defaultValue == null || valueToStringRes.containsKey(defaultValue));
        this.picker = picker;
        displayIntegers = new HashMap<>(valueToStringRes);
        selectableValues = new ArrayList<>(valueToStringRes.size());

        picker.setMinValue(0);
        setSelectableValues(valueToStringRes.keySet(), defaultValue);
    }

    /**
     * Sets #selectableValues to the given values and show the given defaultValue.
     * Every given value must be in the key-set of #displayIntegers.
     * @see #setSelectableValues(Collection)
     * @param values Collection of the new selectable values.
     * @param defaultValue Value that is shown in the beginning.
     *                     If null, then the first value defined by the Comparable interface is used.
     * @throws org.valid4j.errors.RequireViolation When values is empty, or contains a value not present in the key-set of #displayIntegersor,
     * or does not contain defaultValue when defaultValue is not null.
     */
    void setSelectableValues(@NonNull Collection<T> values, @Nullable T defaultValue) {
        // TODO: assert !values.isEmpty());
        // TODO: assert displayIntegers.keySet().containsAll(values));
        // TODO: assert defaultValue == null || values.contains(defaultValue));

        picker.setDisplayedValues(null);
        picker.setValue(0);
        picker.setMaxValue(values.size() - 1);

        selectableValues.clear();
        String[] displayValues = new String[values.size()];
        int i = 0;
        for (T value : values) {
            selectableValues.add(value);
            displayValues[i++] = getDisplayString(value);
        }

        if (defaultValue != null) {
            setValue(defaultValue);
        }
        picker.setDisplayedValues(displayValues);
    }

    /**
     * Returns the NumberPicker wrapped by this ValuePicker.
     *
     * @return A NumberPicker wrapped in this ValuePicker.
     * @see #picker
     */
    @NonNull
    protected NumberPicker getPicker() {
        return picker;
    }

    /**
     * Sets an OnValueChangeListener
     * that is called when the value of the wrapped #picker has changed
     * and stayed the same for 500 ms.
     * The delayed NumberPicker.OnValueChangeListener
     * that is generated from the given OnValueChangeListener using CardNumberPickerAdapter#getDelayedOnValueChangeListener().
     * @see OnValueChangeListener
     * @see CardNumberPickerAdapter#getDelayedOnValueChangeListener(NumberPicker.OnValueChangeListener)
     * @param onValueChangedListener OnValueChangedListener that is to be executed when the value changes.
     */
    void setOnValueChangedListener(@NonNull OnValueChangeListener<T> onValueChangedListener) {
        picker.setOnValueChangedListener(
                CardNumberPickerAdapter.getDelayedOnValueChangeListener((picker, oldVal, newVal) ->
                        onValueChangedListener.onValueChange(ValuePicker.this,
                                selectableValues.get(oldVal),
                                selectableValues.get(newVal)))
        );
    }

    /**
     * Returns the currently selected value.
     * @return A value that is selected in #picker.
     */
    @NonNull
    T getValue() {
        return selectableValues.get(picker.getValue());
    }

    /**
     * Sets the picker to the given value.
     * @param value Value that the picker is set to.
     * @throws org.valid4j.errors.RequireViolation When #selectableValues does not contain the given value.
     */
    void setValue(@NonNull T value) {
        // TODO: assert (position = selectableValues.indexOf(value)) >= 0);
        picker.setValue(selectableValues.indexOf(value));
    }

    /**
     * Returns a localized String representing the given value.
     *
     * @param value Value that should be represented as a String.
     * @return A localized String representing the value.
     */
    @NonNull
    protected abstract String getDisplayString(@NonNull T value);

    /**
     * Returns the context the #picker is running in, through which it can
     * access the current theme, resources, etc.
     *
     * @return A Context object of the wrapped #picker.
     */
    @UiContext
    public Context getContext() {
        return picker.getContext();
    }

    /**
     * An interface defining a callback for changes of the current value.
     */
    interface OnValueChangeListener<T extends Comparable<T>> {
        /**
         * Called upon a change of the current value.
         *
         * @param picker ValuePicker associated with this listener.
         * @param oldVal Value that was previously selected.
         * @param newVal Value that was newly selected.
         */
        void onValueChange(@NonNull ValuePicker<T> picker, T oldVal, T newVal);
    }

    /**
     * Returns the #displayIntegers map.
     *
     * @return A Map from values to Integer objects representing information on their String representation.
     * @see #displayIntegers
     */
    @NonNull
    protected Map<T, Integer> getDisplayIntegers() {
        return displayIntegers;
    }

    /**
     * Returns a List of values that can be selected by this picker.
     *
     * @return A List of values that are selectable.
     * @see #selectableValues
     */
    @NonNull
    protected List<T> getSelectableValues() {
        return selectableValues;
    }

    /**
     * Sets #selectableValues to the given values.
     * Every given value must be in the key-set of #displayIntegers.
     * Wrapper of #setSelectableValues(Collection, Comparable).
     *
     * @param values Collection of the new selectable values.
     * @throws org.valid4j.errors.RequireViolation When values is empty, or contains a value not present in the key-set of #displayIntegersor,
     * or does not contain defaultValue when defaultValue is not null.
     * @see #setSelectableValues(Collection, Comparable)
     */
    void setSelectableValues(@NonNull Collection<T> values) {
        setSelectableValues(values, null);
    }
}
