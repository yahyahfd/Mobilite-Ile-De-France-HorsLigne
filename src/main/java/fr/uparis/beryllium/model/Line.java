package fr.uparis.beryllium.model;

import java.util.ArrayList;

public class Line {

   // private String lineName;
    private String name;
    private ArrayList<Station> stations = new ArrayList<>();

    Line(String n){
        name = n;
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

    public String getName(){ return name; }
    public ArrayList<Station> getStations(){ return stations; }
}

