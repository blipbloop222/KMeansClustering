// package com.example.backend.model;

// public class Point {
//     public double[] values;
//     public int cluster;

//     public Point(double[] values) {
//         this.values = values;
//     }
// }
package com.example.backend.model;

import java.util.Arrays;
import java.util.Objects;

/**
 * Represents a point in a multi-dimensional space with input validation.
 * Ensures immutability and proper encapsulation.
 */
public final class Point {
    private final double[] coordinates;

    /**
     * Constructs a point with the given coordinates.
     * @param coordinates The coordinates of the point
     * @throws IllegalArgumentException if coordinates are null or contain NaN
     */
    public Point(double[] coordinates) {
        validateCoordinates(coordinates);
        this.coordinates = coordinates.clone(); // defensive copy
    }

    /**
     * Validates input coordinates.
     * @param coords Coordinates to validate
     * @throws IllegalArgumentException if invalid
     */
    private void validateCoordinates(double[] coords) {
        if (coords == null) {
            throw new IllegalArgumentException("Coordinates cannot be null");
        }
        for (double coord : coords) {
            if (Double.isNaN(coord)) {
                throw new IllegalArgumentException("Coordinates cannot contain NaN values");
            }
        }
    }

    /**
     * Gets a defensive copy of the coordinates.
     * @return A copy of the point's coordinates
     */
    public double[] getCoordinates() {
        return coordinates.clone();
    }

    /**
     * Gets the dimension of the point.
     * @return Number of dimensions
     */
    public int getDimension() {
        return coordinates.length;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Point point = (Point) o;
        return Arrays.equals(coordinates, point.coordinates);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(coordinates);
    }

    @Override
    public String toString() {
        return "Point{" +
               "coordinates=" + Arrays.toString(coordinates) +
               '}';
    }
}