package fr.uparis.beryllium.model;

import fr.uparis.beryllium.model.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Station {
    String name;
    private Localisation localisation;
    
    /**
     * Neighboring stations or stations reached 
     * directly after the current one (this)
     */
   private Map<Station,ArrayList<Line>> nextStations = new HashMap<>();

    Station(String n){
        name = n;
    }

    public void addNextStation(Station s, Line l){
       if(nextStations.containsKey(s)){
            ArrayList<Line> list = nextStations.get(s);
            if(!list.contains(l)) list.add(l);
        }else {
            ArrayList<Line> tmp = new ArrayList<>();
            tmp.add(l);
            nextStations.put(s,tmp );
        }
    }

    public String getName(){ return name; }
    public Map<Station,ArrayList<Line>> getNextStations(){ return nextStations; }

    public void display(){
        for (Map.Entry<Station,ArrayList<Line>> entry : nextStations.entrySet()){
            System.out.print("        - " + entry.getKey().getName() +" on line : ");
            for(Line l: entry.getValue()) System.out.print( l.getName() + " - ");
            System.out.println();
        }
    }

}
