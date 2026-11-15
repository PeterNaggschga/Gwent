package com.peternaggschga.gwent.ui.dialogs.addcard.filter;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.Collections;

@RunWith(JUnit4.class)
public class AbilityFilterDefaultMethodsUnitTest {
    @Test
    public void zeroDamageAloneStillMakesHeroesAvailable() {
        AbilityFilter filter = mock(AbilityFilter.class, CALLS_REAL_METHODS);
        when(filter.getHeroDamageValues()).thenReturn(Collections.singleton(0));
        assertThat(filter.heroesAvailable()).isTrue();
    }

    @Test
    public void emptyDamageSetMakesHeroesUnavailable() {
        AbilityFilter filter = mock(AbilityFilter.class, CALLS_REAL_METHODS);
        when(filter.getHeroDamageValues()).thenReturn(Collections.emptySet());
        assertThat(filter.heroesAvailable()).isFalse();
    }
}
