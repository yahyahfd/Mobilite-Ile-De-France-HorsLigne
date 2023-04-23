package fr.uparis.beryllium.model;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;

public class Map {
    private ArrayList<Line> lines = new ArrayList<>();
    private ArrayList<Station> stations = new ArrayList<>();

    public ArrayList<Line> getLines() {
        return lines;
    }

    public ArrayList<Station> getStations() {
        return stations;
    }

    public ArrayList<Station> getAllStations() {
        return this.stations;
    }

    /**
     * Return Station if exist in the list else creates and returns the new Station
     *
     * @param name
     * @param localisation
     * @param lineNumber
     * @return
     */
    public Station searchStation(String name, Localisation localisation, String lineNumber) {
        for (Station s : stations) {
            if(s.getName().equals(name)){
                s.getLocalisations().put(lineNumber, localisation);
                return s;
            }
        }

        Station newstation = new Station(name, localisation, lineNumber);
        stations.add(newstation);
        return newstation;

    }

    /**
     * ...
     *
     * @param name
     * @return
     */
    public Station searchStationByName(String name) {
        for (Station station : stations) {
            if (station.getName().equals(name)) {
                return station;
            }
        }

        return null;
    }

    /**
     * ...
     *
     * @param name the name of a line we wanted
     * @return Return the Line if exist else create and return new Line
     */
    public Line searchLine(String name) {
        for (Line l : lines) {
            if ((l.getName()).equals(name)) return l;
        }
        Line newLine = new Line(name);
        lines.add(newLine);
        return newLine;
    }

    /**
     * Method used to remove a station from the list of all stations
     *
     * @param station The station to remove from the list
     */
    public void removeStation(Station station) {
        this.stations.remove(station);
    }

    /**
     * ...
     *
     * @param latitude
     * @param longitude
     * @param name
     */
    public void addStation(Double latitude, Double longitude, String name) {
        Station s = new Station(name, new Localisation(latitude, longitude), "--MARCHE--");
        // we add the station to the list of stations
        this.stations.add(s);
    }

    /**
     * Method used in terminal mode to get all the stations with <code>name</code> as a name
     * @param name A station's name
     * @return An ArrayList of stations with the corresponding name (can be empty if no stations found)
     */
    public ArrayList<Station> getStationsByName(String name){
        ArrayList<Station> result = new ArrayList<Station>();
        for(Station s : stations){
            if(StringUtils.stripAccents(s.getName()).equalsIgnoreCase(StringUtils.stripAccents(name))) result.add(s);
        }
        return result;
    }

    /**
     * Search for all stations inbetween dist (start to dest) and add these stations as neighbors of the initial position
     *
     * @param start           The starting station (coordonnees)
     * @param addFirstStation
     * @param lStart          the localisation of the starting station
     * @param lDest           the localisation of the destination station
     */
    public void walkToBestStation(Station start, Boolean addFirstStation, Localisation lStart, Localisation lDest) {
        // we get the walking line, create it if it doesn't exist
        Line walkingLine = this.searchLine("--MARCHE--");
        // we get the distance from the starting point to the final destination (it will be our aera of search)
        double radius = start.getDistanceToAStation(lStart, lDest);
        // we add the stations that are un this perimeter as neighbors of the position
        start.addWalkingNeighbours(walkingLine, this.getAllStations(), radius, addFirstStation, lStart);
    }



    /**
     * Method used in terminal mode to get the first station with <code>name</code> as a name
     * @param name
     * @return The first station with the corresponding name (can be null if no station found)
     */
    public Station getStationByName(String name){
        for(Station s : stations){
            if(StringUtils.stripAccents(s.getName()).equalsIgnoreCase(StringUtils.stripAccents(name))) return s;
        }
        return null;
    }



}
