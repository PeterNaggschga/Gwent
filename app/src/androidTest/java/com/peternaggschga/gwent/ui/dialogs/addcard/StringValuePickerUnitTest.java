package com.peternaggschga.gwent.ui.dialogs.addcard;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.fail;

import android.content.Context;
import android.widget.NumberPicker;

import androidx.test.core.app.ApplicationProvider;

import com.peternaggschga.gwent.Ability;
import com.peternaggschga.gwent.R;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.valid4j.errors.RequireViolation;

import java.util.Arrays;
import java.util.Collections;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.stream.Collectors;

@RunWith(MockitoJUnitRunner.class)
public class StringValuePickerUnitTest {
    static final SortedMap<Ability, Integer> ABILITY_TO_STRING_RES = new TreeMap<>();
    static final Ability NOT_SELECTABLE = Ability.values()[0];
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
        mockPicker = new NumberPicker(ApplicationProvider.getApplicationContext());
        testPicker = new StringValuePicker<>(mockPicker, ABILITY_TO_STRING_RES);
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
        mockPicker.setMinValue(1);
        new StringValuePicker<>(mockPicker, ABILITY_TO_STRING_RES);
        assertThat(mockPicker.getMinValue()).isEqualTo(0);
        mockPicker.setMinValue(1);
        new StringValuePicker<>(mockPicker, ABILITY_TO_STRING_RES, null);
        assertThat(mockPicker.getMinValue()).isEqualTo(0);
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
        testPicker.setSelectableValues(Collections.singletonList(Ability.HORN));
        assertThat(mockPicker.getMaxValue()).isEqualTo(0);
        testPicker.setSelectableValues(ABILITY_TO_STRING_RES.keySet(), null);
        assertThat(mockPicker.getMaxValue()).isEqualTo(ABILITY_TO_STRING_RES.size() - 1);
    }

    @Test
    public void setSelectableValuesSetsPickerDisplayValues() {
        testPicker.setSelectableValues(Collections.singletonList(Ability.HORN));
        assertThat(mockPicker.getDisplayedValues()[0])
                .isEqualTo(ApplicationProvider.getApplicationContext().getString(R.string.add_picker_ability_horn));

        testPicker.setSelectableValues(ABILITY_TO_STRING_RES.keySet(), null);
        assertThat(Arrays.stream(mockPicker.getDisplayedValues()).collect(Collectors.toSet()))
                .containsExactlyElementsIn(ABILITY_TO_STRING_RES.values()
                        .stream()
                        .map(resId -> ApplicationProvider.getApplicationContext().getString(resId))
                        .collect(Collectors.toSet()));
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
        assertThat(mockPicker.getValue()).isEqualTo(0);

        testPicker.setSelectableValues(ABILITY_TO_STRING_RES.keySet(), Ability.BINDING);
        assertThat(mockPicker.getValue()).isEqualTo(Ability.BINDING.ordinal() - 1);
    }

    @Test
    public void getValueReturnsSelectedValue() {
        for (int i = 0; i < Ability.values().length - 1; i++) {
            mockPicker.setValue(i);
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
            assertThat(mockPicker.getValue()).isEqualTo(i);
        }
    }

    @Test
    public void getContextReturnsPickerContext() {
        assertThat(testPicker.getContext()).isSameInstanceAs(mockPicker.getContext());
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
