package fr.uparis.beryllium;

import fr.uparis.beryllium.exceptions.FormatException;
import fr.uparis.beryllium.model.*;
import fr.uparis.beryllium.model.Map;
import org.apache.commons.lang3.StringUtils;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;
import static java.lang.Character.toLowerCase;

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
    public static void main(String[] args) throws FormatException {
        //we parse the map
        Map m = Parser.readMap("map_data.csv");
        m = Parser.readMapHoraire("newtimetables.csv", m);
        Scanner scanner  = new Scanner(System.in);
        System.out.println("\u001B[36mWelcome to our interactive (Terminal Only) program for finding routes.");
        System.out.println("\u001B[36mIf you ever want to leave, just type \u001B[31mquit\u001B[0m\n");
        ArrayList<Station> chosen_1 = new ArrayList<>();
        ArrayList<Station> chosen_2 = new ArrayList<>();
        boolean localpositionStart = false;
        boolean localpositionDest = false;
        while (true) {
            System.out.println("\u001B[34m\nLet's check if there is a route for you\u001B[0m");
            System.out.print("\u001B[32mEnter your first station's name: (lp : local position) \u001B[0m");
            String station1 = "";
            while (station1.isEmpty() || chosen_1.size() == 0) {
                station1 = scanner.nextLine();
                station1 = station1.trim();
                if (station1.isEmpty()) {
                    System.out.println("Empty String, try again");
                }
                if (station1.trim().equalsIgnoreCase("quit")) break;
                if (station1.trim().equalsIgnoreCase("lp")) break;
                chosen_1 = m.getStationsByName(station1);
                if(chosen_1.size() == 0){
                    System.out.println("No station with the name " + station1 + " was found !");
                    System.out.println("Try again!");
                }
            }
            if (station1.trim().equalsIgnoreCase("quit")) break;
            // we start from our position, not an existing station
            if (station1.trim().equalsIgnoreCase("lp")){
                String name = "localPositionStart";
                addStationByCoordonnees(scanner, m, name);
                chosen_1 = (m.getStationsByName(name));
                localpositionStart = true;
            }

            System.out.print("\u001B[32mEnter your second station's name: (lp : local position) \u001B[0m");
            String station2 = "";
            while(station2.isEmpty() || chosen_2.size() == 0){
                station2 = scanner.nextLine();
                station2 = station2.trim();
                if (station2.isEmpty()) {
                    System.out.println("Empty String");
                }
                if (station2.trim().equalsIgnoreCase("quit")) break;
                if (station2.trim().equalsIgnoreCase("lp")) break;
                chosen_2 = m.getStationsByName(station2);
                if (chosen_2.size() == 0) {
                    System.out.println("No station with the name " + station2 + " was found !");
                    System.out.println("Try again!");
                }
            }
            if (station2.trim().equalsIgnoreCase("quit")) break;
            // we start from our position, not an existing station
            if (station2.trim().equalsIgnoreCase("lp")){
                String name = "localPositionDest";
                addStationByCoordonnees(scanner, m, name);
                chosen_2 = (m.getStationsByName(name));
                localpositionDest = true;
            }
            // list of choice of preferences
            ArrayList<Integer> typePreference = new ArrayList<>(List.of(0, 1, 2));
            // while the given preference is not right, we ask again
            int preference = -1;
            while(preference < 0 || !typePreference.contains(preference)){
                // how do they want to travel
                System.out.print("\u001B[32mHow do you want to travel ? (0 = shortest distance / 1 = unitary / 2 = shortest time : \u001B[0m");
                try{
                    // we convert string to int
                    preference = Integer.parseInt(scanner.nextLine());
                } catch (NumberFormatException e) {
                    System.out.println("Veuillez renseigner un entier");
                }
            }
            if (chosen_1.size()>0 && chosen_2.size()>0) {

                // we search for all stations that we can go by feet within a certain perimeter (dist from start to dest)
                if (localpositionStart) {
                    // On prend la premiere station de ce nom? //////////////////////
                    // m.walkToBestStation(chosen_1, true, (Localisation) chosen_1.getLocalisations().values().toArray()[0], (Localisation) chosen_2.getLocalisations().values().toArray()[0]);
                    // m.walkToBestStation(chosen_1, true, chosen_2.getLocalisation());
                }
                // we add the neighbors for the destination station
                if (localpositionDest) {
                    // m.walkToBestStation(chosen_2, false, (Localisation) chosen_1.getLocalisations().values().toArray()[0], (Localisation) chosen_2.getLocalisations().values().toArray()[0]);
                    // m.walkToBestStation(chosen_2, false, chosen_1.getLocalisation());
                }
                // instance itinerary with all stations of the map
                Itinerary i = new Itinerary(m.getStations());
                // default, actual time, else, the time the user enter
                LocalTime timeWeLeft = LocalTime.now();
                // get the shortest way depending on the preference
                HashMap<Station, Line> route = i.shortestMultiplePaths(chosen_1, chosen_2, preference, timeWeLeft);
                // HashMap<Station, Line> route = i.shortestWay(chosen_1.get(0), chosen_2.get(0), preference);
                
                // We'll add verifications here to check if the names are valid (I don't know if it's necessary?)
                // If we add verifications, we'll set station1 or station2's colors to green or red whether they exist or not
                // We add the method (the algorithm) to look for the path
                if (route == null) {
                    System.out.println("Looks like there is no route to go from \u001B[31m" + station1 + "\u001B[0m to \u001B[31m" + station2 + "\u001B[0m");
                } else {
                    System.out.println("Route to go from \u001B[31m" + station1 + "\u001B[0m to \u001B[31m" + station2 + "\u001B[0m :\n");
                    System.out.println(i.showPath(route, timeWeLeft));
                }
                // if we added temporary station, we remove them of the list of stations
                if (localpositionStart) {
                    // m.removeStation(chosen_1);
                }
                if (localpositionDest) {
                    // removewalkingneighbours pas correct (regarder commentaire sur méthode)
                    // chosen_2.removeWalkingNeighbours(m.getStations(), chosen_1.getDistanceToAStation((Localisation) chosen_1.getLocalisations().values().toArray()[0], (Localisation) chosen_2.getLocalisations().values().toArray()[0]),
                    //         (Localisation) chosen_1.getLocalisations().values().toArray()[0], (Localisation) chosen_2.getLocalisations().values().toArray()[0]);
                    
                            // m.removeStation(chosen_2);
                }
            }
        }
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
                ArrayList<String> neighborLines = s.getNeighboringLines();
                if (neighborLines.isEmpty()) {
                    System.out.println(i + ") " + s + ": Terminus (pas de correspondances)");
                } else {
                    System.out.println(i + ") " + s + ": " + s.getNeighboringLines());
                }
                i++;
            }
            
            int num_chosen = 0;
            while (num_chosen == 0 || num_chosen > i - 1) {
                try {
                    String read = scanner.nextLine();
                    if (read.trim().equalsIgnoreCase("quit")) break;
                    num_chosen = Integer.parseInt(read);
                    System.out.println(stations.get(num_chosen - 1) + " was chosen");
                } catch (Exception e) {
                    System.out.println("You need to choose a valid number ! Try again ! ");
                }
            }
            return stations.get(num_chosen-1);
        }else if(stations.size() == 0){
            System.out.println("No station with the name "+name+ " was found !");
            return null;
        } else {
            return stations.get(0);
        }
    }

    /**
     * ...
     *
     * @param scanner
     * @param m
     * @param name
     */
    public static void addStationByCoordonnees(Scanner scanner, Map m, String name) {
        Double longitude = null;
        Double latitude = null;
        System.out.print("\u001B[32mEnter your position : \u001B[0m");
        while (latitude == null) {
            System.out.print("\u001B[32mLatitude : \u001B[0m");
            // how do they want to travel
            try {
                // we convert string to int
                latitude = Double.parseDouble(scanner.nextLine());
            } catch (NumberFormatException e) {
                System.out.println("Veuillez renseigner un double");
            }
        }
        while (longitude == null) {
            System.out.print("\u001B[32mLongitude : \u001B[0m");
            // how do they want to travel
            try {
                // we convert string to int
                longitude = Double.parseDouble(scanner.nextLine());
            } catch (NumberFormatException e) {
                System.out.println("Veuillez renseigner un double");
            }
        }
        // we add the station to the map
        m.addStation(latitude, longitude, name);
    }

    /**
     * This method propose every possibilities found for a station name
     * @param possibilities List of possibilities
     * @param scanner Same scanner for the whole app.
     * @return chosen Station
     */
    public static Station multi_choice_similar(ArrayList<Station> possibilities, Scanner scanner){
            System.out.println("But possibilites found. Choose one from the list below:");
            int i = 1;
            for(Station s : possibilities){
                ArrayList<String> neighborLines = s.getNeighboringLines();
                if (neighborLines.isEmpty()) {
                    System.out.println(i + ") " + s + ": Terminus (pas de correspondances)");
                } else {
                    System.out.println(i + ") " + s + ": " + s.getNeighboringLines());
                }
                i++;
            }
            int num_chosen = 0;
            while(num_chosen == 0 || num_chosen>i-1){
                try {
                    String read = scanner.nextLine();
                    if(read.trim().equalsIgnoreCase("quit")) break;
                    num_chosen = Integer.parseInt(read);
                    System.out.println(possibilities.get(num_chosen-1) + " was chosen");
                } catch (Exception e) {
                    System.out.println("You need to choose a valid number ! Try again ! ");
                }
            }
            return possibilities.get(num_chosen-1);
    }

     /**
     * This method is used to find stations who has similar name in case the user didn't spell correctly
     * @param name Name of the station to look for
     * @param m The map used in this app
     * @return List of all the stations (with numbers to choose from) that have similar name <code>name</code>,
     * or a single station or nothing if no station found.
     */
    public static ArrayList<Station> similar_names(String name, Map m){
        ArrayList<Station> similar = new ArrayList<>();
        for( Station s : m.getStations()) {
            String nameS = StringUtils.stripAccents(s.getName());
            if (nameS.length() >= name.length() && nameS.toLowerCase().contains(name.toLowerCase())) {
                similar.add(s);
            } else {
                int sameLetters = 0;
                int diff = 0;
                boolean notTheSameWordAnymore = false;
                int index = 0;
                boolean abandon = false;
                while (index != name.length() && index != nameS.length() && !abandon) {
                    if (!notTheSameWordAnymore) {
                        if (toLowerCase(name.charAt(index)) == toLowerCase(nameS.charAt(index))) sameLetters++;
                        else {
                            notTheSameWordAnymore = true;
                            diff ++;
                        }
                    } else {
                        if (diff >=2 ) abandon = true;
                        else if (index + 1 < nameS.length() && toLowerCase(name.charAt(index)) == toLowerCase(nameS.charAt(index+1))
                                || toLowerCase(name.charAt(index - 1)) == toLowerCase(nameS.charAt(index)) || toLowerCase(name.charAt(index)) == toLowerCase(nameS.charAt(index)) ) {
                            sameLetters++;
                            notTheSameWordAnymore = false;
                        }else diff ++;
                    }
                    index++;
                }
                if(sameLetters != 0)
                if (!abandon && sameLetters >=3){
                    similar.add(s);
                }
            }
        }
        return similar;
    }
}