package fr.uparis.beryllium.model;
import java.util.ArrayList;

public class Map {
    private ArrayList<Line> lines = new ArrayList<>();
    private ArrayList<Station> stations = new ArrayList<>();

    //Return Station if exist in the list
    //else create and return the new Station
    public Station searchStation(String name,Localisation l){
        for(Station s : stations){
            if ((s.getName()).equals(name) && (s.getLocalisation()).sameLocalisation(l)) return s;
        }
        Station newStation = new Station(name,l);
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

    /**
     * Method used in terminal mode to check if a station name corresponds to an existing station
     * @param name A station's name
     * @return <code>true</code> if the station exists, <code>false</code> otherwise
     */
    public boolean checkStationExists(String name){
        for(Station s : stations){
            if ((s.getName()).equals(name)) return true;
        }
        return false;
    }
}
