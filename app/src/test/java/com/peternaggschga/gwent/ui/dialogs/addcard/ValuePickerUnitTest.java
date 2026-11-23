package com.peternaggschga.gwent.ui.dialogs.addcard;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.content.Context;
import android.widget.NumberPicker;

import androidx.annotation.NonNull;

import com.peternaggschga.gwent.data.Ability;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Collections;
import java.util.Objects;
import java.util.SortedMap;
import java.util.TreeMap;

import io.reactivex.rxjava3.observers.TestObserver;

@RunWith(MockitoJUnitRunner.class)
public class ValuePickerUnitTest {
    static final SortedMap<Ability, Integer> ABILITY_TO_STRING_RES = new TreeMap<>();
    static final Ability NOT_SELECTABLE = Ability.values()[0];
    @Mock
    NumberPicker mockPicker;
    ValuePicker<Ability> testPicker;
    NumberPicker.OnValueChangeListener pickerListener;

    @BeforeClass
    public static void initAbilityToStringRes() {
        for (int i = 1; i < Ability.values().length; i++) {
            ABILITY_TO_STRING_RES.put(Ability.values()[i], i);
        }
    }

    @Before
    public void initMocks() {
        testPicker = new ValuePicker<Ability>(mockPicker, ABILITY_TO_STRING_RES) {
            @NonNull
            protected String getDisplayString(@NonNull Ability value) {
                return value.name();
            }
        };
        ArgumentCaptor<NumberPicker.OnValueChangeListener> listenerCaptor =
                ArgumentCaptor.forClass(NumberPicker.OnValueChangeListener.class);
        verify(mockPicker).setOnValueChangedListener(listenerCaptor.capture());
        pickerListener = listenerCaptor.getValue();
        reset(mockPicker);
    }

    @Test
    public void constructorAssertsValueToStringResIsNotEmpty() {
        try {
            new ValuePicker<Ability>(mockPicker, Collections.emptySortedMap()) {
                @NonNull
                protected String getDisplayString(@NonNull Ability value) {
                    return value.name();
                }
            };
            fail();
        } catch (IllegalArgumentException ignored) {
        }
        try {
            new ValuePicker<Ability>(mockPicker, Collections.emptySortedMap(), null) {
                @NonNull
                protected String getDisplayString(@NonNull Ability value) {
                    return value.name();
                }
            };
            fail();
        } catch (IllegalArgumentException ignored) {
        }
    }

    @Test
    public void constructorAllowsNullDefaultValue() {
        try {
            new ValuePicker<Ability>(mockPicker, ABILITY_TO_STRING_RES, null) {
                @NonNull
                protected String getDisplayString(@NonNull Ability value) {
                    return value.name();
                }
            };
        } catch (Exception ignored) {
            fail();
        }
    }

    @Test
    public void constructorAssertsDefaultValueInKnownValues() {
        try {
            new ValuePicker<Ability>(mockPicker, ABILITY_TO_STRING_RES, NOT_SELECTABLE) {
                @NonNull
                protected String getDisplayString(@NonNull Ability value) {
                    return value.name();
                }
            };
            fail();
        } catch (IllegalArgumentException ignored) {
        }
    }

    @Test
    public void constructorSetsPickerMinValue() {
        new ValuePicker<Ability>(mockPicker, ABILITY_TO_STRING_RES) {
            @NonNull
            protected String getDisplayString(@NonNull Ability value) {
                return value.name();
            }
        };
        verify(mockPicker).setMinValue(0);
        new ValuePicker<Ability>(mockPicker, ABILITY_TO_STRING_RES, null) {
            @NonNull
            protected String getDisplayString(@NonNull Ability value) {
                return value.name();
            }
        };
        verify(mockPicker, times(2)).setMinValue(0);
    }

    @Test
    public void setSelectableValuesAssertsNonEmptyCollection() {
        try {
            testPicker.setSelectableValues(Collections.emptyList(), null);
            fail();
        } catch (IllegalArgumentException ignored) {
        }
    }

    @Test
    public void setSelectableValuesAssertsKnownValues() {
        try {
            testPicker.setSelectableValues(Arrays.asList(Ability.values()), null);
            fail();
        } catch (IllegalStateException ignored) {
        }
    }

    @Test
    public void setSelectableValuesAssertsKnownDefaultValue() {
        try {
            testPicker.setSelectableValues(ABILITY_TO_STRING_RES.keySet(), NOT_SELECTABLE);
            fail();
        } catch (IllegalArgumentException ignored) {
        }
    }

    @Test
    public void setSelectableValuesSetsPickerMaxValue() {
        testPicker.setSelectableValues(ABILITY_TO_STRING_RES.keySet(), null);
        verify(mockPicker).setMaxValue(ABILITY_TO_STRING_RES.size() - 1);
    }

    @Test
    public void setSelectableValuesSetsPickerDisplayValues() {
        testPicker.setSelectableValues(ABILITY_TO_STRING_RES.keySet(), null);

        ArgumentCaptor<String[]> argumentCaptor = ArgumentCaptor.forClass(String[].class);
        verify(mockPicker, atLeast(2)).setDisplayedValues(argumentCaptor.capture());

        assertThat(
                argumentCaptor.getAllValues()
                        .stream()
                        .filter(Objects::nonNull)
                        .filter(strings -> {
                            boolean result = strings.length == ABILITY_TO_STRING_RES.size();
                            for (int i = 0; i < strings.length; i++) {
                                result &= strings[i].equals(Ability.values()[i + 1].name());
                            }
                            return result;
                        })
                        .count()
        ).isEqualTo(1);
    }

    @Test
    public void setSelectableValuesUpdatesSelectableValues() {
        testPicker.setSelectableValues(Collections.singletonList(Ability.MORAL_BOOST), null);
        assertThat(testPicker.getSelectableValues()).contains(Ability.MORAL_BOOST);
        assertThat(testPicker.getSelectableValues()).hasSize(1);
    }

    @Test
    public void setSelectableValuesSetsDefaultValue() {
        testPicker.setSelectableValues(ABILITY_TO_STRING_RES.keySet(), null);
        verify(mockPicker).setValue(0);

        testPicker.setSelectableValues(ABILITY_TO_STRING_RES.keySet(), Ability.BINDING);
        verify(mockPicker).setValue(Ability.BINDING.ordinal() - 1);
    }

    @Test
    public void valuesImmediatelyEmitsCurrentSelection() {
        when(mockPicker.getValue()).thenReturn(2);
        testPicker.values().test().assertValue(Ability.values()[3]).assertNoErrors();
    }

    @Test
    public void valuesPublishesUserChangesImmediately() {
        TestObserver<Ability> observer = testPicker.values().test();
        pickerListener.onValueChange(mockPicker, 0, 1);
        observer.assertValues(Ability.HORN, Ability.REVENGE).assertNoErrors();
    }

    @Test
    public void valuesPublishesProgrammaticChangesWithoutDuplicates() {
        TestObserver<Ability> observer = testPicker.values().test();
        testPicker.setValue(Ability.BINDING);
        testPicker.setValue(Ability.BINDING);
        testPicker.setValue(Ability.MORAL_BOOST);
        observer.assertValues(Ability.HORN, Ability.BINDING, Ability.MORAL_BOOST).assertNoErrors();
    }

    @Test
    public void valuesPublishesSelectionResetAfterFiltering() {
        TestObserver<Ability> observer = testPicker.values().test();
        testPicker.setSelectableValues(Collections.singletonList(Ability.REVENGE), null);
        observer.assertValues(Ability.HORN, Ability.REVENGE).assertNoErrors();
    }

    @Test
    public void valuesPublishesOnlyFinalDefaultAfterFiltering() {
        TestObserver<Ability> observer = testPicker.values().test();
        when(mockPicker.getValue()).thenReturn(1);
        testPicker.setSelectableValues(Arrays.asList(Ability.REVENGE, Ability.BINDING), Ability.BINDING);
        observer.assertValues(Ability.HORN, Ability.BINDING).assertNoErrors();
    }

    @Test
    public void valuesSupportsMultipleSubscribersWithoutReplacingWidgetListener() {
        TestObserver<Ability> first = testPicker.values().test();
        TestObserver<Ability> second = testPicker.values().test();
        pickerListener.onValueChange(mockPicker, 0, 1);
        first.assertValues(Ability.HORN, Ability.REVENGE);
        second.assertValues(Ability.HORN, Ability.REVENGE);
        verify(mockPicker, never()).setOnValueChangedListener(any());
    }

    @Test
    public void disposedSubscriberReceivesNoFurtherChanges() {
        TestObserver<Ability> observer = testPicker.values().test();
        observer.dispose();
        pickerListener.onValueChange(mockPicker, 0, 1);
        testPicker.setValue(Ability.BINDING);
        observer.assertValue(Ability.HORN);
    }

    @Test
    public void getValueReturnsSelectedValue() {
        for (int i = 0; i < Ability.values().length - 1; i++) {
            when(mockPicker.getValue()).thenReturn(i);
            assertThat(testPicker.getValue()).isEqualTo(Ability.values()[i + 1]);
        }
    }

    @Test
    public void setValueAssertsKnownValue() {
        try {
            testPicker.setValue(Ability.NONE);
            fail();
        } catch (IllegalStateException ignored) {
        }
    }

    @Test
    public void setValueSetsPickerValue() {
        for (int i = 0; i < Ability.values().length - 1; i++) {
            testPicker.setValue(Ability.values()[i + 1]);
            verify(mockPicker).setValue(i);
        }
    }

    @Test
    public void getContextReturnsPickerContext() {
        Context context = mock(Context.class);
        when(mockPicker.getContext()).thenReturn(context);
        assertThat(testPicker.getContext()).isSameInstanceAs(context);
        verify(mockPicker).getContext();
    }
}
