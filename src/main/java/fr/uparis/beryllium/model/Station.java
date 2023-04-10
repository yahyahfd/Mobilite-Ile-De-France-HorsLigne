package fr.uparis.beryllium.model;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
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

    /**
     * Add station as a neighbor
     *
     * @param s    the neighbor station
     * @param l    the line with which you can reach s
     * @param h    the time with which you can reach s
     * @param dist the distance between these stations
     */
    public void addNextStation(Station station, Line line, String[] durationArray, Double distance, Boolean addWalkingNeig) {
        Duration duration = Duration.ZERO;
        // we get the duration from the csv
        if(!addWalkingNeig){
            String temps = durationArray[0].concat(String.valueOf(durationArray[1].charAt(0)));
            duration = duration.plusSeconds(Long.parseLong(temps));
            String milli = durationArray[1].substring(1);
            if(milli == ""){
                milli = "0";
            }
            duration = duration.plusMillis(Long.parseLong(milli));
        }else{
            // we get the duration from calculation when walking
            duration = duration.plusSeconds(Long.parseLong(durationArray[1]));
        }
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

    /**
     * Check if the Neighbor exist in the list for nextStations
     *
     * @param list     the list of the neighbors
     * @param duration the duration between 2 stations
     * @param l        the line used between 2 stations
     * @param dist     the distance between 2 stations
     * @return true if the neighbor already exist in the list of nextStations
     */
    public boolean neighborDataIsIn(ArrayList<NeighborData> list, Duration duration, Line l, Double dist) {
        for (NeighborData n : list) {
            if (n.getDuration().equals(duration) && n.getDistance().equals(dist) && n.getLine() == l) return true;
        }
        return false;
    }

    /**
     * Add all the walking neighbors
     *
     * @param walkingLine the walking line
     * @param allStations the list of all existing stations
     * @param radius the distance we want the neighbors to be in
     */
    public void addWalkingNeighbours(Line walkingLine, ArrayList<Station> allStations, double radius, Boolean addFirstStation) {
        List<Station> reacheable1kmStations = allStations.stream().filter(s -> s.isWithinARadius(this, radius) && !s.equals(this)).toList();
        for (Station s : reacheable1kmStations) {
            double distance = s.getDistanceToAStation(this);
            double time = s.getWalkingTimeInSecondsFromADistance(distance);
            String[] stringTime = {"0", String.valueOf(time).split("\\.")[0]};
            // we only add neighbors to the initial station, because it's temporary (we don't touch the "real" stations)
            this.addNextStation(s, walkingLine, stringTime, distance, true);
            if(!addFirstStation){
                s.addNextStation(this, walkingLine, stringTime, distance, true);
            }
        }
    }

    /**
     * Check if the station is in the radius we wanted
     *
     * @param reachable the station we wanted to reach
     * @param radius    the radius we wanted to be in (in km)
     * @return a boolean that determines if the station is in the radius away or less
     */
    public boolean isWithinARadius(Station reachable, double radius) {
        double distance = getDistanceToAStation(reachable);
        return distance <= radius;
    }

    /**
     * Calculate the distance to the other station
     * @param station the station we wanted to reach
     * @return the distance between our station and the other station in km
     */
    public double getDistanceToAStation(Station station) {
        Localisation localisationStart = station.localisation;
        Localisation reacheableLocalisation = this.localisation;
        double x0 = localisationStart.getLongitude() * 111;
        double y0 = localisationStart.getLatitude() * (111.11 * Math.cos(Math.toRadians(localisationStart.getLongitude())));
        double x = reacheableLocalisation.getLongitude() * 111;
        double y = reacheableLocalisation.getLatitude() * (111.11 * Math.cos(Math.toRadians(reacheableLocalisation.getLongitude())));
        return Math.sqrt(Math.pow(x - x0, 2.0) + Math.pow(y - y0, 2.0));
    }

    /**
     * Calculate the time it takes to cover a distance
     * @param distance the distance between 2 stations in km
     * @return the time it takes to cover the distance
     */
    private double getWalkingTimeInSecondsFromADistance(double distance) {
        double speedHumanWalk = 8.0;
        double timeHumanWalk = 3600;
        
        return (distance * timeHumanWalk) / speedHumanWalk;
    }
    
    /**
     * Remove all occurences of the temporary station which was added
     * @param walkingLine the line used between the two stations
     * @param allStations all the stations
     * @param radius distance between the two stations
     */
    public void removeWalkingNeighbours(Line walkingLine, ArrayList<Station> allStations, double radius){
        // get all stations where we added the temporary station 
        List<Station> reacheable1kmStations = allStations.stream().filter(s -> s.isWithinARadius(this, radius) && !s.equals(this)).toList();
        // for all these stations, we remove the temporary station from nextstation
        for (Station s : reacheable1kmStations) {
            s.nextStations.remove(this);
        }
    }

    /**
     * Returns the station's name and localisation.
     *
     * @return <code>name</code> <code>localisation</code>
     * @see Localisation
     **/
    public String toString() {
        return name;
    }
}
