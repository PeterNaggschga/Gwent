package com.peternaggschga.gwent.ui.dialogs.addcard;

import static com.google.common.truth.Truth.assertThat;
import static com.peternaggschga.gwent.data.UnitEntity.EPIC_DAMAGE_VALUES;
import static com.peternaggschga.gwent.data.UnitEntity.NON_EPIC_DAMAGE_VALUES_UPPER_BOUND;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.clearInvocations;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.IntStream;

import io.reactivex.rxjava3.observers.TestObserver;

@RunWith(MockitoJUnitRunner.class)
public class DamageValuePickerUnitTest {
    @Mock
    NumberPicker mockPicker;
    DamageValuePicker testPicker;

    @Before
    public void initMocks() {
        testPicker = new DamageValuePicker(mockPicker, Arrays.asList(EPIC_DAMAGE_VALUES));
        reset(mockPicker);
    }

    @Test
    public void constructorCallsSuperConstructorWithEpicDamageValues() {
        assertThat(testPicker.getDisplayIntegers())
                .containsExactly(0, 0, 7, 7, 8, 8, 10, 10, 11, 11, 15, 15);
    }

    @Test
    public void constructorInitializesNormalDamageValues() {
        assertThat(testPicker.getSelectableValues())
                .containsExactlyElementsIn(IntStream.rangeClosed(0, NON_EPIC_DAMAGE_VALUES_UPPER_BOUND).boxed().toArray());
    }

    @Test
    public void setSelectableValuesRejectsDirectChanges() {
        List<Integer> before = new ArrayList<>(testPicker.getSelectableValues());
        assertThrows(UnsupportedOperationException.class,
                () -> testPicker.setSelectableValues(Collections.singletonList(7), 7));
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
    public void setEpicValuesPrefersTenWhenPreviousDamageIsNotAvailable() {
        when(mockPicker.getValue()).thenReturn(5);
        testPicker.setEpicValues(true);
        verify(mockPicker).setValue(3);

        when(mockPicker.getValue()).thenReturn(3);
        testPicker.setEpicValues(false);
        verify(mockPicker).setValue(10);
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
        clearInvocations(mockPicker);
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

    @Test
    public void setEpicValuesUsesSortedDistinctFilteredDamageValues() {
        testPicker = new DamageValuePicker(mockPicker, Arrays.asList(15, 7, 15));
        testPicker.setEpicValues(true);
        assertThat(testPicker.getSelectableValues()).containsExactly(7, 15).inOrder();
        verify(mockPicker).setMaxValue(1);
    }

    @Test
    public void setEpicValuesPreservesAllowedDamage() {
        testPicker = new DamageValuePicker(mockPicker, Arrays.asList(7, 10));
        when(mockPicker.getValue()).thenReturn(7, 7, 0);
        TestObserver<Integer> observer = testPicker.values().test();
        testPicker.setEpicValues(true);
        observer.assertValue(7).assertNoErrors();
        assertThat(testPicker.getSelectableValues()).containsExactly(7, 10).inOrder();
    }

    @Test
    public void setEpicValuesFallsBackToLowestDamageWhenTenIsNotAvailable() {
        testPicker = new DamageValuePicker(mockPicker, Arrays.asList(15, 7));
        when(mockPicker.getValue()).thenReturn(5, 5, 0);
        TestObserver<Integer> observer = testPicker.values().test();
        testPicker.setEpicValues(true);
        observer.assertValues(5, 7).assertNoErrors();
    }

    @Test
    public void setEpicValuesSupportsZeroDamageHero() {
        testPicker = new DamageValuePicker(mockPicker, Collections.singleton(0));
        when(mockPicker.getValue()).thenReturn(5, 5, 0);
        TestObserver<Integer> observer = testPicker.values().test();
        testPicker.setEpicValues(true);
        observer.assertValues(5, 0).assertNoErrors();
        assertThat(testPicker.getSelectableValues()).containsExactly(0);
    }

    @Test
    public void emptyHeroValuesAllowNormalModeButRejectHeroModeWithoutChangingState() {
        testPicker = new DamageValuePicker(mockPicker, Collections.emptyList());
        TestObserver<Integer> observer = testPicker.values().test();
        testPicker.setEpicValues(false);
        assertThrows(IllegalStateException.class, () -> testPicker.setEpicValues(true));
        observer.assertValue(0).assertNoErrors();
        assertThat(testPicker.getSelectableValues()).hasSize(NON_EPIC_DAMAGE_VALUES_UPPER_BOUND + 1);
    }

    @Test
    public void constructorRejectsUnknownHeroDamageValues() {
        assertThrows(IllegalArgumentException.class,
                () -> new DamageValuePicker(mockPicker, Collections.singleton(1)));
    }

    @Test
    public void constructorCopiesHeroDamageValues() {
        List<Integer> damageValues = new ArrayList<>(Arrays.asList(7, 15));
        testPicker = new DamageValuePicker(mockPicker, damageValues);
        damageValues.clear();
        testPicker.setEpicValues(true);
        assertThat(testPicker.getSelectableValues()).containsExactly(7, 15).inOrder();
    }
}
