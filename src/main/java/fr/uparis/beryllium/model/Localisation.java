package fr.uparis.beryllium.model;

public class Localisation {

    private final double latitude;
    private final double longitude;

    public Localisation(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

}
