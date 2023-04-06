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

    //Return if both localisation has same attributes
    public boolean sameLocalisation(Localisation l){
        return this.latitude == l.getLatitude() && this.longitude == l.getLongitude();
    }

    /**
     * Returns latitude and longitude in a localisation
     * @return (latitude,longitude) of localisation
     */
    public String toString(){
        return "("+latitude+","+longitude+")";
    }

}
