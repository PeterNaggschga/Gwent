package com.peternaggschga.gwent.domain.damage;

import static com.google.common.truth.Truth.assertThat;
import static com.peternaggschga.gwent.domain.damage.DamageCalculator.Color.DEBUFFED;
import static com.peternaggschga.gwent.domain.damage.DamageCalculator.Color.DEFAULT;
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
    public void calculateDamageZeroDamageAlwaysReturnsZero() {
        assertThat(falseWeather.calculateDamage(0, 0)).isEqualTo(0);
        assertThat(trueWeather.calculateDamage(0, 0)).isEqualTo(0);
    }

    @Test
    public void calculateDamageAssertsNonNegativeDamage() {
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
    public void calculateDamageFalseWeatherAlwaysReturnsDamage() {
        for (int id = 0; id < TESTING_DEPTH; id++) {
            for (int damage = 0; damage < TESTING_DEPTH; damage++) {
                assertThat(falseWeather.calculateDamage(id, damage)).isEqualTo(damage);
            }
        }
    }

    @Test
    public void calculateDamageTrueWeatherAlwaysReturnsOne() {
        for (int id = 0; id < TESTING_DEPTH; id++) {
            for (int damage = 1; damage <= TESTING_DEPTH; damage++) {
                assertThat(trueWeather.calculateDamage(id, damage)).isEqualTo(1);
            }
        }
    }

    @Test
    public void isBuffedReturnsDefaultWhenWeatherIsFalse() {
        assertThat(new WeatherDamageCalculator(false).isBuffed(0)).isEqualTo(DEFAULT);
    }

    @Test
    public void isBuffedReturnsDebuffedWhenWeatherIsTrue() {
        assertThat(new WeatherDamageCalculator(true).isBuffed(0)).isEqualTo(DEBUFFED);
    }
}
