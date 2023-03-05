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
		Line line = new Line("1",stations);
		// add next stations
		a.nextStations.put(b,1);
		a.nextStations.put(c,1);
		b.nextStations.put(a,1);
		b.nextStations.put(c,1);
		b.nextStations.put(d,1);
		c.nextStations.put(a,1);
		c.nextStations.put(b,1);
		c.nextStations.put(f,1);
		d.nextStations.put(b,1);
		d.nextStations.put(e,1);
		e.nextStations.put(f,1);
		e.nextStations.put(d,1);
		f.nextStations.put(c,1);
		f.nextStations.put(e,1);
		System.out.println("station :"+a.name);
		System.out.println("voisin "+a.nextStations.size());
		Function function = new Function(stations);
		ArrayList<Station> res = function.dijsktra(/*map,*/ a, d);
		for(Station s : res) {
			System.out.println("RES FINAL : "+s.name);
		}
	}

}
