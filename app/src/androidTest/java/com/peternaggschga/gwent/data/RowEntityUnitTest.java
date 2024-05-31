package com.peternaggschga.gwent.data;

import androidx.room.Room;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.peternaggschga.gwent.RowType;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class RowEntityUnitTest {
    private static AppDatabase database;

    @Before
    public void initDatabase() {
        database = Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(), AppDatabase.class).build();
    }

    @After
    public void closeDatabase() {
        database.clearAllTables();
        database.close();
    }

    @Test
    public void hornIsFalseByDefault() {
        for (RowType row : RowType.values()) {
            database.rows().insertRow(new RowEntity(row))
                    .andThen(database.rows().isHorn(row))
                    .test()
                    .assertValue(false);
        }
    }

    @Test
    public void weatherIsFalseByDefault() {
        for (RowType row : RowType.values()) {
            database.rows().insertRow(new RowEntity(row))
                    .andThen(database.rows().isWeather(row))
                    .test()
                    .assertValue(false);
        }
    }
}
