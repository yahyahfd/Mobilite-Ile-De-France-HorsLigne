package fr.uparis.beryllium.model;
import java.util.HashMap;
import java.util.Map;

public class Station {
    String name;
    private Localisation localisation;
    
    /**
     * Neighboring stations or stations reached 
     * directly after the current one (this)
     */
    //Map<Station,NeighborData> nextStations;
    
   private Map<Station,Line> nextStations = new HashMap<>();

    Station(String n){
        name = n;
    }

    public void addNextStation(Station s, Line l){
        if(!nextStations.containsKey(s) || nextStations.get(s) != l) nextStations.put(s, l);
    }

    public String getName(){ return name; }
    public Map<Station,Line> getNextStations(){ return nextStations; }

    public void display(){
        for (Map.Entry<Station,Line> entry : nextStations.entrySet()){
            System.out.println("        - " + entry.getKey().getName() +" on line " + entry.getValue().getName());
        }
    }

}
