package fr.uparis.beryllium.model;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class Line {

    private String lineName;
    private HashMap<Station, Localisation> stations = new LinkedHashMap<>();
    private HashMap<Station, ArrayList<LocalTime>> stationsTimes = new LinkedHashMap<>();

    Line(String name) {
        lineName = name;
    }

    public String getName() {
        return lineName;
    }

    public String getLineName() {
        return lineName;
    }

    public String getLineNameWithoutVariant() {
        return lineName.split("\\.")[0];
    }

    @JsonIgnore
    public ArrayList<Station> getStations() {
        ArrayList<Station> result = new ArrayList<>();
        for (Station station : stations.keySet()) {
            result.add(station);
        }
        return result;
    }


    /**
     * Check if a station is already in the line
     * @param name
     * @return
     */
    private boolean isIn(String name) {
        for (Station station : stations.keySet()) {
            if (station.getName().equals(name)) {
                return true;
            }
        }
        return false;
    }


    /**
     * Add a station to the line stations list if it is not already in
     * @param station
     * @param localisation
     */
    public void addStation(Station station, Localisation localisation) {
        if (!isIn(station.getName())) {
            stations.put(station,localisation);
        }
    }

    public String toString() {
        return getLineNameWithoutVariant();
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
        ArrayList<LocalTime> times = stationsTimes.get(station);
        for (LocalTime localTime : times) {
            if (localTime.equals(time) || localTime.isAfter(time)) {
                return localTime;
            }
        }
        return null;
    }

}

