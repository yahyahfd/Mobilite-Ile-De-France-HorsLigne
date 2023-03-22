package fr.uparis.beryllium.model;

import java.util.ArrayList;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

public class Main {

	public static void main(String[] args) {
		Localisation loc = new Localisation(1, 2);
		// create stations
		Station a = new Station("A",loc);
		Station b = new Station("B",loc);
		Station c = new Station("C",loc);
		Station d = new Station("D",loc);
		Station e = new Station("E",loc);

		// create line and add to it the stations
		ArrayList<Station> stations = new ArrayList<>();
		stations.add(a);
		stations.add(b);
		stations.add(c);
		stations.add(d);
		stations.add(e);
		
		Line line1 = new Line("1");
		Line line2 = new Line("2");

		ArrayList<Line> lines1 = new ArrayList<>();
		lines1.add(line1);
		
		ArrayList<Line> lines2 = new ArrayList<>();
		lines2.add(line2);
		
		ArrayList<Line> lines12 = new ArrayList<>();
		lines12.add(line1);
		lines12.add(line2);
		
		String[] d1 = {"1","0"};
		String[] d2 = {"2","0"};
		String[] d3 = {"3","0"};
		
		// add next stations		
		a.addNextStation(b, line1, d1, 1.0);
		a.addNextStation(b, line2, d2, 2.0);
		a.addNextStation(c, line2, d2, 1.0);
		
		b.addNextStation(a, line1, d1, 1.0);
		b.addNextStation(a, line2, d2, 2.0);
		b.addNextStation(e, line1, d3, 3.0);
		b.addNextStation(c, line2, d2, 2.0);
		
		e.addNextStation(b, line1, d3, 1.0);
		e.addNextStation(c, line1, d2, 4.0);
		
		c.addNextStation(a, line2, d1, 1.0);
		c.addNextStation(b, line2, d3, 2.0);
		c.addNextStation(e, line1, d1, 1.0);
		c.addNextStation(d, line1, d2, 2.0);
		
		d.addNextStation(c, line1, d3, 1.0);
		
		Itinerary function = new Itinerary(stations);
		Station start = a;
		Station dest = e;
		
		HashMap<Station, Line> res = function.shortestWay(start, dest, 0);
		ArrayList<Station> stationRes = new ArrayList<>();
		ArrayList<Line> lineRes = new ArrayList<>();
		if(res == null) {
			System.out.println("Il n'existe aucun chemin");
		}else {
			// pour remettre dans le bon sens si c'est dans le mauvais
			for(Map.Entry r : res.entrySet()) {
				if((Station)r.getKey() != null)
					System.out.println("station "+((Station)r.getKey()).getName());
				if((Line)r.getValue() != null)
					System.out.println("line "+((Line)r.getValue()).getName());
				stationRes.add(0,(Station)r.getKey());
				lineRes.add(0,(Line)r.getValue());
			}
			// afficher le chemin du depart jusqu'a dest 
			int i=0;
			while(i<stationRes.size()) {
				if(lineRes.get(i) != null) {
					System.out.println("  |");
					System.out.println("line "+lineRes.get(i).getName());
					System.out.println("  |");
				}
				System.out.println("  "+stationRes.get(i).getName());
				i++;
			}
		}
	}

}
