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
 * @todo Documentation
 * @todo Add testing.
 */
abstract class ValuePicker<T extends Comparable<T>> {
    @NonNull
    protected final NumberPicker picker;
    @NonNull
    protected final Map<T, Integer> displayIntegers;
    @NonNull
    protected final List<T> selectableValues;

    ValuePicker(@NonNull NumberPicker picker, @NonNull SortedMap<T, Integer> valueToStringRes) {
        this(picker, valueToStringRes, null);
    }

    ValuePicker(@NonNull NumberPicker picker, @NonNull SortedMap<T, Integer> valueToStringRes,
                @Nullable T defaultValue) {
        this.picker = picker;
        displayIntegers = new HashMap<>(valueToStringRes.size());
        displayIntegers.putAll(valueToStringRes);
        selectableValues = new ArrayList<>(valueToStringRes.size());

        picker.setMinValue(0);
        setSelectableValues(valueToStringRes.keySet(), defaultValue);
    }

    void setSelectableValues(@NonNull Collection<T> valueMap) {
        setSelectableValues(valueMap, null);
    }

    void setSelectableValues(@NonNull Collection<T> values, @Nullable T defaultValue) {
        picker.setDisplayedValues(null);
        picker.setValue(0);
        picker.setMaxValue(values.size() - 1);

        selectableValues.clear();
        String[] displayValues = new String[values.size()];
        int i = 0;
        for (T value : values) {
            selectableValues.add(value);
            if (value == defaultValue) {
                picker.setValue(i);
            }
            displayValues[i++] = getDisplayString(value);
        }
        picker.setDisplayedValues(displayValues);
    }

    void setOnValueChangedListener(@NonNull OnValueChangeListener<T> onValueChangedListener) {
        picker.setOnValueChangedListener(
                CardNumberPickerAdapter.getDelayedOnValueChangeListener((picker, oldVal, newVal) ->
                        onValueChangedListener.onValueChange(ValuePicker.this,
                                selectableValues.get(oldVal),
                                selectableValues.get(newVal)))
        );
    }

    @NonNull
    T getValue() {
        return selectableValues.get(picker.getValue());
    }

    void setValue(@NonNull T value) {
        picker.setValue(selectableValues.indexOf(value));
    }

    protected abstract String getDisplayString(@NonNull T value);

    /**
     * Returns the context the view is running in, through which it can
     * access the current theme, resources, etc.
     *
     * @return The view's Context.
     */
    @UiContext
    public Context getContext() {
        return picker.getContext();
    }

    /**
     * Interface to listen for changes of the current value.
     */
    interface OnValueChangeListener<T extends Comparable<T>> {
        /**
         * Called upon a change of the current value.
         *
         * @param picker The NumberPicker associated with this listener.
         * @param oldVal The previous value.
         * @param newVal The new value.
         */
        void onValueChange(@NonNull ValuePicker<T> picker, T oldVal, T newVal);
    }
}
