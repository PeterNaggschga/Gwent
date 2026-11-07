package com.peternaggschga.gwent.data;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertThrows;

import android.content.Context;

import androidx.room3.Room;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;


import com.peternaggschga.gwent.domain.cases.DamageCalculatorUseCase;
import com.peternaggschga.gwent.domain.damage.DamageCalculator;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import io.reactivex.rxjava3.subscribers.TestSubscriber;

@RunWith(AndroidJUnit4.class)
public class BerserkerTransformationIntegrationTest {
    private AppDatabase database;
    private UnitRepository repository;

    @Before
    public void initDatabase() {
        Context context = ApplicationProvider.getApplicationContext();
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase.class).build();
        repository = UnitRepository.getRepository(database, context).blockingGet();
    }

    @After
    public void closeDatabase() {
        database.close();
    }

    private void assertTransformed(UnitEntity unit, RowType row) {
        assertThat(unit.getRow()).isEqualTo(row);
        assertThat(unit.getDamage()).isEqualTo(row == RowType.MELEE ? 14 : 8);
        assertThat(unit.getAbility()).isEqualTo(row == RowType.MELEE ? Ability.MORAL_BOOST : Ability.BINDING);
        assertThat(unit.getSquad()).isEqualTo(row == RowType.MELEE ? null : UnitEntity.BERSERKER_SQUAD);
    }

    @Test
    public void activatingMardroemeTransformsOnlyBerserkersInThatRowAndKeepsTheirIds() {
        for (RowType row : new RowType[]{RowType.MELEE, RowType.RANGE}) {
            repository.insertUnit(false, 2, Ability.BERSERKER, null, row, 2).blockingAwait();
            repository.insertUnit(false, 5, Ability.NONE, null, row, 1).blockingAwait();
        }
        for (RowType row : new RowType[]{RowType.MELEE, RowType.RANGE}) {
            List<UnitEntity> original = repository.getUnits(row).blockingGet();
            repository.switchMardroeme(row).blockingAwait();
            for (UnitEntity unit : original) {
                UnitEntity updated = repository.getUnit(unit.getId()).blockingGet();
                if (unit.getAbility() == Ability.BERSERKER) {
                    assertTransformed(updated, row);
                } else {
                    assertThat(updated.getAbility()).isEqualTo(Ability.NONE);
                    assertThat(updated.getDamage()).isEqualTo(5);
                }
            }
            assertThat(repository.countUnits(row).blockingGet()).isEqualTo(3);
        }
    }

    @Test
    public void activeMardroemeTransformsNewlyPersistedUnits() {
        for (RowType row : new RowType[]{RowType.MELEE, RowType.RANGE}) {
            repository.switchMardroeme(row).blockingAwait();
            repository.insertUnit(false, 2, Ability.BERSERKER, null, row, 3).blockingAwait();
            List<UnitEntity> units = repository.getUnits(row).blockingGet();
            assertThat(units).hasSize(3);
            assertThat(units.stream().map(UnitEntity::getId).distinct().count()).isEqualTo(3);
            for (UnitEntity unit : units) {
                assertTransformed(unit, row);
            }
        }
    }

    @Test
    public void hornMustBeClearedBeforeTransformationAndTransformationsArePermanent() {
        repository.insertUnit(false, 2, Ability.BERSERKER, null, RowType.MELEE, 1).blockingAwait();
        repository.switchHorn(RowType.MELEE).blockingAwait();
        repository.switchMardroeme(RowType.MELEE).blockingAwait();
        assertThat(repository.getBuffState(RowType.MELEE).blockingGet()).isEqualTo(RowBuffState.NONE);
        assertThat(repository.getUnits(RowType.MELEE).blockingGet().get(0).getAbility()).isEqualTo(Ability.BERSERKER);
        repository.switchMardroeme(RowType.MELEE).blockingAwait();
        repository.switchMardroeme(RowType.MELEE).blockingAwait();
        assertTransformed(repository.getUnits(RowType.MELEE).blockingGet().get(0), RowType.MELEE);
        repository.switchMardroeme(RowType.MELEE).blockingAwait();
        repository.clearMardroeme().blockingAwait();
        assertTransformed(repository.getUnits(RowType.MELEE).blockingGet().get(0), RowType.MELEE);
    }

    @Test
    public void rangedBerserkersUseAnExclusiveSquadAcrossBatchesAndCopies() {
        repository.insertUnit(false, 8, Ability.BINDING, 1, RowType.RANGE, 1).blockingAwait();
        repository.switchMardroeme(RowType.RANGE).blockingAwait();
        repository.insertUnit(false, 2, Ability.BERSERKER, null, RowType.RANGE, 1).blockingAwait();
        int id = repository.getUnits(RowType.RANGE).blockingGet().stream()
                .filter(unit -> unit.getSquad() == UnitEntity.BERSERKER_SQUAD).findFirst().get().getId();
        repository.copy(id).blockingAwait();
        repository.insertUnit(false, 2, Ability.BERSERKER, null, RowType.RANGE, 1).blockingAwait();
        assertThrows(IllegalArgumentException.class, () -> repository.insertUnit(false, 8, Ability.BINDING,
                UnitEntity.BERSERKER_SQUAD, RowType.RANGE, 1).blockingAwait());
        List<UnitEntity> units = repository.getUnits(RowType.RANGE).blockingGet();
        assertThat(units.stream().filter(unit -> unit.getSquad() == UnitEntity.BERSERKER_SQUAD).count()).isEqualTo(3);
        DamageCalculator calculator = DamageCalculatorUseCase.getDamageCalculator(false, false, units);
        for (UnitEntity unit : units) {
            assertThat(unit.calculateDamage(calculator)).isEqualTo(unit.getSquad() == UnitEntity.BERSERKER_SQUAD ? 24 : 8);
        }
    }

    @Test
    public void unitStreamEmitsChangedStatsWithUnchangedIdsAndCount() {
        repository.insertUnit(false, 2, Ability.BERSERKER, null, RowType.MELEE, 1).blockingAwait();
        TestSubscriber<List<UnitEntity>> observer = repository.getUnitsFlowable(RowType.MELEE).test();
        try {
            observer.awaitCount(1);
            int id = observer.values().get(0).get(0).getId();
            repository.switchMardroeme(RowType.MELEE).blockingAwait();
            observer.awaitCount(2).assertNoErrors().assertValueCount(2);
            assertTransformed(observer.values().get(1).get(0), RowType.MELEE);
            assertThat(observer.values().get(1).get(0).getId()).isEqualTo(id);
        } finally {
            observer.cancel();
        }
    }

    @Test
    public void insertionTransformsOnlyReturnedIdsAndCopyAndResetUseTheSamePath() {
        repository.switchMardroeme(RowType.MELEE).blockingAwait();
        long oldId = database.units().insertUnits(Collections.singletonList(
                new UnitEntity(false, 2, Ability.BERSERKER, null, RowType.MELEE))).blockingGet().get(0);
        UnitEntity oldBerserker = repository.getUnit((int) oldId).blockingGet();

        repository.insertUnit(false, 2, Ability.BERSERKER, null, RowType.MELEE, 1).blockingAwait();
        assertThat(repository.getUnit((int) oldId).blockingGet().getAbility()).isEqualTo(Ability.BERSERKER);
        assertThat(repository.getUnits(RowType.MELEE).blockingGet().stream()
                .filter(unit -> unit.getAbility() == Ability.MORAL_BOOST).count()).isEqualTo(1);

        repository.insertUnits(Collections.singletonList(oldBerserker)).blockingAwait();
        assertThat(repository.countUnits().blockingGet()).isEqualTo(2);
        assertThat(repository.getUnit((int) oldId).blockingGet().getAbility()).isEqualTo(Ability.BERSERKER);

        repository.copy((int) oldId).blockingAwait();
        assertThat(repository.getUnits(RowType.MELEE).blockingGet().stream()
                .filter(unit -> unit.getAbility() == Ability.MORAL_BOOST).count()).isEqualTo(2);
        repository.reset(oldBerserker).blockingAwait();
        assertThat(repository.countUnits().blockingGet()).isEqualTo(1);
        assertThat(repository.getUnit((int) oldId).blockingGet().getAbility()).isEqualTo(Ability.BERSERKER);
        assertThat(repository.getBuffState(RowType.MELEE).blockingGet()).isEqualTo(RowBuffState.NONE);
    }

    @Test
    public void batchUpdateMatchesIdsWithoutInsertingMissingUnits() {
        repository.insertUnit(false, 2, Ability.BERSERKER, null, RowType.MELEE, 2).blockingAwait();
        List<UnitEntity> original = repository.getUnits(RowType.MELEE).blockingGet();
        UnitEntity first = original.get(0).withStats(14, Ability.MORAL_BOOST, null);
        UnitEntity second = original.get(1).withStats(14, Ability.MORAL_BOOST, null);
        UnitEntity missing = new UnitEntity(false, 5, Ability.NONE, null, RowType.MELEE);
        missing.setId(9999);
        repository.updateUnits(Arrays.asList(first, second, missing)).blockingAwait();
        assertThat(repository.countUnits().blockingGet()).isEqualTo(2);
        for (UnitEntity unit : original) {
            assertThat(unit.getDamage()).isEqualTo(2);
            assertThat(unit.getAbility()).isEqualTo(Ability.BERSERKER);
            assertTransformed(repository.getUnit(unit.getId()).blockingGet(), RowType.MELEE);
        }
    }
}
