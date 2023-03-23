package fr.uparis.beryllium.model;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Station {
    private String name;
    private Localisation localisation;
    private Map<Station,ArrayList<NeighborData>> nextStations = new HashMap<>();

    /**
     * Neighboring stations or stations reached 
     * directly after the current one (this)
     */
    Station(String n, Localisation l ){
        name = n;
        localisation = l;
    }

    public String getName(){ return name; }
    public Map<Station,ArrayList<NeighborData>> getNextStations(){ return nextStations; }
    public Localisation getLocalisation() { return localisation; }

    public void addNextStation(Station s, Line l, String[] h, Double dist){
        Duration duration = Duration.ZERO;
        duration = duration.plusMinutes(Long.parseLong(h[0]));
        duration = duration.plusSeconds(Long.parseLong(h[1]));
        NeighborData n = new NeighborData(l, duration, dist);

       if(nextStations.containsKey(s)){
            ArrayList<NeighborData> list = nextStations.get(s);
            if(!NeighborDataIsIn(list, duration, l, dist)) list.add(n);
        }else {
            ArrayList<NeighborData> tmp = new ArrayList<>();
            tmp.add(n);
            nextStations.put(s,tmp );
        }
    }

    //Check if the Neighbor doesn't exist in the list for nextStations
    public boolean NeighborDataIsIn(ArrayList<NeighborData> list, Duration duration, Line l, Double dist){
        for(NeighborData n : list){
            if(n.getDuration() == duration && n.getDistance() == dist && n.getLine() == l) return true;
        }
        return false;
    }

    public String toString(){
        return name + " " + localisation;
    }
}
