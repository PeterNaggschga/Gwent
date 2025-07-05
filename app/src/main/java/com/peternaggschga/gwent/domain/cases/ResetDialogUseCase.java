package com.peternaggschga.gwent.domain.cases;

import static com.peternaggschga.gwent.ui.main.FactionSwitchListener.THEME_MONSTER;
import static com.peternaggschga.gwent.ui.main.FactionSwitchListener.THEME_PREFERENCE_KEY;
import static com.peternaggschga.gwent.ui.main.FactionSwitchListener.THEME_SCOIATAEL;

import android.content.Context;
import android.content.SharedPreferences;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceManager;

import com.peternaggschga.gwent.GwentApplication;
import com.peternaggschga.gwent.R;
import com.peternaggschga.gwent.data.RowType;
import com.peternaggschga.gwent.data.UnitRepository;
import com.peternaggschga.gwent.ui.sounds.SoundManager;

import java.util.Arrays;
import java.util.stream.Collectors;

import io.reactivex.rxjava3.core.Single;

/**
 * A use case class responsible for dispatching a reset call to ResetRepositoryUseCase,
 * possibly after a confirmation by the user obtained from a Dialog.
 *
 * @see ResetRepositoryUseCase
 */
public class ResetDialogUseCase {
    /**
     * Resets the given UnitRepository.
     * May invoke a Dialog asking whether the user really wants
     * to reset depending on the given trigger and warning settings.
     * ResetRepositoryUseCase is used for resetting.
     * Wrapper for #reset(Context, UnitRepository, Trigger).
     *
     * @param context Context where a Dialog can be inflated.
     * @param trigger {@link Trigger} defining what triggered this reset.
     * @param soundManager SoundManager used, if an UnitEntity has the Ability#REVENGE ability.
     * @return A Single emitting a Boolean defining whether the reset really took place.
     * @see #reset(Context, UnitRepository, Trigger, SoundManager)
     * @see ResetRepositoryUseCase#reset(Context, UnitRepository, boolean, SoundManager)
     */
    @NonNull
    public static Single<Boolean> reset(@NonNull Context context, @NonNull Trigger trigger, @NonNull SoundManager soundManager) {
        return GwentApplication.getRepository(context)
                .flatMap(repository -> reset(context, repository, trigger, soundManager));
    }

    /**
     * Resets the given UnitRepository.
     * May invoke a Dialog asking whether the user really wants
     * to reset depending on the given trigger and warning settings.
     * ResetRepositoryUseCase is used for resetting.
     *
     * @param context    Context where a Dialog can be inflated.
     * @param repository UnitRepository that is reset.
     * @param trigger    {@link Trigger} defining what triggered this reset.
     * @param soundManager SoundManager used, if an UnitEntity has the Ability#REVENGE ability.
     * @return A Single emitting a Boolean defining whether the reset really took place.
     * @see ResetRepositoryUseCase#reset(Context, UnitRepository, boolean, SoundManager)
     */
    @NonNull
    protected static Single<Boolean> reset(@NonNull Context context, @NonNull UnitRepository repository,
                                           @NonNull Trigger trigger, @NonNull SoundManager soundManager) {
        return getDialogType(context, repository, trigger).flatMap(dialogType -> {
            if (dialogType == DialogType.NONE) {
                return ResetRepositoryUseCase.reset(context, repository, soundManager).andThen(Single.just(true));
            }
            return Single.create(emitter -> new ResetAlertDialogBuilderAdapter(context, (resetDecision, keepUnit) -> {
                if (!resetDecision) {
                    emitter.onSuccess(false);
                    return;
                }
                // noinspection CheckResult, ResultOfMethodCallIgnored
                ResetRepositoryUseCase.reset(context, repository, keepUnit, soundManager)
                        .doAfterTerminate(() -> emitter.onSuccess(true))
                        .subscribe(unit ->
                                Toast.makeText(context,
                                                context.getString(R.string.alertDialog_factionreset_monster_toast_keep, unit.toString(context)),
                                                Toast.LENGTH_LONG)
                                        .show());
            }).setTrigger(trigger)
                    .setMonsterDialog(dialogType == DialogType.MONSTER)
                    .create()
                    .show());
        });
    }

    /**
     * Returns a DialogType defining which kind of Dialog should be invoked.
     *
     * @param context    Context used for retrieval of SharedPreferences.
     * @param repository UnitRepository used to check if a certain DialogType is even necessary.
     * @param trigger    {@link Trigger} defining what triggered the reset.
     * @return A DialogType defining the kind of Dialog.
     * @see DialogType
     */
    @NonNull
    private static Single<DialogType> getDialogType(@NonNull Context context, @NonNull UnitRepository repository,
                                                    @NonNull Trigger trigger) {
        return Single.concat(Arrays.stream(RowType.values()).map(row ->
                        repository.isWeather(row)
                                .concatWith(repository.isHorn(row))
                                .any(state -> state)
                ).collect(Collectors.toList())).any(state -> state)
                .zipWith(repository.getUnits(), (statusEffects, units) -> {
                    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
                    boolean monsterDialog = trigger != Trigger.FACTION_SWITCH;
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
     * An {@link Enum} listing the possible triggers of a reset.
     */
    public enum Trigger {
        /**
         * Represents, that a reset was triggered by a click on the reset button.
         */
        BUTTON_CLICK,
        /**
         * Represents that a reset was triggered by a faction switch.
         * Only relevant if faction reset is activated, i.e.,
         * the preference at the key referenced by {@link  R.string#preference_key_faction_reset} is true.
         */
        FACTION_SWITCH
    }

    /**
     * An {@link Enum} defining which form of Dialog should be shown.
     *
     * @see #getDialogType(Context, UnitRepository, Trigger)
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
