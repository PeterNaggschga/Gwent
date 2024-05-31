package com.peternaggschga.gwent.domain.cases;

import static com.peternaggschga.gwent.ui.main.FactionSwitchListener.THEME_MONSTER;
import static com.peternaggschga.gwent.ui.main.FactionSwitchListener.THEME_PREFERENCE_KEY;
import static com.peternaggschga.gwent.ui.main.FactionSwitchListener.THEME_SCOIATAEL;

import android.content.Context;
import android.content.SharedPreferences;
import android.widget.Toast;

import androidx.annotation.IntRange;
import androidx.annotation.NonNull;
import androidx.preference.PreferenceManager;

import com.peternaggschga.gwent.GwentApplication;
import com.peternaggschga.gwent.R;
import com.peternaggschga.gwent.RowType;
import com.peternaggschga.gwent.data.UnitRepository;

import java.util.Arrays;
import java.util.stream.Collectors;

import io.reactivex.rxjava3.core.Single;

/**
 * A use case class responsible for dispatching a reset call to ResetRepositoryUseCase,
 * possibly after a confirmation by the user obtained from a Dialog.
 *
 * @todo Change Integer constants to enum fields.
 * @see ResetRepositoryUseCase
 */
public class ResetDialogUseCase {
    /**
     * Integer constant representing that a reset was triggered by a click on the reset button.
     */
    public static final int TRIGGER_BUTTON_CLICK = 0;

    /**
     * Integer constant representing that a reset was triggered by a faction switch.
     * Only relevant if faction reset is activated, i.e.,
     * the SharedPreference referenced by R.string#preference_key_faction_reset.
     *
     * @see R.string#preference_key_faction_reset
     */
    public static final int TRIGGER_FACTION_SWITCH = 1;

    /**
     * Resets the given UnitRepository.
     * May invoke a Dialog asking whether the user really wants
     * to reset depending on the given trigger and warning settings.
     * ResetRepositoryUseCase is used for resetting.
     * Wrapper for #reset(Context, UnitRepository, int).
     *
     * @param context Context where a Dialog can be inflated.
     * @param trigger Integer defining what triggered this reset.
     * @return A Single emitting a Boolean defining whether the reset really took place.
     * @see #reset(Context, UnitRepository, int)
     * @see ResetRepositoryUseCase#reset(Context, UnitRepository, boolean)
     */
    @NonNull
    public static Single<Boolean> reset(@NonNull Context context,
                                        @IntRange(from = TRIGGER_BUTTON_CLICK, to = TRIGGER_FACTION_SWITCH) int trigger) {
        return GwentApplication.getRepository(context)
                .flatMap(repository -> reset(context, repository, trigger));
    }

    /**
     * Resets the given UnitRepository.
     * May invoke a Dialog asking whether the user really wants
     * to reset depending on the given trigger and warning settings.
     * ResetRepositoryUseCase is used for resetting.
     *
     * @param context    Context where a Dialog can be inflated.
     * @param repository UnitRepository that is reset.
     * @param trigger    Integer defining what triggered this reset.
     * @return A Single emitting a Boolean defining whether the reset really took place.
     * @see ResetRepositoryUseCase#reset(Context, UnitRepository, boolean)
     */
    @NonNull
    protected static Single<Boolean> reset(@NonNull Context context, @NonNull UnitRepository repository,
                                           @IntRange(from = TRIGGER_BUTTON_CLICK, to = TRIGGER_FACTION_SWITCH) int trigger) {
        return getDialogType(context, repository, trigger).flatMap(dialogType -> {
            if (dialogType == DialogType.NONE) {
                return ResetRepositoryUseCase.reset(context, repository).andThen(Single.just(true));
            }
            return Single.create(emitter -> {
                new ResetAlertDialogBuilderAdapter(context, (resetDecision, keepUnit) -> {
                    if (!resetDecision) {
                        emitter.onSuccess(false);
                        return;
                    }
                    // noinspection CheckResult, ResultOfMethodCallIgnored
                    ResetRepositoryUseCase.reset(context, repository, keepUnit)
                            .doAfterTerminate(() -> emitter.onSuccess(true))
                            .subscribe(unit ->
                                    Toast.makeText(context,
                                                    context.getString(R.string.alertDialog_factionreset_monster_toast_keep, unit.toString(context)),
                                                    Toast.LENGTH_LONG)
                                            .show());
                }).setTrigger(trigger)
                        .setMonsterDialog(dialogType == DialogType.MONSTER)
                        .create()
                        .show();


            });
        });
    }

    /**
     * Returns a DialogType defining which kind of Dialog should be invoked.
     *
     * @param context    Context used for retrieval of SharedPreferences.
     * @param repository UnitRepository used to check if a certain DialogType is even necessary.
     * @param trigger    Integer defining what triggered the reset.
     * @return A DialogType defining the kind of Dialog.
     * @see DialogType
     */
    @NonNull
    private static Single<DialogType> getDialogType(@NonNull Context context, @NonNull UnitRepository repository,
                                                    @IntRange(from = TRIGGER_BUTTON_CLICK, to = TRIGGER_FACTION_SWITCH) int trigger) {
        return Single.concat(Arrays.stream(RowType.values()).map(row ->
                        repository.isWeather(row)
                                .concatWith(repository.isHorn(row))
                                .any(state -> state)
                ).collect(Collectors.toList())).any(state -> state)
                .zipWith(repository.getUnits(), (statusEffects, units) -> {
                    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
                    boolean monsterDialog = trigger != TRIGGER_FACTION_SWITCH;
                    monsterDialog &= preferences.getInt(THEME_PREFERENCE_KEY, THEME_SCOIATAEL) == THEME_MONSTER;
                    monsterDialog &= units.stream().anyMatch(unit -> !unit.isEpic());
                    if (monsterDialog) {
                        return DialogType.MONSTER;
                    }
                    boolean defaultWarning = statusEffects || !units.isEmpty();
                    defaultWarning &= preferences.getBoolean(context.getString(R.string.preference_key_warning),
                            context.getResources().getBoolean(R.bool.warning_preference_default));
                    if (defaultWarning) {
                        return DialogType.DEFAULT;
                    }
                    return DialogType.NONE;
                });
    }

    /**
     * Enum defining which form of Dialog should be shown.
     *
     * @see #getDialogType(Context, UnitRepository, int)
     */
    private enum DialogType {
        /**
         * No Dialog must be invoked.
         */
        NONE,
        /**
         * A default Dialog asking whether to reset should be invoked.
         */
        DEFAULT,
        /**
         * A monster Dialog asking whether to reset and
         * whether to invoke the monster perk, should be invoked.
         */
        MONSTER
    }
}
