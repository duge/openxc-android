package com.openxc.units;

/**
 * A Level is a step in an arbitrary range of numerical values.
 *
 * e.g. my World of Warcraft character is Level 46.
 */
public class Level extends Quantity<Double> {

    public Level(double value) {
        super(value);
    }
}
