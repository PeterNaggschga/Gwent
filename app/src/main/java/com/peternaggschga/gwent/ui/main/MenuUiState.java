package com.peternaggschga.gwent.ui.main;



import androidx.annotation.IntRange;

/**
 * A data class encapsulating the visible state of the menu in the main view of the application.
 */
public class MenuUiState {
    /**
     * Defines the summed-up damage of all units on the game board.
     */
    @IntRange(from = 0)
    private final int damage;

    /**
     * Defines whether the reset button is clickable.
     */
    private final boolean reset;

    /**
     * Defines whether the weather button is clickable.
     */
    private final boolean weather;

    /**
     * Defines whether the burn button is clickable.
     */
    private final boolean burn;

    /**
     * Constructor of a MenuUiState encapsulating the given data.
     *
     * @param damage  Integer representing the summed-up damage of all units.
     * @param reset   Boolean defining whether the reset button is clickable.
     * @param weather Boolean defining whether the weather button is clickable.
     * @param burn    Boolean defining whether the burn button is clickable.
     * @throws org.valid4j.errors.RequireViolation When damage is negative.
     */
    public MenuUiState(@IntRange(from = 0) int damage, boolean reset, boolean weather, boolean burn) {
        // TODO: assert damage >= 0);
        this.damage = damage;
        this.reset = reset;
        this.weather = weather;
        this.burn = burn;
    }

    /**
     * Returns the summed-up damage of all units on the game board.
     *
     * @return An Integer representing the summed-up damage of all units.
     */
    @IntRange(from = 0)
    public int getDamage() {
        return damage;
    }

    /**
     * Returns whether the reset button is clickable.
     *
     * @return A Boolean defining whether the reset button is clickable.
     */
    public boolean isReset() {
        return reset;
    }

    /**
     * Returns whether the weather button is clickable.
     *
     * @return A Boolean defining whether the weather button is clickable.
     */
    public boolean isWeather() {
        return weather;
    }

    /**
     * Returns whether the burn button is clickable.
     *
     * @return A Boolean defining whether the burn button is clickable.
     */
    public boolean isBurn() {
        return burn;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MenuUiState)) return false;
        MenuUiState that = (MenuUiState) o;
        return damage == that.damage && reset == that.reset && weather == that.weather && burn == that.burn;
    }
}
