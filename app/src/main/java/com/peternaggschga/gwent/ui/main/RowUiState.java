package com.peternaggschga.gwent.ui.main;



import androidx.annotation.IntRange;

/**
 * A data class encapsulating the visible state of a row in the main view of the application.
 */
public class RowUiState {
    /**
     * Defines the summed-up damage of all units in this row.
     */
    @IntRange(from = 0)
    private final int damage;

    /**
     * Defines whether the weather debuff is active in this row.
     */
    private final boolean weather;

    /**
     * Defines whether the commander's horn buff is active in this row.
     */
    private final boolean horn;

    /**
     * Defines the number of units in this row.
     */
    @IntRange(from = 0)
    private final int units;

    /**
     * Constructor of a RowUiState encapsulating the given data.
     * @param damage Integer representing the summed-up damage of all units.
     * @param weather Boolean defining whether the weather debuff is active.
     * @param horn Boolean defining whether the commander's horn buff is active.
     * @param units Integer representing the number of units.
     * @throws IllegalArgumentException When damage or units is negative.
     */
    public RowUiState(@IntRange(from = 0) int damage, boolean weather, boolean horn, @IntRange(from = 0) int units) {
        if (damage < 0) {
            throw new IllegalArgumentException("Damage must be greater or equal to 0 but is " + damage + ".");
        }
        if (units < 0) {
            throw new IllegalArgumentException("Units must be greater or equal to 0 but is " + units + ".");
        }
        this.damage = damage;
        this.weather = weather;
        this.horn = horn;
        this.units = units;
    }

    /**
     * Returns the summed-up damage of all units in this row.
     * @return An Integer representing the summed-up damage of all units.
     */
    @IntRange(from = 0)
    public int getDamage() {
        return damage;
    }

    /**
     * Returns whether the weather debuff is active in this row.
     * @return A Boolean defining whether the weather debuff is active.
     */
    public boolean isWeather() {
        return weather;
    }

    /**
     * Returns whether the commander's horn buff is active in this row.
     * @return A Boolean whether the commander's horn buff is active.
     */
    public boolean isHorn() {
        return horn;
    }

    /**
     * Returns the number of units in this row.
     * @return An Integer representing the number of units in this row.
     */
    @IntRange(from = 0)
    public int getUnits() {
        return units;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RowUiState)) return false;
        RowUiState state = (RowUiState) o;
        return damage == state.damage && weather == state.weather && horn == state.horn && units == state.units;
    }
}
