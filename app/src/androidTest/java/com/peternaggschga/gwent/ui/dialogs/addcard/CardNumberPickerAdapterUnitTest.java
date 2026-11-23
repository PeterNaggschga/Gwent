package com.peternaggschga.gwent.ui.dialogs.addcard;

import static com.google.common.truth.Truth.assertThat;
import static com.peternaggschga.gwent.ui.main.FactionSwitchListener.THEME_NORTHERN_KINGDOMS;

import android.content.Context;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.NumberPicker;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.peternaggschga.gwent.R;
import com.peternaggschga.gwent.data.RowType;
import com.peternaggschga.gwent.ui.dialogs.addcard.filter.AbilityFilter;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Collections;

import io.reactivex.rxjava3.schedulers.TestScheduler;

@RunWith(AndroidJUnit4.class)
public class CardNumberPickerAdapterUnitTest {
    @Test
    public void initializesRealPickersWithFilteredNormalOptions() {
        assertInitialOptions(RowType.RANGE, 2, 4);
    }

    @Test
    public void unavailableHeroesCannotBeSelected() {
        assertInitialOptions(RowType.SIEGE, 1, 3);
    }

    private void assertInitialOptions(RowType row, int heroOptions, int abilityOptions) {
        InstrumentationRegistry.getInstrumentation().runOnMainSync(() -> {
            Context context = new ContextThemeWrapper(
                    InstrumentationRegistry.getInstrumentation().getTargetContext(),
                    R.style.NorthernKingdomsTheme);
            ViewGroup layout = (ViewGroup) LayoutInflater.from(context).inflate(R.layout.popup_add_card, null);
            ViewGroup pickerGroup = layout.findViewById(R.id.card_layout);
            CardNumberPickerAdapter adapter = new CardNumberPickerAdapter(pickerGroup,
                    new SquadManager(Collections.emptyList()),
                    AbilityFilter.create(THEME_NORTHERN_KINGDOMS, row), new TestScheduler());
            try {
                NumberPicker hero = pickerGroup.findViewById(R.id.popup_add_card_epic_picker);
                NumberPicker ability = pickerGroup.findViewById(R.id.popup_add_card_ability_picker);
                assertThat(hero.getDisplayedValues()).hasLength(heroOptions);
                assertThat(ability.getDisplayedValues()).hasLength(abilityOptions);
                assertThat(hero.getValue()).isEqualTo(0);
                assertThat(ability.getValue()).isEqualTo(0);
                assertThat(pickerGroup.findViewById(R.id.popup_add_card_binding_picker).getVisibility())
                        .isEqualTo(View.GONE);
            } finally {
                adapter.dispose();
            }
        });
    }
}
