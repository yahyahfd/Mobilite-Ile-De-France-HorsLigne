package fr.uparis.beryllium;

import fr.uparis.beryllium.exceptions.FormatException;
import fr.uparis.beryllium.model.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.MutablePair;

import java.time.LocalTime;
import java.time.Duration;
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

        Scanner scanner = new Scanner(System.in);

        System.out.println("\u001B[36mWelcome to our interactive (Terminal Only) program for finding routes.");
        System.out.println("\u001B[36mIf you ever want to leave, just type \u001B[31mquit\u001B[0m\n\n");

        while (true) {

            System.out.println("\u001B[34mWhat do you want to do ?\u001B[0m");
            System.out.println("\u001B[34m1) Search an itinerary\u001B[0m");
            System.out.println("\u001B[34m2) See schedules\u001B[0m");

            String choice = "";
            while (!isFirstChoiceCorrect(choice)) {

                choice = scanner.nextLine().trim();

                if (!isFirstChoiceCorrect(choice)) {
                    System.out.println("Try again !");
                }

            }

            switch (choice) {
                case "1" -> searchItinerary(m, scanner);
                case "2" -> searchSchedule(m, scanner);
            }

        }

    }

    /**
     * @param choice
     * @return
     */
    private static boolean isFirstChoiceCorrect(String choice) {
        return choice.equals("1") || choice.equals("2");
    }

    /**
     * @param m
     * @param scanner
     */
    private static void searchItinerary(Map m, Scanner scanner) {

        ArrayList<Station> chosen_1 = new ArrayList<>();
        ArrayList<Station> chosen_2 = new ArrayList<>();
        boolean localpositionStart = false;
        boolean localpositionDest = false;
        
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
            if (chosen_1 == null) {
                ArrayList<Station> list_1 = similar_names(StringUtils.stripAccents(station1),m);
                if(!list_1.isEmpty()) chosen_1 = multi_choice_similar(list_1, scanner);
                else  System.out.println("No station with the name " + station1 + " was found !"); System.out.println("Try again!");
            }
        }

        if (station1.trim().equalsIgnoreCase("quit")) {
            // TODO
        }
        // we start from our position, not an existing station
        else if (station1.trim().equalsIgnoreCase("lp")) {
            String name = "localPositionStart";
            addStationByCoordonnees(scanner, m, name);
            chosen_1 = (m.getStationsByName(name));
            localpositionStart = true;
        }

        System.out.print("\u001B[32mEnter your second station's name: (lp : local position) \u001B[0m");
        String station2 = "";
        while (station2.isEmpty() || chosen_2.size() == 0) {
            station2 = scanner.nextLine();
            station2 = station2.trim();
            if (station2.isEmpty()) {
                System.out.println("Empty String");
            }
            if (station2.trim().equalsIgnoreCase("quit")) break;
            if (station2.trim().equalsIgnoreCase("lp")) break;
            chosen_2 = m.getStationsByName(station2);
            if (chosen_2.size() == 0) {
                ArrayList<Station> list_2 = similar_names(StringUtils.stripAccents(station2),m);
                if(!list_2.isEmpty()) chosen_2 = multi_choice_similar(list_2, scanner);
                else  System.out.println("No station with the name " + station2 + " was found !"); System.out.println("Try again!");
            }
        }

        if (station2.trim().equalsIgnoreCase("quit")) {
            // TODO
        }
        // we start from our position, not an existing station
        else if (station2.trim().equalsIgnoreCase("lp")) {
            String name = "localPositionDest";
            addStationByCoordonnees(scanner, m, name);
            chosen_2 = (m.getStationsByName(name));
            localpositionDest = true;
        }

        // list of choice of preferences
        ArrayList<Integer> typePreference = new ArrayList<>(List.of(0, 1, 2));
        // while the given preference is not right, we ask again
        int preference = -1;
        while (preference < 0 || !typePreference.contains(preference)) {
            // how do they want to travel
            System.out.print("\u001B[32mHow do you want to travel ? (0 = shortest distance / 1 = shortest time / 2 = unitary : \u001B[0m");
            try {
                // we convert string to int
                preference = Integer.parseInt(scanner.nextLine());
            } catch (NumberFormatException e) {
                System.out.println("Veuillez renseigner un entier");
            }
        }

        if (chosen_1.size()>0 && chosen_2.size()>0) {

            findRoute(m, chosen_1, chosen_2, localpositionStart, localpositionDest, preference);

            }
    }

    /**
     * This method is used twice in terminal mode to either propose all the stations
     * that have the same name, or just choose the single station with the name specified.
     *
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
            return stations.get(num_chosen - 1);
        } else if (stations.size() == 0) {
            System.out.println("No station with the name " + name + " was found !");
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
     * @param m
     * @param chosen_1
     * @param chosen_2
     * @param localpositionStart
     * @param localpositionDest
     * @param preference
     */
    private static void findRoute(Map m, ArrayList<Station> chosen_1, ArrayList<Station> chosen_2, boolean localpositionStart, boolean localpositionDest, int preference) {
        // we search for all stations that we can go by feet within a certain perimeter (dist from start to dest)
        if (localpositionStart) {
            m.walkToBestStation(chosen_1, true, (Localisation) chosen_1.getLocalisations().values().toArray()[0], (Localisation) chosen_2.getLocalisations().values().toArray()[0]);
        }
        // we add the neighbors for the destination station
        if (localpositionDest) {
            m.walkToBestStation(chosen_2, false, (Localisation) chosen_1.getLocalisations().values().toArray()[0], (Localisation) chosen_2.getLocalisations().values().toArray()[0]);
        }
        // instance itinerary with all stations of the map
        Itinerary i = new Itinerary(m.getAllStations());
        // default, actual time, else, the time the user enter
        LocalTime timeWeLeft = LocalTime.now();
        // get the shortest way depending on the preference
        HashMap<Station, Line> route = i.shortestMultiplePaths(chosen_1, chosen_2, preference, timeWeLeft);
        HashMap<Station, MutablePair<Double, Double>> distTimeToStart = i.getDistTime();
        // We'll add verifications here to check if the names are valid (I don't know if it's necessary?)
        // If we add verifications, we'll set station1 or station2's colors to green or red whether they exist or not
        // We add the method (the algorithm) to look for the path
        if (route == null) {
            System.out.println("Looks like there is no route to go from \u001B[31m" + chosen_1.getName() + "\u001B[0m to \u001B[31m" + chosen_2.getName() + "\u001B[0m");
        } else {
            System.out.println("Route to go from \u001B[31m" + chosen_1.getName() + "\u001B[0m to \u001B[31m" + chosen_2.getName() + "\u001B[0m :\n");
            System.out.println(showPath(route, distTimeToStart, timeWeLeft));
        }
        // if we added temporary station, we remove them of the list of stations
        if (localpositionStart) {
            m.removeStation(chosen_1.get(0));
        }
        if (localpositionDest) {
            chosen_2.removeWalkingNeighbours(m.getStations(), chosen_1.getDistanceToAStation((Localisation) chosen_1.getLocalisations().values().toArray()[0], (Localisation) chosen_2.getLocalisations().values().toArray()[0]),
                    (Localisation) chosen_1.getLocalisations().values().toArray()[0], (Localisation) chosen_2.getLocalisations().values().toArray()[0]);
            m.removeStation(chosen_2.get(0));
        }
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

    /**
     * Show the shortest way to go from a station to another
     *
     * @param res Res of the algorithm
     * @return a string of the stations and line in order
     */
    public static String showPath(HashMap<Station, Line> res, HashMap<Station, MutablePair<Double, Double>> distTimeToStart) {
        ArrayList<Station> stationRes = new ArrayList<>();
        ArrayList<Line> lineRes = new ArrayList<>();
        StringBuilder path = new StringBuilder();
        if (res == null) {
            path.append("Il n'existe aucun chemin");
        } else {
            // pour remettre dans le bon sens si c'est dans le mauvais
            for (java.util.Map.Entry<Station, Line> r : res.entrySet()) {
                stationRes.add(0, r.getKey());
                lineRes.add(0, r.getValue());
            }
            // afficher le chemin du depart jusqu'a dest
            int i = 0;
            String downArrow = "↓";

            String normalColor = "\033[0m";
            String blue_bold = "\033[1;34m";
            String purple_bold = "\033[1;35m";
            String yellow_bold = "\033[1;33m";
            MutablePair<Double, Long> distTime;
            while (i < stationRes.size()) {
                if (i == 0) {
                    MutablePair<Double, Double> distTimeFromDestination = distTimeToStart.get(stationRes.get(stationRes.size() - 1));
                    Duration d = Duration.ZERO;
                    d = d.plusMillis((long) (distTimeFromDestination.getRight() - 0));
                    path.append(yellow_bold).append("-- Trajet :     ").append(d.toMinutes() + 1).append("min. ").append(normalColor).append("~ ").append(yellow_bold).append(distTimeFromDestination.getLeft()).append("km.\n");
                    distTime = getDistTimeForALine(stationRes, lineRes, i, distTimeToStart);
                    path.append(purple_bold).append("Ligne ").append(lineRes.get(1).getName()).append(": ").append("     ").append(yellow_bold).append(distTime.getRight()).append("min. ").append(normalColor).append("~ ").append(yellow_bold).append(distTime.getLeft()).append("km.\n");
                    path.append(purple_bold).append("|     ").append(blue_bold).append(stationRes.get(i).getName()).append("\n");
                } else {
                    if (i != 1) {
                        if (lineRes.get(i) != lineRes.get(i - 1) && lineRes.get(i) != null) {
                            MutablePair<Double, Long> tempDistTime = getDistTimeForALine(stationRes, lineRes, i - 1, distTimeToStart);
                            path.append(purple_bold).append("Ligne ").append(lineRes.get(i).getName()).append(": ").append("     ").append(yellow_bold).append(tempDistTime.getRight()).append("min. ").append(normalColor).append("~ ").append(yellow_bold).append(tempDistTime.getLeft()).append("km.\n");
                            path.append(purple_bold).append("|     ").append(blue_bold).append(stationRes.get(i - 1).getName()).append("\n");
                        }
                    }
                    if (i + 1 < stationRes.size()) {
                        if (lineRes.get(i) != lineRes.get(i + 1) && lineRes.get(i) != null) {
                            path.append(purple_bold).append(downArrow).append("     ").append(blue_bold).append(stationRes.get(i).getName()).append("\n");
                        } else {
                            path.append(purple_bold).append("|         ").append(blue_bold).append("| ").append(normalColor).append(stationRes.get(i).getName()).append("\n");
                        }
                    } else if (i + 1 == stationRes.size()) {
                        path.append(purple_bold).append("|     ").append(blue_bold).append(stationRes.get(i).getName()).append("\n");
                    } else {
                        path.append(purple_bold).append("|         ").append(blue_bold).append("| ").append(normalColor).append(stationRes.get(i).getName()).append("\n");
                    }
                }
                i++;
            }
        }
        return path.toString();
    }

    /**
     * Get distance and time traveled on a line
     *
     * @param stationRes list of all the station
     * @param lineRes    list of all the line
     * @param position   the position of the station in the stationRes
     * @return a couple of distance/time which represent the distance and time to travel the line of the station at the position
     */
    private static MutablePair<Double, Long> getDistTimeForALine(ArrayList<Station> stationRes, ArrayList<Line> lineRes, int position, HashMap<Station, MutablePair<Double, Double>> distTimeToStart) {

        MutablePair<Double, Double> distTimeStart = new MutablePair<>(0.0, 0.0);
        if (position != 0) {
            distTimeStart = distTimeToStart.get(stationRes.get(position));
        }
        int tempPos = position + 2;
        while (tempPos < stationRes.size()) {
            if (lineRes.get(position + 1) != lineRes.get(tempPos)) break;
            tempPos++;
        }
        MutablePair<Double, Double> distTimeDestination = distTimeToStart.get(stationRes.get(tempPos - 1));
        MutablePair<Double, Long> result = new MutablePair<>();
        result.setLeft(distTimeDestination.getLeft() - distTimeStart.getLeft());
        Duration d = Duration.ZERO;
        d = d.plusMillis((long) (distTimeDestination.getRight() - distTimeStart.getRight()));
        result.setRight(d.toMinutes() + 1); // 40s => 1min pour arrondir
        return result;
    }

    /**
     * @param m
     * @param scanner
     */
    private static void searchSchedule(Map m, Scanner scanner) {

        // Afficher les lignes
        printLines(m);

        // demande la ligne
        Line lineChoice = askChoiceLine(m, scanner);

        // afficher les stations de la ligne
        printStationsLine(lineChoice);

        // demande la station
        Station stationChoice = askChoiceStation(lineChoice, scanner);

        // afficher les horaires de la station
        printSchedules(lineChoice, stationChoice);
    }

    private static void printLines(Map m) {

        System.out.println("\u001B[34mWe have the lines :\u001B[0m");
        ArrayList<Line> lines = m.getLines();

        for (int i = 0; i < lines.size(); i++) {
            System.out.println("\u001B[34m" + i + ")\u001B[0m " + lines.get(i).getLineName());
        }
    }

    private static Line askChoiceLine(Map m, Scanner scanner) {

        System.out.println("Select your line :");
        String choice = "";
        ArrayList<Line> lines = m.getLines();
        int sizeLine = lines.size();

        while (!isChoiceLineCorrect(choice, sizeLine)) {

            choice = scanner.nextLine().trim();

            if (!isChoiceLineCorrect(choice, sizeLine)) {
                // TODO revoir
                System.out.println("Try again !");
            }

        }

        int position = Integer.parseInt(choice);
        return lines.get(position);

    }

    private static boolean isChoiceLineCorrect(String choice, int size) {

        try {

            int pos = Integer.parseInt(choice);
            if (pos >= 0 && pos < size) {
                return true;
            }

        } catch (NumberFormatException e) {
            System.out.println("We want a number please!");
        }

        return false;

    }

    private static void printStationsLine(Line line) {

        System.out.println("\u001B[34mWe have the stations :\u001B[0m");
        ArrayList<Station> stations = line.getStations();

        for (int i = 0; i < stations.size(); i++) {
            System.out.println("\u001B[34m" + i + ")\u001B[0m " + stations.get(i).getName());
        }
    }

    private static Station askChoiceStation(Line l, Scanner scanner) {

        System.out.println("Select your station :");
        String choice = "";
        ArrayList<Station> stations = l.getStations();
        int sizeLine = stations.size();

        while (!isChoiceLineCorrect(choice, sizeLine)) {

            choice = scanner.nextLine().trim();

            if (!isChoiceLineCorrect(choice, sizeLine)) {
                // TODO revoir
                System.out.println("Try again !");
            }

        }

        int position = Integer.parseInt(choice);
        return stations.get(position);

    }

    private static void printSchedules(Line line, Station station) {

        ArrayList<LocalTime> schedules = line.getStationTimes(station);
        System.out.println("\u001B[34mFor the Station \"" + station.getName() + "\" on the Line " + line.getLineName() + ", we have theses schedules :\u001B[0m");

        for (LocalTime localTime : schedules) {
            System.out.println(localTime);
        }

    }

}