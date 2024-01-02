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
    public void insertUnitAssertsNull() {
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
    public void deleteUnitsAssertsNull() {
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
            for (int i = 0; i < 5; i++) {
                unitDao.insertUnit(false, i, Ability.values()[i / 2], null, row)
                        .blockingAwait();
            }
            assertThat(unitDao.getUnits(row).blockingGet()).hasSize(5);
        }
        assertThat(unitDao.getUnits().blockingGet()).hasSize(RowType.values().length * 5);
    }

    @Test
    public void getUnitsAssertsNull() {
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
        for (int i = 0; i < RowType.values().length; i++) {
            RowType row = RowType.values()[i];
            unitDao.countUnits(row)
                    .test()
                    .assertValue(0);
            for (int j = 0; j < 5; j++) {
                unitDao.countUnits()
                        .test()
                        .assertValue(i * 5 + j);
                unitDao.insertUnit(false, j, Ability.values()[j / 2], null, row)
                        .andThen(unitDao.countUnits(row))
                        .test()
                        .assertValue(j + 1);
            }
            unitDao.countUnits()
                    .test()
                    .assertValue((i + 1) * 5);
        }

    }

    @Test
    public void countUnitsAssertsNull() {
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

