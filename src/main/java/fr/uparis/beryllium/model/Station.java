package fr.uparis.beryllium.model;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class Station {
    private String name;
    private Localisation usedLocalisation;

    private HashMap<String,Localisation> localisations = new HashMap<>();
    @JsonIgnore
    private Map<Station,ArrayList<NeighborData>> nextStations = new HashMap<>();

    /**
     * All neighboring lines
     * @return all lines of current station to reach a neighbor station
     */
    public ArrayList<String> getNeighboringLines(){
        ArrayList<String> result = new ArrayList<>();
        nextStations.forEach((station, neighborDataList) -> {
            for(NeighborData nd:neighborDataList){
                String lineName = nd.getLine().getLineNameWithoutVariant();
                if(!result.contains(lineName)){
                    result.add(lineName);
                }
            }
        });
        return result;
    }

    /**
     * Neighboring stations or stations reached 
     * directly after the current one (this)
     */
    Station(String n, Localisation localisation, String lineNumber) {
        name = n;
        //localisation = localisation;
        localisations.put(lineNumber, localisation);
    }

    public String getName() {
        return name;
    }

    public Map<Station, ArrayList<NeighborData>> getNextStations() {
        return nextStations;
    }

    public Localisation getLocalisation() {
        return usedLocalisation;
    }

    public void setLocalisation(Localisation localisation) {
        this.usedLocalisation = localisation;
    }

    public HashMap<String,Localisation> getLocalisations() {
        return localisations;
    }

    public void setLocalisations(HashMap<String,Localisation> localisations) {
        this.localisations = localisations;
    }

    public boolean hasThisLocalisation(Localisation localisation){
        for (Localisation localisationI : localisations.values()){
            if(localisationI.sameLocalisation(localisation)){
                return true;
            }
        }
        return false;
    }

    public void addNextStation(Station station, Line line, String[] durationArray, Double distance) {
        Duration duration = Duration.ZERO;
        duration = duration.plusMinutes(Long.parseLong(durationArray[0]));
        duration = duration.plusSeconds(Long.parseLong(durationArray[1]));
        NeighborData n = new NeighborData(line, duration, distance);

        if (nextStations.containsKey(station)) {
            ArrayList<NeighborData> neighborDataArrayList = nextStations.get(station);
            if (!neighborDataIsIn(neighborDataArrayList, duration, line, distance)) {
                neighborDataArrayList.add(n);
            }
        } else {
            ArrayList<NeighborData> tmp = new ArrayList<>();
            tmp.add(n);
            nextStations.put(station, tmp);
        }
    }

    //Check if the Neighbor doesn't exist in the list for nextStations
    public boolean NeighborDataIsIn(ArrayList<NeighborData> list, Duration duration, Line l, Double dist) {
        for (NeighborData n : list) {
            if (n.getDuration().equals(duration) && n.getDistance().equals(dist) && n.getLine() == l) return true;
        }
        return false;
    }

    /**
     * TODO explication de fonction
     *
     * @param reachable the station we wanted to reach
     * @param radius    the radius we wanted to be in
     * @return a boolean that determines if the station is radius km away or less
     */
    public boolean isWithinARadius(Station reachable, int radius) {
        double distance = getDistanceToAStation(reachable);
        return distance < radius;
    }

    /**
     * Calculate the distance to the other station
     *
     * @param station the station we wanted to reach
     * @return the distance between our station and the other station in km
     */
    private double getDistanceToAStation(Station station) {
        Localisation localisationStart = station.localisation;
        Localisation reacheableLocalisation = this.localisation;

        double x0 = localisationStart.getLongitude() * 111;
        double y0 = localisationStart.getLatitude() * (111.11 * Math.cos(Math.toRadians(localisationStart.getLongitude())));
        double x = reacheableLocalisation.getLongitude() * 111;
        double y = reacheableLocalisation.getLatitude() * (111.11 * Math.cos(Math.toRadians(reacheableLocalisation.getLongitude())));

        return Math.sqrt(Math.pow(x - x0, 2.0) + Math.pow(y - y0, 2.0));
    }

    /**
     * TODO explication de fonction
     *
     * @param station
     * @return
     */
    public double[] getDistanceAndTimeFromAStation(Station station) {
        double[] data = new double[2];
        double distance = getDistanceToAStation(station);

        data[0] = distance;
        data[1] = getWalkingTimeInSecondsFromADistance(distance);
        return data;
    }

    /**
     * Calculate the time it takes to cover a distance
     *
     * @param distance the distance between 2 stations in km
     * @return the time it takes to cover the distance
     */
    private double getWalkingTimeInSecondsFromADistance(double distance) {
        double speedHumanWalk = 8.0;
        double timeHumanWalk = 3600;

        return (distance * timeHumanWalk) / speedHumanWalk;
    }

    /**
    //  * Returns the station's name and localisation.
    //  * @return <code>name</code> <code>localisation</code>
    //  * @see Localisation
    //  */
     public String toString(){
         return getName();
     }
}

