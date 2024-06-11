package com.peternaggschga.gwent.ui.dialogs.addcard;

import static com.google.common.truth.Truth.assertThat;
import static com.peternaggschga.gwent.ui.dialogs.addcard.DamageValuePicker.EPIC_DAMAGE_VALUES;
import static com.peternaggschga.gwent.ui.dialogs.addcard.DamageValuePicker.NON_EPIC_DAMAGE_VALUES_UPPER_BOUND;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.widget.NumberPicker;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.IntStream;

@RunWith(MockitoJUnitRunner.class)
public class DamageValuePickerUnitTest {
    @Mock
    NumberPicker mockPicker;
    DamageValuePicker testPicker;

    @Before
    public void initMocks() {
        testPicker = new DamageValuePicker(mockPicker);
        reset(mockPicker);
    }

    @Test
    public void constructorCallsSuperConstructorWithEpicDamageValues() {
        assertThat(testPicker.getDisplayIntegers())
                .containsExactly(0, 0, 7, 7, 8, 8, 10, 10, 11, 11, 15, 15);
    }

    @Test
    public void constructorCallsSetEpicValues() {
        assertThat(testPicker.getSelectableValues())
                .containsExactlyElementsIn(IntStream.rangeClosed(0, NON_EPIC_DAMAGE_VALUES_UPPER_BOUND).boxed().toArray());
    }

    @Test
    public void setSelectableValuesDoesNotChangeSelectableValues() {
        List<Integer> before = testPicker.getSelectableValues();
        testPicker.setSelectableValues(Collections.emptyList());
        assertThat(testPicker.getSelectableValues()).containsExactlyElementsIn(before);
    }

    @Test
    public void setEpicValuesSetsPickerMaxValue() {
        testPicker.setEpicValues(true);
        verify(mockPicker).setMaxValue(EPIC_DAMAGE_VALUES.length - 1);
        testPicker.setEpicValues(false);
        verify(mockPicker).setMaxValue(NON_EPIC_DAMAGE_VALUES_UPPER_BOUND);
    }

    @Test
    public void setEpicValuesSetsPickerDisplayValues() {
        testPicker.setEpicValues(false);
        verify(mockPicker).setDisplayedValues(null);
        testPicker.setEpicValues(true);

        ArgumentCaptor<String[]> argumentCaptor = ArgumentCaptor.forClass(String[].class);
        verify(mockPicker, atLeast(2)).setDisplayedValues(argumentCaptor.capture());

        assertThat(
                argumentCaptor.getAllValues()
                        .stream()
                        .filter(Objects::nonNull)
                        .filter(strings -> {
                            boolean result = strings.length == EPIC_DAMAGE_VALUES.length;
                            for (int i = 0; i < strings.length; i++) {
                                result &= strings[i].equals(String.valueOf(EPIC_DAMAGE_VALUES[i]));
                            }
                            return result;
                        })
                        .count()
        ).isEqualTo(1);
    }

    @Test
    public void setEpicValuesUpdatesSelectableValues() {
        testPicker.setEpicValues(true);
        assertThat(testPicker.getSelectableValues())
                .containsExactlyElementsIn(EPIC_DAMAGE_VALUES);
        testPicker.setEpicValues(false);
        assertThat(testPicker.getSelectableValues())
                .containsExactlyElementsIn(IntStream.rangeClosed(0, NON_EPIC_DAMAGE_VALUES_UPPER_BOUND).boxed().toArray());
    }

    @Test
    public void setSelectableValuesSetsDefaultValue() {
        testPicker.setEpicValues(true);
        verify(mockPicker).setValue(3);

        testPicker.setEpicValues(false);
        verify(mockPicker).setValue(5);
    }

    @Test
    public void getValueReturnsPickerValueWhenNotEpic() {
        for (int pickerValue = 0; pickerValue <= NON_EPIC_DAMAGE_VALUES_UPPER_BOUND; pickerValue++) {
            when(mockPicker.getValue()).thenReturn(pickerValue);
            assertThat(testPicker.getValue()).isEqualTo(pickerValue);
            verify(mockPicker, times(pickerValue + 1)).getValue();
        }
    }

    @Test
    public void getValueReturnsEpicValueWhenEpic() {
        testPicker.setEpicValues(true);
        for (int i = 0; i < EPIC_DAMAGE_VALUES.length; i++) {
            when(mockPicker.getValue()).thenReturn(i);
            assertThat(testPicker.getValue()).isEqualTo(EPIC_DAMAGE_VALUES[i]);
            verify(mockPicker, times(i + 1)).getValue();
        }
    }

    @Test
    public void setValueAssertsEpicValueWhenEpic() {
        testPicker.setEpicValues(true);
        try {
            testPicker.setValue(1);
            fail();
        } catch (IllegalStateException ignored) {
        }
    }

    @Test
    public void setValueSetsEpicValueWhenEpic() {
        testPicker.setEpicValues(true);
        reset(mockPicker);
        for (int i = 0; i < EPIC_DAMAGE_VALUES.length; i++) {
            testPicker.setValue(EPIC_DAMAGE_VALUES[i]);
            verify(mockPicker).setValue(i);
        }
    }

    @Test
    public void setValueSetsPickerValueWhenNotEpic() {
        for (int i = 0; i <= NON_EPIC_DAMAGE_VALUES_UPPER_BOUND; i++) {
            testPicker.setValue(i);
            verify(mockPicker).setValue(i);
        }
    }

    @Test
    public void setValueAssertsValueInBoundsWhenNotEpic() {
        try {
            testPicker.setValue(-1);
            fail();
        } catch (IllegalStateException ignored) {
        }
        try {
            testPicker.setValue(21);
            fail();
        } catch (IllegalStateException ignored) {
        }
    }

    @Test
    public void getDisplayStringAssertsValueInMapWhenEpic() {
        try {
            testPicker.getDisplayString(1);
        } catch (Exception ignored) {
            fail();
        }
        testPicker.setEpicValues(true);
        try {
            testPicker.getDisplayString(1);
            fail();
        } catch (IllegalStateException ignored) {
        }
    }

    @Test
    public void getDisplayStringReturnsEpicValueWhenEpic() {
        testPicker.setEpicValues(true);
        for (Integer epicDamageValue : EPIC_DAMAGE_VALUES) {
            assertThat(testPicker.getDisplayString(epicDamageValue)).isEqualTo(String.valueOf(epicDamageValue));
        }
    }

    @Test
    public void getDisplayStringReturnsPickerValueWhenNotEpic() {
        for (int i = 0; i <= NON_EPIC_DAMAGE_VALUES_UPPER_BOUND; i++) {
            assertThat(testPicker.getDisplayString(i)).isEqualTo(String.valueOf(i));
        }
    }
}
