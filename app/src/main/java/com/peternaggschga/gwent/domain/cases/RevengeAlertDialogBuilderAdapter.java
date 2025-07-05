package com.peternaggschga.gwent.domain.cases;

import android.content.Context;
import android.content.DialogInterface;

import androidx.annotation.IntRange;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import com.peternaggschga.gwent.R;
import com.peternaggschga.gwent.data.Ability;
import com.peternaggschga.gwent.data.RowType;
import com.peternaggschga.gwent.data.UnitRepository;
import com.peternaggschga.gwent.ui.sounds.SoundManager;

import io.reactivex.rxjava3.core.Completable;

/**
 * An adapter class adapting AlertDialog.Builder to provide an interface
 * for creating an AlertDialog asking the user whether they want to invoke the Ability#REVENGE ability.
 *
 */
class RevengeAlertDialogBuilderAdapter {
    /**
     * Boolean constant defining
     * whether the default UnitEntity summoned by the Ability#REVENGE ability is epic.
     *
     * @see Ability#REVENGE
     */
    private static final boolean AVENGER_EPIC = false;

    /**
     * Ability constant defining the Ability of the default UnitEntity
     * summoned by the Ability#REVENGE ability.
     *
     * @see Ability#REVENGE
     */
    private static final Ability AVENGER_ABILITY = Ability.NONE;

    /**
     * Integer constant defining the damage of the default UnitEntity
     * summoned by the Ability#REVENGE ability.
     *
     * @see Ability#REVENGE
     */
    private static final int AVENGER_DAMAGE = 8;

    /**
     * Integer constant defining the squad of the default UnitEntity
     * summoned by the Ability#REVENGE ability.
     *
     * @see Ability#REVENGE
     */
    private static final Integer AVENGER_SQUAD = null;

    /**
     * RowType constant defining the row of the default UnitEntity
     * summoned by the Ability#REVENGE ability.
     *
     * @see Ability#REVENGE
     */
    private static final RowType AVENGER_ROW = RowType.MELEE;

    /**
     * AlertDialog.Builder that is adapted by this class.
     */
    @NonNull
    private final AlertDialog.Builder adapteeBuilder;

    /**
     * Constructor of a RevengeAlertDialogBuilderAdapter.
     * Initializes the buttons with empty callbacks.
     *
     * @param context Context of the built AlertDialog.
     */
    RevengeAlertDialogBuilderAdapter(@NonNull Context context) {
        this.adapteeBuilder = new AlertDialog.Builder(context)
                .setIconAttribute(android.R.attr.alertDialogIcon)
                .setTitle(R.string.alertDialog_revenge_title)
                .setMessage(R.string.alertDialog_revenge_msg)
                .setCancelable(false)
                .setPositiveButton(R.string.alertDialog_revenge_positive, (dialog, which) -> dialog.cancel())
                .setNegativeButton(R.string.alertDialog_revenge_negative, (dialog, which) -> dialog.cancel());
    }

    /**
     * Inserts numberOfAvengers avenger units into the given UnitRepository.
     * The inserted UnitEntity objects have the attributes defined in #AVENGER_EPIC,
     * #AVENGER_DAMAGE, #AVENGER_ABILITY, #AVENGER_SQUAD, and #AVENGER_ROW.
     *
     * @param repository       UnitRepository where avengers are inserted.
     * @param numberOfAvengers Integer defining how many avengers are inserted.
     * @return A Completable tracking operation status.
     */
    @NonNull
    public static Completable insertAvengers(@NonNull UnitRepository repository, @IntRange(from = 0) int numberOfAvengers, @NonNull SoundManager soundManager) {
        return repository.insertUnit(AVENGER_EPIC, AVENGER_DAMAGE, AVENGER_ABILITY, AVENGER_SQUAD, AVENGER_ROW, numberOfAvengers)
                .doOnComplete(() -> soundManager.playCardAddSound(AVENGER_ROW, AVENGER_EPIC));
    }

    /**
     * Creates an AlertDialog with the arguments supplied to this builder.
     * Basically just calls AlertDialog.Builder#create() on #adapteeBuilder.
     *
     * @see AlertDialog.Builder#create()
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
     * @see #insertAvengers(UnitRepository, int, SoundManager)
     */
    @NonNull
    RevengeAlertDialogBuilderAdapter setPositiveCallback(@NonNull DialogInterface.OnClickListener onPositiveButtonClick) {
        adapteeBuilder.setPositiveButton(R.string.alertDialog_revenge_positive, onPositiveButtonClick);
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
