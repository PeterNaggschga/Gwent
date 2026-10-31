package com.peternaggschga.gwent.data;

import androidx.room3.Room;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import kotlinx.coroutines.BuildersKt;
import kotlinx.coroutines.Dispatchers;

@RunWith(AndroidJUnit4.class)
public class RowEntityUnitTest {
    private static AppDatabase database;

    @Before
    public void initDatabase() {
        database = Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(), AppDatabase.class).build();
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
    public void hornIsFalseByDefault() throws InterruptedException {
        for (RowType row : RowType.values()) {
            database.rows().insertRow(new RowEntity(row))
                    .andThen(database.rows().isHorn(row))
                    .test()
                    .await()
                    .assertValue(false);
        }
    }

    @Test
    public void weatherIsFalseByDefault() throws InterruptedException {
        for (RowType row : RowType.values()) {
            database.rows().insertRow(new RowEntity(row))
                    .andThen(database.rows().isWeather(row))
                    .test()
                    .await()
                    .assertValue(false);
        }
    }
}
