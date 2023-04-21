package fr.uparis.beryllium.model;

import org.apache.commons.lang3.tuple.MutablePair;

import java.time.Duration;
import java.util.Map;
import java.util.*;

public class Itinerary {

	private final ArrayList<Station> stations;
	private final HashMap<Station, HashMap<Station, Line>> stationBefore = new HashMap<>();
	private HashMap<Station, MutablePair<Double, Double>> distTimeToStart = new HashMap<>();

	public Itinerary(ArrayList<Station> stations) {
		this.stations = stations;
	}

	public HashMap<Station, MutablePair<Double, Double>> getDistTime() {
		return distTimeToStart;
	}

	public void setDistTimeToStart(HashMap<Station, MutablePair<Double, Double>> distTimeToStart) {
		this.distTimeToStart = distTimeToStart;
	}

	/**
	 * Initialize the graph of the distance and time between a station and the starting point
	 *
	 * @param start Station from where we start
	 */
	public void init(Station start) {
		// for each station, initialize dist and time to infinite
		for (Station s : stations) {
			MutablePair<Double, Double> mp = new MutablePair<>(Double.MAX_VALUE, Double.MAX_VALUE);
			distTimeToStart.put(s, mp);
		}
		// except for start where dist adn time from start is 0
		MutablePair<Double, Double> mp = new MutablePair<>(0.0, 0.0);
		distTimeToStart.put(start, mp);
	}

    /**
	 * Search in all stations didn't visit the nearest station from the start
	 *
	 * @param notVisited
	 * @return the nearest station to starting point
	 */
	public Station shortestDist(ArrayList<Station> notVisited, int preference) {
		Double min = Double.MAX_VALUE;
		Station station = null;
		Double dist;
		Double time;
		// for all stations not yet visited
		for (Station s : notVisited) {
			// we keep the nearest station
			MutablePair<Double, Double> distTime = distTimeToStart.get(s);
			// we get the time and the dist
			dist = distTime.getLeft();
			time = distTime.getRight();
			// depending on the preference, we get the next nearest station...
			switch (preference) {
				case 0, 2 -> {
					if (dist < min) {
						min = dist;
						station = s;
					}
				}
				case 1 -> {
					if (time < min) {
						min = time;
						station = s;
					}
				}
				default -> {}
			}
		}
		return station;
	}

	/**
	 * Update distance and time between s1 and s2: which station is the most appropriate
	 *
	 * @param s1 Station start
	 * @param s2 Station dest
	 * @param n NeighborData all information of s2
	 * @param preference Integer depending on the choice of the user: 0=shortest dist / 1=shortest time
	 */
	public void updateDist(Station s1, Station s2, NeighborData n, Integer preference) {
		// we get all the time and dist of the two stations
		Double weight, dist1, dist2, time1, time2;
		MutablePair<Double, Double> distTimeS1 = distTimeToStart.get(s1);
		MutablePair<Double, Double> distTimeS2 = distTimeToStart.get(s2);
		MutablePair<Double, Double> distTime = new MutablePair<>();
		// we get the time and the dist of s1
		dist1 = distTimeS1.getLeft();
		time1 = distTimeS1.getRight();
		// we get the time and the dist of s2
		dist2 = distTimeS2.getLeft();
		time2 = distTimeS2.getRight();
		switch (preference) {
			case 0, 2 -> {
				weight = n.getDistance();
				if (dist2 > Double.sum(dist1, weight)) {
					HashMap<Station, Line> statB = stationBefore.get(s2);
					if ((statB == null) || (!n.getLine().getName().equals("--MARCHE--"))) {
						distTime.setLeft(Double.sum(dist1, weight));
						distTime.setRight(Double.sum(time1, (double) n.getDuration().toMillis()));
						distTimeToStart.put(s2, distTime);
						HashMap<Station, Line> statLine = new HashMap<>();
						statLine.put(s1, n.getLine());
						stationBefore.put(s2, statLine);
					}
				}
			}
			case 1 -> {
				weight = (double) n.getDuration().toSeconds();
				if (time2 > Double.sum(time1, weight)) {
					HashMap<Station, Line> statB = stationBefore.get(s2);
					if ((statB == null) || (!n.getLine().getName().equals("--MARCHE--"))) {
						distTime.setLeft(Double.sum(dist1, n.getDistance()));
						distTime.setRight(Double.sum(time1, weight));
						distTimeToStart.put(s2, distTime);
						HashMap<Station, Line> statLine = new HashMap<>();
						statLine.put(s1, n.getLine());
						stationBefore.put(s2, statLine);
					}
				}
			}
			default -> {
			}
		}
	}

	/**
	 * Search for the shortest way to go from a station to another
	 *
	 * @param start Station from where we start
	 * @param dest Station of destination
	 * @param preference Integer : 0 = shortest distance / 1 = shortest time / 2 = unitaire
	 * @return all stations and lines from start to destination
	 */
	public HashMap<Station, Line> shortestWay(Station start, Station dest, Integer preference) {
		// initialize the map
		init(start);
		// all stations of the map
		ArrayList<Station> allStations = new ArrayList<>(stations);
		Station s1;
		// while allstation is not empty
		while(allStations.size() > 0) {
			// we get the min of all stations
			s1 = shortestDist(allStations, preference);
			// the remaining stations are not reachable
			if (s1 == null) {
				allStations.clear();
			} else {
				// we remove the station from the list
				allStations.remove(s1);
				Map<Station,ArrayList<NeighborData>> nextStationOfs1 = s1.getNextStations();
				// for all next stations of s1, we update the distance
				for (Map.Entry<Station, ArrayList<NeighborData>> entry : nextStationOfs1.entrySet()) {
					// we get all information of the neighbors
					Station s2 = entry.getKey();
					ArrayList<NeighborData> neighbors = entry.getValue();
					// if we can stay on the same line for the next station, we take it, else, we take the first line of the list
					for (NeighborData n : neighbors) {
						updateDist(s1, s2, n, preference);
					}
				}
			}
		}
		return getShortestPath(start, dest);
	}

	/**
	 * ...
	 *
	 * @param start
	 * @param dest
	 * @return
	 */
	private HashMap<Station, Line> getShortestPath(Station start, Station dest) {
		// linkedHashmap to preserve the order of insertion
		HashMap<Station, Line> shortestPath = new LinkedHashMap<>();
		Station s = dest;
		Line l = null;
		Station before = null;
		// we save the path from dest to start
		while (s != start) {
			if (s == null) {
				return null;
			}
			// for each station, we get the station before and the line between the stations
			for (Map.Entry<Station, HashMap<Station, Line>> sb : stationBefore.entrySet()) {
				if ((sb.getKey()) == s) {
					HashMap<Station, Line> statL;
					statL = sb.getValue();
					for (Map.Entry<Station, Line> sl : statL.entrySet()) {
						l = sl.getValue();
						before = sl.getKey();
					}
				}
			}
			s.setLocalisation(s.getLocalisations().get(l.getLineNameWithoutVariant()));
			// we add the station at the beginning of the list
			shortestPath.put(s, l);
			// we follow the path
			s = before;
		}
		// add first station
		start.setLocalisation(start.getLocalisations().get(l.getLineNameWithoutVariant()));
		shortestPath.put(start,l);
		return shortestPath;
	}

	/**
     * Show the shortest way to go from a station to another
	 *
     * @param res Res of the algorithm
     * @return a string of the stations and line in order
     */
	public String showPath(HashMap<Station, Line> res) {
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
			while (i < stationRes.size()) {
				if (i == 0 ) {
					MutablePair<Double, Double> distTimeFromDestination = distTimeToStart.get(stationRes.get(stationRes.size() - 1));
					Duration d = Duration.ZERO;
					d = d.plusSeconds((long) (distTimeFromDestination.getRight() - 0));
					path.append(yellow_bold).append("-- Trajet :     ").append(d.toMinutes() + 1).append("min. ").append(normalColor).append("~ ").append(yellow_bold).append(distTimeFromDestination.getLeft()).append("km.\n");
					distTime = getDistTimeForALine(stationRes, lineRes, i);
					path.append(purple_bold).append("Ligne ").append(lineRes.get(1).getName()).append(": ").append("     ").append(yellow_bold).append(distTime.getRight()).append("min. ").append(normalColor).append("~ ").append(yellow_bold).append(distTime.getLeft()).append("km.\n");
					path.append(purple_bold).append("|     ").append(blue_bold).append(stationRes.get(i).getName()).append("\n");
				} else {
					if (i != 1) {
						if (lineRes.get(i) != lineRes.get(i - 1) && lineRes.get(i) != null) {
							MutablePair<Double, Long> tempDistTime = getDistTimeForALine(stationRes, lineRes, i - 1);
							path.append(purple_bold).append("Ligne ").append(lineRes.get(i).getName()).append(": ").append("     ").append(yellow_bold).append(tempDistTime.getRight()).append("min. ").append(normalColor).append("~ ").append(yellow_bold).append(tempDistTime.getLeft()).append("km.\n");
							path.append(purple_bold).append("|     ").append(blue_bold).append(stationRes.get(i - 1).getName()).append("\n");
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

		MutablePair<Double, Double> distTimeStart = new MutablePair<>(0.0, 0.0);
		if (position != 0) {
			distTimeStart = distTimeToStart.get(stationRes.get(position));
		}
		int tempPos = position + 2;
		while (tempPos < stationRes.size()) {
			if (lineRes.get(position + 1) != lineRes.get(tempPos)) break;
			tempPos++;
		}
		MutablePair<Double, Double> distTimeDestination = distTimeToStart.get(stationRes.get(tempPos - 1));
		MutablePair<Double, Long> result = new MutablePair<>();
		result.setLeft(distTimeDestination.getLeft() - distTimeStart.getLeft());
		Duration d = Duration.ZERO;
		d = d.plusSeconds((long) (distTimeDestination.getRight() - distTimeStart.getRight()));
		result.setRight(d.toMinutes() + 1); // 40s => 1min pour arrondir
		return result;
	}

	/**
	 * This method is used to return an ordered list of all stations in a path
	 *
	 * @param res HashMap of the path calculated by shortestWay Method
	 * @return An ordered list of stations in the path (can be empty)
	 */
	public ArrayList<Station> getPathStations(HashMap<Station, Line> res){
		if(res == null){
			return new ArrayList<>();
		}

		ArrayList<Station> stationRes = new ArrayList<>(res.keySet());
		Collections.reverse(stationRes);

		return stationRes;
	}

}
