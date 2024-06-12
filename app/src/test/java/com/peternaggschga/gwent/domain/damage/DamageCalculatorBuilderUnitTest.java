package com.peternaggschga.gwent.domain.damage;

import static com.google.common.truth.Truth.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class DamageCalculatorBuilderUnitTest {
    @Test
    public void getResultNeverReturnsNull() {
        assertThat(new DamageCalculatorBuilder().getResult()).isNotNull();
    }
}
