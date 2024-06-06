package com.peternaggschga.gwent.data;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.fail;

import androidx.room.Room;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

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
    public void closeDatabase() {
        database.clearAllTables();
        database.close();
    }

    @Test
    public void insertUnitAssertsNonNull() {
        try {
            //noinspection DataFlowIssue
            unitDao.insertUnit(null)
                    .blockingAwait();
            fail();
        } catch (NullPointerException ignored) {
        }
        try {
            //noinspection DataFlowIssue
            unitDao.insertUnit(false, 0, null, null, RowType.MELEE)
                    .blockingAwait();
            fail();
        } catch (NullPointerException ignored) {
        }
        try {
            //noinspection DataFlowIssue
            unitDao.insertUnit(false, 0, Ability.NONE, null, null)
                    .blockingAwait();
            fail();
        } catch (NullPointerException ignored) {
        }
    }

    @Test
    public void insertUnitIgnoresDoubles() {
        //noinspection OptionalGetWithoutIsPresent
        UnitEntity entity = unitDao.insertUnit(new UnitEntity(false, 0, Ability.NONE, null, RowType.MELEE))
                .andThen(unitDao.getUnits())
                .blockingGet()
                .stream().findFirst().get();
        try {
            unitDao.insertUnit(entity)
                    .andThen(unitDao.countUnits())
                    .test()
                    .assertValue(1);
        } catch (Exception ignored) {
            fail();
        }

    }

    @Test
    public void deleteUnitDeletesUnit() {
        getUnitsReturnsAllUnits();
        List<UnitEntity> units = unitDao.getUnits().blockingGet();
        assertThat(units).hasSize(RowType.values().length * 5);

        for (int i = 0; i < units.size(); ) {
            unitDao.deleteUnit(units.get(i).getId())
                    .andThen(unitDao.countUnits())
                    .test()
                    .assertValue(units.size() - ++i);
        }
    }

    @Test
    public void deleteUnitsDeletesUnits() {
        getUnitsReturnsAllUnits();
        List<UnitEntity> units = unitDao.getUnits().blockingGet();
        assertThat(units).hasSize(RowType.values().length * 5);
        unitDao.deleteUnits(units)
                .andThen(unitDao.countUnits())
                .test()
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
                        unitDao.insertUnit(false, unitNumber, Ability.values()[unitNumber / 2], null, row)
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
    public void countUnitsCountsCorrectly() {
        for (int rowTypeNumber = 0; rowTypeNumber < RowType.values().length; rowTypeNumber++) {
            RowType row = RowType.values()[rowTypeNumber];
            unitDao.countUnits(row)
                    .test()
                    .assertValue(0);
            for (int unitNumber = 0; unitNumber < 5; unitNumber++) {
                unitDao.countUnits()
                        .test()
                        .assertValue(rowTypeNumber * 5 + unitNumber);
                unitDao.insertUnit(false, unitNumber, Ability.values()[unitNumber / 2], null, row)
                        .andThen(unitDao.countUnits(row))
                        .test()
                        .assertValue(unitNumber + 1);
            }
            unitDao.countUnits()
                    .test()
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
}
