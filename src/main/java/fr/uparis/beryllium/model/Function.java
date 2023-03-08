package model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Function {
	ArrayList<Station> stations;
	HashMap<Station, Integer> graph;
    HashMap<Station, HashMap<Station, Line>> stationBefore = new HashMap<Station, HashMap<Station,Line>> ();
    Integer weight = 1; // here 1 because we don't have a real dist or time
    HashMap<Station, Integer> distToStart = new HashMap<>(); // distance between station and starting point
    
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
     * @return the nearest station to starting point
     */
    public Station shortestDist(ArrayList<Station> notVisited){
    	Integer min = Integer.MAX_VALUE;
    	Station station = null;
    	// for all station not yet visited
    	for(Station s : notVisited) {
    		System.out.println("station not visited "+s.name);
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
    public void updateDist(Station s1, Line l, Station s2) {
    	// if dist to s1 is shortest than dist to s2
    	System.out.println("s2 "+distToStart.get(s2)+"> s1:"+(distToStart.get(s1)+1));
    	if(distToStart.get(s2) > distToStart.get(s1)+weight) { 
    		distToStart.put(s2, (distToStart.get(s1)+weight));
    		System.out.println("on update dist de "+s2.name+" = "+distToStart.get(s2));
    		System.out.println("STATION BEFORE "+s2.name+" is "+s1.name);
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
			System.out.println("allstation.size "+allStations.size());
			// we get the min of all stations
			s1 = shortestDist(allStations);
			System.out.println("la station la plus proche est "+s1.name);
			// we remove the station from the list
			allStations.remove(s1);
			Map<Station,ArrayList<Line>> nextStationOfs1 = s1.nextStations;
			// for all next stations of s1, we update dist
			for(Map.Entry s2 : nextStationOfs1.entrySet()) {
				System.out.println("next station of "+s1.name+" are "+((Station)s2.getKey()).name);
				ArrayList<Line> ll = new ArrayList<>();
				ll = (ArrayList<Line>) s2.getValue();
				for(Line l : ll) { // if we can stay in the same line for the next station, we take it, else, we take the first line of the list
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
		HashMap<Station, Line> shortestPath = new HashMap<>();
		Station s = dest;
		Line l = null;
		Station before = null;
		System.out.println("stationBefore "+stationBefore.size());
		while(s != start) {
			System.out.println("station s "+s.name);
			for(Map.Entry sb : stationBefore.entrySet()) {
				if(( (Station) sb.getKey() ) == s) {
					HashMap<Station, Line> statL = new HashMap<>();
					statL = (HashMap<Station, Line>) sb.getValue();
					for(Map.Entry sl: statL.entrySet()) {
						l = (Line) sl.getValue();
						System.out.println("on prend la ligne : "+l.name);
						before = (Station) sl.getKey();
						System.out.println("statbefore : "+before.name);
					}
				}
			}
			// we add the station at the begining of the list
			shortestPath.put(s,l);
			System.out.println("shortestPath size "+shortestPath.size());
			// we follow the path
			s = before;
		}
		// add first station 
		shortestPath.put(start,null);
		return shortestPath;
	}

}
