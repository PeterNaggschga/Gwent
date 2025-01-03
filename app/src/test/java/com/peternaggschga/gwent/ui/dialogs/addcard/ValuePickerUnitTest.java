package com.peternaggschga.gwent.ui.dialogs.addcard;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.mock;
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

@RunWith(MockitoJUnitRunner.class)
public class ValuePickerUnitTest {
    static final SortedMap<Ability, Integer> ABILITY_TO_STRING_RES = new TreeMap<>();
    static final Ability NOT_SELECTABLE = Ability.values()[0];
    @Mock
    NumberPicker mockPicker;
    ValuePicker<Ability> testPicker;

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
            testPicker.setSelectableValues(Collections.emptyList());
            fail();
        } catch (IllegalArgumentException ignored) {
        }
        try {
            testPicker.setSelectableValues(Collections.emptyList(), null);
            fail();
        } catch (IllegalArgumentException ignored) {
        }
    }

    @Test
    public void setSelectableValuesAssertsKnownValues() {
        try {
            testPicker.setSelectableValues(Arrays.asList(Ability.values()));
            fail();
        } catch (IllegalStateException ignored) {
        }
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
        testPicker.setSelectableValues(ABILITY_TO_STRING_RES.keySet());
        testPicker.setSelectableValues(ABILITY_TO_STRING_RES.keySet(), null);
        verify(mockPicker, times(2)).setMaxValue(ABILITY_TO_STRING_RES.size() - 1);
    }

    @Test
    public void setSelectableValuesSetsPickerDisplayValues() {
        testPicker.setSelectableValues(ABILITY_TO_STRING_RES.keySet());
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
        ).isEqualTo(2);
    }

    @Test
    public void setSelectableValuesUpdatesSelectableValues() {
        testPicker.setSelectableValues(Collections.singletonList(Ability.MORAL_BOOST));
        assertThat(testPicker.getSelectableValues()).contains(Ability.MORAL_BOOST);
        assertThat(testPicker.getSelectableValues()).hasSize(1);
    }

    @Test
    public void setSelectableValuesSetsDefaultValue() {
        testPicker.setSelectableValues(ABILITY_TO_STRING_RES.keySet());
        verify(mockPicker).setValue(0);

        testPicker.setSelectableValues(ABILITY_TO_STRING_RES.keySet(), Ability.BINDING);
        verify(mockPicker).setValue(Ability.BINDING.ordinal() - 1);
    }

    @Test
    public void setOnValueChangedListenerSetsListener() {
        //noinspection unchecked
        testPicker.setOnValueChangedListener(mock(ValuePicker.OnValueChangeListener.class));
        verify(mockPicker).setOnValueChangedListener(any());
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
