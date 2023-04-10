package fr.uparis.beryllium.model;

import java.util.ArrayList;
import java.util.HashMap;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class Line {

    private String lineName;
    private HashMap<Station, Localisation> stations = new HashMap<>();

    Line(String name) {
        lineName = name;
    }

    public String getLineName() {
        return lineName;
    }

    public String getLineNameWithoutVariant() {
        return lineName.split("\\.")[0];
    }

    //Like contains method for ArrayList but with String intead of Object
    private boolean isIn(String name) {
        for (Station station : stations.keySet()) {
            if (station.getName().equals(name)) {
                return true;
            }
        }
        return false;
    }

    public void addStation(Station station, Localisation localisation) {
        if (!isIn(station.getName())) {
            stations.put(station,localisation);
        }
    }

    public String getName() {
        return lineName;
    }

    public void setLineName(String lineName) {
        this.lineName = lineName;
    }

    @JsonIgnore
    public ArrayList<Station> getStations(){
        return (ArrayList<Station>) stations.keySet();
    }

    public String toString(){
         return getLineNameWithoutVariant();
    }
}

