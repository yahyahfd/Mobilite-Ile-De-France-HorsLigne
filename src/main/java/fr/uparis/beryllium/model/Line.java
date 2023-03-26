package fr.uparis.beryllium.model;

import java.util.ArrayList;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class Line {

    private String lineName;
    private ArrayList<Station> stations = new ArrayList<>();

    Line(String n){
        lineName = n;
    }

    //Like contains method for ArrayList but with String intead of Object
    private boolean isIn(String name){
        for(Station s : stations){
            if((s.getName()).equals(name)) return true;
        }
        return false;
    }

    public void addStation(Station s){
        if(!isIn(s.getName())) stations.add(s);
    }
    
    public String getName(){ return lineName; }
    @JsonIgnore
    public ArrayList<Station> getStations(){ return stations; }
}

