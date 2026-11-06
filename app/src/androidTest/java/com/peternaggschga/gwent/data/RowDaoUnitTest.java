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

import io.reactivex.rxjava3.subscribers.TestSubscriber;
import kotlinx.coroutines.BuildersKt;
import kotlinx.coroutines.Dispatchers;

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
    public void closeDatabase() throws InterruptedException {
        BuildersKt.runBlocking(
                Dispatchers.getUnconfined(),
                ((coroutineScope, continuation) -> database.clearAllTables(continuation))
        );
        database.close();
    }

    @Test
    public void insertRowAssertsNonNull() {
        try {
            //noinspection DataFlowIssue
            rowDao.insertRow(null).blockingAwait();
            fail();
        } catch (NullPointerException ignored) {
        }
    }

    @Test
    public void insertRowIgnoresDoubles() throws InterruptedException {
        for (RowType row : RowType.values()) {
            rowDao.insertRow(new RowEntity(row))
                    .andThen(rowDao.updateWeather(row))
                    .andThen(rowDao.updateBuffState(row, RowBuffState.MARDROEME))
                    .andThen(rowDao.insertRow(new RowEntity(row)))
                    .blockingAwait();
            rowDao.isWeather(row).test().await().assertValue(true);
            rowDao.getBuffState(row).test().await().assertValue(RowBuffState.MARDROEME);
        }
    }

    @Test
    public void clearRowsClearsRows() throws InterruptedException {
        for (RowType row : RowType.values()) {
            rowDao.insertRow(new RowEntity(row))
                    .andThen(rowDao.clearRows())
                    .andThen(rowDao.isWeather(row))
                    .test()
                    .await()
                    .assertError(EmptyResultSetException.class);
        }
    }

    @Test
    public void updateWeatherUpdatesWeather() throws InterruptedException {
        for (RowType row : RowType.values()) {
            rowDao.insertRow(new RowEntity(row))
                    .andThen(rowDao.isWeather(row))
                    .test()
                    .await()
                    .assertValue(false);
            rowDao.updateWeather(row)
                    .andThen(rowDao.isWeather(row))
                    .test()
                    .await()
                    .assertValue(true);
            rowDao.updateWeather(row)
                    .andThen(rowDao.isWeather(row))
                    .test()
                    .await()
                    .assertValue(false);
        }
    }

    @Test
    public void updateWeatherAssertsNonNull() {
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
    public void clearWeatherClearsWeather() throws InterruptedException {
        for (RowType row : RowType.values()) {
            rowDao.insertRow(new RowEntity(row))
                    .andThen(rowDao.updateWeather(row))
                    .blockingAwait();
        }
        rowDao.clearWeather().blockingAwait();
        for (RowType row : RowType.values()) {
            rowDao.isWeather(row)
                    .test()
                    .await()
                    .assertValue(false);
        }
    }

    @Test
    public void clearMardroemePreservesHornAndWeather() {
        for (RowType target : RowType.values()) {
            rowDao.clearRows().blockingAwait();
            for (RowType row : RowType.values()) {
                rowDao.insertRow(new RowEntity(row))
                        .andThen(rowDao.updateBuffState(row, row == target ? RowBuffState.MARDROEME : RowBuffState.HORN))
                        .andThen(rowDao.updateWeather(row)).blockingAwait();
            }
            rowDao.clearMardroeme().blockingAwait();
            for (RowType row : RowType.values()) {
                assertThat(rowDao.getBuffState(row).blockingGet())
                        .isEqualTo(row == target ? RowBuffState.NONE : RowBuffState.HORN);
                assertThat(rowDao.isWeather(row).blockingGet()).isTrue();
            }
        }
    }

    @Test
    public void updateBuffStateStoresAllStates() throws InterruptedException {
        for (RowType row : RowType.values()) {
            rowDao.insertRow(new RowEntity(row))
                    .andThen(rowDao.getBuffState(row))
                    .test()
                    .await()
                    .assertValue(RowBuffState.NONE);
            for (RowBuffState state : new RowBuffState[]{RowBuffState.HORN, RowBuffState.MARDROEME, RowBuffState.NONE}) {
                rowDao.updateBuffState(row, state)
                        .andThen(rowDao.getBuffState(row))
                        .test()
                        .await()
                        .assertValue(state);
            }
        }
    }

    @Test
    public void updateBuffStateAssertsNonNullRow() {
        try {
            //noinspection DataFlowIssue
            rowDao.insertRow(new RowEntity(RowType.MELEE))
                    .andThen(rowDao.updateBuffState(null, RowBuffState.HORN))
                    .blockingAwait();
            fail();
        } catch (NullPointerException ignored) {
        }
    }

    @Test
    @SuppressWarnings("DataFlowIssue")
    public void updateBuffStateAssertsNonNullState() {
        rowDao.insertRow(new RowEntity(RowType.MELEE)).blockingAwait();
        assertThrows(NullPointerException.class,
                () -> rowDao.updateBuffState(RowType.MELEE, null).blockingAwait());
    }

    @Test
    public void isWeatherAssertsNonNull() {
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
    public void getBuffStateAssertsNonNull() {
        try {
            //noinspection DataFlowIssue
            rowDao.insertRow(new RowEntity(RowType.MELEE))
                    .andThen(rowDao.getBuffState(null))
                    .test()
                    .dispose();
            fail();
        } catch (NullPointerException ignored) {
        }
    }

    @Test
    public void isWeatherFlowableAssertsNonNull() {
        try {
            //noinspection DataFlowIssue
            rowDao.isWeatherFlowable(null)
                    .test()
                    .cancel();
            fail();
        } catch (NullPointerException ignored) {
        }
    }

    @Test
    public void isWeatherFlowableHasValueFromBeginning() {
        assertThat(rowDao.insertRow(new RowEntity(RowType.MELEE))
                .andThen(rowDao.isWeatherFlowable(RowType.MELEE))
                .blockingFirst()).isFalse();
    }

    @Test
    public void getBuffStateFlowableAssertsNonNull() {
        try {
            //noinspection DataFlowIssue
            rowDao.getBuffStateFlowable(null)
                    .test()
                    .cancel();
            fail();
        } catch (NullPointerException ignored) {
        }
    }

    @Test
    public void getBuffStateFlowableHasValueFromBeginning() {
        assertThat(rowDao.insertRow(new RowEntity(RowType.MELEE))
                .andThen(rowDao.getBuffStateFlowable(RowType.MELEE))
                .blockingFirst()).isEqualTo(RowBuffState.NONE);
    }

    @Test
    public void getBuffStateFlowableReturnsAllStates() {
        for (RowType row : RowType.values()) {
            rowDao.insertRow(new RowEntity(row)).blockingAwait();
            TestSubscriber<RowBuffState> observer = rowDao.getBuffStateFlowable(row).test();
            try {
                observer.awaitCount(1).assertValues(RowBuffState.NONE);
                RowBuffState[] states = {RowBuffState.HORN, RowBuffState.MARDROEME, RowBuffState.NONE};
                for (int i = 0; i < states.length; i++) {
                    rowDao.updateBuffState(row, states[i]).blockingAwait();
                    observer.awaitCount(i + 2);
                }
                observer.assertNoErrors().assertValues(RowBuffState.NONE, RowBuffState.HORN,
                        RowBuffState.MARDROEME, RowBuffState.NONE);
            } finally {
                observer.cancel();
            }
        }
    }
}
