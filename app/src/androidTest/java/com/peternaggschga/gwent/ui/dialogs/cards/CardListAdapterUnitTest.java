package com.peternaggschga.gwent.ui.dialogs.cards;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.util.Consumer;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.peternaggschga.gwent.data.Ability;
import com.peternaggschga.gwent.data.UnitEntity;
import com.peternaggschga.gwent.domain.damage.DamageCalculator;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;

@RunWith(AndroidJUnit4.class)
public class CardListAdapterUnitTest {
    Consumer<Integer> mockOnCopy;
    Consumer<Integer> mockOnDelete;
    CardListAdapter testAdapter;

    TextView mockDamageView;
    ImageView mockAbilityView;
    TextView mockBindingView;
    CardListAdapter.CardViewHolder mockViewHolder;

    @Before
    public void initMocks() {
        //noinspection unchecked
        mockOnCopy = (Consumer<Integer>) mock(Consumer.class);
        //noinspection unchecked
        mockOnDelete = (Consumer<Integer>) mock(Consumer.class);
        testAdapter = new CardListAdapter(mockOnCopy, mockOnDelete);

        mockDamageView = mock(TextView.class);
        mockAbilityView = mock(ImageView.class);
        mockBindingView = mock(TextView.class);
        mockViewHolder = mock(CardListAdapter.CardViewHolder.class);
        when(mockViewHolder.getDamageView()).thenReturn(mockDamageView);
        when(mockViewHolder.getAbilityView()).thenReturn(mockAbilityView);
        when(mockViewHolder.getBindingView()).thenReturn(mockBindingView);
    }

    @Test
    public void constructorCallsSetHasStableIds() {
        assertThat(testAdapter.hasStableIds()).isTrue();
    }

    @NonNull
    private List<CardUiState> getRepresentativeCardUiStateList() {
        List<UnitEntity> units = new ArrayList<>(2 * Ability.values().length);

        int id = 0;
        for (boolean epic : new boolean[]{false, true}) {
            for (Ability ability : Ability.values()) {
                UnitEntity mockUnit = mock(UnitEntity.class);
                when(mockUnit.calculateDamage(any())).thenReturn(7);
                switch (ability) {
                    default:
                        when(mockUnit.isBuffed(any())).thenReturn(DamageCalculator.Color.DEFAULT);
                        break;
                    case REVENGE:
                        when(mockUnit.isBuffed(any())).thenReturn(epic ? DamageCalculator.Color.DEFAULT : DamageCalculator.Color.DEBUFFED);
                        break;
                    case BINDING:
                        when(mockUnit.isBuffed(any())).thenReturn(epic ? DamageCalculator.Color.DEFAULT : DamageCalculator.Color.BUFFED);
                }
                when(mockUnit.isBuffed(any())).thenReturn(DamageCalculator.Color.DEFAULT);
                when(mockUnit.isEpic()).thenReturn(epic);
                when(mockUnit.getAbility()).thenReturn(ability);
                when(mockUnit.getSquad()).thenReturn(ability == Ability.BINDING ? 1 : null);
                when(mockUnit.getId()).thenReturn(id++);
                units.add(mockUnit);
            }
        }

        return new CardUiStateFactory(ApplicationProvider.getApplicationContext(),
                true,
                false).createCardUiState(units);
    }

    @Test
    public void onBindViewHolderSetsDamageViewText() {
        List<CardUiState> testStates = getRepresentativeCardUiStateList();
        testAdapter.submitList(testStates);
        for (int i = 0; i < testStates.size(); i++) {
            reset(mockDamageView);
            testAdapter.onBindViewHolder(mockViewHolder, i);
            verify(mockDamageView).setText(testStates.get(i).getDamageString());
        }
    }

    @Test
    public void onBindViewHolderSetsDamageViewBackgroundResource() {
        List<CardUiState> testStates = getRepresentativeCardUiStateList();
        testAdapter.submitList(testStates);
        for (int i = 0; i < testStates.size(); i++) {
            reset(mockDamageView);
            testAdapter.onBindViewHolder(mockViewHolder, i);
            verify(mockDamageView).setBackgroundResource(testStates.get(i).getDamageBackgroundImageId());
        }
    }

    @Test
    public void onBindViewHolderSetsDamageViewTextColor() {
        List<CardUiState> testStates = getRepresentativeCardUiStateList();
        testAdapter.submitList(testStates);
        for (int i = 0; i < testStates.size(); i++) {
            reset(mockDamageView);
            testAdapter.onBindViewHolder(mockViewHolder, i);
            verify(mockDamageView).setTextColor(testStates.get(i).getDamageTextColor());
        }
    }

    @Test
    public void onBindViewHolderSetsAbilityViewImageRes() {
        List<CardUiState> testStates = getRepresentativeCardUiStateList();
        testAdapter.submitList(testStates);
        for (int i = 0; i < testStates.size(); i++) {
            reset(mockAbilityView);
            testAdapter.onBindViewHolder(mockViewHolder, i);
            if (testStates.get(i).showAbility()) {
                verify(mockAbilityView).setImageResource(testStates.get(i).getAbilityImageId());
            } else {
                verify(mockAbilityView, never()).setImageResource(anyInt());
            }
        }
    }

    @Test
    public void onBindViewHolderSetsAbilityViewVisibility() {
        List<CardUiState> testStates = getRepresentativeCardUiStateList();
        testAdapter.submitList(testStates);
        for (int i = 0; i < testStates.size(); i++) {
            reset(mockAbilityView);
            testAdapter.onBindViewHolder(mockViewHolder, i);
            verify(mockAbilityView).setVisibility(testStates.get(i).showAbility() ? View.VISIBLE : View.GONE);
        }
    }

    @Test
    public void onBindViewHolderSetsBindingViewText() {
        List<CardUiState> testStates = getRepresentativeCardUiStateList();
        testAdapter.submitList(testStates);
        for (int i = 0; i < testStates.size(); i++) {
            reset(mockBindingView);
            testAdapter.onBindViewHolder(mockViewHolder, i);
            if (testStates.get(i).showSquad()) {
                verify(mockBindingView).setText(testStates.get(i).getSquadString());
            } else {
                verify(mockBindingView, never()).setText(testStates.get(i).getSquadString());
            }
        }
    }

    @Test
    public void onBindViewHolderSetsBindingViewVisibility() {
        List<CardUiState> testStates = getRepresentativeCardUiStateList();
        testAdapter.submitList(testStates);
        for (int i = 0; i < testStates.size(); i++) {
            reset(mockBindingView);
            testAdapter.onBindViewHolder(mockViewHolder, i);
            verify(mockBindingView).setVisibility(testStates.get(i).showSquad() ? View.VISIBLE : View.GONE);
        }
    }

    @Test
    public void onBindViewHolderSetsUnitId() {
        List<CardUiState> testStates = getRepresentativeCardUiStateList();
        testAdapter.submitList(testStates);
        for (int i = 0; i < testStates.size(); i++) {
            testAdapter.onBindViewHolder(mockViewHolder, i);
            verify(mockViewHolder).setItemId(testStates.get(i).getUnitId());
        }
    }

    @Test
    public void getItemIdReturnsUnitId() {
        List<CardUiState> testStates = getRepresentativeCardUiStateList();
        testAdapter.submitList(testStates);
        for (int i = 0; i < testStates.size(); i++) {
            assertThat(testAdapter.getItemId(i)).isEqualTo(testStates.get(i).getUnitId());
        }
    }
}
