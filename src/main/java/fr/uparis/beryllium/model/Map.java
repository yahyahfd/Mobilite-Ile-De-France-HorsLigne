package fr.uparis.beryllium.model;
import java.util.ArrayList;

public class Map {
    private ArrayList<Line> lines = new ArrayList<>();
    private ArrayList<Station> stations = new ArrayList<>();

    //Return Station if exist in the list
    //else create and return the new Station
    public Station searchStation(String name, Double x, Double y){
        for(Station s : stations){
            if ((s.getName()).equals(name) && s.geLocalisation().getLatitude() == x && s.geLocalisation().getLongitude() == y) return s;
        }
        Station newStation = new Station(name,x,y);
        stations.add(newStation);
        return newStation;
    }
    
    //Return Line if exist
    //else create and return new Line
    public Line searchLine(String name){
        for(Line l : lines){
            if((l.getName()).equals(name)) return l;
        }
        Line newLine = new Line(name);
        lines.add(newLine);
        return newLine;
    }

    //Temporary test function ? 
    public void display(){
        System.out.println("-------------------Map's lines------------------- ");
        for(Line l : lines){
            System.out.println("Line " + l.getName() + " contains : ");
            for(Station s : l.getStations()){
                System.out.println("        - "+ s.getName());
            }
            System.out.println();
        }
        /*System.out.println("-------------------Map's stations------------------- ");
        for(Station s : stations){
            System.out.println("Station "+ s.getName() + " and it's neighbours : ");
            s.display();
        }*/
    }
}
