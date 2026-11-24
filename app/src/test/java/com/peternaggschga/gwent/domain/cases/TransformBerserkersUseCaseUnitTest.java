package com.peternaggschga.gwent.domain.cases;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import android.content.Context;
import android.content.res.Resources;
import android.widget.Toast;

import com.peternaggschga.gwent.R;
import com.peternaggschga.gwent.data.Ability;
import com.peternaggschga.gwent.data.RowBuffState;
import com.peternaggschga.gwent.data.RowType;
import com.peternaggschga.gwent.data.UnitEntity;
import com.peternaggschga.gwent.data.UnitRepository;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import io.reactivex.rxjava3.android.plugins.RxAndroidPlugins;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.observers.TestObserver;
import io.reactivex.rxjava3.schedulers.Schedulers;
import io.reactivex.rxjava3.subjects.CompletableSubject;

public class TransformBerserkersUseCaseUnitTest {
    private Resources resources;
    private UnitRepository repository;
    private Context context;
    private Toast notification;
    private MockedStatic<Toast> toasts;

    @Before
    public void initMocks() {
        RxAndroidPlugins.setInitMainThreadSchedulerHandler(ignored -> Schedulers.trampoline());
        RxAndroidPlugins.setMainThreadSchedulerHandler(ignored -> Schedulers.trampoline());
        resources = mock(Resources.class);
        repository = mock(UnitRepository.class);
        context = mock(Context.class);
        notification = mock(Toast.class);
        when(context.getResources()).thenReturn(resources);
        when(resources.getQuantityString(eq(R.plurals.berserkers_transformed), anyInt(), anyInt()))
                .thenReturn("Berserkers transformed");
        toasts = mockStatic(Toast.class);
        toasts.when(() -> Toast.makeText(eq(context), anyString(), eq(Toast.LENGTH_SHORT))).thenReturn(notification);
        when(repository.updateUnits(anyList())).thenReturn(Completable.complete());
    }

    @After
    public void closeMocks() {
        toasts.close();
        RxAndroidPlugins.reset();
    }

    private UnitEntity unit(RowType row, Ability ability) {
        UnitEntity unit = mock(UnitEntity.class);
        when(unit.getRow()).thenReturn(row);
        when(unit.getAbility()).thenReturn(ability);
        return unit;
    }

    @Test
    public void rowTransformationBatchesUpdatesWithoutCheckingMardroeme() {
        UnitEntity first = unit(RowType.MELEE, Ability.BERSERKER);
        UnitEntity second = unit(RowType.MELEE, Ability.BERSERKER);
        UnitEntity ordinary = unit(RowType.MELEE, Ability.NONE);
        UnitEntity transformedFirst = unit(RowType.MELEE, Ability.MORAL_BOOST);
        UnitEntity transformedSecond = unit(RowType.MELEE, Ability.MORAL_BOOST);
        when(first.withStats(14, Ability.MORAL_BOOST, null)).thenReturn(transformedFirst);
        when(second.withStats(14, Ability.MORAL_BOOST, null)).thenReturn(transformedSecond);
        when(repository.getUnits(RowType.MELEE)).thenReturn(Single.just(Arrays.asList(first, second, ordinary)));

        TransformBerserkersUseCase.transformRow(context, repository, RowType.MELEE).test().assertComplete();

        verify(repository).updateUnits(Arrays.asList(transformedFirst, transformedSecond));
        verify(repository, never()).getBuffState(RowType.MELEE);
        verify(repository, never()).insertUnits(anyList());
        verify(resources).getQuantityString(R.plurals.berserkers_transformed, 2, 2);
        verify(notification).show();
    }

    @Test
    public void retrievesAddedUnitsByIdAndChecksOnlyBerserkerRows() {
        List<Long> ids = Arrays.asList(1L, 2L, 3L);
        UnitEntity melee = unit(RowType.MELEE, Ability.BERSERKER);
        UnitEntity range = unit(RowType.RANGE, Ability.BERSERKER);
        UnitEntity ordinary = unit(RowType.SIEGE, Ability.NONE);
        UnitEntity transformed = unit(RowType.MELEE, Ability.MORAL_BOOST);
        when(repository.getUnitsByIds(ids)).thenReturn(Single.just(Arrays.asList(melee, range, ordinary)));
        when(melee.withStats(14, Ability.MORAL_BOOST, null)).thenReturn(transformed);
        when(repository.getBuffState(RowType.MELEE)).thenReturn(Single.just(RowBuffState.MARDROEME));
        when(repository.getBuffState(RowType.RANGE)).thenReturn(Single.just(RowBuffState.HORN));
        when(repository.getUnits(RowType.RANGE)).thenReturn(Single.just(Collections.singletonList(range)));

        TransformBerserkersUseCase.transformAddedUnits(context, repository, ids).test().assertComplete();

        verify(repository).getUnitsByIds(ids);
        verify(repository).getBuffState(RowType.MELEE);
        verify(repository).getBuffState(RowType.RANGE);
        verify(repository, never()).getBuffState(RowType.SIEGE);
        verify(repository).updateUnits(Collections.singletonList(transformed));
        verify(repository, never()).insertUnits(anyList());
        verify(resources).getQuantityString(R.plurals.berserkers_transformed, 1, 1);
        verify(notification).show();
    }

    @Test
    public void addedMardroemeTransformsExistingAndAddedBerserkersInOneBatch() {
        List<Long> ids = Arrays.asList(1L, 2L, 3L);
        UnitEntity mardroeme = unit(RowType.MELEE, Ability.MARDROEME);
        UnitEntity secondMardroeme = unit(RowType.MELEE, Ability.MARDROEME);
        UnitEntity addedBerserker = unit(RowType.MELEE, Ability.BERSERKER);
        UnitEntity existingBerserker = unit(RowType.MELEE, Ability.BERSERKER);
        UnitEntity firstTransformed = unit(RowType.MELEE, Ability.MORAL_BOOST);
        UnitEntity secondTransformed = unit(RowType.MELEE, Ability.MORAL_BOOST);
        when(repository.getUnitsByIds(ids)).thenReturn(Single.just(Arrays.asList(mardroeme, secondMardroeme, addedBerserker)));
        when(repository.getUnits(RowType.MELEE)).thenReturn(Single.just(
                Arrays.asList(existingBerserker, mardroeme, secondMardroeme, addedBerserker)));
        when(existingBerserker.withStats(14, Ability.MORAL_BOOST, null)).thenReturn(firstTransformed);
        when(addedBerserker.withStats(14, Ability.MORAL_BOOST, null)).thenReturn(secondTransformed);

        TransformBerserkersUseCase.transformAddedUnits(context, repository, ids).test().assertComplete();

        verify(repository).getUnits(RowType.MELEE);
        verify(repository, never()).getUnits(RowType.RANGE);
        verify(repository, never()).getBuffState(RowType.MELEE);
        verify(repository).updateUnits(Arrays.asList(firstTransformed, secondTransformed));
        verify(resources).getQuantityString(R.plurals.berserkers_transformed, 2, 2);
        verify(notification).show();
    }

    @Test
    public void existingMardroemeUnitTransformsAddedBerserkersEvenWithHorn() {
        UnitEntity berserker = unit(RowType.RANGE, Ability.BERSERKER);
        UnitEntity mardroeme = unit(RowType.RANGE, Ability.MARDROEME);
        UnitEntity transformed = unit(RowType.RANGE, Ability.BINDING);
        when(repository.getUnitsByIds(Collections.singletonList(1L)))
                .thenReturn(Single.just(Collections.singletonList(berserker)));
        when(repository.getBuffState(RowType.RANGE)).thenReturn(Single.just(RowBuffState.HORN));
        when(repository.getUnits(RowType.RANGE)).thenReturn(Single.just(Arrays.asList(berserker, mardroeme)));
        when(berserker.withStats(8, Ability.BINDING, UnitEntity.BERSERKER_SQUAD)).thenReturn(transformed);

        TransformBerserkersUseCase.transformAddedUnits(context, repository, Collections.singletonList(1L))
                .test().assertComplete();

        verify(repository).updateUnits(Collections.singletonList(transformed));
        verify(notification).show();
    }

    @Test
    public void addedMardroemeWithoutBerserkersDoesNotUpdateOrNotify() {
        UnitEntity mardroeme = unit(RowType.MELEE, Ability.MARDROEME);
        when(repository.getUnitsByIds(Collections.singletonList(1L)))
                .thenReturn(Single.just(Collections.singletonList(mardroeme)));
        when(repository.getUnits(RowType.MELEE)).thenReturn(Single.just(Collections.singletonList(mardroeme)));

        TransformBerserkersUseCase.transformAddedUnits(context, repository, Collections.singletonList(1L))
                .test().assertComplete();

        verify(repository, never()).updateUnits(anyList());
        verifyNoInteractions(notification);
    }

    @Test
    public void transformationsAcrossRowsUseOneUpdateAndOneNotification() {
        List<Long> ids = Arrays.asList(1L, 2L, 3L);
        UnitEntity first = unit(RowType.RANGE, Ability.BERSERKER);
        UnitEntity second = unit(RowType.RANGE, Ability.BERSERKER);
        UnitEntity melee = unit(RowType.MELEE, Ability.BERSERKER);
        UnitEntity transformedFirst = unit(RowType.RANGE, Ability.BINDING);
        UnitEntity transformedSecond = unit(RowType.RANGE, Ability.BINDING);
        UnitEntity transformedMelee = unit(RowType.MELEE, Ability.MORAL_BOOST);
        when(repository.getUnitsByIds(ids)).thenReturn(Single.just(Arrays.asList(first, second, melee)));
        when(first.withStats(8, Ability.BINDING, UnitEntity.BERSERKER_SQUAD)).thenReturn(transformedFirst);
        when(second.withStats(8, Ability.BINDING, UnitEntity.BERSERKER_SQUAD)).thenReturn(transformedSecond);
        when(melee.withStats(14, Ability.MORAL_BOOST, null)).thenReturn(transformedMelee);
        when(repository.getBuffState(RowType.RANGE)).thenReturn(Single.just(RowBuffState.MARDROEME));
        when(repository.getBuffState(RowType.MELEE)).thenReturn(Single.just(RowBuffState.MARDROEME));

        TransformBerserkersUseCase.transformAddedUnits(context, repository, ids).test().assertComplete();

        verify(repository).getBuffState(RowType.RANGE);
        verify(repository).getBuffState(RowType.MELEE);
        verify(repository).updateUnits(argThat(batch -> batch.size() == 3 && batch.containsAll(
                Arrays.asList(transformedFirst, transformedSecond, transformedMelee))));
        verify(resources).getQuantityString(R.plurals.berserkers_transformed, 3, 3);
        verify(notification).show();
    }

    @Test
    public void notificationWaitsForSuccessfulUpdate() {
        UnitEntity berserker = unit(RowType.MELEE, Ability.BERSERKER);
        UnitEntity transformed = unit(RowType.MELEE, Ability.MORAL_BOOST);
        when(berserker.withStats(14, Ability.MORAL_BOOST, null)).thenReturn(transformed);
        when(repository.getUnits(RowType.MELEE)).thenReturn(Single.just(Collections.singletonList(berserker)));
        CompletableSubject update = CompletableSubject.create();
        when(repository.updateUnits(anyList())).thenReturn(update);

        TestObserver<Void> observer = TransformBerserkersUseCase.transformRow(context, repository, RowType.MELEE).test();
        observer.assertNotComplete();
        verifyNoInteractions(notification);
        update.onComplete();
        observer.assertComplete();
        verify(notification).show();
    }

    @Test
    public void failedUpdateDoesNotNotify() {
        UnitEntity berserker = unit(RowType.MELEE, Ability.BERSERKER);
        UnitEntity transformed = unit(RowType.MELEE, Ability.MORAL_BOOST);
        when(berserker.withStats(14, Ability.MORAL_BOOST, null)).thenReturn(transformed);
        when(repository.getUnitsByIds(Collections.singletonList(1L)))
                .thenReturn(Single.just(Collections.singletonList(berserker)));
        when(repository.getBuffState(RowType.MELEE)).thenReturn(Single.just(RowBuffState.MARDROEME));
        when(repository.updateUnits(anyList())).thenReturn(Completable.error(new IllegalStateException("Update failed")));

        TransformBerserkersUseCase.transformAddedUnits(context, repository, Collections.singletonList(1L))
                .test().assertError(IllegalStateException.class);
        verifyNoInteractions(notification);
    }

    @Test
    public void emptyIdsNeedNoRetrievalOrNotification() {
        TransformBerserkersUseCase.transformAddedUnits(context, repository, Collections.emptyList())
                .test().assertComplete();
        verifyNoInteractions(repository, notification);
    }

    @Test
    public void noBerserkersMeansNoBuffQueriesOrNotification() {
        UnitEntity ordinary = unit(RowType.MELEE, Ability.NONE);
        when(repository.getUnitsByIds(Collections.singletonList(1L)))
                .thenReturn(Single.just(Collections.singletonList(ordinary)));
        TransformBerserkersUseCase.transformAddedUnits(context, repository, Collections.singletonList(1L))
                .test().assertComplete();
        verify(repository, never()).getBuffState(RowType.MELEE);
        verify(repository, never()).updateUnits(anyList());
        verify(repository, never()).insertUnits(anyList());
        verifyNoInteractions(notification);
    }
}
