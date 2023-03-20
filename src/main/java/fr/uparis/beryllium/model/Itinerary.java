package fr.uparis.beryllium.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class Itinerary {
	// all stations of the map
	ArrayList<Station> stations;
	// remember the way, witch station is link to the other one
    HashMap<Station, HashMap<Station, Line>> stationBefore = new HashMap<Station, HashMap<Station,Line>> ();
    // here 1 because we don't have a real dist or time
    Integer weight = 1;
 	// distance between a station and the starting point
    HashMap<Station, Integer> distToStart = new HashMap<>();
    
    public Itinerary(ArrayList<Station> stations) {
    	this.stations = stations;
    }
    
    /**
     * Initialize the graph of the distance between a station and the starting point
     * @param start Station from where we start
     */
    public void init(Station start) {
    	// for each station, initialize dist to infinite
    	for(Station s : stations) {
    		distToStart.put(s,Integer.MAX_VALUE);
    	}
    	// except for start where dist from start is 0
    	distToStart.put(start, 0);
    }
    
    /**
     * Search in all stations not visited the nearest station from the start 
     * @param notVisited
     * @return the nearest station to starting point
     */
    public Station shortestDist(ArrayList<Station> notVisited){
    	Integer min = Integer.MAX_VALUE;
    	Station station = null;
    	// for all station not yet visited
    	for(Station s : notVisited) {
			// we keep the nearest station
    		if(distToStart.get(s) < min) {
    			min = distToStart.get(s);
    			station = s;
    		}
    	}
    	return station;
    }
    
    /**
     * Update distance between s1 and s2 : which station is the most appropriate
     * @param s1 Station
     * @param l Line of the station
     * @param s2 Station
     */
    public void updateDist(Station s1, Line l, Station s2) {
    	// if dist to s1 is shortest than dist to s2
    	if(distToStart.get(s2) > distToStart.get(s1)+weight) { 
    		distToStart.put(s2, (distToStart.get(s1)+weight));
    		// we memorize the way : station before s2 is : s1, with the line
    		HashMap<Station, Line> statLine = new HashMap<>();
    		statLine.put(s1,l);
    		stationBefore.put(s2, statLine);
    	}
    }
	
    /**
     * Search for the shortest way to go from a station to another
     * @param start Station from where we start
     * @param dest Station of destination
     * @return all stations and lines from start to destination
     */
	public HashMap<Station, Line> shortestWay(Station start, Station dest) {
		// initialize the map
		init(start);
		// all stations of the map
		ArrayList<Station> allStations = new ArrayList<>();
		allStations.addAll(stations);
		Station s1 = null;
		Line lineAlreadyUse = null;
		Boolean stayOnline = false;
		// while allstation is not empty
		while(allStations.size() > 0) {
			// we get the min of all stations
			s1 = shortestDist(allStations);
			// the remaining stations are not reachable
			if(s1 == null) {
				allStations.removeAll(allStations);
				System.out.println("stations not reachable");
			}else {
				// we remove the station from the list
				allStations.remove(s1);
				//Map<Station,ArrayList<Line>> nextStationOfs1 = s1.getNextStations();
				Map<Station,ArrayList<NeighborData>> nextStationOfs1 = s1.getNextStations();

				// for all next stations of s1, we update the distance
				for(Map.Entry s2 : nextStationOfs1.entrySet()) {
					ArrayList<Line> ll = new ArrayList<>();
					ll = (ArrayList<Line>) s2.getValue();
					// if we can stay on the same line for the next station, we take it, else, we take the first line of the list
					for(Line l : ll) {
						if(l == lineAlreadyUse) {
							stayOnline = true;
						}
					}
					if(stayOnline) {
						updateDist(s1, lineAlreadyUse, (Station) s2.getKey());
					}else {
						updateDist(s1, ll.get(0), (Station) s2.getKey());
						lineAlreadyUse = ll.get(0);
					}
				}
			}
		}
		// linkedHashmap to preserve the order of insertion
		HashMap<Station, Line> shortestPath = new LinkedHashMap<>();
		Station s = dest;
		Line l = null;
		Station before = null;
		// we save the path from dest to start
		while(s != start) {
			if(s == null) {
				return null;
			}
			// for each station, we get the station before and the line between the stations
			for(Map.Entry sb : stationBefore.entrySet()) {
				if(( (Station) sb.getKey() ) == s) {
					HashMap<Station, Line> statL = new HashMap<>();
					statL = (HashMap<Station, Line>) sb.getValue();
					for(Map.Entry sl: statL.entrySet()) {
						l = (Line) sl.getValue();
						before = (Station) sl.getKey();
					}
				}
			}
			// we add the station at the begining of the list
			shortestPath.put(s,l);
			// we follow the path
			s = before;
		}
		// add first station 
		shortestPath.put(start,null);
		return shortestPath;
	}

}
