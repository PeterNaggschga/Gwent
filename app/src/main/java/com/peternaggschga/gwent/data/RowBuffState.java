package com.peternaggschga.gwent.data;

/**
 * An enum listing the mutually exclusive buff states of a combat row.
 */
public enum RowBuffState {
    /**
     * Represents a row with no active buff.
     */
    NONE,

    /**
     * Represents a row with the commander's horn buff active.
     */
    HORN,

    /**
     * Represents a row with the Mardroeme effect active.
     */
    MARDROEME,
}
