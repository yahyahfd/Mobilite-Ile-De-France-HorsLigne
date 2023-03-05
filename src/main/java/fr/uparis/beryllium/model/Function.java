package model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Function {
	ArrayList<Station> stations;
	HashMap<Station, Integer> graph;
    HashMap<Station, Station> stationBefore = new HashMap<Station, Station> ();
    Integer weight = 1; // here 1 because we don't have a real dist or time
    HashMap<Station, Integer> distToStart = new HashMap<>();
    
    public Function(ArrayList<Station> stations) {
    	this.stations = stations;
    }
    
    /**
     * 
     * @param graph
     * @param start
     * initialize the graph
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
     * @return the nearest station
     */
    public Station shortestDist(ArrayList<Station> notVisited, Station start){
    	Integer min = Integer.MAX_VALUE;
    	Station station = null;
    	// for all station not yet visited
    	for(Station s : notVisited) {
    		System.out.println("station not visited "+s.name);
    		// we memorize the nearest one
    		System.out.println("dist station "+s.nextStations.get(start)+" min "+min);
    		if(distToStart.get(s) < min) {
    			min = distToStart.get(s);
    			station = s;
    		}
    	}
    	System.out.println("shortest dist "+station.name);
    	return station;
    }
    
    /**
     * 
     * @param s1
     * @param s2
     * update dist between s1 and s2 : which station is the most appropriate
     */
    public void updateDist(Station s1, Station s2) {
    	// if dist to s1 is shortest than dist to s2
    	System.out.println("s2 "+distToStart.get(s2)+"> s1:"+(distToStart.get(s1)+1));
    	if(distToStart.get(s2) > distToStart.get(s1)+1) { 
    		distToStart.put(s2, (distToStart.get(s1)+1));
    		System.out.println("on update dist de "+s2.name+" = "+distToStart.get(s2));
    		System.out.println("STATION BEFORE "+s2.name+" is "+s1.name);
    		// we memorize the way : station before s2 is s1
    		stationBefore.put(s2, s1);
    	}
    }
	
    /**
     * 
     * @param map
     * @param start
     * @param dest
     * @return a list of all the station to go from start to dest in order
     */
	public ArrayList<Station> dijsktra(/*HashMap<Station, Integer> graph,*/ Station start, Station dest) {
		// initialize the map (graph)
		init(/*graph,*/ start);
		ArrayList<Station> allStations = new ArrayList<>();
		allStations.addAll(stations);
		Station s1 = null;
		// while allstation is not empty
		while(allStations.size() > 0) {
			System.out.println("allstation.size "+allStations.size());
			// we get the min of all stations
			s1 = shortestDist(allStations, start);
			System.out.println("la station la plus proche est "+s1.name);
			// we remove the station from the list
			allStations.remove(s1);
			Map<Station,Integer> nextStationOfs1 = s1.nextStations;
			// for all next stations of s1, we update dist
			for(Map.Entry s2 : nextStationOfs1.entrySet()) {
				System.out.println("next station of "+s1.name+" are "+((Station)s2.getKey()).name);
				updateDist(s1, (Station) s2.getKey());
			}
		}
		ArrayList<Station> shortestPath = new ArrayList<>();
		Station s = dest;
		System.out.println("stationBefore "+stationBefore.size());
		while(s != start) {
			System.out.println("station s "+s.name);
			System.out.println("station before "+s.name+" is "+stationBefore.get(s).name);
			// we add the station at the begining of the list
			shortestPath.add(0,s);
			System.out.println("shortestPath size "+shortestPath.size());
			System.out.println("shortestPath elt "+shortestPath.get(0));
			// we follow the path
			s = stationBefore.get(s);
		}
		// add first station 
		shortestPath.add(0,start);
		return shortestPath;
	}

}
