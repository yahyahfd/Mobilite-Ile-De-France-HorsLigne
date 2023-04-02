package fr.uparis.beryllium.model;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;

class ItineraryTest {

    @Test
    void shortestWay() {

        Map m = Parser.readMap("src/main/test/fr/uparis/beryllium/model/map_data2.csv");
        Station station1 = m.searchStation("République", new Localisation(2.363149406900382, 48.86742161002706));
        Station station2 = m.searchStation("Quai de la Rapée", new Localisation(2.365884650750405, 48.846427325479766));

        for (Station sta : m.getAllStations()) {
            System.out.println("\n\n-------" + sta + "--------");
            for (java.util.Map.Entry<Station, ArrayList<NeighborData>> entry : sta.getNextStations().entrySet()) {
                Station s = entry.getKey();
                ArrayList<NeighborData> liste = entry.getValue();
                System.out.println(s.toString() + ":");
                for (NeighborData n : liste) {
                    System.out.println("line : " + n.getLine().getName() + ", distance : " + n.getDistance() + ", temps : " + n.getDuration().toMinutes());
                }
                System.out.println();
            }
        }

        Itinerary itinerary = new Itinerary(m.getAllStations());
        var x = itinerary.shortestWay(station1, station2, 1);
        var s = itinerary.showPath(x);
        System.out.println(s);

    }
}