package com.peternaggschga.gwent.domain.damage;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.fail;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.valid4j.errors.RequireViolation;

@RunWith(JUnit4.class)
public class WeatherDamageCalculatorUnitTest {
    private static final int TESTING_DEPTH = 500;
    private final WeatherDamageCalculator trueWeather = new WeatherDamageCalculator(true);
    private final WeatherDamageCalculator falseWeather = new WeatherDamageCalculator(false);

    @Test
    public void zeroDamageAlwaysReturnsZero() {
        assertThat(falseWeather.calculateDamage(0, 0)).isEqualTo(0);
        assertThat(trueWeather.calculateDamage(0, 0)).isEqualTo(0);
    }

    @Test
    public void assertsNonNegativeDamage() {
        try {
            trueWeather.calculateDamage(0, -1);
            fail();
        } catch (RequireViolation ignored) {
        }
        try {
            falseWeather.calculateDamage(0, -1);
            fail();
        } catch (RequireViolation ignored) {
        }
    }

    @Test
    public void falseWeatherAlwaysReturnsDamage() {
        for (int i = 0; i < TESTING_DEPTH; i++) {
            for (int j = 0; j < TESTING_DEPTH; j++) {
                assertThat(falseWeather.calculateDamage(i, j)).isEqualTo(j);
            }
        }
    }

    @Test
    public void trueWeatherAlwaysReturnsOne() {
        for (int i = 0; i < TESTING_DEPTH; i++) {
            for (int j = 1; j <= TESTING_DEPTH; j++) {
                assertThat(trueWeather.calculateDamage(i, j)).isEqualTo(1);
            }
        }
    }
}
