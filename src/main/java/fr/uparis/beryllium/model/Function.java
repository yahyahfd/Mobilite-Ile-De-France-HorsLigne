package model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class Function {
	// all stations of the map
	ArrayList<Station> stations;
	// remember the way, witch station link to the other one
    HashMap<Station, HashMap<Station, Line>> stationBefore = new HashMap<Station, HashMap<Station,Line>> ();
    // here 1 because we don't have a real dist or time
    Integer weight = 1;
 // distance between station and starting point
    HashMap<Station, Integer> distToStart = new HashMap<>();
    
    public Function(ArrayList<Station> stations) {
    	this.stations = stations;
    }
    
    /**
     * 
     * @param graph
     * @param start
     * initialize the graph of the distance between a station and the starting point
     */
    public void init(/*HashMap<Station, Integer> graph,*/ Station start) {
    	// for each station, initialize dist to infinite
    	for(Station s : stations) {
    		distToStart.put(s,Integer.MAX_VALUE);
    	}
    	// except for start where dist from start is 0
    	distToStart.put(start, 0);
    }
    
    /**
     * 
     * @param notVisited
     * @return the nearest station to starting point
     */
    public Station shortestDist(ArrayList<Station> notVisited){
    	Integer min = Integer.MAX_VALUE;
    	Station station = null;
    	// for all station not yet visited
    	for(Station s : notVisited) {
    		if(distToStart.get(s) < min) {
    			min = distToStart.get(s);
    			station = s;
    		}
    	}
    	return station;
    }
    
    /**
     * 
     * @param s1
     * @param l
     * @param s2
     * update dist between s1 and s2 : which station is the most appropriate
     */
    public void updateDist(Station s1, Line l, Station s2) {
    	// if dist to s1 is shortest than dist to s2
    	System.out.println("s2 "+distToStart.get(s2)+"> s1:"+(distToStart.get(s1)+1));
    	if(distToStart.get(s2) > distToStart.get(s1)+weight) { 
    		distToStart.put(s2, (distToStart.get(s1)+weight));
    		System.out.println("on update dist de "+s2.name+" = "+distToStart.get(s2));
    		System.out.println("STATION BEFORE "+s2.name+" is "+s1.name+" avec la ligne "+l.name);
    		// we memorize the way : station before s2 is : s1, with the line
    		HashMap<Station, Line> statLine = new HashMap<>();
    		statLine.put(s1,l);
    		stationBefore.put(s2, statLine);
    	}
    }
	
    /**
     * 
     * @param map
     * @param start
     * @param dest
     * @return a list of all the station to go from start to dest in order
     */
	public HashMap<Station, Line> dijsktra(/*HashMap<Station, Integer> graph,*/ Station start, Station dest) {
		// initialize the map (graph)
		init(/*graph,*/ start);
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
				System.out.println("la station la plus proche est "+s1.name);
				// we remove the station from the list
				allStations.remove(s1);
				Map<Station,ArrayList<Line>> nextStationOfs1 = s1.nextStations;
				// for all next stations of s1, we update dist
				for(Map.Entry s2 : nextStationOfs1.entrySet()) {
					System.out.println("next station of "+s1.name+" is "+((Station)s2.getKey()).name);
					ArrayList<Line> ll = new ArrayList<>();
					ll = (ArrayList<Line>) s2.getValue();
					// if we can stay in the same line for the next station, we take it, else, we take the first line of the list
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
		// link hashmap to preserve the order of insertion
		HashMap<Station, Line> shortestPath = new LinkedHashMap<>();
		Station s = dest;
		Line l = null;
		Station before = null;
		System.out.println("stationBefore "+stationBefore.size());
		while(s != start) {
			if(s == null) {
				return null;
			}
			System.out.println("station s "+s.name);
			for(Map.Entry sb : stationBefore.entrySet()) {
				if(( (Station) sb.getKey() ) == s) {
					HashMap<Station, Line> statL = new HashMap<>();
					statL = (HashMap<Station, Line>) sb.getValue();
					for(Map.Entry sl: statL.entrySet()) {
						l = (Line) sl.getValue();
						System.out.println("on prend la ligne : "+l.name);
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
