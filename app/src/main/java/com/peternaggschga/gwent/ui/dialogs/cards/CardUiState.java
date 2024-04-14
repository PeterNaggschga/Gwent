package com.peternaggschga.gwent.ui.dialogs.cards;

import static org.valid4j.Assertive.require;

import androidx.annotation.ColorInt;
import androidx.annotation.DrawableRes;
import androidx.annotation.IntRange;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DiffUtil;

import com.peternaggschga.gwent.data.UnitEntity;
import com.peternaggschga.gwent.domain.damage.DamageCalculator;

import java.util.Objects;

/**
 * A data class
 * encapsulating the visible state of a card in the card list shown by the ShowUnitsDialog.
 * Can be created from the represented UnitEntity using CardUiStateFactory.
 * @see CardUiStateFactory
 * @see ShowUnitsDialog
 * @todo Add testing.
 */
public class CardUiState {
    /**
     * DiffUtil.ItemCallback used to compare different CardUiState objects in a ListAdapter,
     * e.g., CardListAdapter.
     *
     * @see #unitId
     * @see #equals(Object)
     */
    @NonNull
    public static final DiffUtil.ItemCallback<CardUiState> DIFF_CALLBACK = new DiffUtil.ItemCallback<CardUiState>() {
        @Override
        public boolean areItemsTheSame(@NonNull CardUiState oldItem, @NonNull CardUiState newItem) {
            return oldItem.unitId == newItem.unitId;
        }

        @Override
        public boolean areContentsTheSame(@NonNull CardUiState oldItem, @NonNull CardUiState newItem) {
            return Objects.equals(oldItem, newItem);
        }
    };

    /**
     * Integer that is used instead of a drawable resource
     * to indicate the absence of a meaningful #abilityImage.
     * @see #abilityImageId
     */
    public static final int UNUSED = -1;

    /**
     * Integer referencing the UnitEntity#id of the represented UnitEntity.
     * The only member that is not shown in the UI.
     * @see #getUnitId()
     */
    private final int unitId;

    /**
     * Integer referencing the drawable resource shown as the background of the damage view.
     * @see #getDamageBackgroundImageId()
     */
    @DrawableRes
    private final int damageBackgroundImageId;

    /**
     * String containing the number shown in the damage view.
     * @see #getDamageString()
     */
    @NonNull
    private final String damageString;

    /**
     * Integer representing the text color of the damage view.
     * @see #getDamageTextColor()
     */
    @ColorInt
    private final int damageTextColor;

    /**
     * Integer referencing the drawable resource shown in the ability view.
     * May be #UNUSED if the view is not visible.
     * @see #UNUSED
     * @see #getAbilityImageId()
     */
    @DrawableRes
    private final int abilityImageId;

    /**
     * String containing the number shown in the squad view.
     * @see #getSquadString()
     */
    @NonNull
    private final String squadString;

    /**
     * Constructor of a CardUiState encapsulating the given data.
     * @param unitId Integer representing the UnitEntity#id of the represented UnitEntity.
     * @param damageBackgroundImageId Integer referencing the drawable resource shown by the damage view.
     * @param damage Integer representing the damage of the represented UnitEntity.
     * @param damageTextColor Integer representing the text color of the damage view.
     * @param abilityImageId Integer referencing the drawable resource shown by the ability image view or #UNUSED.
     * @param squad Integer representing the UnitEntity#squad of the represented UnitEntity.
     * @throws org.valid4j.errors.RequireViolation When damage is less than zero or squad is neither null nor greater than zero.
     * @see CardUiStateFactory#createCardUiState(UnitEntity, DamageCalculator)
     */
    public CardUiState(int unitId, @DrawableRes int damageBackgroundImageId, @IntRange(from = 0) int damage,
                       @ColorInt int damageTextColor, @DrawableRes int abilityImageId,
                @Nullable @IntRange(from = 1) Integer squad) {
        require(damage >= 0);
        require(squad == null || squad >= 1);
        this.unitId = unitId;
        this.damageBackgroundImageId = damageBackgroundImageId;
        this.damageString = String.valueOf(damage);
        this.damageTextColor = damageTextColor;
        this.abilityImageId = abilityImageId;
        this.squadString = squad == null ? "" : String.valueOf(squad);
    }

    /**
     * Returns whether the ability view is shown, i.e.,
     * if the unit has an ability other than Ability#NONE.
     * @return A Boolean defining whether the ability view is shown.
     */
    public boolean showAbility() {
        return abilityImageId != UNUSED;
    }

    /**
     * Returns whether the squad view is shown, i.e.,
     * if the unit has the Ability#BINDING ability.
     * @return A Boolean defining whether the squad view is shown.
     */
    public boolean showSquad() {
        return showAbility() && !squadString.isEmpty();
    }

    /**
     * Returns the #unitId of the represented UnitEntity.
     * @see #unitId
     * @return An Integer referencing the UnitEntity#id of the represented UnitEntity.
     */
    public int getUnitId() {
        return unitId;
    }

    /**
     * Returns the drawable resource shown as the background of the damage view.
     * @return An Integer referencing a drawable resource.
     * @see #damageBackgroundImageId
     */
    @DrawableRes
    public int getDamageBackgroundImageId() {
        return damageBackgroundImageId;
    }

    /**
     * Returns the String shown in the damage view.
     * @return A String containing the damage of the represented UnitEntity.
     * @see #damageString
     */
    @NonNull
    public String getDamageString() {
        return damageString;
    }

    /**
     * Returns the text color of the damage view.
     * @return An Integer representing a color.
     * @see #damageTextColor
     */
    @ColorInt
    public int getDamageTextColor() {
        return damageTextColor;
    }

    /**
     * Returns the drawable resource shown in the ability view.
     * @return An Integer referencing a drawable resource.
     * @see #abilityImageId
     * @see #showAbility()
     */
    @DrawableRes
    public int getAbilityImageId() {
        return abilityImageId;
    }

    /**
     * Returns the String shown in the squad view.
     * @return A String containing the squad of the represented UnitEntity or nothing.
     * @see #squadString
     * @see #showSquad()
     */
    @NonNull
    public String getSquadString() {
        return squadString;
    }

    /**
     * Checks whether the given Object looks the same as this CardUiState.
     * Does not compare #unitId
     * since that field does not influence the visual representation of CardUiState objects.
     * @param o Object that is being compared with this CardUiState.
     * @return A Boolean defining whether the objects look the same.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CardUiState)) return false;
        CardUiState that = (CardUiState) o;
        return damageBackgroundImageId == that.damageBackgroundImageId
                && damageTextColor == that.damageTextColor
                && abilityImageId == that.abilityImageId
                && Objects.equals(damageString, that.damageString)
                && Objects.equals(squadString, that.squadString);
    }
}
