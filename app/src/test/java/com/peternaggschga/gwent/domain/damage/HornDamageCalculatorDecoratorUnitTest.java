package com.peternaggschga.gwent.domain.damage;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.valid4j.errors.RequireViolation;

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
        for (int i = 0; i < TESTING_DEPTH; i++) {
            assertThat(decorator.calculateDamage(i, 0)).isEqualTo(TESTING_DAMAGE);
        }
    }

    @Test
    public void calculateDamageAssertsNonNegativeDamage() {
        try {
            new HornDamageCalculatorDecorator(component, Collections.emptyList()).calculateDamage(0, -1);
            fail();
        } catch (RequireViolation ignored) {
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
}
