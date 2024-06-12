package com.peternaggschga.gwent.domain.damage;

import static com.google.common.truth.Truth.assertThat;
import static com.peternaggschga.gwent.domain.damage.DamageCalculator.Color.BUFFED;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collections;
import java.util.List;

@RunWith(MockitoJUnitRunner.class)
public class HornDamageCalculatorDecoratorUnitTest {
    private static final int TESTING_DEPTH = 500;
    private static final int TESTING_DAMAGE = 5;
    private final WeatherDamageCalculator component = Mockito.mock(WeatherDamageCalculator.class);

    @Before
    public void initMock() {
        when(component.calculateDamage(anyInt(), anyInt())).thenReturn(TESTING_DAMAGE);
    }

    @Test
    public void calculateDamageEmptyListReturnsInput() {
        HornDamageCalculatorDecorator decorator = new HornDamageCalculatorDecorator(component, Collections.emptyList());
        for (int id = 0; id < TESTING_DEPTH; id++) {
            assertThat(decorator.calculateDamage(id, 0)).isEqualTo(TESTING_DAMAGE);
        }
    }

    @Test
    public void calculateDamageAssertsNonNegativeDamage() {
        try {
            new HornDamageCalculatorDecorator(component, Collections.emptyList()).calculateDamage(0, -1);
            fail();
        } catch (IllegalArgumentException ignored) {
        }
    }

    @Test
    public void calculateDamageDoublesDamageForNullElement() {
        // noinspection unchecked cast
        List<Integer> list = (List<Integer>) Mockito.mock(List.class);
        when(list.contains(null)).thenReturn(true);
        HornDamageCalculatorDecorator decorator = new HornDamageCalculatorDecorator(component, list);
        assertThat(decorator.calculateDamage(0, 0)).isEqualTo(2 * TESTING_DAMAGE);
    }

    @Test
    public void calculateDamageDoublesDamageForOtherElement() {
        // noinspection unchecked cast
        List<Integer> list = (List<Integer>) Mockito.mock(List.class);
        when(list.contains(anyInt())).thenReturn(false);
        when(list.contains(null)).thenReturn(false);
        HornDamageCalculatorDecorator decorator = new HornDamageCalculatorDecorator(component, list);
        assertThat(decorator.calculateDamage(0, 0)).isEqualTo(2 * TESTING_DAMAGE);
    }

    @Test
    public void calculateDamageDoesNotDoubleOnlyHornUnit() {
        // noinspection unchecked cast
        List<Integer> list = (List<Integer>) Mockito.mock(List.class);
        when(list.contains(anyInt())).thenReturn(false);
        when(list.contains(0)).thenReturn(true);
        when(list.size()).thenReturn(1);
        HornDamageCalculatorDecorator decorator = new HornDamageCalculatorDecorator(component, list);
        assertThat(decorator.calculateDamage(0, 0)).isEqualTo(TESTING_DAMAGE);
    }

    @Test
    public void calculateDamageMultipleHornUnitsDoubleEachOther() {
        // noinspection unchecked cast
        List<Integer> list = (List<Integer>) Mockito.mock(List.class);
        when(list.contains(anyInt())).thenReturn(true);
        when(list.size()).thenReturn(2);
        HornDamageCalculatorDecorator decorator = new HornDamageCalculatorDecorator(component, list);
        assertThat(decorator.calculateDamage(0, 0)).isEqualTo(TESTING_DAMAGE * 2);
        assertThat(decorator.calculateDamage(1, 0)).isEqualTo(TESTING_DAMAGE * 2);
    }


    @Test
    public void calculateDamageDoublesHornUnitWithHornCard() {
        // noinspection unchecked cast
        List<Integer> list = (List<Integer>) Mockito.mock(List.class);
        when(list.contains(anyInt())).thenReturn(true);
        when(list.size()).thenReturn(2);
        HornDamageCalculatorDecorator decorator = new HornDamageCalculatorDecorator(component, list);
        assertThat(decorator.calculateDamage(0, 0)).isEqualTo(TESTING_DAMAGE * 2);
    }

    @Test
    public void isBuffedReturnsBuffedWhenUnitIsDoubled() {
        // noinspection unchecked cast
        List<Integer> list = (List<Integer>) Mockito.mock(List.class);
        when(list.contains(anyInt())).thenReturn(true);
        when(list.size()).thenReturn(2);
        HornDamageCalculatorDecorator decorator = new HornDamageCalculatorDecorator(component, list);
        assertThat(decorator.isBuffed(0)).isEqualTo(BUFFED);
    }

    @Test
    public void isBuffedCallsComponentWhenUnitIsNotDoubled() {
        // noinspection unchecked cast
        List<Integer> list = (List<Integer>) Mockito.mock(List.class);
        when(list.contains(anyInt())).thenReturn(true);
        when(list.size()).thenReturn(1);
        HornDamageCalculatorDecorator decorator = new HornDamageCalculatorDecorator(component, list);
        decorator.isBuffed(0);
        verify(component).isBuffed(0);
    }
}
