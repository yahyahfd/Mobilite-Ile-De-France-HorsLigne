package fr.uparis.beryllium;
import java.util.Scanner;

import fr.uparis.beryllium.model.Map;
import fr.uparis.beryllium.model.Parser;
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
            String station1 = scanner.nextLine();
            if (station1.equalsIgnoreCase("quit")) {
                break;
            }
            System.out.print("\u001B[32mEnter your second station's name: \u001B[0m");
            String station2 = scanner.nextLine();
            String route = "";
            if (station2.equalsIgnoreCase("quit")) {
                break;
            }
            boolean exists_1 = m.checkStationExists(station1);
            boolean exists_2 = m.checkStationExists(station2);
            // If we add verifications, we'll set station1 or station2's colors to green or red whether they exist or not
            //calculate route below
            //  HERE calculate (waiting for Algorithm)
            route.trim();
            station1 = exists_1?"\u001B[32m"+station1+"\u001B[0m":"\u001B[31m"+station1+"\u001B[0m";
            station2 = exists_2?"\u001B[32m"+station2+"\u001B[0m":"\u001B[31m"+station2+"\u001B[0m";
            if(route.isEmpty()){
                System.out.println("Looks like there is no route to go from "+station1+" to "+station2);   
            }else{
                System.out.println("Here is your route going from "+station1+" to "+station2);
                //Print de la route
            }
        }
        
        scanner.close();
    }
}
