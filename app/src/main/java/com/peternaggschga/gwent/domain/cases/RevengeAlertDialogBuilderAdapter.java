package com.peternaggschga.gwent.domain.cases;

import android.content.Context;
import android.content.DialogInterface;

import androidx.annotation.IntRange;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import com.peternaggschga.gwent.R;
import com.peternaggschga.gwent.data.Ability;
import com.peternaggschga.gwent.data.RowType;
import com.peternaggschga.gwent.data.UnitEntity;
import com.peternaggschga.gwent.data.UnitRepository;
import com.peternaggschga.gwent.ui.sounds.SoundManager;

import java.util.Collection;

import io.reactivex.rxjava3.core.Completable;

/**
 * An adapter class adapting AlertDialog.Builder to provide an interface
 * for creating an AlertDialog asking the user whether they want to invoke the Ability.REVENGE ability.
 *
 */
class RevengeAlertDialogBuilderAdapter {
    /**
     * Boolean constant defining
     * whether the default UnitEntity summoned by the Ability.REVENGE ability is epic.
     *
     * @see Ability.REVENGE
     */
    private static final boolean AVENGER_EPIC = false;

    /**
     * Ability constant defining the Ability of the default UnitEntity
     * summoned by the Ability.REVENGE ability.
     *
     * @see Ability.REVENGE
     */
    private static final Ability AVENGER_ABILITY = Ability.NONE;

    /**
     * Integer constant defining the damage of the default UnitEntity
     * summoned by the Ability.REVENGE ability.
     *
     * @see Ability.REVENGE
     */
    private static final int AVENGER_DAMAGE = 8;

    /**
     * Strength of the melee hero summoned by a removed melee Avenger card.
     */
    private static final int HERO_AVENGER_DAMAGE = 11;

    /**
     * Integer constant defining the squad of the default UnitEntity
     * summoned by the Ability.REVENGE ability.
     *
     * @see Ability.REVENGE
     */
    private static final Integer AVENGER_SQUAD = null;

    /**
     * RowType constant defining the row of the default UnitEntity
     * summoned by the Ability.REVENGE ability.
     *
     * @see Ability.REVENGE
     */
    private static final RowType AVENGER_ROW = RowType.MELEE;

    /**
     * AlertDialog.Builder that is adapted by this class.
     */
    @NonNull
    private final AlertDialog.Builder adapteeBuilder;

    /**
     * Localized positive button label for the number of avengers to summon.
     */
    @NonNull
    private final String positiveButtonText;

    /**
     * Constructor of a RevengeAlertDialogBuilderAdapter.
     * Initializes the buttons with empty callbacks.
     *
     * @param context Context of the built AlertDialog.
     * @param numberOfAvengers Number of removed units whose ability can be activated.
     */
    RevengeAlertDialogBuilderAdapter(@NonNull Context context, @IntRange(from = 1) int numberOfAvengers) {
        positiveButtonText = context.getResources().getQuantityString(
                R.plurals.alertDialog_revenge_positive, numberOfAvengers);
        this.adapteeBuilder = new AlertDialog.Builder(context)
                .setIconAttribute(android.R.attr.alertDialogIcon)
                .setTitle(R.string.alertDialog_revenge_title)
                .setMessage(context.getResources().getQuantityString(
                        R.plurals.alertDialog_revenge_msg, numberOfAvengers, numberOfAvengers))
                .setCancelable(false)
                .setPositiveButton(positiveButtonText, (dialog, which) -> dialog.cancel())
                .setNegativeButton(R.string.alertDialog_revenge_negative, (dialog, which) -> dialog.cancel());
    }

    /**
     * Inserts avengers corresponding to the removed cards into the given UnitRepository.
     * Melee cards summon strength-11 melee heroes; other rows summon strength-8 normal melee units.
     * All summoned units have no ability or squad. Plays one sound after successful insertion,
     * giving the hero sound precedence if any heroes are summoned.
     *
     * @param repository       UnitRepository where avengers are inserted.
     * @param avengers Removed cards with the Avenger ability, whose rows select the summoned units.
     * @param soundManager     SoundManager used to play the card add sound.
     * @return A Completable tracking operation status.
     */
    @NonNull
    public static Completable insertAvengers(@NonNull UnitRepository repository,
                                             @NonNull Collection<UnitEntity> avengers, @NonNull SoundManager soundManager) {
        int heroes = (int) avengers.stream().filter(unit -> unit.getRow() == RowType.MELEE).count();
        int normal = avengers.size() - heroes;
        Completable normalInsertion = normal == 0 ? Completable.complete()
                : repository.insertUnit(AVENGER_EPIC, AVENGER_DAMAGE, AVENGER_ABILITY, AVENGER_SQUAD, AVENGER_ROW, normal);
        return normalInsertion.andThen(Completable.defer(() -> heroes == 0 ? Completable.complete()
                        : repository.insertUnit(true, HERO_AVENGER_DAMAGE, AVENGER_ABILITY, AVENGER_SQUAD, AVENGER_ROW, heroes)))
                .doOnComplete(() -> {
                    if (!avengers.isEmpty()) {
                        soundManager.playCardAddSound(AVENGER_ROW, heroes > 0);
                    }
                });
    }

    /**
     * Creates an AlertDialog with the arguments supplied to this builder.
     * Basically just calls AlertDialog.Builder.create() on .adapteeBuilder.
     *
     * @return The AlertDialog created by this builder.
     * @see AlertDialog.Builder.create()
     */
    @NonNull
    AlertDialog create() {
        return adapteeBuilder.create();
    }

    /**
     * Adds the given callback to the positive button of the built Dialog.
     * Callback should call insertAvengers().
     *
     * @param onPositiveButtonClick DialogInterface.OnClickListener that is called, when the positive button is clicked.
     * @return The RevengeAlertDialogBuilder with the updated positive callback.
     * @see .insertAvengers(UnitRepository, Collection, SoundManager)
     */
    @NonNull
    RevengeAlertDialogBuilderAdapter setPositiveCallback(@NonNull DialogInterface.OnClickListener onPositiveButtonClick) {
        adapteeBuilder.setPositiveButton(positiveButtonText, onPositiveButtonClick);
        return this;
    }

    /**
     * Adds the given callback to the negative button of the built Dialog.
     *
     * @param onNegativeButtonClick DialogInterface.OnClickListener that is called, when the negative button is clicked.
     * @return The RevengeAlertDialogBuilder with the updated negative callback.
     */
    @NonNull
    RevengeAlertDialogBuilderAdapter setNegativeCallback(@NonNull DialogInterface.OnClickListener onNegativeButtonClick) {
        adapteeBuilder.setNegativeButton(R.string.alertDialog_revenge_negative, onNegativeButtonClick);
        return this;
    }
}
