package fr.uparis.beryllium.model;
import java.util.Map;

public class Station {
    String name;
    
    /**
     * Neighboring stations or stations reached 
     * directly after the current one (this)
     */
    Map<Station,Line> nextStations;
}
