package fr.uparis.beryllium.model;

import org.apache.commons.lang3.tuple.MutablePair;

import java.time.Duration;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.MutableTriple;

/**
 * Our Itinerary calculation class.
 * Contains the algorithm that calculates the best path between 
 * two stations (the only public method)
 * This itinerary should be unique for each two stations (distinct or not)
 * and therefore there is no need to specify these stations as attributes of this class
 */
public class Itinerary{

	/**
	 * We stock all of our stations here
	 */
	private final ArrayList<Station> allStations;

	/**
	 * This HashMap stocks the best distance, the least amount of stations and the
	 * best duration of travel for each station (compared to our fixed starting station)
	 */
	private final HashMap<Station, MutableTriple<Double,Integer,Long>> 
	distCountTimeToStart = new HashMap<>();

	/**
	 * This HashMap stocks the best neighbor of each station (and the line to take)
	 * to reach the starting point
	 */
	private final HashMap<Station, MutablePair<Station,Line>> bestPreviousNeighbors =
	new HashMap<>();

	/**
	 * Constructor for our Itinerary class
	 * 
	 * @param stations ArrayList of all our stations (in our map)
	 * @param start The starting station
	 */
	public Itinerary(ArrayList<Station> stations,Station start){
		allStations = stations;
		init(start);
	}

	/**
	 * Method used to initialize the graph of distances, station counts and time
	 * between all the stations and our starting point
	 * The three values are initizalized to the <code>MAX_VALUE</code>
	 * for all the stations except for the <code>start</> station
	 * which is obviously at 0
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
	 * by preference
	 * 
	 * @param notVisited ArrayList of stations not visited yet
	 * @param preference 0: shortest distance, 1: closest in the tree, 2: shortest time
	 * @return the closest station to our starting point
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
	 * Updates distance, count and time for the neighbors (doesn't check if better).
	 * Adds this neighbor to the bestPreviousNeighbors HashMap (replaces if exists).
	 * needs to be changed
	 * 
	 * @param station the station to verify
	 */           ///////////modifier pour pas update systematiquement
	private void updateDistCountTime(Station station){
		HashMap<Station, ArrayList<NeighborData>> neighborsOfStation =
		station.getNextStations();
		MutableTriple<Double,Integer,Long> stationDistCountTime =
		distCountTimeToStart.get(station);
		Double distStation = stationDistCountTime.getLeft();
		Integer countStation = stationDistCountTime.getMiddle();
		Long timeStation = stationDistCountTime.getRight();

		neighborsOfStation.forEach((neighborStation,nDataArray)->{
			for(NeighborData nData : nDataArray){
				Double distWeight = Double.sum(distStation,nData.getDistance());
				Integer countWeight = Integer.sum(countStation,1);
				Long timeWeight = Long.sum(timeStation,nData.getMillisDuration());
				MutableTriple<Double,Integer,Long> neighborDistCountTime =
				distCountTimeToStart.get(neighborStation);
				neighborDistCountTime.setLeft(distWeight);
				neighborDistCountTime.setMiddle(countWeight);
				neighborDistCountTime.setRight(timeWeight);
				MutablePair<Station,Line> statLine = new MutablePair<Station,Line>
				(station, nData.getLine());
				bestPreviousNeighbors.put(neighborStation,statLine);
			}
		});
	}

	/**
	 * Search for the shortest way to get from a station to another
	 * 
	 * @param start our starting station
	 * @param dest our destination station
	 * @param preference 0: shortest distance, 1: closest in the tree, 2: shortest time
	 * @return the whole path from start to destination in order (LinkedHashMap)
	 */
	public HashMap<Station,Line> shortestWay
	(Station start, Station dest, Integer preference){
		ArrayList<Station> notVisited = new ArrayList<>(allStations);
		while(notVisited.size() > 0){
			Station station = bestStationByPreference(notVisited,preference);
			if(station == null){
				notVisited.clear();
			}else{
				notVisited.remove(station);
				updateDistCountTime(station);
			}
		}

		return getShortestPath(start, dest);
	}

	private HashMap<Station, Line> getShortestPath(Station start, Station dest){
		// linkedHashmap to preserve the order of insertion
		HashMap<Station, Line> shortestPath = new LinkedHashMap<>();
		Station s = dest; // on part de dest, et on cherche start, tant que s != de start, s prend le previous
		Line l = null;
		Station before = null;
		while(s!=start){
			if(s == null) return null; // C'est un terminus, il n'y a pas de previous
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
		shortestPath.put(start,l); // premiere station
		return shortestPath;
	}

	public String showPath(HashMap<Station, Line> route) {
		StringBuilder res = new StringBuilder();
		route.forEach((station,line)->{
			res.append(station+"\n");
			res.append(line+"\n");
		});
		return res.toString();
	}

}