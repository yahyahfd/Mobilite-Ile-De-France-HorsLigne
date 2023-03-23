package fr.uparis.beryllium;
import java.util.ArrayList;
import java.util.Scanner;

import fr.uparis.beryllium.model.Map;
import fr.uparis.beryllium.model.Parser;
import fr.uparis.beryllium.model.Station;
/**
 * The Controller is considered the main in our MVC
 */
public class TerminalApplication {

    /**
     * No arguments are needed.
     * Unicodes used for colors here: 
     * Red: "\u001B[31m"
     * Green: "\u001B[32m"
     * Blue: "\u001B[34m"
     * Cyan: "\u001B[36m"
     * Color resetting at the end of each string: "\u001B[0m"
     */
    public static void main(String[] args) {
        //We parse the map
        Map m = Parser.readMap("map_data.csv");

        Scanner scanner  = new Scanner(System.in);
        System.out.println("\u001B[36mWelcome to our interactive (Terminal Only) program for finding routes.");
        System.out.println("\u001B[36mIf you ever want to leave, just type \u001B[31mquit\u001B[0m");
        while(true){
            System.out.println("\u001B[34m\nLet's check if there is a route for you\u001B[0m");
            System.out.print("\u001B[32mEnter your first station's name: \u001B[0m");
            String station1 = "";
            while(station1.isEmpty()){
                station1 = scanner.nextLine();
                station1 = station1.trim();
                if (station1.isEmpty()){
                    System.out.println("Empty String");
                }
            }
            if (station1.trim().equalsIgnoreCase("quit")) break;
            Station chosen_1 = multi_choice(station1,m,scanner);
            
            System.out.print("\u001B[32mEnter your second station's name: \u001B[0m");
            String station2 = "";
            while(station2.isEmpty()){
                station2 = scanner.nextLine();
                station2 = station2.trim();
                if (station2.isEmpty()){
                    System.out.println("Empty String");
                }
            }
            if (station2.trim().equalsIgnoreCase("quit")) break;
            Station chosen_2 = multi_choice(station2,m,scanner);
        }
        
        //calculate route below using chosen_1 and chosen_2
        //  HERE calculate (waiting for Algorithm)
        // String route = "";
        // route.trim();
        // station1 = exists_1?"\u001B[32m"+station1.trim()+"\u001B[0m":"\u001B[31m"+station1.trim()+"\u001B[0m";
        // station2 = exists_2?"\u001B[32m"+station2.trim()+"\u001B[0m":"\u001B[31m"+station2.trim()+"\u001B[0m";
        // if(route.isEmpty()){
        //     System.out.println("Looks like there is no route to go from "+station1+" to "+station2);   
        // }else{
        //     System.out.println("Here is your route going from "+station1+" to "+station2);
        //     //Print de la route
        // }

        scanner.close();
    }

    /**
     * This method is used twice in terminal mode to either propose all the stations
     * that have the same name, or just choose the single station with the name specified.
     * @param name Name of the station to look for
     * @param m The map used in this app
     * @param scanner Same scanner for the whole app.
     * @return List of all the stations (with numbers to choose from) that have the name <code>name</code>, 
     * or a single station or nothing if no station found.
     */
    public static Station multi_choice(String name, Map m, Scanner scanner){
        ArrayList<Station> stations = m.getStationsByName(name);
        if(stations.size()>1){
            System.out.println("Multiple stations with the name "+name+" found. Choose one from the list below:");
            int i = 1;
            for(Station s : stations){
                System.out.println(i+") "+s);
                i++;
            }
            
            int num_chosen = 0;
            while(num_chosen == 0 || num_chosen>i-1){
                try {
                    String read = scanner.nextLine();
                    if(read.trim().equalsIgnoreCase("quit")) break;
                    num_chosen = Integer.parseInt(read);
                    System.out.println(stations.get(num_chosen-1) + " was chosen");
                } catch (Exception e) {
                    System.out.println("You need to choose a valid number ! Try again ! ");
                }
            }
            return stations.get(num_chosen-1);
        }else if(stations.size() == 0){
            System.out.println("No station with the name "+name+ " was found !");
            return null;
        }else{
            return stations.get(0);
        }
    }
}
