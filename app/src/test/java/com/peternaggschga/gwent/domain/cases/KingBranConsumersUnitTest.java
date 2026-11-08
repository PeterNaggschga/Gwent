package com.peternaggschga.gwent.domain.cases;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import android.content.Context;

import com.peternaggschga.gwent.data.Ability;
import com.peternaggschga.gwent.data.RowType;
import com.peternaggschga.gwent.data.UnitEntity;
import com.peternaggschga.gwent.data.UnitRepository;
import com.peternaggschga.gwent.domain.damage.DamageCalculator;
import com.peternaggschga.gwent.ui.dialogs.cards.CardUiStateFactory;

import org.junit.Test;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import io.reactivex.rxjava3.core.Single;

public class KingBranConsumersUnitTest {
    private UnitEntity unit(int id, int strength, RowType row) {
        UnitEntity unit = mock(UnitEntity.class);
        when(unit.getId()).thenReturn(id);
        when(unit.getRow()).thenReturn(row);
        when(unit.getAbility()).thenReturn(Ability.NONE);
        when(unit.getSquad()).thenReturn(null);
        when(unit.calculateDamage(any())).thenAnswer(invocation ->
                ((DamageCalculator) invocation.getArgument(0)).calculateDamage(id, strength));
        when(unit.isBuffed(any())).thenAnswer(invocation ->
                ((DamageCalculator) invocation.getArgument(0)).isBuffed(id));
        return unit;
    }

    @Test
    public void cardListUsesSavedKingBranSelection() {
        Context context = mock(Context.class);
        UnitEntity unit = unit(1, 5, RowType.MELEE);
        UnitRepository repository = mock(UnitRepository.class);
        when(repository.isKingBran()).thenReturn(true);
        assertThat(new CardUiStateFactory(context, true, false, repository.isKingBran())
                .createCardUiState(Collections.singletonList(unit)).get(0).getDamageString()).isEqualTo("2");
        when(repository.isKingBran()).thenReturn(false);
        assertThat(new CardUiStateFactory(context, true, false, repository.isKingBran())
                .createCardUiState(Collections.singletonList(unit)).get(0).getDamageString()).isEqualTo("1");
    }

    @Test
    @SuppressWarnings("unchecked")
    public void scorchTargetsUseTheSameWeatherPenaltyAsDisplayedStrength() throws Exception {
        UnitRepository repository = mock(UnitRepository.class);
        UnitEntity melee = unit(1, 5, RowType.MELEE);
        UnitEntity ranged = unit(2, 2, RowType.RANGE);
        when(repository.getUnits()).thenReturn(Single.just(Arrays.asList(melee, ranged)));
        for (RowType row : RowType.values()) {
            when(repository.isWeather(row)).thenReturn(Single.just(row == RowType.MELEE));
            when(repository.isHorn(row)).thenReturn(Single.just(false));
            when(repository.getUnits(row)).thenReturn(Single.just(row == RowType.MELEE
                    ? Collections.singletonList(melee) : row == RowType.RANGE
                    ? Collections.singletonList(ranged) : Collections.emptyList()));
        }
        Method method = BurnDialogUseCase.class.getDeclaredMethod("getBurnUnits", UnitRepository.class);
        method.setAccessible(true);
        when(repository.isKingBran()).thenReturn(false);
        List<UnitEntity> ordinary = ((Single<List<UnitEntity>>) method.invoke(null, repository)).blockingGet();
        when(repository.isKingBran()).thenReturn(true);
        List<UnitEntity> kingBran = ((Single<List<UnitEntity>>) method.invoke(null, repository)).blockingGet();
        assertThat(ordinary).containsExactly(ranged);
        assertThat(kingBran).containsExactly(melee, ranged);
    }
}
