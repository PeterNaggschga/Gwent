package com.peternaggschga.gwent.data;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.fail;

import androidx.room3.Room;
import androidx.room3.rxjava3.EmptyResultSetException;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import io.reactivex.rxjava3.core.Flowable;
import kotlinx.coroutines.BuildersKt;
import kotlinx.coroutines.Dispatchers;

@RunWith(AndroidJUnit4.class)
public class UnitDaoUnitTest {
    private AppDatabase database;
    private UnitDao unitDao;

    @Before
    public void initDatabase() {
        database = Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(), AppDatabase.class).build();
        for (RowType row : RowType.values()) {
            database.rows().insertRow(new RowEntity(row)).blockingAwait();
        }
        unitDao = database.units();
    }

    @After
    public void closeDatabase() throws InterruptedException {
        BuildersKt.runBlocking(
                Dispatchers.getUnconfined(),
                ((coroutineScope, continuation) -> database.clearAllTables(continuation))
        );
        database.close();
    }

    @Test
    public void insertUnitsAssertsNonNull() {
        try {
            //noinspection DataFlowIssue
            unitDao.insertUnits(null)
                    .blockingAwait();
            fail();
        } catch (NullPointerException ignored) {
        }
        try {
            //noinspection DataFlowIssue
            unitDao.insertUnits(Collections.singletonList(new UnitEntity(false, 0, null, null, RowType.MELEE)))
                    .blockingAwait();
            fail();
        } catch (NullPointerException ignored) {
        }
        try {
            //noinspection DataFlowIssue
            unitDao.insertUnits(Collections.singletonList(new UnitEntity(false, 0, Ability.NONE, null, null)))
                    .blockingAwait();
            fail();
        } catch (NullPointerException ignored) {
        }
    }

    @Test
    public void insertUnitsIgnoresDoubles() {
        //noinspection OptionalGetWithoutIsPresent
        UnitEntity entity = unitDao.insertUnits(Collections.singletonList(new UnitEntity(false, 0, Ability.NONE, null, RowType.MELEE)))
                .andThen(unitDao.getUnits())
                .blockingGet()
                .stream().findFirst().get();
        try {
            unitDao.insertUnits(Collections.singletonList(entity))
                    .andThen(unitDao.countUnits())
                    .test()
                    .await()
                    .assertValue(1);
        } catch (Exception ignored) {
            fail();
        }

    }

    @Test
    public void insertUnitsInsertsBatchWithDistinctGeneratedIds() {
        List<UnitEntity> batch = new ArrayList<>();
        for (RowType row : RowType.values()) {
            for (int damage = 1; damage <= 3; damage++) {
                batch.add(new UnitEntity(false, damage, Ability.NONE, null, row));
            }
        }
        unitDao.insertUnits(batch).blockingAwait();
        List<UnitEntity> inserted = unitDao.getUnits().blockingGet();
        assertThat(inserted).hasSize(batch.size());
        assertThat(inserted.stream().map(UnitEntity::getId).distinct().count()).isEqualTo(batch.size());
        assertThat(inserted.stream().allMatch(unit -> unit.getId() > 0)).isTrue();
        for (RowType row : RowType.values()) {
            assertThat(unitDao.getUnits(row).blockingGet().stream().map(UnitEntity::getDamage)
                    .collect(Collectors.toList())).containsExactly(1, 2, 3);
        }
    }

    @Test
    public void insertUnitsAcceptsEmptyBatch() {
        unitDao.insertUnits(Collections.emptyList()).blockingAwait();
        assertThat(unitDao.countUnits().blockingGet()).isEqualTo(0);
    }

    @Test
    public void insertUnitsIgnoresExistingIdsButInsertsNewUnitsInSameBatch() {
        unitDao.insertUnits(Collections.singletonList(new UnitEntity(false, 5, Ability.NONE, null, RowType.MELEE)))
                .blockingAwait();
        UnitEntity existing = unitDao.getUnits().blockingGet().get(0);
        unitDao.insertUnits(Arrays.asList(existing, new UnitEntity(false, 8, Ability.NONE, null, RowType.RANGE)))
                .blockingAwait();
        assertThat(unitDao.countUnits().blockingGet()).isEqualTo(2);
        assertThat(unitDao.getUnit(existing.getId()).blockingGet().getDamage()).isEqualTo(5);
    }

    @Test
    public void insertUnitsRollsBackBatchOnForeignKeyFailure() {
        database.rows().clearRows().andThen(database.rows().insertRow(new RowEntity(RowType.MELEE))).blockingAwait();
        assertThrows(RuntimeException.class, () -> unitDao.insertUnits(Arrays.asList(
                new UnitEntity(false, 5, Ability.NONE, null, RowType.MELEE),
                new UnitEntity(false, 5, Ability.NONE, null, RowType.RANGE))).blockingAwait());
        assertThat(unitDao.countUnits().blockingGet()).isEqualTo(0);
    }

    @Test
    public void deleteUnitDeletesUnit() throws InterruptedException {
        getUnitsReturnsAllUnits();
        List<UnitEntity> units = unitDao.getUnits().blockingGet();
        assertThat(units).hasSize(RowType.values().length * 5);

        for (int i = 0; i < units.size(); ) {
            unitDao.deleteUnit(units.get(i).getId())
                    .andThen(unitDao.countUnits())
                    .test()
                    .await()
                    .assertValue(units.size() - ++i);
        }
    }

    @Test
    public void deleteUnitsDeletesUnits() throws InterruptedException {
        getUnitsReturnsAllUnits();
        List<UnitEntity> units = unitDao.getUnits().blockingGet();
        assertThat(units).hasSize(RowType.values().length * 5);
        unitDao.deleteUnits(units)
                .andThen(unitDao.countUnits())
                .test()
                .await()
                .assertValue(0);
    }

    @Test
    public void deleteUnitsAssertsNonNull() {
        try {
            //noinspection DataFlowIssue
            unitDao.deleteUnits(null)
                    .blockingAwait();
            fail();
        } catch (NullPointerException ignored) {
        }
    }

    @Test
    public void getUnitsReturnsAllUnits() {
        for (RowType row : RowType.values()) {
            for (int unitNumber = 0; unitNumber < 5; unitNumber++) {
                assertThat(
                        unitDao.insertUnits(Collections.singletonList(new UnitEntity(false, unitNumber, Ability.values()[unitNumber / 2], null, row)))
                                .andThen(unitDao.getUnits(row))
                                .blockingGet())
                        .hasSize(unitNumber + 1);
            }
        }
        assertThat(
                unitDao.getUnits()
                        .blockingGet())
                .hasSize(RowType.values().length * 5);
    }

    @Test
    public void getUnitsAssertsNonNull() {
        try {
            //noinspection DataFlowIssue
            unitDao.getUnits(null)
                    .test()
                    .dispose();
            fail();
        } catch (NullPointerException ignored) {
        }
    }

    @Test
    public void getUnitReturnsUnit() {
        try {
            UnitEntity unitEntity = unitDao.insertUnits(Collections.singletonList(new UnitEntity(false, 0, Ability.NONE, null, RowType.MELEE)))
                    .andThen(unitDao.getUnit(1)).blockingGet();
            assertThat(
                    !unitEntity.isEpic() && unitEntity.getDamage() == 0 && unitEntity.getAbility() == Ability.NONE
                            && unitEntity.getSquad() == null && unitEntity.getRow() == RowType.MELEE
            ).isTrue();


        } catch (Exception ignored) {
            fail();
        }
    }

    @Test
    public void getUnitAssertsCorrectId() {
        try {
            // noinspection ResultOfMethodCallIgnored
            unitDao.getUnit(-1).blockingGet();
            fail();
        } catch (EmptyResultSetException ignored) {
        }
    }

    @Test
    public void getUnitsFlowableReturnsAllUnits() {
        for (RowType row : RowType.values()) {
            Flowable<List<UnitEntity>> unitsFlowable = unitDao.getUnitsFlowable(row);
            for (int unitNumber = 0; unitNumber < 5; unitNumber++) {
                assertThat(
                        unitDao.insertUnits(Collections.singletonList(new UnitEntity(false, unitNumber, Ability.values()[unitNumber / 2], null, row)))
                                .andThen(unitsFlowable)
                                .blockingFirst())
                        .hasSize(unitNumber + 1);
            }
        }
    }

    @Test
    public void getUnitsFlowableAssertsNonNull() {
        try {
            //noinspection DataFlowIssue, ResultOfMethodCallIgnored
            unitDao.getUnitsFlowable(null).blockingFirst();
            fail();
        } catch (NullPointerException ignored) {
        }
    }

    @Test
    public void countUnitsCountsCorrectly() throws InterruptedException {
        for (int rowTypeNumber = 0; rowTypeNumber < RowType.values().length; rowTypeNumber++) {
            RowType row = RowType.values()[rowTypeNumber];
            unitDao.countUnits(row)
                    .test()
                    .await()
                    .assertValue(0);
            for (int unitNumber = 0; unitNumber < 5; unitNumber++) {
                unitDao.countUnits()
                        .test()
                        .await()
                        .assertValue(rowTypeNumber * 5 + unitNumber);
                unitDao.insertUnits(Collections.singletonList(new UnitEntity(false, unitNumber, Ability.values()[unitNumber / 2], null, row)))
                        .andThen(unitDao.countUnits(row))
                        .test()
                        .await()
                        .assertValue(unitNumber + 1);
            }
            unitDao.countUnits()
                    .test()
                    .await()
                    .assertValue((rowTypeNumber + 1) * 5);
        }

    }

    @Test
    public void countUnitsAssertsNonNull() {
        try {
            //noinspection DataFlowIssue
            unitDao.countUnits(null)
                    .test()
                    .dispose();
            fail();
        } catch (NullPointerException ignored) {
        }
    }

    @Test
    public void hasNonEpicUnitsFlowableHasValueFromBeginning() {
        assertThat(unitDao.hasNonEpicUnitsFlowable().blockingFirst()).isFalse();
    }
}
