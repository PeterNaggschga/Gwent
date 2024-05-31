package com.peternaggschga.gwent.data;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * A class representing the state of an attack row, i.e., weather and commanders horn.
 * Is a persistent Entity and is therefore saved in a database table named `rows`.
 */
@Entity(tableName = "rows")
@SuppressWarnings("unused")
class RowEntity {
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
     * Defines whether the commanders horn buff is present in the represented row, i.e.,
     * if set to true, the damage value of units in the represented row is doubled.
     * Is set to `false` by default.
     */
    @ColumnInfo(defaultValue = "false")
    private boolean horn;

    /**
     * Constructor of a RowEntity.
     * #weather and #horn cannot be set here
     * since a new row must start with the respective default values.
     *
     * @param id RowType of the represented row.
     */
    RowEntity(@NonNull RowType id) {
        this.id = id;
    }

    /**
     * Getter for #id.
     * Only used by Room extension.
     *
     * @return RowType of the represented row.
     */
    @NonNull
    RowType getId() {
        return id;
    }

    /**
     * Getter for #weather.
     * Only used by Room extension.
     *
     * @return Boolean representing the current status of the weather debuff.
     */
    boolean isWeather() {
        return weather;
    }

    /**
     * Setter for #weather.
     * Only used by Room extension.
     *
     * @param weather Boolean representing the new status of the weather debuff.
     */
    void setWeather(boolean weather) {
        this.weather = weather;
    }

    /**
     * Getter for #horn.
     * Only used by Room extension.
     *
     * @return Boolean representing the current status of the horn buff.
     */
    boolean isHorn() {
        return horn;
    }

    /**
     * Setter for #horn.
     * Only used by Room extension.
     *
     * @param horn Boolean representing the new status of the horn buff.
     */
    void setHorn(boolean horn) {
        this.horn = horn;
    }
}
