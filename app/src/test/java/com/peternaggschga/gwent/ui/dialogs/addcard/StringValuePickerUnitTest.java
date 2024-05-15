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
import androidx.test.core.app.ApplicationProvider;

import com.peternaggschga.gwent.Ability;
import com.peternaggschga.gwent.R;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.valid4j.errors.RequireViolation;

import java.util.Arrays;
import java.util.Collections;
import java.util.Objects;
import java.util.SortedMap;
import java.util.TreeMap;

@RunWith(MockitoJUnitRunner.class)
public class StringValuePickerUnitTest {
    static final SortedMap<Ability, Integer> ABILITY_TO_STRING_RES = new TreeMap<>();
    static final Ability NOT_SELECTABLE = Ability.values()[0];
    @Mock
    NumberPicker mockPicker;
    StringValuePicker<Ability> testPicker;

    @BeforeClass
    public static void initAbilityToStringRes() {
        ABILITY_TO_STRING_RES.put(Ability.HORN, R.string.add_picker_ability_horn);
        ABILITY_TO_STRING_RES.put(Ability.REVENGE, R.string.add_picker_ability_revenge);
        ABILITY_TO_STRING_RES.put(Ability.BINDING, R.string.add_picker_ability_binding);
        ABILITY_TO_STRING_RES.put(Ability.MORAL_BOOST, R.string.add_picker_ability_moralBoost);
    }

    @Before
    public void initMocks() {
        testPicker = new StringValuePicker<Ability>(mockPicker, ABILITY_TO_STRING_RES) {
            @NonNull
            protected String getDisplayString(@NonNull Ability value) {
                return value.name();
            }
        };
        reset(mockPicker);
        when(mockPicker.getContext()).thenReturn(ApplicationProvider.getApplicationContext());
    }

    @Test
    public void constructorAssertsValueToStringResIsNotEmpty() {
        try {
            new StringValuePicker<Ability>(mockPicker, Collections.emptySortedMap());
            fail();
        } catch (RequireViolation ignored) {
        }
        try {
            new StringValuePicker<Ability>(mockPicker, Collections.emptySortedMap(), null);
            fail();
        } catch (RequireViolation ignored) {
        }
    }

    @Test
    public void constructorAllowsNullDefaultValue() {
        try {
            new StringValuePicker<>(mockPicker, ABILITY_TO_STRING_RES, null);
        } catch (Exception ignored) {
            fail();
        }
    }

    @Test
    public void constructorAssertsDefaultValueInKnownValues() {
        try {
            new StringValuePicker<>(mockPicker, ABILITY_TO_STRING_RES, NOT_SELECTABLE);
            fail();
        } catch (RequireViolation ignored) {
        }
    }

    @Test
    public void constructorSetsPickerMinValue() {
        new StringValuePicker<>(mockPicker, ABILITY_TO_STRING_RES);
        verify(mockPicker).setMinValue(0);
        new StringValuePicker<>(mockPicker, ABILITY_TO_STRING_RES, null);
        verify(mockPicker, times(2)).setMinValue(0);
    }

    @Test
    public void setSelectableValuesAssertsNonEmptyCollection() {
        try {
            testPicker.setSelectableValues(Collections.emptyList());
            fail();
        } catch (RequireViolation ignored) {
        }
        try {
            testPicker.setSelectableValues(Collections.emptyList(), null);
            fail();
        } catch (RequireViolation ignored) {
        }
    }

    @Test
    public void setSelectableValuesAssertsKnownValues() {
        try {
            testPicker.setSelectableValues(Arrays.asList(Ability.values()));
            fail();
        } catch (RequireViolation ignored) {
        }
        try {
            testPicker.setSelectableValues(Arrays.asList(Ability.values()), null);
            fail();
        } catch (RequireViolation ignored) {
        }
    }

    @Test
    public void setSelectableValuesAssertsKnownDefaultValue() {
        try {
            testPicker.setSelectableValues(ABILITY_TO_STRING_RES.keySet(), NOT_SELECTABLE);
            fail();
        } catch (RequireViolation ignored) {
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
        } catch (RequireViolation ignored) {
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

    @Test
    public void getDisplayStringAssertsValueInMap() {
        try {
            testPicker.getDisplayString(Ability.NONE);
            fail();
        } catch (RequireViolation ignored) {
        }
    }

    @Test
    public void getDisplayStringReturnsResourceString() {
        Context context = ApplicationProvider.getApplicationContext();
        assertThat(testPicker.getDisplayString(Ability.HORN)).isEqualTo(context.getString(R.string.add_picker_ability_horn));
        assertThat(testPicker.getDisplayString(Ability.REVENGE)).isEqualTo(context.getString(R.string.add_picker_ability_revenge));
        assertThat(testPicker.getDisplayString(Ability.BINDING)).isEqualTo(context.getString(R.string.add_picker_ability_binding));
        assertThat(testPicker.getDisplayString(Ability.MORAL_BOOST)).isEqualTo(context.getString(R.string.add_picker_ability_moralBoost));
    }
}
