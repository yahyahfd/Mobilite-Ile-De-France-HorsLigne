package fr.uparis.beryllium.model;
import java.util.Map;

public class Station {
    String name;
    private Localisation localisation;
    
    /**
     * Neighboring stations or stations reached 
     * directly after the current one (this)
     */
    Map<Station,NeighborData> nextStations;
}
