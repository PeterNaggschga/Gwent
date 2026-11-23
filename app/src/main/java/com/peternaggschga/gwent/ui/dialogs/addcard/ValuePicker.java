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

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.subjects.PublishSubject;

/**
 * A generic abstract wrapper class around NumberPicker
 * used to select values of arbitrary types extending Comparable.
 * <p>
 * Must be subclassed to provide the displayed String values using getDisplayString().
 * The default case, which is implemented in StringValuePicker,
 * is that displayIntegers maps the selectableValues to a String resource id.
 * @see NumberPicker
 * @see StringValuePicker
 */
abstract class ValuePicker<T extends Comparable> {
    /**
     * NumberPicker that is wrapped by this class, used to select the values.
     *
     * @see getValue()
     * @see setValue(Comparable)
     * @see getPicker()
     */
    @NonNull
    private final NumberPicker picker;

    /**
     * Map mapping each of the selectableValues to an Integer that may be used
     * to select the displayed value Strings in implementations of getDisplayString().
     * @see getDisplayIntegers()
     */
    @NonNull
    private final Map<T, Integer> displayIntegers;

    /**
     * List of values that are selectable using this ValuePicker.
     * @see setSelectableValues(Collection, Comparable)
     * @see getSelectableValues()
     */
    @NonNull
    private final List<T> selectableValues;

    /**
     * User and programmatic selections, published after the picker state is updated.
     */
    @NonNull
    private final PublishSubject<T> changes = PublishSubject.create();

    /**
     * Constructor of a ValuePicker wrapping the given NumberPicker and offering the given values.
     * Wrapper around ValuePicker(NumberPicker, SortedMap, Comparable) with null as defaultValue.
     * @see ValuePicker(NumberPicker, SortedMap, Comparable)
     * @param picker NumberPicker wrapped by the created ValuePicker.
     * @param valueToStringRes SortedMap with all selectableValues as keys and the respective displayIntegers as value.
     * @throws IllegalArgumentException When valueToStringRes is empty.
     */
    ValuePicker(@NonNull NumberPicker picker, @NonNull SortedMap<T, Integer> valueToStringRes) {
        this(picker, valueToStringRes, null);
    }

    /**
     * Constructor of a ValuePicker wrapping the given NumberPicker,
     * offering the given values and showing the given defaultValue.
     *
     * @see ValuePicker(NumberPicker, SortedMap)
     * @param picker NumberPicker wrapped by the created ValuePicker.
     * @param valueToStringRes SortedMap with all selectableValues as keys and the respective displayIntegers as value.
     * @param defaultValue Value that is shown in the beginning.
     *                     If null, then the first value defined by the Comparable interface is used.
     * @throws IllegalArgumentException When valueToStringRes is empty.
     * or when defaultValue is not null but not contained in valueToStringRes.
     */
    ValuePicker(@NonNull NumberPicker picker, @NonNull SortedMap<T, Integer> valueToStringRes,
                @Nullable T defaultValue) {
        if (valueToStringRes.isEmpty()) {
            throw new IllegalArgumentException("SortedMap<T, Integer> valueToStringRes must not be empty.");
        }
        if (defaultValue != null && !valueToStringRes.containsKey(defaultValue)) {
            throw new IllegalArgumentException("DefaultValue must be null or a key in the given SortedMap.");
        }
        this.picker = picker;
        displayIntegers = new HashMap<>(valueToStringRes);
        selectableValues = new ArrayList<>(valueToStringRes.size());

        picker.setMinValue(0);
        applySelectableValues(valueToStringRes.keySet(), defaultValue);
        picker.setOnValueChangedListener(this::onPickerChanged);
    }

    /**
     * Sets selectableValues to the given values and show the given defaultValue.
     * Every given value must be in the key-set of displayIntegers.
     * @param values Collection of the new selectable values.
     * @param defaultValue Value that is shown in the beginning.
     *                     If null, then the first value defined by the Comparable interface is used.
     * @throws IllegalArgumentException When values is empty or does not contain a non-null defaultValue.
     * @throws IllegalStateException When values contains a value not present in the key-set of displayIntegers.
     */
    void setSelectableValues(@NonNull Collection<T> values, @Nullable T defaultValue) {
        applySelectableValues(values, defaultValue);
    }

    /**
     * Configures the widget without calling a subclass override during construction.
     *
     * @param values       New selectable values.
     * @param defaultValue Value to select, or null to select the first value.
     */
    private void applySelectableValues(@NonNull Collection<T> values, @Nullable T defaultValue) {
        if (values.isEmpty()) {
            throw new IllegalArgumentException("Collection<T> values must not be empty.");
        }
        if (!displayIntegers.keySet().containsAll(values)) {
            throw new IllegalStateException("DisplayIntegers must contain all given values.");
        }
        if (defaultValue != null && !values.contains(defaultValue)) {
            throw new IllegalArgumentException("DefaultValue must be null or one of the selectable values.");
        }

        List<T> newValues = new ArrayList<>(values);
        picker.setDisplayedValues(null);
        picker.setValue(0);
        picker.setMaxValue(newValues.size() - 1);

        selectableValues.clear();
        String[] displayValues = new String[newValues.size()];
        int i = 0;
        for (T value : newValues) {
            selectableValues.add(value);
            displayValues[i++] = getDisplayString(value);
        }

        if (defaultValue != null) {
            picker.setValue(selectableValues.indexOf(defaultValue));
        }
        picker.setDisplayedValues(displayValues);
        publishValue(getValue());
    }

    /**
     * Returns the NumberPicker wrapped by this ValuePicker.
     *
     * @return A NumberPicker wrapped in this ValuePicker.
     * @see .picker
     */
    @NonNull
    protected NumberPicker getPicker() {
        return picker;
    }

    /**
     * Publishes a user selection as a typed value.
     *
     * @param picker NumberPicker reporting the change.
     * @param oldVal Previous index in selectableValues.
     * @param newVal Newly selected index in selectableValues.
     */
    private void onPickerChanged(@NonNull NumberPicker picker, int oldVal, int newVal) {
        publishValue(selectableValues.get(newVal));
    }

    /**
     * Observes the current selection followed by distinct user and programmatic updates.
     * Subscribe, mutate the picker, and dispose on the Android main thread.
     * Multiple subscribers share the same NumberPicker listener.
     *
     * @return An Observable emitting the current value immediately to each subscriber.
     */
    @NonNull
    Observable<T> values() {
        return Observable.defer(() -> changes.startWithItem(getValue())).distinctUntilChanged();
    }

    /**
     * Publishes a completed programmatic update from this picker or a subclass.
     *
     * @param value Selected value after all picker bounds and labels have been updated.
     */
    protected void publishValue(@NonNull T value) {
        changes.onNext(value);
    }

    /**
     * Returns the currently selected value.
     * @return A value that is selected in .picker.
     */
    @NonNull
    T getValue() {
        return selectableValues.get(picker.getValue());
    }

    /**
     * Sets the picker to the given value.
     * @param value Value that the picker is set to.
     * @throws IllegalStateException When selectableValues does not contain the given value.
     */
    void setValue(@NonNull T value) {
        int position;
        if ((position = selectableValues.indexOf(value)) < 0) {
            throw new IllegalStateException("SelectableValues must contain the given value.");
        }
        picker.setValue(position);
        publishValue(value);
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
     * Returns the context the .picker is running in, through which it can
     * access the current theme, resources, etc.
     *
     * @return A Context object of the wrapped .picker.
     */
    @UiContext
    public Context getContext() {
        return picker.getContext();
    }

    /**
     * Returns the displayIntegers map.
     *
     * @return A Map from values to Integer objects representing information on their String representation.
     * @see displayIntegers
     */
    @NonNull
    protected Map<T, Integer> getDisplayIntegers() {
        return displayIntegers;
    }

    /**
     * Returns a List of values that can be selected by this picker.
     *
     * @return A List of values that are selectable.
     * @see selectableValues
     */
    @NonNull
    protected List<T> getSelectableValues() {
        return selectableValues;
    }
}
