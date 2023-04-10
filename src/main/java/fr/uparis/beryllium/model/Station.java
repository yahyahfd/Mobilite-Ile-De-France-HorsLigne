package fr.uparis.beryllium.model;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class Station {
    private String name;
    private Localisation localisation;

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
        return localisation;
    }

    public void setLocalisation(Localisation localisation) {
        this.localisation = localisation;
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
    public boolean neighborDataIsIn(ArrayList<NeighborData> list, Duration duration, Line l, Double dist) {
        for (NeighborData n : list) {
            if (n.getLine() == l) {
                return true;
            }
        }
        return false;
    }

    // /**
    //  * Returns the station's name and localisation.
    //  * @return <code>name</code> <code>localisation</code>
    //  * @see Localisation
    //  */
     public String toString(){
         return getName();
     }
}

