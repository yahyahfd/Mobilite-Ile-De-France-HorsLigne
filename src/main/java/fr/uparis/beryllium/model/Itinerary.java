package fr.uparis.beryllium.model;

import org.apache.commons.lang3.tuple.MutablePair;

import java.time.Duration;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import org.apache.commons.lang3.tuple.MutableTriple;

/**
 * Our Itinerary computing class.
 * Contains the algorithm that calculates the best path between 
 * two stations given the preference.
 * This itinerary should be unique for each two stations (distinct or not)
 * and therefore there is no need to specify these stations as attributes of this class
 */
public class Itinerary{

	/**
	 * We stock all of our stations here.
	 */
	private final ArrayList<Station> allStations;

	/**
	 * We stock the station and the horaire we took the train at this station
	 */
	private HashMap<Station, LocalTime> itineraryTimes = new LinkedHashMap<>();


	/**
	 * Getter for allStations.
	 * @return <code>allStations</code>
	 */
	public ArrayList<Station> getAllStations(){
		return allStations;
	}

	/**
	 * This HashMap stocks the best distance, the least amount of stations and the
	 * best duration of travel for each station (compared to our fixed starting station)
	 */
	private final HashMap<Station, MutableTriple<Double,Integer,Long>>
	distCountTimeToStart = new HashMap<>();

	/**
	 * Getter for distCountTimeToStart.
	 * @return <code>distCountTimeToStart</code>
	 */
	public HashMap<Station, MutableTriple<Double,Integer,Long>>
	getDistCountTimeToStart(){
		return distCountTimeToStart;
	}

	/**
	 * This HashMap stocks the best neighbor of each station (and the line to take)
	 * to reach the starting point.
	 */
	private final HashMap<Station, MutablePair<Station,Line>> bestPreviousNeighbors =
	new HashMap<>();

	/**
	 * Constructor for our Itinerary class.
	 * 
	 * @param stations ArrayList of all our stations (in our map)
	 * @param start The starting station
	 */
	public Itinerary(ArrayList<Station> stations){
		allStations = stations;
	}

	/**
	 * Method used to initialize the graph of distances, station counts and time
	 * between all the stations and our starting point.
	 * The three values are initizalized to the <code>MAX_VALUE</code>
	 * for all the stations except for the <code>start</> station
	 * which is obviously at 0.
	 * 
	 * @param start The starting station
	 */
	private void init(Station start){
		for(Station s: allStations){
			MutableTriple<Double,Integer,Long> distCountTime = 
			new MutableTriple<Double,Integer,Long>
			(Double.MAX_VALUE, Integer.MAX_VALUE, Long.MAX_VALUE);
			distCountTimeToStart.put(s, distCountTime);
		}
		MutableTriple<Double,Integer,Long> distCountTime = 
			new MutableTriple<Double,Integer,Long>
			(0.0,0 , 0L);
		distCountTimeToStart.put(start, distCountTime);
	}

	/**
	 * Method used to look for the nearest station to our starting point
	 * by preference.
	 * 
	 * @param notVisited ArrayList of stations not visited yet
	 * @param preference 0: shortest distance, 1: closest in the tree, 2: shortest time
	 * @return the closest station to our starting point
	 * @throws IllegalArgumentException when you choose a preference other than 0, 1 or 2
	 */
	private Station bestStationByPreference
	(ArrayList<Station> notVisited, int preference){
		Station resultStation = null;
		Double minDistance = Double.MAX_VALUE, dist;
		Integer minCount = Integer.MAX_VALUE, count;
		Long minTime = Long.MAX_VALUE, time;

		for(Station s : notVisited){
			MutableTriple<Double,Integer,Long> distCountTime =
			distCountTimeToStart.get(s);
			dist = distCountTime.getLeft();
			count = distCountTime.getMiddle();
			time = distCountTime.getRight();
			switch(preference){
				case 0 -> {
					if(dist < minDistance){
						minDistance = dist;
						resultStation = s;
					}
				}

				case 1 -> {
					if(count < minCount){
						minCount = count;
						resultStation = s;
					}
				}

				case 2 -> {
					if(time < minTime){
						minTime = time;
						resultStation = s;
					}
				}
				default -> {
					throw new IllegalArgumentException
					("Invalid preference value " + preference 
					+ ". It's supposed to be a value between 0 and 2");
				}
			}
		}
		return resultStation;
	}

	/**
	 * Gets the best neighbor of <code>station</code> to reach.
	 * Updates for each neighbor of <code>station</code> the best way to reach
	 * our starting point.
	 * Updates distance, count and time for the neighbors.
	 * Adds this neighbor to the bestPreviousNeighbors HashMap if it's better.
	 * 
	 * @param station the station to verify
	 * @param preference 0: shortest distance, 1: closest in the tree, 2: shortest time
	 * @throws IllegalArgumentException when you choose a preference other than 0, 1 or 2
	 */
	private void updateDistCountTime(Station station, Integer preference, LocalTime actualTime){
		HashMap<Station, ArrayList<NeighborData>> neighborsOfStation =
		station.getNextStations();
		MutableTriple<Double,Integer,Long> stationDistCountTime =
		distCountTimeToStart.get(station);
		Double distStation = stationDistCountTime.getLeft();
		Integer countStation = stationDistCountTime.getMiddle();
		Long timeStation = stationDistCountTime.getRight();

		neighborsOfStation.forEach((neighborStation,nDataArray)->{
			for(NeighborData nData : nDataArray){
				// for each neighbor, we calcuate the time to wait for the next train
				long timeToWait = 0;
				String lineName = nData.getLine().getName();
				LocalTime nextTrainTime = null;
				// if we walk, we don't have wainting time
				if(lineName != "--MARCHE--"){
					// for the next train time, we compare hour:minutes:seconds (not millis)
					nextTrainTime = neighborStation.getNextTrainTime(nData.getLine(), actualTime.withNano(0));
					// ^waiting time to take the station
					if(nextTrainTime != null){
						timeToWait = Duration.between(actualTime.withNano(0), nextTrainTime).toMillis();
					}else{
						// there is no horaire for this station on this line, time to wait is infinite (we can not take this station on this line)
						timeToWait = Long.MAX_VALUE;
					}
				}
				Double distWeight = Double.sum(distStation,nData.getDistance());
				Integer countWeight = Integer.sum(countStation,1);
				// time of travel between the station and the neihbor station + the time we wait for the train
				Long timeWeight = Long.MAX_VALUE;
				// time between the station + the wainting time
				Long timeBetweenStation = Long.sum(nData.getMillisDuration(),timeToWait);
				// if the waiting time is not infinite and it doesn't turn to negative, timeWeight = time to go to station + wainting time
				if(timeToWait != Long.MAX_VALUE && timeBetweenStation >= 0){
					timeWeight = Long.sum(timeStation,timeBetweenStation);
				}
				MutableTriple<Double,Integer,Long> neighborDistCountTime = distCountTimeToStart.get(neighborStation);
				boolean swap = false;
				switch(preference){
					case 0 ->{
						// even if it's the shortest dist, if we don't have any train to go there, we don't take this road
						if(distWeight<neighborDistCountTime.getLeft() && (timeToWait != Long.MAX_VALUE)) swap = true;
					}
					case 1 ->{
						if(countWeight<neighborDistCountTime.getMiddle() && (timeToWait != Long.MAX_VALUE)) swap = true;
					}
					case 2 ->{
						if(timeWeight<neighborDistCountTime.getRight()) swap = true;
					}
					default -> {
						throw new IllegalArgumentException
						("Invalid preference value " + preference 
						+ ". It's supposed to be a value between 0 and 2");
					} 
				}
				// in each case, we update the time and dist to go to the neighbor station
				if(swap){
					// for each station taken, we remember the horaire we took the train
					if(nextTrainTime != null){
						itineraryTimes.put(station,nextTrainTime);
					}
					neighborDistCountTime.setLeft(distWeight);
					neighborDistCountTime.setMiddle(countWeight);
					neighborDistCountTime.setRight(timeWeight);
					MutablePair<Station,Line> statLine = new MutablePair<Station,Line>
					(station, nData.getLine());
					bestPreviousNeighbors.put(neighborStation,statLine);
				}
			}
		});
	}

	/**
	 * Search for the shortest way to get from a station to another.
	 * 
	 * @param start our starting station
	 * @param dest our destination station
	 * @param preference 0: shortest distance, 1: closest in the tree, 2: shortest time
	 * @return the whole path from start to destination in order (LinkedHashMap), null if no path
	 */
	private HashMap<Station,Line> shortestWay
	(Station start, Station dest, Integer preference, LocalTime timeWeLeft){
		init(start);
		ArrayList<Station> notVisited = new ArrayList<>(allStations);
		LocalTime actualTime = null;
		while(notVisited.size() > 0){
			Station station = bestStationByPreference(notVisited,preference);
			if(station == null){
				notVisited.clear();
			}else{
				// actual time = time we left + time to get to the actual station
				MutableTriple<Double,Integer,Long> distTime = distCountTimeToStart.get(station);
				// duration of the travel (start to s1)
				long durationInMillis = distTime.getRight();
				// add the duration of the travel to the time we left
				actualTime = timeWeLeft.plus(Duration.ofMillis(durationInMillis));
				notVisited.remove(station);
				updateDistCountTime(station, preference, actualTime);
			}
		}

		return getShortestPath(start, dest);
	}

	/**
	 * This method is called in shortestWay to calculate the shortest path
	 * between two stations.
	 * This method uses a LinkedHashMap to preserve the order of insertion.
	 * 
	 * @param start our starting station
	 * @param dest our destination station
	 * @return the whole path from start to destination in order (LinkedHashMap), null if no path
	 */
	private HashMap<Station, Line> getShortestPath(Station start, Station dest){
		// linkedHashmap to preserve the order of insertion
		HashMap<Station, Line> shortestPath = new LinkedHashMap<>();
		Station s = dest; // on part de dest, et on cherche start, tant que s != de start, s prend le previous
		Line l = null;
		Station before = null;
		while(s!=start){
			if(s == null) return null; // 
			for(Map.Entry<Station,MutablePair<Station,Line>> sb : bestPreviousNeighbors.entrySet()){
				if(sb.getKey() == s){
					MutablePair<Station,Line> statL;
					statL = sb.getValue();
					l = statL.getRight();
					before = statL.getLeft();
				}
			}
			shortestPath.put(s,l);
			s= before;
		}
		shortestPath.put(start,l); //first station
		return shortestPath;
	}

	/**
	 * Computes the shortest path between all pairs of start and dest stations defined in
	 * the corresponding arraylists using the shortestWay algorithm on each pair:
	 * For each station in <code>start</code> and each station in <code>dest</code>, this method calculates
	 * the shortestWay between the two stations, using the corresponding method
	 * and returns the best path, depending on the <code>preference</code> specified
	 * between all the computed paths.
	 * 
	 * @param start ArrayList of starting points
	 * @param dest ArrayList of destination points	 
	 * @param preference 0: shortest distance, 1: closest in the tree, 2: shortest time
	 * @return the best path possible between a station called X and a station called Y
	 * @see Station 
	 * @throws IllegalArgumentException when you choose a preference other than 0, 1 or 2
	 */
	public HashMap<Station,Line> shortestMultiplePaths(ArrayList<Station> start, 
	ArrayList<Station> dest, Integer preference, LocalTime timeWeLeft){
		HashMap<Station,Line> res = new HashMap<>();
		Double minDist = Double.MAX_VALUE;
		Integer minCount = Integer.MAX_VALUE;
		Long minTime = Long.MAX_VALUE;
		for(Station s1: start){
			for(Station s2: dest){
				HashMap<Station, Line> tmp = shortestWay(s1, s2, preference, timeWeLeft);

				double dist = distCountTimeToStart.get(s2).getLeft();
				Integer count = distCountTimeToStart.get(s2).getMiddle();
				Long time = distCountTimeToStart.get(s2).getRight();
				switch(preference){
					case 0 -> {
						if(dist<minDist){
							minDist = dist;
							res = tmp;
						}
					}

					case 1 -> {
						if(count<minCount){
							minCount = count;
							res = tmp;
						}
					}
					case 2 -> {
						if (time < minTime) {
							minTime = time;
							res = tmp;
						}
					}
					default -> {
						throw new IllegalArgumentException
						("Invalid preference value " + preference 
						+ ". It's supposed to be a value between 0 and 2");
					}
				}
			}
		}
		return res;
	}

	// à supprimer, reverse dans la méthode de calcul plutot, et traitement dans le mapController./////////////
    /**
	 * This method is used to return an ordered list of all stations in a path
	 *
	 * @param res HashMap of the path calculated by shortestWay Method
	 * @return An ordered list of stations in the path (can be empty)
	 */
	public LinkedList<Station> getPathStations(HashMap<Station, Line> res){
		if(res == null){
			return new LinkedList<>();
		}

		LinkedList<Station> stationRes = new LinkedList<>(res.keySet());
		Collections.reverse(stationRes);

		return stationRes;
	}

	public LinkedList<Line> getPathLines(HashMap<Station, Line> res){
		if(res == null){
			return new LinkedList<>();
		}

		LinkedList<Line> lineRes = new LinkedList<>(res.values());
		Collections.reverse(lineRes);

		return lineRes;
	}

	// // à modifier/déplacer vers le terminalAPP, reverse dans la méthode de calcul plutot, et traitement dans le TERMINAL./////////////
	// public String showPath(HashMap<Station, Line> route) {
	// 	StringBuilder res = new StringBuilder();
	// 	route.forEach((station,line)->{
	// 		res.append(line+"\n");
	// 		res.append(station+"\n");
	// 	});
	// 	return res.toString();
	// }

	/**
     * Show the shortest way to go from a station to another
	 *
     * @param res Res of the algorithm
     * @return a string of the stations and line in order
     */
	public String showPath(HashMap<Station, Line> res, LocalTime timeWeLeft) {
		ArrayList<Station> stationRes = new ArrayList<>();
		ArrayList<Line> lineRes = new ArrayList<>();
		StringBuilder path = new StringBuilder();
		if (res == null) {
			path.append("Il n'existe aucun chemin");
		} else {
			// pour remettre dans le bon sens si c'est dans le mauvais
			for (Map.Entry<Station, Line> r : res.entrySet()) {
				stationRes.add(0, r.getKey());
				lineRes.add(0, r.getValue());
			}
			// afficher le chemin du depart jusqu'a dest
			int i = 0;
			String downArrow = "↓";

			String normalColor = "\033[0m";
			String blue_bold = "\033[1;34m";
			String purple_bold = "\033[1;35m";
			String yellow_bold = "\033[1;33m";
			MutablePair<Double, Long> distTime;
			path.append(blue_bold).append("Heure de départ: ").append(timeWeLeft);
			while (i < stationRes.size()) {
				if (i == 0 ) {
					MutableTriple<Double,Integer,Long> distTimeFromDestination = distCountTimeToStart.get(stationRes.get(stationRes.size() - 1));
					Duration d = Duration.ZERO;
					d = d.plusMillis((long) (distTimeFromDestination.getRight() - 0));
					path.append(yellow_bold).append("	-- Trajet :     ").append(d.toMinutes() + 1).append("min. ").append(normalColor).append("~ ").append(yellow_bold).append(distTimeFromDestination.getLeft()).append("km.\n");
					distTime = getDistTimeForALine(stationRes, lineRes, i);
					path.append(purple_bold).append("Ligne ").append(lineRes.get(1).getName()).append(": ").append("     ").append(yellow_bold).append(distTime.getRight()).append("min. ").append(normalColor).append("~ ").append(yellow_bold).append(distTime.getLeft()).append("km.\n");
					LocalTime horaire = itineraryTimes.get(stationRes.get(i));
					path.append(purple_bold).append("|     ").append(blue_bold).append(stationRes.get(i).getName());
					if(horaire != null && lineRes.get(i).getName() != "--MARCHE--"){
						path.append(" - "+horaire);
					}
					path.append("\n");
				} else {
					if (i != 1) {
						if (lineRes.get(i) != lineRes.get(i - 1) && lineRes.get(i) != null) {
							MutablePair<Double, Long> tempDistTime = getDistTimeForALine(stationRes, lineRes, i - 1);
							path.append(purple_bold).append("Ligne ").append(lineRes.get(i).getName()).append(": ").append("     ").append(yellow_bold).append(tempDistTime.getRight()).append("min. ").append(normalColor).append("~ ").append(yellow_bold).append(tempDistTime.getLeft()).append("km.\n");
							path.append(purple_bold).append("|     ").append(blue_bold).append(stationRes.get(i - 1).getName());
							LocalTime horaire = itineraryTimes.get(stationRes.get(i-1));
							if(horaire != null && lineRes.get(i).getName() != "--MARCHE--"){
								path.append(" - "+horaire);
							}
							path.append("\n");
						}
					}
					if (i + 1 < stationRes.size()) {
						if (lineRes.get(i) != lineRes.get(i + 1) && lineRes.get(i) != null) {
							path.append(purple_bold).append(downArrow).append("     ").append(blue_bold).append(stationRes.get(i).getName()).append("\n");
						} else {
							path.append(purple_bold).append("|         ").append(blue_bold).append("| ").append(normalColor).append(stationRes.get(i).getName()).append("\n");
						}
					} else if (i + 1 == stationRes.size()) {
						path.append(purple_bold).append("|     ").append(blue_bold).append(stationRes.get(i).getName()).append("\n");
					} else {
						path.append(purple_bold).append("|         ").append(blue_bold).append("| ").append(normalColor).append(stationRes.get(i).getName()).append("\n");
					}
				}
				i++;
			}
		}
		return path.toString();
	}

	/**
	 * Get distance and time traveled on a line
	 *
	 * @param stationRes list of all the station
	 * @param lineRes list of all the line
	 * @param position the position of the station in the stationRes
	 * @return a couple of distance/time which represent the distance and time to travel the line of the station at the position
	 */
	private MutablePair<Double, Long> getDistTimeForALine(ArrayList<Station> stationRes, ArrayList<Line> lineRes, int position) {
		MutableTriple<Double,Integer,Long> distTimeStart = new MutableTriple<Double,Integer,Long>(0.0, 0, (long)0.0);
		if (position != 0) {
			distTimeStart = distCountTimeToStart.get(stationRes.get(position));
		}
		int tempPos = position + 2;
		while (tempPos < stationRes.size()) {
			if (lineRes.get(position + 1) != lineRes.get(tempPos)) break;
			tempPos++;
		}
		MutableTriple<Double,Integer,Long> distTimeDestination = distCountTimeToStart.get(stationRes.get(tempPos - 1));
		MutablePair<Double, Long> result = new MutablePair<>();
		result.setLeft(distTimeDestination.getLeft() - distTimeStart.getLeft());
		Duration d = Duration.ZERO;
		d = d.plusMillis((long) (distTimeDestination.getRight() - distTimeStart.getRight()));
		result.setRight(d.toMinutes() + 1); // 40s => 1min pour arrondir
		return result;
	}
}