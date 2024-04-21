package com.peternaggschga.gwent.ui.dialogs.addcard;

import android.widget.NumberPicker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Objects;
import java.util.SortedMap;

/**
 * @todo Documentation
 * @todo Add testing.
 */
class StringValuePicker<T extends Comparable<T>> extends ValuePicker<T> {
    @SuppressWarnings("unused")
    StringValuePicker(@NonNull NumberPicker picker, @NonNull SortedMap<T, Integer> valueToStringRes) {
        this(picker, valueToStringRes, null);
    }

    StringValuePicker(@NonNull NumberPicker picker, @NonNull SortedMap<T, Integer> valueToStringRes, @Nullable T defaultValue) {
        super(picker, valueToStringRes, defaultValue);
    }

    @Override
    protected String getDisplayString(@NonNull T value) {
        return picker.getContext().getString(Objects.requireNonNull(displayIntegers.get(value)));
    }
}
