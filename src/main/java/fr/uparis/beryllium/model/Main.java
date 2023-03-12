package model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Main {

	public static void main(String[] args) {
		// create stations
		Station a = new Station("A", new HashMap<>());
		Station b = new Station("B", new HashMap<>());
		Station c = new Station("C", new HashMap<>());
		Station d = new Station("D", new HashMap<>());
		Station e = new Station("E", new HashMap<>());
		Station f = new Station("F", new HashMap<>());
		// create line and add to it the stations
		ArrayList<Station> stations = new ArrayList<>();
		stations.add(a);
		stations.add(b);
		stations.add(c);
		stations.add(d);
		stations.add(e);
		stations.add(f);
		Line line1 = new Line("1",stations);
		Line line2 = new Line("2",stations);

		ArrayList<Line> lines1 = new ArrayList<>();
		lines1.add(line1);
		
		ArrayList<Line> lines2 = new ArrayList<>();
		lines2.add(line2);
		
		ArrayList<Line> lines12 = new ArrayList<>();
		lines12.add(line1);
		lines12.add(line2);
		// add next stations
		a.nextStations.put(b,lines2);
		a.nextStations.put(c,lines1);
		b.nextStations.put(a,lines2);
		b.nextStations.put(c,lines1);
		b.nextStations.put(d,lines2);
		c.nextStations.put(a,lines1);
		c.nextStations.put(b,lines1);
//		c.nextStations.put(f,lines2);
		d.nextStations.put(b,lines2);
//		d.nextStations.put(e,lines2);
		e.nextStations.put(f,lines12);
//		e.nextStations.put(d,lines2);
//		f.nextStations.put(c,lines2);
		f.nextStations.put(e,lines12);
		Function function = new Function(stations);
		Station start = c;
		Station dest = d;
		HashMap<Station, Line> res = function.dijsktra(/*map,*/ start, dest);
		ArrayList<Station> stationRes = new ArrayList<>();
		ArrayList<Line> lineRes = new ArrayList<>();
		if(res == null) {
			System.out.println("Il n'existe aucun chemin");
		}else {
			// pour remettre dans le bon sens si c'est dans le mauvais
			for(Map.Entry r : res.entrySet()) {
				if((Station)r.getKey() != null)
					System.out.println("station "+((Station)r.getKey()).name);
				if((Line)r.getValue() != null)
					System.out.println("line "+((Line)r.getValue()).name);
				stationRes.add(0,(Station)r.getKey());
				lineRes.add(0,(Line)r.getValue());
			}
			// afficher le chemin du depart jusqu'a dest 
			int i=0;
			while(i<stationRes.size()) {
				if(lineRes.get(i) != null) {
					System.out.println("  |");
					System.out.println("line "+lineRes.get(i).name);
					System.out.println("  |");
				}
				System.out.println("  "+stationRes.get(i).name);
				i++;
			}
		}
	}

}
