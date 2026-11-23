package com.peternaggschga.gwent.ui.dialogs.addcard;

import android.content.SharedPreferences;
import android.view.View;
import android.view.ViewGroup;
import android.widget.NumberPicker;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceManager;

import com.peternaggschga.gwent.GwentApplication;
import com.peternaggschga.gwent.R;
import com.peternaggschga.gwent.data.Ability;
import com.peternaggschga.gwent.data.RowType;
import com.peternaggschga.gwent.ui.dialogs.addcard.filter.AbilityFilter;
import com.peternaggschga.gwent.ui.main.FactionSwitchListener;

import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Scheduler;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;

/**
 * A helper class responsible for initializing the NumberPicker views of an AddCardDialog
 * using reactive selections
 * and adding UnitEntity objects with the selected attributes when .addSelectedUnits() is called.
 * @see AddCardDialog
 */
class CardNumberPickerAdapter implements Disposable {
    /**
     * Filter supplying options for the current faction and row.
     */
    @NonNull
    private final AbilityFilter abilityFilter;

    /**
     * Picker subscriptions and pending visibility and toast debounce tasks.
     */
    @NonNull
    private final CompositeDisposable disposables = new CompositeDisposable();

    /**
     * ValuePicker used to decide the value of UnitEntity.epic.
     */
    @NonNull
    private final ValuePicker<Boolean> epicPicker;

    /**
     * DamageValuePicker used to decide the value of UnitEntity.damage.
     */
    @NonNull
    private final DamageValuePicker damagePicker;

    /**
     * ValuePicker used to decide the value of UnitEntity.ability.
     *
     * @see .squadPicker
     */
    @NonNull
    private final ValuePicker<Ability> abilityPicker;

    /**
     * ValuePicker used to decide the value of UnitEntity.squad.
     * Visible after a 500 ms debounce only when Tight Bond offers multiple squads.
     * @see .abilityPicker
     */
    @NonNull
    private final ValuePicker<Integer> squadPicker;

    /**
     * NumberPicker used to decide the number of UnitEntity objects that are inserted.
     */
    @NonNull
    private final NumberPicker numberPicker;

    /**
     * Constructor of a CardNumberPickerAdapter
     * managing the NumberPicker views in the given ViewGroup using the given SquadManager.
     * Sets value bounds and connects the reactive picker selections.
     * The ViewGroup must be the ConstraintLayout with the id R.id.card_layout from popup_add_card.xml.
     * @see R.id.card_layout
     * @param row RowType of the row that units are added to.
     * @param pickerGroup ViewGroup containing the managed NumberPicker views.
     * @param squadManager SquadManager containing up-to-date SquadState.
     */
    CardNumberPickerAdapter(@NonNull RowType row, @NonNull ViewGroup pickerGroup, @NonNull SquadManager squadManager) {
        this(pickerGroup, squadManager, getAbilityFilter(row, pickerGroup), AndroidSchedulers.mainThread());
    }

    /**
     * Creates the adapter with explicit filtering and debounce scheduling.
     * Picker changes and scheduled callbacks must run on the Android main thread.
     *
     * @param pickerGroup   ViewGroup containing the managed NumberPicker views.
     * @param squadManager  Current squad information for the edited row.
     * @param abilityFilter Filter supplying selectable card options.
     * @param scheduler     Scheduler used for visibility and toast debounce callbacks.
     */
    CardNumberPickerAdapter(@NonNull ViewGroup pickerGroup, @NonNull SquadManager squadManager,
                            @NonNull AbilityFilter abilityFilter, @NonNull Scheduler scheduler) {
        this.abilityFilter = abilityFilter;

        SortedMap<Boolean, Integer> epicStringResources = new TreeMap<>();
        epicStringResources.put(false, R.string.add_picker_epic_normal);
        if (abilityFilter.heroesAvailable()) {
            epicStringResources.put(true, R.string.add_picker_epic_epic);
        }
        epicPicker = new StringValuePicker<>(pickerGroup.findViewById(R.id.popup_add_card_epic_picker),
                epicStringResources,
                false);

        damagePicker = new DamageValuePicker(pickerGroup.findViewById(R.id.popup_add_card_dmg_picker),
                abilityFilter.getHeroDamageValues());

        SortedMap<Ability, Integer> abilityStringResources = new TreeMap<>();
        abilityStringResources.put(Ability.NONE, R.string.add_picker_ability_default);
        abilityStringResources.put(Ability.HORN, R.string.add_picker_ability_horn);
        abilityStringResources.put(Ability.REVENGE, R.string.add_picker_ability_revenge);
        abilityStringResources.put(Ability.BERSERKER, R.string.add_picker_ability_berserker);
        abilityStringResources.put(Ability.MARDROEME, R.string.add_picker_ability_mardroeme);
        abilityStringResources.put(Ability.BINDING, R.string.add_picker_ability_binding);
        abilityStringResources.put(Ability.MORAL_BOOST, R.string.add_picker_ability_moralBoost);
        abilityPicker = new StringValuePicker<>(pickerGroup.findViewById(R.id.popup_add_card_ability_picker),
                abilityStringResources,
                Ability.NONE);

        SortedMap<Integer, Integer> squadValues = new TreeMap<>();
        for (int squad = 1; squad <= Math.max(1, abilityFilter.getSquadCount()); squad++) {
            squadValues.put(squad, squad);
        }
        squadPicker = new ValuePicker<>(pickerGroup.findViewById(R.id.popup_add_card_binding_picker),
                squadValues, 1) {
            @Override
            @NonNull
            protected String getDisplayString(@NonNull Integer value) {
                return String.valueOf(value);
            }
        };
        squadPicker.getPicker().setVisibility(View.GONE);

        numberPicker = pickerGroup.findViewById(R.id.popup_add_card_number_picker);
        numberPicker.setMinValue(1);
        numberPicker.setMaxValue(10);

        disposables.add(epicPicker.values().subscribe(damagePicker::setEpicValues));
        disposables.add(epicPicker.values().subscribe(this::updateAbilities));

        int squadCount = abilityFilter.getSquadCount();
        if (squadCount > 0) {
            disposables.add(abilityPicker.values()
                    .filter(ability -> ability == Ability.BINDING)
                    .subscribe(ability -> squadPicker.setValue(
                            squadManager.getFirstSquadWithMembers(squadCount))));
            disposables.add(abilityPicker.values()
                    .switchMap(ability -> ability == Ability.BINDING
                            ? squadPicker.values()
                            .doOnNext(squad -> squadManager.setSquadDamage(squad, damagePicker))
                            .debounce(500, TimeUnit.MILLISECONDS, scheduler)
                            : Observable.empty())
                    .subscribe(squad -> squadManager.showSquadInfo(squad, numberPicker.getContext())));
        }
        if (squadCount > 1) {
            disposables.add(abilityPicker.values()
                    .debounce(500, TimeUnit.MILLISECONDS, scheduler)
                    .subscribe(ability -> squadPicker.getPicker().setVisibility(ability == Ability.BINDING ? View.VISIBLE : View.GONE)));
        }
    }

    /**
     * Resolves the current faction preference into a row-specific filter.
     *
     * @param row Row receiving the new cards.
     * @param pickerGroup ViewGroup whose context supplies faction preferences.
     * @return The filter for the current faction and row.
     */
    @NonNull
    private static AbilityFilter getAbilityFilter(@NonNull RowType row, @NonNull ViewGroup pickerGroup) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(pickerGroup.getContext());
        int faction = preferences.getInt(FactionSwitchListener.THEME_PREFERENCE_KEY,
                FactionSwitchListener.THEME_SCOIATAEL);
        return AbilityFilter.create(faction, row);
    }

    /**
     * Applies sorted ability options, centering NONE when there are exactly three choices.
     * Preserves the current selection when valid, otherwise prefers NONE when available.
     * A programmatic fallback is published so dependent squad state updates immediately.
     *
     * @param hero Whether hero abilities should be offered.
     */
    private void updateAbilities(boolean hero) {
        List<Ability> abilities = abilityFilter.getAbilities(hero).stream().sorted().collect(Collectors.toList());
        boolean hasNone = abilities.contains(Ability.NONE);
        if (abilities.size() == 3 && hasNone) {
            abilities.remove(Ability.NONE);
            abilities.add(1, Ability.NONE);
        }
        Ability previous = abilityPicker.getValue();
        Ability selected = abilities.contains(previous) ? previous : (hasNone ? Ability.NONE : abilities.get(0));
        abilityPicker.setSelectableValues(abilities, selected);
    }

    /**
     * Adds new UnitEntity objects the attributes selected by the managed pickers.
     * @param row RowType defining to which row the units are added.
     * @return A Single tracking operation status and containing whether the added unit is epic or not.
     */
    @NonNull
    Single<Boolean> addSelectedUnits(@NonNull RowType row) {
        boolean hero = epicPicker.getValue();
        int damage = damagePicker.getValue();
        Ability ability = abilityPicker.getValue();
        Integer squad = ability == Ability.BINDING ? squadPicker.getValue() : null;
        int count = numberPicker.getValue();
        return GwentApplication.getRepository(numberPicker.getContext())
                .flatMapCompletable(repository ->
                        repository.insertUnit(hero,
                                damage,
                                ability,
                                squad,
                                row,
                                count))
                .andThen(Single.just(hero));
    }

    /**
     * Disposes picker subscriptions and cancels pending debounce callbacks on dialog dismissal.
     */
    @Override
    public void dispose() {
        disposables.dispose();
    }

    /**
     * Returns whether the picker subscriptions have been disposed.
     *
     * @return True after dispose() has been called.
     */
    @Override
    public boolean isDisposed() {
        return disposables.isDisposed();
    }
}
