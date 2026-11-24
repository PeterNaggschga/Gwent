package com.peternaggschga.gwent.domain.cases;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.peternaggschga.gwent.data.Ability;
import com.peternaggschga.gwent.data.RowType;
import com.peternaggschga.gwent.data.UnitEntity;
import com.peternaggschga.gwent.data.UnitRepository;
import com.peternaggschga.gwent.ui.sounds.SoundManager;

import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.observers.TestObserver;
import io.reactivex.rxjava3.subjects.CompletableSubject;

public class AvengerSummoningUnitTest {
    private UnitRepository repository;
    private SoundManager sounds;

    @Before
    public void initMocks() {
        repository = mock(UnitRepository.class);
        sounds = mock(SoundManager.class);
    }

    private UnitEntity avenger(RowType row) {
        UnitEntity unit = mock(UnitEntity.class);
        when(unit.getRow()).thenReturn(row);
        return unit;
    }

    @Test
    public void meleeSummonsStrengthElevenHeroesInMelee() {
        when(repository.insertUnit(true, 11, Ability.NONE, null, RowType.MELEE, 2))
                .thenReturn(Completable.complete());

        RevengeAlertDialogBuilderAdapter.insertAvengers(repository,
                Collections.nCopies(2, avenger(RowType.MELEE)), sounds).test().assertComplete();

        verify(repository).insertUnit(true, 11, Ability.NONE, null, RowType.MELEE, 2);
        verify(sounds).playCardAddSound(RowType.MELEE, true);
        verifyNoMoreInteractions(repository, sounds);
    }

    @Test
    public void rangedAndSiegeKeepNormalStrengthEightMeleeAvengers() {
        when(repository.insertUnit(false, 8, Ability.NONE, null, RowType.MELEE, 2))
                .thenReturn(Completable.complete());

        RevengeAlertDialogBuilderAdapter.insertAvengers(repository,
                Arrays.asList(avenger(RowType.RANGE), avenger(RowType.SIEGE)), sounds).test().assertComplete();

        verify(repository).insertUnit(false, 8, Ability.NONE, null, RowType.MELEE, 2);
        verify(sounds).playCardAddSound(RowType.MELEE, false);
        verifyNoMoreInteractions(repository, sounds);
    }

    @Test
    public void mixedRowsSummonBothTypesAndWaitForEachInsertion() {
        CompletableSubject normalInsertion = CompletableSubject.create();
        CompletableSubject heroInsertion = CompletableSubject.create();
        when(repository.insertUnit(false, 8, Ability.NONE, null, RowType.MELEE, 1)).thenReturn(normalInsertion);
        when(repository.insertUnit(true, 11, Ability.NONE, null, RowType.MELEE, 2)).thenReturn(heroInsertion);

        TestObserver<Void> observer = RevengeAlertDialogBuilderAdapter.insertAvengers(repository,
                Arrays.asList(avenger(RowType.MELEE), avenger(RowType.RANGE), avenger(RowType.MELEE)), sounds).test();
        observer.assertNotComplete();
        verifyNoInteractions(sounds);
        normalInsertion.onComplete();
        observer.assertNotComplete();
        verifyNoInteractions(sounds);
        heroInsertion.onComplete();
        observer.assertComplete();
        verify(repository).insertUnit(true, 11, Ability.NONE, null, RowType.MELEE, 2);
        verify(sounds).playCardAddSound(RowType.MELEE, true);
        verifyNoMoreInteractions(sounds);
    }

    @Test
    public void emptyCollectionDoesNotInsertOrPlaySounds() {
        RevengeAlertDialogBuilderAdapter.insertAvengers(repository, Collections.emptyList(), sounds)
                .test().assertComplete();
        verifyNoInteractions(repository, sounds);
    }

    @Test
    public void failedHeroInsertionInMixedBatchDoesNotPlaySounds() {
        when(repository.insertUnit(false, 8, Ability.NONE, null, RowType.MELEE, 1))
                .thenReturn(Completable.complete());
        when(repository.insertUnit(true, 11, Ability.NONE, null, RowType.MELEE, 1))
                .thenReturn(Completable.error(new IllegalStateException("Insert failed")));

        RevengeAlertDialogBuilderAdapter.insertAvengers(repository,
                        Arrays.asList(avenger(RowType.RANGE), avenger(RowType.MELEE)), sounds)
                .test().assertError(IllegalStateException.class);

        verifyNoInteractions(sounds);
    }

    @Test
    public void failedInsertionDoesNotPlaySounds() {
        when(repository.insertUnit(true, 11, Ability.NONE, null, RowType.MELEE, 1))
                .thenReturn(Completable.error(new IllegalStateException("Insert failed")));
        RevengeAlertDialogBuilderAdapter.insertAvengers(repository,
                        Collections.singletonList(avenger(RowType.MELEE)), sounds)
                .test().assertError(IllegalStateException.class);
        verifyNoInteractions(sounds);
    }
}
