package fr.uparis.beryllium;

import fr.uparis.beryllium.exceptions.FormatException;
import fr.uparis.beryllium.exceptions.QuitException;
import fr.uparis.beryllium.model.Map;
import fr.uparis.beryllium.model.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.MutablePair;

import java.time.Duration;
import java.time.LocalTime;
import java.util.*;

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

        System.out.println("\u001B[36mWelcome to our interactive (Terminal Only) program for finding routes.");
        System.out.println("\u001B[36mIf you ever want to leave, just type \u001B[31mquit\u001B[0m");
        System.out.println("\033[1;30mLoading data...\u001B[0m");

        //we parse the map
        Map m = Parser.readMap("map_data.csv");
        m = Parser.readMapHoraire("newtimetables.csv", m);

        // instance itinerary with all stations of the map
        Itinerary i = new Itinerary(m.getStations());

        Scanner scanner = new Scanner(System.in);

        try {

            while (true) {

                System.out.println("\n\u001B[34mWhat do you want to do ?\u001B[0m");
                System.out.println("\u001B[34m1) Search an itinerary\u001B[0m");
                System.out.println("\u001B[34m2) See schedules\u001B[0m");

                String choice = "";
                boolean choiceIsOk = false;
                while (!choiceIsOk) {

                    choice = scanner.nextLine().trim();

                    if (isFirstChoiceCorrect(choice)) {
                        choiceIsOk = true;
                    } else {
                        System.out.println("Try again !");
                    }

                }

                switch (choice) {
                    case "1" -> searchItinerary(m, scanner, i);
                    case "2" -> searchSchedule(m, scanner);
                    case "quit" -> throw new QuitException();
                }

            }
        } catch (QuitException e) {
            System.out.println("\u001B[36m\n" + e.getMessage() + "\u001B[0m");
        }

    }

    /**
     * Method to know if the first choice (itinerary or schedules) is correct
     *
     * @param choice the string choice
     * @return true if our choice is corect
     */
    private static boolean isFirstChoiceCorrect(String choice) {
        return choice.equals("1") || choice.equals("2") || choice.equals("quit");
    }

    /**
     * Search an itinerary
     *
     * @param m       the map we use for data
     * @param scanner the scanner for terminal
     */
    private static void searchItinerary(Map m, Scanner scanner, Itinerary itinerary) throws QuitException {

        ArrayList<Station> chosen_1 = new ArrayList<>();
        ArrayList<Station> chosen_2 = new ArrayList<>();
        boolean localpositionStart = false;
        boolean localpositionDest = false;

        System.out.println("\u001B[34m\nLet's check if there is a route for you\u001B[0m");
        System.out.print("\u001B[32mEnter your first station's name: (lp : local position) \u001B[0m");
        String station1 = "";

        // Choix de la 1ere station

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
                ArrayList<Station> list_1 = similar_names(StringUtils.stripAccents(station1),m.getAllStations());
                if(!list_1.isEmpty()) chosen_1 = multi_choice_similar(list_1, scanner);
                else  System.out.println("No station with the name " + station1 + " was found !"); System.out.println("Try again!");
            }
        }
        if (station1.trim().equalsIgnoreCase("quit")) {
            throw new QuitException();
        } else if (station1.trim().equalsIgnoreCase("lp")) {
            String name = "localPositionStart";
            addStationByCoordonnees(scanner, m, name);
            chosen_1 = (m.getStationsByName(name));
            localpositionStart = true;
        }

        // Choix de la 2eme station

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
                ArrayList<Station> list_2 = similar_names(StringUtils.stripAccents(station2),m.getStations());
                if(!list_2.isEmpty()) chosen_2 = multi_choice_similar(list_2, scanner);
                else System.out.println("No station with the name " + station2 + " was found !"); System.out.println("Try again!");
            }else if(chosen_2 == chosen_1){
                System.out.println("This is your start station, please enter another!");
                chosen_2 = null;
                station2 = "";
            }
        }
        if (station2.trim().equalsIgnoreCase("quit")) {
            throw new QuitException();
        } else if (station2.trim().equalsIgnoreCase("lp")) {
            String name = "localPositionDest";
            addStationByCoordonnees(scanner, m, name);
            chosen_2 = (m.getStationsByName(name));
            localpositionDest = true;
        }

        // Choix de la preference

        ArrayList<Integer> typePreference = new ArrayList<>(List.of(0, 1, 2));
        int preference = -1;
        while (preference < 0 || !typePreference.contains(preference)) {
            // how do they want to travel
            System.out.print("\u001B[32mHow do you want to travel ? (0 = shortest distance / 1 = shortest time / 2 = unitary : \u001B[0m");
            try {
                // we convert string to int
                String choice = scanner.nextLine();
                if (choice.trim().equals("quit")) {
                    throw new QuitException();
                }
                preference = Integer.parseInt(choice);
            } catch (NumberFormatException e) {
                System.out.println("Veuillez renseigner un entier");
            }
        }

        if (chosen_1.size()>0 && chosen_2.size()>0) {

            findRoute(m, chosen_1, chosen_2, localpositionStart, localpositionDest, preference, itinerary);

        }
    }

    /**
     * This method is used twice in terminal mode to either propose all the stations
     * that have the same name, or just choose the single station with the name specified.
     *
     * @param name    Name of the station to look for
     * @param m       The map used in this app
     * @param scanner Same scanner for the whole app.
     * @return List of all the stations (with numbers to choose from) that have the name <code>name</code>,
     * or a single station or nothing if no station found.
     */
    private static Station multi_choice(String name, Map m, Scanner scanner) throws QuitException {
        ArrayList<Station> stations = m.getStationsByName(name);
        if (stations.size() > 1) {
            System.out.println("Multiple stations with the name " + name + " found. Choose one from the list below:");
            return chooseAStation(stations, scanner);
        } else if (stations.size() == 0) {
            System.out.println("No station with the name " + name + " was found !");
            return null;
        } else {
            return stations.get(0);
        }
    }

    /**
     * Choose a station between a list of stations print
     *
     * @param stations list of stations
     * @param scanner  the scanner for terminal
     * @return the station choosen
     */
    private static Station chooseAStation(ArrayList<Station> stations, Scanner scanner) throws QuitException {
        int i = 1;
        for (Station s : stations) {
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
                if (read.trim().equalsIgnoreCase("quit")) throw new QuitException();
                num_chosen = Integer.parseInt(read);
                System.out.println(stations.get(num_chosen - 1) + " was chosen");
            } catch (NumberFormatException e) {
                System.out.println("You need to choose a valid number ! Try again ! ");
            }
        }
        return stations.get(num_chosen - 1);
    }

    /**
     * This method is used to find stations who have similar name in case the user didn't spell correctly
     *
     * @param name     Name of the station to look for
     * @param stations The stations used in this app
     * @return List of all the stations (with numbers to choose from) that have similar name <code>name</code>,
     * or a single station or nothing if no station found.
     */
    public static ArrayList<Station> similar_names(String name, ArrayList<Station> stations) {
        ArrayList<Station> similar = new ArrayList<>();
        for (Station s : stations) {
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
                            diff++;
                        }
                    } else {
                        if (diff >= 2) abandon = true;
                        else if (index + 1 < nameS.length() && toLowerCase(name.charAt(index)) == toLowerCase(nameS.charAt(index + 1))
                                || toLowerCase(name.charAt(index - 1)) == toLowerCase(nameS.charAt(index)) || toLowerCase(name.charAt(index)) == toLowerCase(nameS.charAt(index))) {
                            sameLetters++;
                            notTheSameWordAnymore = false;
                        } else diff++;
                    }
                    index++;
                }
                if (sameLetters != 0)
                    if (!abandon && sameLetters >= 3) {
                        similar.add(s);
                    }
            }
        }
        return similar;
    }

    /**
     * This method proposes every possibility found for a station name
     *
     * @param possibilities List of possibilities
     * @param scanner       Same scanner for the whole app.
     * @return chosen Station
     */
    private static Station multi_choice_similar(ArrayList<Station> possibilities, Scanner scanner) throws QuitException {
        System.out.println("No station with this name was found !");
        System.out.println("But possibilites found. Choose one from the list below:");
        return chooseAStation(possibilities, scanner);
    }

    /**
     * Add a station with latitude and longitude
     *
     * @param scanner the scanner for terminal
     * @param m       the Map
     * @param name    the name of the station
     */
    private static void addStationByCoordonnees(Scanner scanner, Map m, String name) {
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
     * Find a route between 2 points
     *
     * @param m                  the map
     * @param chosen_1           the first station
     * @param chosen_2           the second station
     * @param localpositionStart if we start from a localisation and not from a station
     * @param localpositionDest  if we are going to a localisation and not to a station
     * @param preference         our preference for the itinerary
     */
    private static void findRoute(Map m, ArrayList<Station> chosen_1, ArrayList<Station> chosen_2, boolean localpositionStart, boolean localpositionDest, int preference, Itinerary i) {
        // we search for all stations that we can go by feet within a certain perimeter (dist from start to dest)
        if (localpositionStart) {
            m.walkToBestStation(chosen_1, true, (Localisation) chosen_1.getLocalisations().values().toArray()[0], (Localisation) chosen_2.getLocalisations().values().toArray()[0]);
        }
        // we add the neighbors for the destination station
        if (localpositionDest) {
            m.walkToBestStation(chosen_2, false, (Localisation) chosen_1.getLocalisations().values().toArray()[0], (Localisation) chosen_2.getLocalisations().values().toArray()[0]);
        }

        // get the shortest way depending on the preference
        LocalTime timeWeLeft = LocalTime.now();
        HashMap<Station, Line> route = i.shortestMultiplePaths(chosen_1, chosen_2, preference, timeWeLeft);
        HashMap<Station, MutablePair<Double, Double>> distTimeToStart = i.getDistTime();
        HashMap<Station, LocalTime> itineraryTimes = i.getItineraryTimes();
        // We'll add verifications here to check if the names are valid (I don't know if it's necessary?)
        // If we add verifications, we'll set station1 or station2's colors to green or red whether they exist or not
        // We add the method (the algorithm) to look for the path
        if (route == null) {
            System.out.println("Looks like there is no route to go from \u001B[31m" + chosen_1.getName() + "\u001B[0m to \u001B[31m" + chosen_2.getName() + "\u001B[0m");
        } else {
            System.out.println("Route to go from \u001B[31m" + chosen_1.getName() + "\u001B[0m to \u001B[31m" + chosen_2.getName() + "\u001B[0m :\n");
            System.out.println(showPath(route, distTimeToStart, itineraryTimes, timeWeLeft));
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
     * Show the shortest way to go from a station to another
     *
     * @param res             Res of the algorithm
     * @param distTimeToStart couples of distances and time for stations
     * @param itineraryTimes  couple of stations and time
     * @param timeWeLeft      the time we left
     * @return a string of the stations and line in order
     */
    private static String showPath(HashMap<Station, Line> res, HashMap<Station, MutablePair<Double, Double>> distTimeToStart, HashMap<Station, LocalTime> itineraryTimes, LocalTime timeWeLeft) {
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
            path.append(blue_bold).append("Heure de départ: ").append(timeWeLeft).append("\n");
            while (i < stationRes.size()) {
                if (i == 0) {
                    MutablePair<Double, Double> distTimeFromDestination = distTimeToStart.get(stationRes.get(stationRes.size() - 1));
                    Duration d = Duration.ZERO;
                    d = d.plusMillis((long) (distTimeFromDestination.getRight() - 0));
                    path.append(yellow_bold).append("-- Trajet :     ").append(d.toMinutes() + 1).append("min. ").append(normalColor).append("~ ").append(yellow_bold).append(distTimeFromDestination.getLeft()).append("km.\n");
                    distTime = getDistTimeForALine(stationRes, lineRes, i, distTimeToStart);
                    path.append(purple_bold).append("Ligne ").append(lineRes.get(1).getName()).append(": ").append("     ").append(yellow_bold).append(distTime.getRight()).append("min. ").append(normalColor).append("~ ").append(yellow_bold).append(distTime.getLeft()).append("km.\n");
                    LocalTime horaire = itineraryTimes.get(stationRes.get(i));
                    path.append(purple_bold).append("|     ").append(blue_bold).append(stationRes.get(i).getName());
                    if (horaire != null && !Objects.equals(lineRes.get(i).getName(), "--MARCHE--")) {
                        path.append(" - ").append(horaire);
                    }
                    path.append("\n");
                } else {
                    if (i != 1) {
                        if (lineRes.get(i) != lineRes.get(i - 1) && lineRes.get(i) != null) {
                            MutablePair<Double, Long> tempDistTime = getDistTimeForALine(stationRes, lineRes, i - 1, distTimeToStart);
                            path.append(purple_bold).append("Ligne ").append(lineRes.get(i).getName()).append(": ").append("     ").append(yellow_bold).append(tempDistTime.getRight()).append("min. ").append(normalColor).append("~ ").append(yellow_bold).append(tempDistTime.getLeft()).append("km.\n");
                            path.append(purple_bold).append("|     ").append(blue_bold).append(stationRes.get(i - 1).getName());
                            LocalTime horaire = itineraryTimes.get(stationRes.get(i - 1));
                            if (horaire != null && !Objects.equals(lineRes.get(i).getName(), "--MARCHE--")) {
                                path.append(" - ").append(horaire);
                            }
                            path.append("\n");
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
     * @param distTimeToStart couples of distances and time for stations
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
     * Search all schedules for a station in a line
     *
     * @param m       the map we use for data
     * @param scanner the scanner for terminal
     */
    private static void searchSchedule(Map m, Scanner scanner) throws QuitException {

        // Afficher les lignes
        ArrayList<String> linesNames = printLines(m);

        // demande la ligne
        String lineChoice = askChoiceLine(scanner, linesNames);

        // afficher les stations de la ligne
        ArrayList<Station> stations = printStationsLine(lineChoice, m);

        // demande la station
        Station stationChoice = askChoiceStation(scanner, stations);

        // afficher les horaires de la station
        printSchedules(m, lineChoice, stationChoice);
    }

    /**
     * Print all possibles lines and return all the line names
     *
     * @param m the map
     * @return all the lines names
     */
    private static ArrayList<String> printLines(Map m) {

        System.out.println("\n\u001B[34mWe have the lines :\u001B[0m");
        ArrayList<Line> lines = m.getLines();
        ArrayList<String> linesNames = new ArrayList<>();

        for (Line line : lines) {
            String name = line.getLineNameWithoutVariant();
            if (!linesNames.contains(name) && !line.getStationsTimes().isEmpty()) {
                System.out.println("\u001B[34m--\u001B[0m " + name);
                linesNames.add(name);
            }
        }

        return linesNames;
    }

    /**
     * Ask the choice of the line
     *
     * @param scanner    the scanner for terminal
     * @param linesNames all the name's lines
     * @return the choice of the line
     */
    private static String askChoiceLine(Scanner scanner, ArrayList<String> linesNames) throws QuitException {

        System.out.println("\n\u001B[34mSelect your line :\u001B[0m");
        String choice = "";
        boolean choiceIsOk = false;

        while (!choiceIsOk) {

            choice = scanner.nextLine().trim();

            if (isChoiceLineCorrect(choice, linesNames)) {
                choiceIsOk = true;
            } else {
                System.out.println("Try again !");
            }
        }
        if (choice.equals("quit")) {
            throw new QuitException();
        } else {
            return choice;
        }
    }

    /**
     * Check if the choice of the line is correct
     *
     * @param choice     the choice of the line
     * @param linesNames all the name's lines
     * @return true if the choice is correct
     */
    private static boolean isChoiceLineCorrect(String choice, ArrayList<String> linesNames) {
        return linesNames.contains(choice) || choice.equals("quit");
    }

    /**
     * Print all the stations for a line and return all the stations
     *
     * @param line the line we want
     * @param m    the map
     * @return all the stations in the line
     */
    private static ArrayList<Station> printStationsLine(String line, Map m) {

        System.out.println("\n\u001B[34mFor the Line " + line + ", we have the stations :\u001B[0m");
        ArrayList<Line> lines = m.getLinesByName(line);
        ArrayList<Station> stations = getAllStationsForALine(lines);

        for (Station station : stations) {
            System.out.println("\u001B[34m-- \u001B[0m " + station.getName());
        }

        return stations;
    }

    /**
     * Get all the stations for a line
     *
     * @param lines all the lines
     * @return a list of all stations
     */
    private static ArrayList<Station> getAllStationsForALine(ArrayList<Line> lines) {
        ArrayList<Station> stations = new ArrayList<>();

        for (Line line : lines) {
            for (Station station : line.getStations()) {
                if (!stations.contains(station)) {
                    stations.add(station);
                }
            }
        }

        return stations;
    }

    /**
     * Ask the choice of the station
     *
     * @param scanner  the scanner for terminal
     * @param stations all the stations of a line
     * @return the station
     */
    private static Station askChoiceStation(Scanner scanner, ArrayList<Station> stations) throws QuitException {

        System.out.println("\n\u001B[34mSelect your station :\u001B[0m");
        String choice = "";
        boolean choiceIsOk = false;

        while (!choiceIsOk) {

            choice = scanner.nextLine().trim();

            if (isChoiceStationCorrect(choice, stations)) {
                choiceIsOk = true;
            } else {
                ArrayList<Station> list_2 = similar_names(StringUtils.stripAccents(choice), stations);
                if (!list_2.isEmpty()) return multi_choice_similar(list_2, scanner);
                System.out.println("Try again !");
            }

        }

        if (choice.equals("quit")) {
            throw new QuitException();
        } else {
            for (Station station : stations) {
                if (station.getName().equals(choice)) {
                    return station;
                }
            }
            return null;
        }
    }

    /**
     * Check if the choice of the station is correct
     *
     * @param choice   the choice of the station
     * @param stations all the stations
     * @return true if the choice is correct
     */
    private static boolean isChoiceStationCorrect(String choice, ArrayList<Station> stations) {
        for (Station station : stations) {
            if (station.getName().equals(choice)) {
                return true;
            }
        }
        return choice.equals("quit");
    }

    /**
     * print all the schedules for a station and a line
     *
     * @param m        the map
     * @param lineName the name of the line
     * @param station  the station
     */
    private static void printSchedules(Map m, String lineName, Station station) {
        ArrayList<Line> lines = m.getLinesByName(lineName);
        lines = getLinesWhoContainsStation(station, lines);
        ArrayList<LocalTime> schedules = new ArrayList<>();
        for (Line line : lines) {
            if (line.getStationTimes(station) != null) { // Au cas où ce serait un terminus : pas d'horaires
                for (LocalTime localTime : line.getStationTimes(station)) {
                    if (!schedules.contains(localTime)) {
                        schedules.add(localTime);
                    }
                }
            }
        }
        System.out.println("\n\u001B[34mFor the Station \"" + station.getName() + "\" on the Line " + lineName + ", we have theses schedules :\u001B[0m");

        // trier les horaires
        schedules.sort((o1, o2) -> {
            if (o1.isBefore(o2)) {
                return -1;
            } else if (o1.isAfter(o2)) {
                return 1;
            } else {
                return 0;
            }
        });
        int count = 1;
        for (LocalTime localTime : schedules) {
            System.out.print(localTime);
            if (count % 6 == 0) {
                System.out.println();
            } else {
                if (!(schedules.get(schedules.size() - 1) == localTime)) {
                    System.out.print(", ");
                }
            }
            count++;
        }

        System.out.println();

    }

    /**
     * Get all lines witch contains a station
     *
     * @param station the station
     * @param lines   all lines
     * @return all lines
     */
    private static ArrayList<Line> getLinesWhoContainsStation(Station station, ArrayList<Line> lines) {
        ArrayList<Line> linesWithStation = new ArrayList<>();

        for (Line line : lines) {
            if (line.getStations().contains(station)) {
                linesWithStation.add(line);
            }
        }

        return linesWithStation;
    }

}