package com.peternaggschga.gwent.ui.dialogs.cards;

import androidx.annotation.ColorInt;
import androidx.annotation.DrawableRes;
import androidx.annotation.IntRange;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DiffUtil;

import java.util.Objects;

/**
 * @todo Documentation
 * @todo Add testing.
 */
public class CardUiState {
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
    public static final int UNUSED = -1;
    private final int unitId;
    @DrawableRes
    private final int damageBackgroundImageId;
    @NonNull
    private final String damageString;
    @ColorInt
    private final int damageTextColor;
    @DrawableRes
    private final int abilityImage;
    @NonNull
    private final String squadString;

    public CardUiState(int unitId, @DrawableRes int damageBackgroundImageId, @IntRange(from = 0) int damage,
                @ColorInt int damageTextColor, @DrawableRes int abilityImage,
                @Nullable @IntRange(from = 1) Integer squad) {
        this.unitId = unitId;
        this.damageBackgroundImageId = damageBackgroundImageId;
        this.damageString = String.valueOf(damage);
        this.damageTextColor = damageTextColor;
        this.abilityImage = abilityImage;
        this.squadString = squad == null ? "" : String.valueOf(squad);
    }

    public boolean showAbility() {
        return abilityImage != UNUSED;
    }

    public boolean showSquad() {
        return showAbility() && !squadString.isEmpty();
    }

    public int getUnitId() {
        return unitId;
    }

    @DrawableRes
    public int getDamageBackgroundImageId() {
        return damageBackgroundImageId;
    }

    @NonNull
    public String getDamageString() {
        return damageString;
    }

    @ColorInt
    public int getDamageTextColor() {
        return damageTextColor;
    }

    @DrawableRes
    public int getAbilityImage() {
        return abilityImage;
    }

    @NonNull
    public String getSquadString() {
        return squadString;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CardUiState)) return false;
        CardUiState that = (CardUiState) o;
        return damageBackgroundImageId == that.damageBackgroundImageId
                && damageTextColor == that.damageTextColor
                && abilityImage == that.abilityImage
                && Objects.equals(damageString, that.damageString)
                && Objects.equals(squadString, that.squadString);
    }
}
