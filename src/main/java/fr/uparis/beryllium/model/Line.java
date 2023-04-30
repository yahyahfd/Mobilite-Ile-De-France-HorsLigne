package fr.uparis.beryllium.model;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;

/**
 * Our line class. Used to stock a line object that links
 * a station to its neighbor.
 */
public class Line {

    /**
     * Name of the lane: "number.variant".
     * 
     * @see Parser#readMap(String) details on syntax here
     */
    private String lineName;


    private LinkedList<Station> stations = new LinkedList<>();
    private HashMap<Station, ArrayList<LocalTime>> stationsTimes = new LinkedHashMap<>();

    /**
     * Constructor of a line.
     * 
     * @param name line name
     */
    public Line(String name) {
        lineName = name;
    }

    /**
     * Getter for lineName.
     * 
     * @return <code>lineName</code>
     */
    public String getName() {
        return lineName;
    }

    public LinkedList<Station> getStations() {
        return stations;
    }

    public String getLineNameWithoutVariant() {

        return (!lineName.equals("--MARCHE--")) ? lineName.split("\\.")[0] : "--MARCHE--";
    }

    /**
     * Method used to return the linename without the variant.
     * Example: "1.1" -> "1".
     * 
     * @return lineName without variant name
     */
    public String toString() {
        return lineName.split("\\.")[0];
    }

    /**
     * Check if a station is already in the line
     * @param name
     * @return
     */
    private boolean isIn(String name) {
        for (Station station : stations) {
            if (station.getName().equals(name)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Add a station to the line stations list if it is not already in
     * @param station
     */
    public void addStation(Station station) {
        if (!isIn(station.getName())) {
            stations.add(station);
        }
    }

    public HashMap<Station, ArrayList<LocalTime>> getStationsTimes() {
        return stationsTimes;
    }

    public ArrayList<LocalTime> getStationTimes(Station station) {
        return stationsTimes.get(station);
    }


    /**
     * Add a time to a station in the line stations times list
     * @param station
     * @param time
     */
    public void addStationTime(Station station, LocalTime time) {
        if (stationsTimes.containsKey(station)) {
            stationsTimes.get(station).add(time);
        } else {
            ArrayList<LocalTime> times = new ArrayList<>();
             times.add(time);
            stationsTimes.put(station, times);
        }
        Collections.sort(stationsTimes.get(station));
    }

    public LocalTime getNextTrainTime(Station station, LocalTime time) {
        // there is no horaire for this station on the given line
        ArrayList<LocalTime> times = stationsTimes.get(station);
        if(times == null){
            return null;
        }
        for (LocalTime localTime : times) {
            if (localTime.equals(time) || localTime.isAfter(time)) {
                return localTime;
            }
        }
        return null;
    }



}

