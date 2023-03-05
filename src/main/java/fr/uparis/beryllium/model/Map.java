package fr.uparis.beryllium.model;
import java.util.ArrayList;
import java.util.HashMap;

public class Map {
    private ArrayList<Line> lines = new ArrayList<>();
    private ArrayList<Station> stations = new ArrayList<>();

    //Return Station if exist in the list
    //else create and return the new Station
    public Station searchStation(String name){
        for(Station s : stations){
            if ((s.getName()).equals(name)) return s;
        }
        Station newStation = new Station(name);
        stations.add(newStation);
        return newStation;
    }
    
    //Return Line if exist
    //else create and return new Line
    public Line searchLine(String name){
        for(Line l : lines){
            if((l.getName()).equals(name)) return l;
        }
        Line newLine = new Line(name);
        lines.add(newLine);
        return newLine;
    }
}
