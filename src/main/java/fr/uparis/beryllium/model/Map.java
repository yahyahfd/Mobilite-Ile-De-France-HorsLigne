package fr.uparis.beryllium.model;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

import org.apache.commons.lang3.StringUtils;
// import java.text.Normalizer;

public class Map {
    private ArrayList<Line> lines = new ArrayList<>();
    private ArrayList<Station> stations = new ArrayList<>();

    public ArrayList<Line> getLines() {
        return lines;
    }

    public ArrayList<Station> getStations() {
        return stations;
    }

    //Return Station if exist in the list
    //else create and return the new Station
    public Station searchStation(String name, Localisation localisation, String lineNumber) {
        for (Station s : stations) {
            //if ((s.getName()).equals(name) && (s.getLocalisation()).sameLocalisation(l)) return s;
            if(s.getName().equals(name)){
                if(!s.hasThisLocalisation(localisation)) {
                    s.getLocalisations().put(lineNumber, localisation);
                }
                return s;
            }
        }

        Station newstation = new Station(name, localisation, lineNumber);
        stations.add(newstation);
        return newstation;

    }

    public Station searchStationByName(String name){
        for(Station station : stations) {
            if(station.getName().equals(name)) {
                return station;
            }
        }

        return null;
    }
    
    //Return Line if exist
    //else create and return new Line
    public Line searchLine(String name) {
        for (Line l : lines) {
            if ((l.getName()).equals(name)) return l;
        }
        Line newLine = new Line(name);
        lines.add(newLine);
        return newLine;
    }

    public ArrayList<Station> getAllStations(){
        return this.stations;
    }

    /**
     * Method used in terminal mode to get all the stations with <code>name</code> as a name
     * @param name A station's name
     * @return An ArrayList of stations with the corresponding name (can be empty if no stations found)
     */
    public ArrayList<Station> getStationsByName(String name){
        ArrayList<Station> result = new ArrayList<Station>();
        for(Station s : stations){
            // String s1 = Normalizer.normalize(s.getName(), Normalizer.Form.NFD)
            //             .replaceAll("\\p{InCombiningDiacriticalMarks}+", "")
            //             .toLowerCase();
            // String s2 = Normalizer.normalize(name, Normalizer.Form.NFD)
            //             .replaceAll("\\p{InCombiningDiacriticalMarks}+", "")
            //             .toLowerCase();
            // if (s1.equals(s2)) result.add(s);
            if(StringUtils.stripAccents(s.getName()).equalsIgnoreCase(StringUtils.stripAccents(name))) result.add(s);
        }
        return result;
    }
}
