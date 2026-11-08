package com.peternaggschga.gwent.data;

import androidx.annotation.NonNull;
import androidx.room3.ColumnInfo;
import androidx.room3.Entity;
import androidx.room3.PrimaryKey;

/**
 * A class representing the state of an attack row, i.e., weather and its active buff.
 * Is a persistent Entity and is therefore saved in a database table named `rows`.
 */
@Entity(tableName = "rows")
@SuppressWarnings("unused")
public class RowEntity {
    /**
     * Defines the type of the row as one of the values in RowType,
     * thereby limiting the number of different rows to three.
     * Primary key of the represented row.
     */
    @PrimaryKey
    @NonNull
    private final RowType id;

    /**
     * Defines whether the weather debuff is present in the represented row, i.e.,
     * if set to true, the damage value of units in the represented row is set to 1.
     * Is set to `false` by default.
     */
    @ColumnInfo(defaultValue = "false")
    private boolean weather;

    /**
     * Defines the active buff of the represented row as one of the values in RowBuffState.
     * The commander's horn and Mardroeme effects are mutually exclusive.
     * Is set to RowBuffState.NONE by default.
     */
    @NonNull
    @ColumnInfo(defaultValue = "'NONE'")
    private RowBuffState buffState = RowBuffState.NONE;

    /**
     * Constructor of a RowEntity.
     * .weather and .buffState cannot be set here
     * since a new row must start with the respective default values.
     *
     * @param id RowType of the represented row.
     */
    protected RowEntity(@NonNull RowType id) {
        this.id = id;
    }

    /**
     * Getter for .id.
     * Only used by Room extension.
     *
     * @return RowType of the represented row.
     */
    @NonNull
    protected RowType getId() {
        return id;
    }

    /**
     * Getter for .weather.
     * Only used by Room extension.
     *
     * @return Boolean representing the current status of the weather debuff.
     */
    protected boolean isWeather() {
        return weather;
    }

    /**
     * Setter for .weather.
     * Only used by Room extension.
     *
     * @param weather Boolean representing the new status of the weather debuff.
     */
    protected void setWeather(boolean weather) {
        this.weather = weather;
    }

    /**
     * Getter for .buffState.
     * Only used by Room extension.
     *
     * @return RowBuffState representing the currently active buff, or NONE if no buff is active.
     */
    @NonNull
    protected RowBuffState getBuffState() {
        return buffState;
    }

    /**
     * Setter for .buffState.
     * Only used by Room extension.
     *
     * @param buffState RowBuffState representing the new active buff, or NONE to clear it.
     */
    protected void setBuffState(@NonNull RowBuffState buffState) {
        this.buffState = buffState;
    }
}
