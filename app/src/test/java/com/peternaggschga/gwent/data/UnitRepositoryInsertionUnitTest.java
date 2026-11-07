package com.peternaggschga.gwent.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import android.content.Context;
import android.content.res.Resources;
import android.widget.Toast;

import com.peternaggschga.gwent.R;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import io.reactivex.rxjava3.android.plugins.RxAndroidPlugins;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.observers.TestObserver;
import io.reactivex.rxjava3.plugins.RxJavaPlugins;
import io.reactivex.rxjava3.schedulers.Schedulers;
import io.reactivex.rxjava3.subjects.SingleSubject;

public class UnitRepositoryInsertionUnitTest {
    private AppDatabase database;
    private UnitDao units;
    private RowDao rows;
    private UnitRepository repository;

    @Before
    public void initRepository() {
        RxJavaPlugins.setIoSchedulerHandler(ignored -> Schedulers.trampoline());
        RxAndroidPlugins.setInitMainThreadSchedulerHandler(ignored -> Schedulers.trampoline());
        RxAndroidPlugins.setMainThreadSchedulerHandler(ignored -> Schedulers.trampoline());
        database = mock(AppDatabase.class);
        units = mock(UnitDao.class);
        rows = mock(RowDao.class);
        when(database.units()).thenReturn(units);
        when(database.rows()).thenReturn(rows);
        when(rows.insertRow(any())).thenReturn(Completable.complete());
        when(units.insertUnits(anyList())).thenAnswer(invocation -> {
            List<UnitEntity> batch = invocation.getArgument(0);
            return Single.just(IntStream.range(0, batch.size()).mapToObj(i -> (long) i + 1).collect(Collectors.toList()));
        });
        when(units.getUnitsByIds(anyList())).thenReturn(Single.just(Collections.emptyList()));
        Context context = mock(Context.class);
        when(context.getApplicationContext()).thenReturn(context);
        repository = UnitRepository.getRepository(database, context).blockingGet();
    }

    @After
    public void resetSchedulers() {
        RxJavaPlugins.reset();
        RxAndroidPlugins.reset();
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private List<UnitEntity> insertedBatch() {
        ArgumentCaptor<List<UnitEntity>> batch = ArgumentCaptor.forClass((Class) List.class);
        verify(units).insertUnits(batch.capture());
        return batch.getValue();
    }

    @Test
    public void bulkInsertionUsesOneDaoCallWithDistinctUnits() {
        repository.insertUnit(false, 8, Ability.BINDING, 2, RowType.RANGE, 5).test().assertComplete();
        List<UnitEntity> batch = insertedBatch();
        assertEquals(5, batch.size());
        for (int i = 0; i < batch.size(); i++) {
            UnitEntity unit = batch.get(i);
            assertEquals(0, unit.getId());
            assertEquals(8, unit.getDamage());
            assertEquals(Ability.BINDING, unit.getAbility());
            assertEquals(Integer.valueOf(2), unit.getSquad());
            assertEquals(RowType.RANGE, unit.getRow());
            if (i > 0) assertNotSame(batch.get(i - 1), unit);
        }
        verify(units).getUnitsByIds(Arrays.asList(1L, 2L, 3L, 4L, 5L));
        verifyNoMoreInteractions(units);
    }

    @Test
    public void singleInsertionUsesTheSameDaoWithSingletonList() {
        repository.insertUnit(false, 5, Ability.NONE, null, RowType.MELEE, 1).test().assertComplete();
        assertEquals(1, insertedBatch().size());
        verify(units).getUnitsByIds(Collections.singletonList(1L));
        verifyNoMoreInteractions(units);
    }

    @Test
    public void zeroUnitsUsesAnEmptyBatch() {
        repository.insertUnit(false, 5, Ability.NONE, null, RowType.MELEE, 0).test().assertComplete();
        assertEquals(0, insertedBatch().size());
    }

    @Test
    public void invalidStatsFailBeforeCallingDao() {
        assertThrows(IllegalArgumentException.class,
                () -> repository.insertUnit(false, -1, Ability.NONE, null, RowType.MELEE, 5));
        assertThrows(IllegalArgumentException.class,
                () -> repository.insertUnit(false, 21, Ability.NONE, null, RowType.MELEE, 5));
        assertThrows(IllegalArgumentException.class,
                () -> repository.insertUnit(true, 5, Ability.NONE, null, RowType.MELEE, 5));
        assertThrows(IllegalArgumentException.class,
                () -> repository.insertUnit(false, 5, Ability.BINDING, null, RowType.MELEE, 5));
        assertThrows(IllegalArgumentException.class,
                () -> repository.insertUnit(false, 5, Ability.BINDING, 0, RowType.MELEE, 5));
        assertThrows(IllegalArgumentException.class,
                () -> repository.insertUnit(false, 5, Ability.NONE, 1, RowType.MELEE, 5));
        verifyNoInteractions(units);
    }

    @Test
    public void copyingUsesSingletonBatchWithNewIdentity() {
        UnitEntity original = new UnitEntity(false, 5, Ability.NONE, null, RowType.MELEE);
        original.setId(12);
        when(units.getUnit(12)).thenReturn(Single.just(original));
        repository.copy(12).test().assertComplete();
        List<UnitEntity> batch = insertedBatch();
        assertEquals(1, batch.size());
        assertEquals(0, batch.get(0).getId());
        assertEquals(original.getDamage(), batch.get(0).getDamage());
        assertNotSame(original, batch.get(0));
    }

    @Test
    public void resetKeepsExistingUnitUsingSingletonBatch() {
        UnitEntity kept = new UnitEntity(false, 5, Ability.NONE, null, RowType.MELEE);
        kept.setId(12);
        when(rows.clearRows()).thenReturn(Completable.complete());
        repository.reset(kept).test().assertComplete();
        List<UnitEntity> batch = insertedBatch();
        assertEquals(1, batch.size());
        assertEquals(kept, batch.get(0));
    }

    @Test
    public void insertionWaitsForDaoAndPropagatesErrors() {
        SingleSubject<List<Long>> insertion = SingleSubject.create();
        when(units.insertUnits(anyList())).thenReturn(insertion);
        TestObserver<Void> observer =
                repository.insertUnit(false, 5, Ability.NONE, null, RowType.MELEE, 3).test();
        observer.assertNotComplete();
        insertion.onError(new IllegalStateException("Insert failed"));
        observer.assertError(IllegalStateException.class);
        verify(units, never()).getUnitsByIds(anyList());
    }

    @Test
    public void transformationNotificationsUseApplicationContextNotActivityContext() {
        Context activity = mock(Context.class);
        Context application = mock(Context.class);
        Resources resources = mock(Resources.class);
        Toast notification = mock(Toast.class);
        when(activity.getApplicationContext()).thenReturn(application);
        when(application.getResources()).thenReturn(resources);
        when(resources.getQuantityString(R.plurals.berserkers_transformed, 1, 1)).thenReturn("1 Berserker transformed.");
        UnitEntity berserker = new UnitEntity(false, 2, Ability.BERSERKER, null, RowType.MELEE);
        berserker.setId(1);
        when(units.getUnitsByIds(Collections.singletonList(1L))).thenReturn(Single.just(Collections.singletonList(berserker)));
        when(rows.getBuffState(RowType.MELEE)).thenReturn(Single.just(RowBuffState.MARDROEME));
        when(units.updateUnits(anyList())).thenReturn(Completable.complete());
        UnitRepository activityCreatedRepository = UnitRepository.getRepository(database, activity).blockingGet();
        try (MockedStatic<Toast> toasts = org.mockito.Mockito.mockStatic(Toast.class)) {
            toasts.when(() -> Toast.makeText(application, "1 Berserker transformed.", Toast.LENGTH_SHORT)).thenReturn(notification);
            activityCreatedRepository.insertUnit(false, 2, Ability.BERSERKER, null, RowType.MELEE, 1)
                    .test().assertComplete();
            toasts.verify(() -> Toast.makeText(application, "1 Berserker transformed.", Toast.LENGTH_SHORT));
            verify(notification).show();
            verify(activity, never()).getResources();
        }
    }

    @Test
    public void transformationStartsOnlyAfterInsertionAndReceivesOnlySuccessfulIds() {
        SingleSubject<List<Long>> insertion = SingleSubject.create();
        when(units.insertUnits(anyList())).thenReturn(insertion);
        TestObserver<Void> observer = repository.insertUnit(false, 5, Ability.NONE, null, RowType.MELEE, 4).test();
        observer.assertNotComplete();
        verify(units, never()).getUnitsByIds(anyList());
        insertion.onSuccess(Arrays.asList(-1L, 42L, -1L, 43L));
        observer.assertComplete();
        verify(units).getUnitsByIds(Arrays.asList(42L, 43L));
    }

    @Test
    public void ignoredInsertionsDoNotRetrieveOrTransformExistingUnits() {
        when(units.insertUnits(anyList())).thenReturn(Single.just(Collections.singletonList(-1L)));
        repository.insertUnit(false, 5, Ability.NONE, null, RowType.MELEE, 1).test().assertComplete();
        verify(units, never()).getUnitsByIds(anyList());
    }
}
