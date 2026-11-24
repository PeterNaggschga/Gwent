package com.peternaggschga.gwent.domain.cases;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.content.Context;
import android.content.DialogInterface;

import androidx.appcompat.app.AlertDialog;

import com.peternaggschga.gwent.data.Ability;
import com.peternaggschga.gwent.data.RowType;
import com.peternaggschga.gwent.data.UnitEntity;
import com.peternaggschga.gwent.data.UnitRepository;
import com.peternaggschga.gwent.ui.sounds.SoundManager;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedConstruction;

import java.util.Arrays;
import java.util.List;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;

public class AvengerRemovalUnitTest {
    private UnitRepository repository;
    private SoundManager sounds;
    private Context context;
    private MockedConstruction<RevengeAlertDialogBuilderAdapter> dialogs;

    @Before
    public void initMocks() {
        repository = mock(UnitRepository.class);
        sounds = mock(SoundManager.class);
        context = mock(Context.class);
        dialogs = mockConstruction(RevengeAlertDialogBuilderAdapter.class, (builder, construction) -> {
            AlertDialog dialog = mock(AlertDialog.class);
            when(builder.create()).thenReturn(dialog);
            when(builder.setNegativeCallback(any())).thenReturn(builder);
            when(builder.setPositiveCallback(any())).thenAnswer(invocation -> {
                DialogInterface.OnClickListener callback = invocation.getArgument(0);
                doAnswer(ignored -> {
                    callback.onClick(dialog, DialogInterface.BUTTON_POSITIVE);
                    return null;
                }).when(dialog).show();
                return builder;
            });
        });
    }

    @After
    public void closeMocks() {
        dialogs.close();
    }

    private UnitEntity unit(RowType row, Ability ability, boolean epic) {
        UnitEntity unit = mock(UnitEntity.class);
        when(unit.getRow()).thenReturn(row);
        when(unit.getAbility()).thenReturn(ability);
        when(unit.isEpic()).thenReturn(epic);
        return unit;
    }

    @Test
    public void removalPreservesSourceRowsWhenSummoningMixedAvengers() {
        List<UnitEntity> units = Arrays.asList(unit(RowType.MELEE, Ability.REVENGE, false),
                unit(RowType.RANGE, Ability.REVENGE, false), unit(RowType.MELEE, Ability.NONE, false));
        when(repository.delete(units)).thenReturn(Completable.complete());
        when(repository.insertUnit(false, 8, Ability.NONE, null, RowType.MELEE, 1)).thenReturn(Completable.complete());
        when(repository.insertUnit(true, 11, Ability.NONE, null, RowType.MELEE, 1)).thenReturn(Completable.complete());

        RemoveUnitsUseCase.remove(context, repository, units, sounds).test().assertComplete();

        verify(repository).delete(units);
        verify(repository).insertUnit(false, 8, Ability.NONE, null, RowType.MELEE, 1);
        verify(repository).insertUnit(true, 11, Ability.NONE, null, RowType.MELEE, 1);
    }

    @Test
    public void resetDoesNotSummonAnAvengerForTheKeptUnit() {
        UnitEntity kept = unit(RowType.MELEE, Ability.REVENGE, false);
        UnitEntity removed = unit(RowType.RANGE, Ability.REVENGE, true);
        when(repository.getUnits()).thenReturn(Single.just(Arrays.asList(kept, removed)));
        when(repository.reset(kept)).thenReturn(Completable.complete());
        when(repository.insertUnit(false, 8, Ability.NONE, null, RowType.MELEE, 1)).thenReturn(Completable.complete());

        ResetRepositoryUseCase.reset(context, repository, true, sounds).test().assertResult(kept);

        verify(repository).reset(kept);
        verify(repository).insertUnit(false, 8, Ability.NONE, null, RowType.MELEE, 1);
        verify(repository, never())
                .insertUnit(true, 11, Ability.NONE, null, RowType.MELEE, 1);
    }
}
