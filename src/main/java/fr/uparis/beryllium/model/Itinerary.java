package fr.uparis.beryllium.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.lang3.tuple.MutablePair;

public class Itinerary {
	// all stations of the map
	ArrayList<Station> stations;
	// remember the way, witch station is link to the other one
    HashMap<Station, HashMap<Station, Line>> stationBefore = new HashMap<Station, HashMap<Station,Line>> ();
 	// distance ans time between a station and the starting point
    HashMap<Station, MutablePair<Double,Double>> distTimeToStart = new HashMap<>();
    
    public Itinerary(ArrayList<Station> stations) {
    	this.stations = stations;
    }

	public HashMap<Station, MutablePair<Double,Double>> getDistTime(){
		return distTimeToStart;
	}
    
    /**
     * Initialize the graph of the distance and time between a station and the starting point
     * @param start Station from where we start
     */
    public void init(Station start) {
    	// for each station, initialize dist and time to infinite
    	for(Station s : stations) {
			MutablePair<Double,Double> mp = new MutablePair<Double,Double> (Double.MAX_VALUE,Double.MAX_VALUE);
			distTimeToStart.put(s,mp);
    	}
    	// except for start where dist adn time from start is 0
		MutablePair<Double,Double> mp = new MutablePair<Double,Double> (0.0,0.0);
    	distTimeToStart.put(start, mp);
    }
    
    /**
     * Search in all stations not visited the nearest station from the start 
     * @param notVisited
     * @return the nearest station to starting point
     */
	public Station shortestDist(ArrayList<Station> notVisited, int preference){
		Double min = Double.MAX_VALUE;
		Station station = null;
		Double dist = null;
		Double time = null;
		// for all station not yet visited
		for(Station s : notVisited) {
			// we keep the nearest station
			MutablePair<Double, Double> distTime = distTimeToStart.get(s);
			// we get the time and the dist
			dist = distTime.getLeft();
			time = distTime.getRight();
			// depending on the preference, we get the next nearest station...
			switch(preference) {
				// ... in dist
				case 0 : 
				if(dist < min) {
					min = dist;
					station = s;
				}
				break;
				// ... in time
				case 1 :
				if(time < min) {
					min = time;
					station = s;
				}
				break;
				// ... in dist unitary
				case 2 : 
				if(dist < min) {
					min = dist;
					station = s;
				}
				break;
				default :
					break;
				}
			}
    	return station;
    }
    
    /**
     * Update distance and time between s1 and s2 : which station is the most appropriate
     * @param s1 Station start
     * @param s2 Station dest
     * @param neighbor NeighborData all informations of s2
     * @param preference Integer depending on the choice of the user : 0=shortest dist / 1=shortest time
     */
    public void updateDist(Station s1, Station s2, NeighborData n, Integer preference) {
		// we get all time and dist of the two stations
    	Double weight=null, dist1=null, dist2=null, time1=null, time2=null;
		MutablePair<Double, Double> distTimeS1 = distTimeToStart.get(s1);
		MutablePair<Double, Double> distTimeS2 = distTimeToStart.get(s2);
		MutablePair<Double, Double> distTime = new MutablePair<>();
		// we get the time and the dist of s1
		dist1 = distTimeS1.getLeft();
		time1 = distTimeS1.getRight();
		// we get the time and the dist of s2
		dist2 = distTimeS2.getLeft();
		time2 = distTimeS2.getRight();
		// we chose the weight depending on the preference	
		switch(preference) {
			// update time and dist if the path preference is the shortest dist 
			case 0 : 
				weight = n.getDistance();
				// if dist to s1 is shortest than dist to s2
				if(dist2 > Double.sum(dist1,weight)) {
					distTime.setLeft(Double.sum(dist1,weight));
					distTime.setRight(Double.sum(time1,(double) n.getDuration().toSeconds()));
					distTimeToStart.put(s2, distTime);
					// we memorize the way : station before s2 is : s1, with the line
					HashMap<Station, Line> statLine = new HashMap<>();
					statLine.put(s1,n.getLine());
					stationBefore.put(s2, statLine);
				}
				break;
			// update time and dist if the path preference is the shortest time 
			case 1 :
				weight = (double) n.getDuration().toSeconds();
				// if time to s1 is shortest than time to s2
				if(time2 > Double.sum(time1,weight)) {
					distTime.setLeft(Double.sum(dist1,n.getDistance()));
					distTime.setRight(Double.sum(time1,weight));
					distTimeToStart.put(s2, distTime);
					// we memorize the way : station before s2 is : s1, with the line
					HashMap<Station, Line> statLine = new HashMap<>();
					statLine.put(s1,n.getLine());
					stationBefore.put(s2, statLine);
				}
			break;
			// update time and dist if the path preference is unitary dist (+1)  
			case 2 :
				weight = 1.0;
				// if time to s1 is shortest than time to s2
				if(dist2 > Double.sum(dist1,weight)) {
					distTime.setLeft(Double.sum(dist1,weight));
					distTime.setRight(Double.sum(time1,(double) n.getDuration().toSeconds()));
					distTimeToStart.put(s2, distTime);
					// we memorize the way : station before s2 is : s1, with the line
					HashMap<Station, Line> statLine = new HashMap<>();
					statLine.put(s1,n.getLine());
					stationBefore.put(s2, statLine);
				}
			break;
			default :
				break;
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
			s1 = shortestDist(allStations, preference);
			// the remaining stations are not reachable
			if(s1 == null) {
				allStations.removeAll(allStations);
			}else {
				// we remove the station from the list
				allStations.remove(s1);
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
						updateDist(s1, (Station) s2.getKey(), neighbor, preference);
					}else {
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
			// we follow the path
			s = before;
		}
		// add first station 
		shortestPath.put(start,null);
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
				stationRes.add(0,(Station)r.getKey());
				lineRes.add(0,(Line)r.getValue());
			}
			// afficher le chemin du depart jusqu'a dest 
			int i=0;
			MutablePair<Double, Double> distTime = new MutablePair<>();
			while(i<stationRes.size()) {
				if(lineRes.get(i) != null) {
					path +="  |\n";
					path +=" "+lineRes.get(i).getName()+"\n";
					path +="  |\n";
				}
				path +=stationRes.get(i).getName()+"\n";
				distTime = distTimeToStart.get(stationRes.get(i));
				path +="dist :"+distTime.getLeft()+"m \n";
				path +="time :"+distTime.getRight()+"s \n";
				i++;
			}
		}
		return path;
	}

	/**
	 * This method is used to return an ordered list of all stations in a path
	 * @param res HashMap of the path calculated by shortestWay Method
	 * @return An ordred list of stations in the path (can be empty)
	 */
	public ArrayList<Station> getPathStations(HashMap<Station, Line> res){
		if(res == null){
			return new ArrayList<>();
		}
		return new ArrayList<>(res.keySet());
	}

}
