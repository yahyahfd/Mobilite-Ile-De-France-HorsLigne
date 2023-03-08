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
		c.nextStations.put(f,lines2);
		d.nextStations.put(b,lines2);
//		d.nextStations.put(e,lines2);
		e.nextStations.put(f,lines12);
//		e.nextStations.put(d,lines2);
		f.nextStations.put(c,lines2);
		f.nextStations.put(e,lines12);
		Function function = new Function(stations);
		HashMap<Station, Line> res = function.dijsktra(/*map,*/ a, e);
		for(Map.Entry r : res.entrySet()) {
			if((Line)r.getValue() == null) {
				System.out.println("RES : "+((Station)r.getKey()).name+" Départ"); // Changer sens + écrire ligne plus clairement

			}else {
				System.out.println("RES : "+((Station)r.getKey()).name+" line"+((Line)r.getValue()).name); // faire verification ligne (prendre celle sur laquelle on est deja si elle y va
			}
		}
	}

}
