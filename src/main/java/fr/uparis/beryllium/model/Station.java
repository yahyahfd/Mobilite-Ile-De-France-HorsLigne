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
    Station(String n, Double x, Double y){
        name = n;
        localisation = new Localisation(x, y);
    }

    public String getName(){ return name; }
    public Map<Station,ArrayList<NeighborData>> getNextStations(){ return nextStations; }
    public Localisation geLocalisation() { return localisation; }

    public void addNextStation(Station s, Line l, String[] h, Double dist){
        Duration d = Duration.ZERO;
        d = d.plusMinutes(Long.parseLong(h[0]));
        d = d.plusSeconds(Long.parseLong(h[1]));
        NeighborData n = new NeighborData(l, d, dist);

       if(nextStations.containsKey(s)){
            ArrayList<NeighborData> list = nextStations.get(s);
            //est ce que je vérifie si le neigborData est déjà dans la liste ou pas ? 
            //Si oui, on considère que c est le même objet quand ? Quand y a la même ligne, même durée et même dist ? 
            //ou juste même ligne ?
            //Ou alors c'est inutile car cette situation n'arrivera jamais ?
            if(!NeighborDataIsIn(list, d, l, dist)) list.add(n);
        }else {
            ArrayList<NeighborData> tmp = new ArrayList<>();
            tmp.add(n);
            nextStations.put(s,tmp );
        }
    }

    //Check if the Neighbor doesn't exist in the list for nextStations
    public boolean NeighborDataIsIn(ArrayList<NeighborData> list, Duration d, Line l, Double dist){
        for(NeighborData n : list){
            if(n.getDuration() == d && n.getDistance() == dist && n.getLine() == l) return true;
        }
        return false;
    }
  
    //test display
    public void display(){
        for (Map.Entry<Station,ArrayList<NeighborData>> entry : nextStations.entrySet()){
            System.out.print("        - " + entry.getKey().getName() +" on line : ");
            for(NeighborData n: entry.getValue()) System.out.print( n.getLine().getName() + "("+n.getDistance()+" , "+n.getDuration()+")" + " - ");
            System.out.println();
        }
    }

}
