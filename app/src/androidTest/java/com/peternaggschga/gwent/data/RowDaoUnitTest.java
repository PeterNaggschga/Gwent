package com.peternaggschga.gwent.data;

import static org.junit.Assert.fail;

import androidx.room.Room;
import androidx.room.rxjava3.EmptyResultSetException;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class RowDaoUnitTest {
    private AppDatabase database;
    private RowDao rowDao;

    @Before
    public void initDatabase() {
        database = Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(), AppDatabase.class).build();
        rowDao = database.rows();
    }

    @After
    public void closeDatabase() {
        database.clearAllTables();
        database.close();
    }

    @Test
    public void insertRowAssertsNull() {
        try {
            //noinspection DataFlowIssue
            rowDao.insertRow(null).blockingAwait();
            fail();
        } catch (NullPointerException ignored) {
        }
    }

    @Test
    public void insertRowIgnoresDoubles() {
        for (RowType row : RowType.values()) {
            rowDao.insertRow(new RowEntity(row))
                    .andThen(rowDao.updateWeather(row))
                    .andThen(rowDao.insertRow(new RowEntity(row)))
                    .blockingAwait();
            rowDao.isWeather(row).test().assertValue(true);
        }
    }

    @Test
    public void clearRowsClearsRows() {
        for (RowType row : RowType.values()) {
            rowDao.insertRow(new RowEntity(row))
                    .andThen(rowDao.clearRows())
                    .andThen(rowDao.isWeather(row))
                    .test()
                    .assertError(EmptyResultSetException.class);
        }
    }

    @Test
    public void updateWeatherUpdatesWeather() {
        for (RowType row : RowType.values()) {
            rowDao.insertRow(new RowEntity(row))
                    .andThen(rowDao.isWeather(row))
                    .test()
                    .assertValue(false);
            rowDao.updateWeather(row)
                    .andThen(rowDao.isWeather(row))
                    .test()
                    .assertValue(true);
            rowDao.updateWeather(row)
                    .andThen(rowDao.isWeather(row))
                    .test()
                    .assertValue(false);
        }
    }

    @Test
    public void updateWeatherAssertsNull() {
        try {
            //noinspection DataFlowIssue
            rowDao.insertRow(new RowEntity(RowType.MELEE))
                    .andThen(rowDao.updateWeather(null))
                    .blockingAwait();
            fail();
        } catch (NullPointerException ignored) {
        }
    }

    @Test
    public void clearWeatherClearsWeather() {
        for (RowType row : RowType.values()) {
            rowDao.insertRow(new RowEntity(row))
                    .andThen(rowDao.updateWeather(row))
                    .blockingAwait();
        }
        rowDao.clearWeather().blockingAwait();
        for (RowType row : RowType.values()) {
            rowDao.isWeather(row)
                    .test()
                    .assertValue(false);
        }
    }

    @Test
    public void updateHornUpdatesHorn() {
        for (RowType row : RowType.values()) {
            rowDao.insertRow(new RowEntity(row))
                    .andThen(rowDao.isHorn(row))
                    .test()
                    .assertValue(false);
            rowDao.updateHorn(row)
                    .andThen(rowDao.isHorn(row))
                    .test()
                    .assertValue(true);
            rowDao.updateHorn(row)
                    .andThen(rowDao.isHorn(row))
                    .test()
                    .assertValue(false);
        }
    }

    @Test
    public void updateHornAssertsNull() {
        try {
            //noinspection DataFlowIssue
            rowDao.insertRow(new RowEntity(RowType.MELEE))
                    .andThen(rowDao.updateHorn(null))
                    .blockingAwait();
            fail();
        } catch (NullPointerException ignored) {
        }
    }

    @Test
    public void isWeatherAssertsNull() {
        try {
            //noinspection DataFlowIssue
            rowDao.insertRow(new RowEntity(RowType.MELEE))
                    .andThen(rowDao.isWeather(null))
                    .test()
                    .dispose();
            fail();
        } catch (NullPointerException ignored) {
        }
    }

    @Test
    public void isHornAssertsNull() {
        try {
            //noinspection DataFlowIssue
            rowDao.insertRow(new RowEntity(RowType.MELEE))
                    .andThen(rowDao.isHorn(null))
                    .test()
                    .dispose();
            fail();
        } catch (NullPointerException ignored) {
        }
    }
}
