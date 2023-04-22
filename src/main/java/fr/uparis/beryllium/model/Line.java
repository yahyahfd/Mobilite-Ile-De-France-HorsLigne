package fr.uparis.beryllium.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.ArrayList;
import java.util.HashMap;

public class Line {

    private final String lineName;
    private final HashMap<Station, Localisation> stations = new HashMap<>();

    public Line(String name) {
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
        return (ArrayList<Station>) stations.keySet();
    }

    /**
     * Like contains method for ArrayList but with String instead of Object
     *
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
     * ...
     *
     * @param station
     * @param localisation
     */
    public void addStation(Station station, Localisation localisation) {
        if (!isIn(station.getName())) {
            stations.put(station, localisation);
        }
    }

    public String toString() {
        return getLineNameWithoutVariant();
    }
}

