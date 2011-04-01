package com.fitbit.api.common.model.units;

/**
 * User: gkutlu
 * Date: Mar 25, 2010
 * Time: 4:08:00 PM
 */
public enum MeasurementUnits {
    CM("Centimeter"),
    INCHES("Inch");

    String unit;

    MeasurementUnits(String unit) {
        this.unit = unit;
    }

    public String getUnit() {
        return unit;
    }
}