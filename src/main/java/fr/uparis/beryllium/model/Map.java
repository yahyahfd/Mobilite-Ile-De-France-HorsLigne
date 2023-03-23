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
     * Method used in terminal mode to get all the stations with <code>name</code> as a name
     * @param name A station's name
     * @return An ArrayList of stations with the corresponding name (can be empty if no stations found)
     */
    public ArrayList<Station> getStationsByName(String name){
        ArrayList<Station> result = new ArrayList<Station>();
        for(Station s : stations){
            if ((s.getName()).equals(name)) result.add(s);
        }
        return result;
    }

}
