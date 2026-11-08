package com.peternaggschga.gwent.domain.damage;

import androidx.annotation.IntRange;

/**
 * Applies King Bran's weather penalty before the row's other strength modifiers.
 */
class KingBranDamageCalculator implements DamageCalculator {
    /**
     * Whether weather is active in the row.
     */
    private final boolean weather;

    /**
     * Creates the base calculator used when King Bran is selected.
     *
     * @param weather Whether weather is active in the row.
     */
    KingBranDamageCalculator(boolean weather) {
        this.weather = weather;
    }

    /**
     * Halves base strength under weather, rounding down; otherwise leaves it unchanged.
     *
     * @param id     ID of the unit being evaluated.
     * @param damage Nonnegative base strength.
     * @return Base strength after King Bran's weather penalty.
     * @throws IllegalArgumentException When damage is negative.
     */
    @Override
    public int calculateDamage(int id, @IntRange(from = 0) int damage) {
        if (damage < 0) {
            throw new IllegalArgumentException("Damage must not be less than zero but is " + damage + ".");
        }
        // TODO: Verify whether King Bran's weather penalty should round down or up.
        return weather ? damage / 2 : damage;
    }

    /**
     * Indicates the weather penalty using the same color as ordinary weather.
     *
     * @param id ID of the unit being evaluated.
     * @return DEBUFFED under weather, otherwise DEFAULT.
     */
    @Override
    public Color isBuffed(int id) {
        return weather ? Color.DEBUFFED : Color.DEFAULT;
    }
}
