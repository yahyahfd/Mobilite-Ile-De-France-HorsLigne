package fr.uparis.beryllium.model;

import java.util.ArrayList;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class Line {

    private String lineName;
    private ArrayList<Station> stations = new ArrayList<>();

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
        for (Station station : stations) {
            if (station.getName().equals(name)) {
                return true;
            }
        }
        return false;
    }

    public void addStation(Station station) {
        if (!isIn(station.getName())) stations.add(station);
    }

    public String getName() {
        return lineName;
    }

    @JsonIgnore
    public ArrayList<Station> getStations(){
        return stations;
    }
}

