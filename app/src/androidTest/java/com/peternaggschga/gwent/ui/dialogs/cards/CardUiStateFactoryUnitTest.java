package com.peternaggschga.gwent.ui.dialogs.cards;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import android.content.Context;

import androidx.core.util.Pair;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.peternaggschga.gwent.R;
import com.peternaggschga.gwent.data.Ability;
import com.peternaggschga.gwent.data.UnitEntity;
import com.peternaggschga.gwent.domain.cases.DamageCalculatorUseCase;
import com.peternaggschga.gwent.domain.damage.DamageCalculator;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Collections;
import java.util.stream.Stream;

@RunWith(AndroidJUnit4.class)
public class CardUiStateFactoryUnitTest {
    private static final int TESTING_DEPTH = 50;
    CardUiStateFactory factory;
    DamageCalculator calculator;
    UnitEntity unitMock;

    @Before
    public void initMocks() {
        factory = new CardUiStateFactory(ApplicationProvider.getApplicationContext(), false, false);
        calculator = DamageCalculatorUseCase.getDamageCalculator(false, false, Collections.emptyList());
        unitMock = mock(UnitEntity.class);
        when(unitMock.calculateDamage(any())).thenCallRealMethod();
        when(unitMock.isBuffed(any())).thenCallRealMethod();
        when(unitMock.isEpic()).thenReturn(false);
        when(unitMock.getAbility()).thenReturn(Ability.NONE);
        when(unitMock.getSquad()).thenReturn(null);
    }

    @Test
    public void createCardUiStateIdIsSetFromUnit() {
        for (int id = 0; id < TESTING_DEPTH; id++) {
            when(unitMock.getId()).thenReturn(id);
            assertThat(factory.createCardUiState(unitMock, calculator).getUnitId()).isEqualTo(id);
        }
    }

    @Test
    public void createCardUiStateDamageBackgroundIdIsCorrectForNonEpicUnits() {
        CardUiState state = factory.createCardUiState(unitMock, calculator);
        assertThat(state.getDamageBackgroundImageId()).isEqualTo(R.drawable.icon_damage_background);
    }


    @Test
    public void createCardUiStateDamageBackgroundIdIsCorrectForEpicUnits() {
        when(unitMock.isEpic()).thenReturn(true);
        Stream.of(
                Pair.create(0, R.drawable.icon_epic_damage_0),
                Pair.create(7, R.drawable.icon_epic_damage_7),
                Pair.create(8, R.drawable.icon_epic_damage_8),
                Pair.create(10, R.drawable.icon_epic_damage_10),
                Pair.create(11, R.drawable.icon_epic_damage_11),
                Pair.create(15, R.drawable.icon_epic_damage_15)
        ).forEach(pair -> {
            when(unitMock.calculateDamage(calculator)).thenReturn(pair.first);
            CardUiState state = factory.createCardUiState(unitMock, calculator);
            assertThat(state.getDamageBackgroundImageId()).isEqualTo(pair.second);
        });
    }

    @Test
    public void createCardUiStateDamageIsEqualToCalculatorDamage() {
        for (boolean weather : new boolean[]{false, true}) {
            for (boolean horn : new boolean[]{false, true}) {
                calculator = DamageCalculatorUseCase.getDamageCalculator(weather, horn, Collections.emptyList());
                factory = new CardUiStateFactory(ApplicationProvider.getApplicationContext(), weather, horn);
                for (int damage = 0; damage < TESTING_DEPTH; damage++) {
                    when(unitMock.calculateDamage(calculator)).thenReturn(calculator.calculateDamage(0, damage));
                    CardUiState state = factory.createCardUiState(unitMock, calculator);
                    assertThat(state.getDamageString())
                            .isEqualTo(String.valueOf(calculator.calculateDamage(0, damage)));
                }
            }
        }
    }

    @Test
    public void createCardUiStateDamageIsEmptyForEpicUnits() {
        when(unitMock.isEpic()).thenReturn(true);
        CardUiState state = factory.createCardUiState(unitMock, calculator);
        assertThat(state.getDamageString()).isEmpty();
    }

    @Test
    public void createCardUiStateTextColorIsCorrect() {
        Context context = ApplicationProvider.getApplicationContext();

        assertThat(factory.createCardUiState(unitMock, calculator).getDamageTextColor())
                .isEqualTo(context.getColor(R.color.color_damage_textColor));

        factory = new CardUiStateFactory(context, true, false);
        calculator = DamageCalculatorUseCase.getDamageCalculator(true, false, Collections.emptyList());
        assertThat(factory.createCardUiState(unitMock, calculator).getDamageTextColor())
                .isEqualTo(context.getColor(R.color.color_damage_textColor_debuffed));

        factory = new CardUiStateFactory(context, true, true);
        calculator = DamageCalculatorUseCase.getDamageCalculator(true, true, Collections.emptyList());
        assertThat(factory.createCardUiState(unitMock, calculator).getDamageTextColor())
                .isEqualTo(context.getColor(R.color.color_damage_textColor_buffed));
    }

    @Test
    public void createCardUiStateAbilityImageIsCorrect() {
        Stream.of(
                Pair.create(Ability.NONE, CardUiState.UNUSED),
                Pair.create(Ability.BINDING, R.drawable.icon_binding),
                Pair.create(Ability.HORN, R.drawable.icon_horn),
                Pair.create(Ability.MORAL_BOOST, R.drawable.icon_moral_boost),
                Pair.create(Ability.REVENGE, R.drawable.icon_revenge)
        ).forEach(pair -> {
            when(unitMock.getAbility()).thenReturn(pair.first);
            CardUiState state = factory.createCardUiState(unitMock, calculator);
            assertThat(state.getAbilityImageId()).isEqualTo(pair.second);
        });
    }

    @Test
    public void createCardUiStateSquadIsSetFromUnit() {
        when(unitMock.getSquad()).thenReturn(null);
        assertThat(factory.createCardUiState(unitMock, calculator).getSquadString()).isEmpty();

        for (int squad = 1; squad <= TESTING_DEPTH; squad++) {
            when(unitMock.getSquad()).thenReturn(squad);
            assertThat(factory.createCardUiState(unitMock, calculator).getSquadString()).isEqualTo(String.valueOf(squad));
        }
    }
}
