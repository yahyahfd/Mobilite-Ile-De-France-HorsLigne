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
 	// distance between a station and the starting point
    HashMap<Station, Double> distToStart = new HashMap<>();
    
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
    		distToStart.put(s,Double.MAX_VALUE);
    	}
    	// except for start where dist from start is 0
    	distToStart.put(start, 0.0);
    }
    
    /**
     * Search in all stations not visited the nearest station from the start 
     * @param notVisited
     * @return the nearest station to starting point
     */
    public Station shortestDist(ArrayList<Station> notVisited){
    	Double min = Double.MAX_VALUE;
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
     * Update distance +1 between s1 and s2 : which station is the most appropriate
     * @param s1 Station start
     * @param s2 Station dest
     * @param neighbor NeighborData all informations of s2
     * @param preference Integer depending on the choice of the user : 0=shortest dist / 1=shortest time / 2=unitaire
     */
    public void updateDist(Station s1, Station s2, NeighborData n, Integer preference) {
    	Double weight = null;
    	// we chose the weight depending on the preference	
    	switch(preference) {
    	case 0 : 
    		weight = n.getDistance();
    		break;
    	case 1 :
    		weight = (double) n.getDuration().toSeconds();
    		break;
    	case 2 :
    		weight = 1.0;
    		break;
    	default :
    		break;
    	}
    	// if dist to s1 is shortest than dist to s2
    	// System.out.println(distToStart.get(s2) +" "+ Double.sum(distToStart.get(s1),weight)+" "+distToStart.get(s1)+ " "+weight);
    	if(distToStart.get(s2) > Double.sum(distToStart.get(s1),weight)) { 
    		distToStart.put(s2, (Double.sum(distToStart.get(s1),weight)));
    		// we memorize the way : station before s2 is : s1, with the line
    		HashMap<Station, Line> statLine = new HashMap<>();
    		statLine.put(s1,n.getLine());
    		stationBefore.put(s2, statLine);
    		// System.out.println("stat before "+s2.getName()+" "+s1.getName());
    	}
    }
	
    /**
     * Search for the shortest way to go from a station to another
     * @param start Station from where we start
     * @param dest Station of destination
     * @param preference Integer : 0 = shortest distance / 1 = shortest time / 2 = unitaire
     * @return all stations and lines from start to destination
     */
	public HashMap<Station, Line> shortestWay(Station start, Station dest, Integer preference) {
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
				// System.out.println("stations not reachable");
			}else {
				// we remove the station from the list
				allStations.remove(s1);
				// System.out.println("neibot of "+s1.getName());
				Map<Station,ArrayList<NeighborData>> nextStationOfs1 = s1.getNextStations();
				// for all next stations of s1, we update the distance
				for(Map.Entry s2 : nextStationOfs1.entrySet()) {
					// we get all informations of the neighbors
					ArrayList<NeighborData> neighbors = (ArrayList<NeighborData>) s2.getValue();
					NeighborData neighbor = null;
					// if we can stay on the same line for the next station, we take it, else, we take the first line of the list
					for(NeighborData n : neighbors) {
						if(n.getLine() == lineAlreadyUse) {
							stayOnline = true;
							neighbor = n;
						}else {
							stayOnline = false;
						}
					}
					if(stayOnline) {
						// System.out.println("neihbor if"+neighbor.getLine().getName()+" "+neighbor.getDistance().toString()+" "+neighbor.getDuration().toString());
						updateDist(s1, (Station) s2.getKey(), neighbor, preference);
					}else {
						// System.out.println("neihbor else"+neighbors.get(0).getLine().getName()+" "+neighbors.get(0).getDistance().toString()+" "+neighbors.get(0).getDuration().toString());
						lineAlreadyUse = neighbors.get(0).getLine();
						updateDist(s1, (Station) s2.getKey(), neighbors.get(0), preference);
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
			// System.out.println("shortest path : "+s.getName()+" "+l.getName());
			// we follow the path
			s = before;
		}
		// add first station 
		shortestPath.put(start,null);
		// System.out.println("shortest path : "+s.getName()+" "+l.getName());
		return shortestPath;
	}

	/**
     * Show the shortest way to go from a station to another
     * @param res Res of the algorithm
     * @return a string of the stations and line in order
     */
	public String showPath(HashMap<Station, Line> res){
		ArrayList<Station> stationRes = new ArrayList<>();
		ArrayList<Line> lineRes = new ArrayList<>();
		String path = "";
		if(res == null) {
			path += "Il n'existe aucun chemin";
		}else {
			// pour remettre dans le bon sens si c'est dans le mauvais
			for(Map.Entry r : res.entrySet()) {
				// if((Station)r.getKey() != null)
				// 	System.out.println("print "+((Station)r.getKey()).getName());
				// if((Line)r.getValue() != null)
				// 	System.out.println("print "+((Line)r.getValue()).getName());
				stationRes.add(0,(Station)r.getKey());
				lineRes.add(0,(Line)r.getValue());
			}
			// afficher le chemin du depart jusqu'a dest 
			int i=0;
			while(i<stationRes.size()) {
				if(lineRes.get(i) != null) {
					path +="  |\n";
					path +=" "+lineRes.get(i).getName()+"\n";
					path +="  |\n";
				}
				path +=stationRes.get(i).getName()+"\n";
				i++;
			}
		}
		return path;
	}

}
