package fr.uparis.beryllium.model;

/**
 * Our location class used to define the position of a station.
 * Contains a method to compare locations
 */
public class Location {

    /**
     * x coordinate of a location.
     */
    private final double latitude;
    /**
     * y coordinate of a location.
     */
    private final double longitude;

    /**
     * Constructor for a location.
     * 
     * @param latitude x coordinate of the location
     * @param longitude y coordinate of the location
     */
    public Location(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    /**
     * Getter for a location's latitude.
     * 
     * @return <code>latitude</code>
     */
    public double getLatitude() {
        return latitude;
    }

    /**
     * Getter for a location's longitude.
     * 
     * @return <code>longitude</code>
     */
    public double getLongitude() {
        return longitude;
    }

    /**
     * Checks if two locations have the same attributes.
     *
     * @param l the location to compare to <code>this</code>
     * @return true if both locations are the same, false otherwise
     */
    public boolean sameLocation(Location l) {
        return this.latitude == l.getLatitude() && this.longitude == l.getLongitude();
    }

    /**
     * Returns the location as follows: (latitude,longitude).
     * 
     * @return both coordinates of a location
     */
    public String toString() {
        return "(" + latitude + "," + longitude + ")";
    }

}
