package com.peternaggschga.gwent.ui.dialogs.addcard;

import static com.google.common.truth.Truth.assertThat;
import static com.peternaggschga.gwent.ui.main.FactionSwitchListener.THEME_NORTHERN_KINGDOMS;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.NumberPicker;

import com.peternaggschga.gwent.GwentApplication;
import com.peternaggschga.gwent.R;
import com.peternaggschga.gwent.data.Ability;
import com.peternaggschga.gwent.data.RowType;
import com.peternaggschga.gwent.data.UnitRepository;
import com.peternaggschga.gwent.ui.dialogs.addcard.filter.AbilityFilter;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;

import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.TestScheduler;

public class ReactiveCardNumberPickerAdapterUnitTest {
    private final Context context = mock(Context.class);
    private final ViewGroup group = mock(ViewGroup.class);
    private final SquadManager squads = mock(SquadManager.class);
    private final TestScheduler scheduler = new TestScheduler();
    private Picker hero;
    private Picker damage;
    private Picker ability;
    private Picker squad;
    private Picker count;
    private CardNumberPickerAdapter adapter;

    @Before
    public void setUp() {
        when(context.getString(anyInt())).thenAnswer(call -> String.valueOf((int) call.getArgument(0)));
        hero = new Picker(R.id.popup_add_card_epic_picker);
        damage = new Picker(R.id.popup_add_card_dmg_picker);
        ability = new Picker(R.id.popup_add_card_ability_picker);
        squad = new Picker(R.id.popup_add_card_binding_picker);
        count = new Picker(R.id.popup_add_card_number_picker);
        when(squads.hasNonEmptySquad()).thenReturn(true);
        when(squads.getFirstSquadWithMembers(anyInt())).thenReturn(1);
    }

    @After
    public void tearDown() {
        if (adapter != null) {
            adapter.dispose();
        }
    }

    private void create(RowType row) {
        create(row, squads);
    }

    private void create(RowType row, SquadManager squadManager) {
        adapter = new CardNumberPickerAdapter(group, squadManager,
                AbilityFilter.create(THEME_NORTHERN_KINGDOMS, row), scheduler);
    }

    private void chooseBinding() {
        ability.selectLabel(String.valueOf(R.string.add_picker_ability_binding));
    }

    @Test
    public void initializesFilteredNormalOptionsAndHidesSquads() {
        create(RowType.SIEGE);
        assertThat(hero.labels).asList().containsExactly(String.valueOf(R.string.add_picker_epic_normal));
        assertThat(ability.labels).asList().containsExactly(
                String.valueOf(R.string.add_picker_ability_moralBoost),
                String.valueOf(R.string.add_picker_ability_default),
                String.valueOf(R.string.add_picker_ability_binding)).inOrder();
        assertThat(ability.value).isEqualTo(1);
        assertThat(squad.visibility).isEqualTo(View.GONE);
        assertThat(count.value).isEqualTo(1);
    }

    @Test
    public void heroSelectionImmediatelyFiltersDamageAndResetsBinding() {
        create(RowType.MELEE);
        chooseBinding();
        hero.selectIndex(1);
        assertThat(damage.labels).asList().containsExactly("0", "7", "10", "15").inOrder();
        assertThat(damage.labels[damage.value]).isEqualTo("10");
        assertThat(ability.labels).asList().containsExactly(String.valueOf(R.string.add_picker_ability_default));
        assertThat(ability.value).isEqualTo(0);
        scheduler.advanceTimeBy(1, TimeUnit.SECONDS);
        assertThat(squad.visibility).isEqualTo(View.GONE);
        verify(squads, never()).showSquadInfo(anyInt(), any());
    }

    @Test
    public void preservesValidDamageAndAbilityWhenHeroStatusChanges() {
        create(RowType.MELEE);
        damage.selectIndex(7);
        hero.selectIndex(1);
        assertThat(damage.labels[damage.value]).isEqualTo("7");
        hero.selectIndex(0);
        assertThat(damage.value).isEqualTo(7);
        assertThat(ability.labels[ability.value]).isEqualTo(String.valueOf(R.string.add_picker_ability_default));
    }

    @Test
    public void bindingVisibilityWaitsForFullDebounce() {
        when(squads.getFirstSquadWithMembers(anyInt())).thenReturn(2);
        create(RowType.MELEE);
        chooseBinding();
        assertThat(squad.visibility).isEqualTo(View.GONE);
        scheduler.advanceTimeBy(499, TimeUnit.MILLISECONDS);
        assertThat(squad.visibility).isEqualTo(View.GONE);
        scheduler.advanceTimeBy(1, TimeUnit.MILLISECONDS);
        assertThat(squad.visibility).isEqualTo(View.VISIBLE);
        verify(squads).setSquadDamage(eq(2), any());
        verify(squads).showSquadInfo(2, context);
    }

    @Test
    public void rowWithoutSquadsKeepsPickerHiddenAndStartsSquadOne() {
        SquadManager emptySquads = spy(new SquadManager(Collections.emptyList()));
        create(RowType.MELEE, emptySquads);
        damage.selectIndex(7);
        count.view.setValue(3);
        chooseBinding();
        assertThat(squad.visibility).isEqualTo(View.GONE);
        assertSaved(false, 7, Ability.BINDING, 1, RowType.MELEE, 3);
        scheduler.advanceTimeBy(1, TimeUnit.SECONDS);
        assertThat(squad.visibility).isEqualTo(View.GONE);
        assertSaved(false, 7, Ability.BINDING, 1, RowType.MELEE, 3);
        verify(emptySquads, never()).setSquadDamage(anyInt(), any());
        verify(emptySquads, never()).showSquadInfo(anyInt(), any());
    }

    @Test
    public void scrollingPastBindingDoesNotShowSquadsOrToast() {
        create(RowType.MELEE);
        chooseBinding();
        scheduler.advanceTimeBy(300, TimeUnit.MILLISECONDS);
        ability.selectIndex(0);
        scheduler.advanceTimeBy(1, TimeUnit.SECONDS);
        assertThat(squad.visibility).isEqualTo(View.GONE);
        verify(squads, never()).showSquadInfo(anyInt(), any());
    }

    @Test
    public void returningToBindingRestartsDebounce() {
        create(RowType.MELEE);
        chooseBinding();
        scheduler.advanceTimeBy(300, TimeUnit.MILLISECONDS);
        ability.selectIndex(0);
        chooseBinding();
        scheduler.advanceTimeBy(499, TimeUnit.MILLISECONDS);
        assertThat(squad.visibility).isEqualTo(View.GONE);
        scheduler.advanceTimeBy(1, TimeUnit.MILLISECONDS);
        assertThat(squad.visibility).isEqualTo(View.VISIBLE);
    }

    @Test
    public void hiddenSingleSquadSavesSquadOneBeforeDebounce() {
        create(RowType.RANGE);
        chooseBinding();
        assertThat(squad.visibility).isEqualTo(View.GONE);
        verify(squads).setSquadDamage(eq(1), any());
        verify(squads, never()).showSquadInfo(anyInt(), any());
        assertSaved(false, 5, Ability.BINDING, 1, RowType.RANGE);
        scheduler.advanceTimeBy(1, TimeUnit.SECONDS);
        assertThat(squad.visibility).isEqualTo(View.GONE);
        verify(squads).showSquadInfo(1, context);
    }

    @Test
    public void saveUsesCurrentSquadEvenBeforeVisibilityDebounce() {
        create(RowType.MELEE);
        chooseBinding();
        squad.selectIndex(1);
        assertThat(squad.visibility).isEqualTo(View.GONE);
        assertSaved(false, 5, Ability.BINDING, 2, RowType.MELEE);
    }

    @Test
    public void immediateHeroSaveUsesFilteredValuesAndNoSquad() {
        create(RowType.MELEE);
        chooseBinding();
        scheduler.advanceTimeBy(500, TimeUnit.MILLISECONDS);
        hero.selectIndex(1);
        assertSaved(true, 10, Ability.NONE, null, RowType.MELEE);
    }

    @Test
    public void disposalCancelsPendingVisibilityAndToast() {
        create(RowType.MELEE);
        chooseBinding();
        adapter.dispose();
        scheduler.advanceTimeBy(1, TimeUnit.SECONDS);
        assertThat(adapter.isDisposed()).isTrue();
        assertThat(squad.visibility).isEqualTo(View.GONE);
        verify(squads, never()).showSquadInfo(anyInt(), any());
    }

    private void assertSaved(boolean epic, int selectedDamage, Ability selectedAbility,
                             Integer selectedSquad, RowType row) {
        assertSaved(epic, selectedDamage, selectedAbility, selectedSquad, row, 1);
    }

    private void assertSaved(boolean epic, int selectedDamage, Ability selectedAbility,
                             Integer selectedSquad, RowType row, int selectedCount) {
        UnitRepository repository = mock(UnitRepository.class);
        when(repository.insertUnit(epic, selectedDamage, selectedAbility, selectedSquad, row, selectedCount))
                .thenReturn(Completable.complete());
        try (MockedStatic<GwentApplication> application = mockStatic(GwentApplication.class)) {
            application.when(() -> GwentApplication.getRepository(context)).thenReturn(Single.just(repository));
            adapter.addSelectedUnits(row).test().assertResult(epic);
        }
        verify(repository).insertUnit(epic, selectedDamage, selectedAbility, selectedSquad, row, selectedCount);
    }

    private class Picker {
        private final NumberPicker view = mock(NumberPicker.class);
        private NumberPicker.OnValueChangeListener listener;
        private int value;
        private int min;
        private int max;
        private int visibility;
        private String[] labels;

        private Picker(int id) {
            when(group.findViewById(id)).thenReturn(view);
            when(view.getContext()).thenReturn(context);
            when(view.getValue()).thenAnswer(call -> value);
            doAnswer(call -> {
                min = call.getArgument(0);
                max = Math.max(min, max);
                value = Math.max(min, value);
                return null;
            }).when(view).setMinValue(anyInt());
            doAnswer(call -> {
                max = call.getArgument(0);
                value = Math.min(max, value);
                return null;
            }).when(view).setMaxValue(anyInt());
            doAnswer(call -> {
                value = Math.max(min, Math.min(max, call.getArgument(0)));
                return null;
            }).when(view).setValue(anyInt());
            doAnswer(call -> {
                listener = call.getArgument(0);
                return null;
            }).when(view).setOnValueChangedListener(any());
            doAnswer(call -> {
                labels = call.getArgument(0);
                return null;
            }).when(view).setDisplayedValues(any());
            doAnswer(call -> {
                visibility = call.getArgument(0);
                return null;
            }).when(view).setVisibility(anyInt());
        }

        private void selectIndex(int index) {
            int previous = value;
            view.setValue(index);
            listener.onValueChange(view, previous, value);
        }

        private void selectLabel(String label) {
            int index = Arrays.asList(labels).indexOf(label);
            assertThat(index).isAtLeast(0);
            selectIndex(index);
        }
    }
}
