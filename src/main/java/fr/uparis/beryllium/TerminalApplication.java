package fr.uparis.beryllium;

import fr.uparis.beryllium.exceptions.FormatException;
import fr.uparis.beryllium.exceptions.QuitException;
import fr.uparis.beryllium.model.Itinerary;
import fr.uparis.beryllium.model.Line;
import fr.uparis.beryllium.model.Map;
import fr.uparis.beryllium.model.Parser;
import fr.uparis.beryllium.model.Station;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.MutableTriple;

import java.time.Duration;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
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
     * @throws UnsupportedEncodingException
     */
    public static void main(String[] args) throws FormatException {

        System.out.println("\u001B[36mWelcome to our interactive (Terminal Only) program for finding routes.");
        System.out.println("\u001B[36mIf you ever want to leave, just type \u001B[31mquit\u001B[0m");
        System.out.println("\033[1;30mLoading data... Might take some time...\u001B[0m");

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
            scanner.close();
        }

    }

    /**
     * Method to know if the first choice (itinerary or schedules) is correct
     *
     * @param choice the string choice
     * @return true if our choice is correct
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
            if (station1.trim().equalsIgnoreCase("quit") ||
                    station1.trim().equalsIgnoreCase("lp")) break;
            chosen_1 = m.getStationsByName(station1);
            if (chosen_1.size() == 0 && !station1.isEmpty()) {
                ArrayList<Station> list_1 = similar_names(StringUtils.stripAccents(station1), m.getStations());
                if (!list_1.isEmpty()) {
                    chosen_1 = multi_choice_similar(m, list_1, scanner);
                } else {
                    System.out.println("\nNo station with the name " + station1 + " was found !");
                    System.out.println("Try again!");
                }
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
            if (station2.trim().equalsIgnoreCase("quit") ||
                    station2.trim().equalsIgnoreCase("lp")) break;
            chosen_2 = m.getStationsByName(station2);
            if (chosen_2.size() == 0 && !station2.isEmpty()) {
                ArrayList<Station> list_2 = similar_names(StringUtils.stripAccents(station2), m.getStations());
                if (!list_2.isEmpty()) {
                    chosen_2 = multi_choice_similar(m, list_2, scanner);
                } else {
                    System.out.println("\nNo station with the name " + station2 + " was found !");
                    System.out.println("Try again!");
                }
            }
            if (chosen_2.containsAll(chosen_1)) {
                System.out.println("This is your start station, please enter another!");
                chosen_2.clear();
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
     * Choose a station between a list of stations print
     *
     * @param stations list of stations
     * @param scanner  the scanner for terminal
     * @return the station chosen
     */
    private static ArrayList<Station> chooseAStation(Map m, ArrayList<Station> stations, Scanner scanner) throws QuitException {

        int i = 1;
        ArrayList<String> stationNames = new ArrayList<>();

        for (Station s : stations) {

            if (!stationNames.contains(s.getName())) {

                ArrayList<Station> stationWithSameName = m.getStationsByName(s.getName());
                ArrayList<String> neighborLinesForAllStations = new ArrayList<>();

                for (Station station : stationWithSameName) {

                    ArrayList<String> neighborLinesForAStation = station.getNeighboringLines();
                    for (String name : neighborLinesForAStation) {
                        if (!neighborLinesForAllStations.contains(name.split("\\.")[0])) {
                            neighborLinesForAllStations.add(name.split("\\.")[0]);
                        }
                    }

                }

                if (neighborLinesForAllStations.isEmpty()) {
                    System.out.println(i + ") " + s + ": (pas de correspondances)");
                } else {
                    System.out.println(i + ") " + s + ": " + neighborLinesForAllStations);
                }
                i++;

                stationNames.add(s.getName());
            }
        }
        int num_chosen = 0;
        while (num_chosen == 0 || num_chosen > i - 1) {
            try {
                String read = scanner.nextLine();
                if (read.trim().equalsIgnoreCase("quit")) throw new QuitException();
                num_chosen = Integer.parseInt(read);
                System.out.println(stationNames.get(num_chosen - 1) + " was chosen");
            } catch (NumberFormatException e) {
                System.out.println("You need to choose a valid number ! Try again ! ");
            }
        }
        return m.getStationsByName(stationNames.get(num_chosen - 1));
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
    private static ArrayList<Station> multi_choice_similar(Map m, ArrayList<Station> possibilities, Scanner scanner) throws QuitException {
        System.out.println("\nNo station with this name was found !");
        System.out.println("But possibilites found. Choose one from the list below:");
        return chooseAStation(m, possibilities, scanner);
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
     * @param localpositionDest  if we're going to a localisation and not to a station
     * @param preference         our preference for the itinerary
     */
    private static void findRoute(Map m, ArrayList<Station> chosen_1, ArrayList<Station> chosen_2, boolean localpositionStart, boolean localpositionDest, int preference, Itinerary i) {
        // we search for all stations that we can go by feet within a certain perimeter (dist from start to dest)
        if (localpositionStart) {
            m.walkToBestStation(chosen_1.get(0), true, chosen_2.get(0).getLocation());
        }
        // we add the neighbors for the destination station
        if (localpositionDest) {
            m.walkToBestStation(chosen_2.get(0), false, chosen_1.get(0).getLocation());
        }

        // get the shortest way depending on the preference
        LocalTime timeWeLeft = LocalTime.now();
        HashMap<Station, Line> route = i.shortestMultiplePaths(chosen_1, chosen_2, preference, timeWeLeft);
        // We'll add verifications here to check if the names are valid (I don't know if it's necessary?)
        // If we add verifications, we'll set station1 or station2's colors to green or red whether they exist or not
        // We add the method (the algorithm) to look for the path
        if (route == null) {
            System.out.println("Looks like there is no route to go from \u001B[31m" + chosen_1.get(0).getName() + "\u001B[0m to \u001B[31m" + chosen_2.get(0).getName() + "\u001B[0m");
        } else {
            System.out.println("Route to go from \u001B[31m" + chosen_1.get(0).getName() + "\u001B[0m to \u001B[31m" + chosen_2.get(0).getName() + "\u001B[0m :\n");
            System.out.println(showPath(route, timeWeLeft, i));
        }
        // if we added temporary station, we remove them of the list of stations
        if (localpositionStart) {
            m.removeStation(chosen_1.get(0));
            localpositionStart = false;
        }
        if (localpositionDest) {
            chosen_2.get(0).getNextStations().clear();
            m.removeStation(chosen_2.get(0));
            localpositionDest = false;
        }
    }

    /**
     * Show the shortest way to go from a station to another
     *
     * @param res        Res of the algorithm
     * @param timeWeLeft the time we left
     * @param itinerary  the itinerary
     * @return a string of the stations and line in order
     */
    private static String showPath(HashMap<Station, Line> res, LocalTime timeWeLeft, Itinerary itinerary) {
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
                    MutableTriple<Double, Integer, Long> distCountTimeFromDestination = itinerary.getDistCountTimeToStart().get(stationRes.get(stationRes.size() - 1));
                    Duration d = Duration.ZERO;
                    d = d.plusMillis(distCountTimeFromDestination.getRight());
                    path.append(yellow_bold).append("-- Trajet :     ").append(d.toMinutes() + 1).append("min. ").append(normalColor).append("~ ").append(yellow_bold).append(distCountTimeFromDestination.getLeft()).append("km.\n");
                    distTime = itinerary.getDistTimeForALine(stationRes, lineRes, i);
                    path.append(purple_bold).append("Ligne ").append(lineRes.get(1).getLineNameWithoutVariant()).append(": ").append("     ").append(yellow_bold).append(distTime.getRight()).append("min. ").append(normalColor).append("~ ").append(yellow_bold).append(distTime.getLeft()).append("km.\n");
                    LocalTime horaire = itinerary.getItineraryTimes().get(stationRes.get(i));
                    path.append(purple_bold).append("|     ").append(blue_bold).append(stationRes.get(i).getName());
                    if (horaire != null && !Objects.equals(lineRes.get(i).getName(), "--MARCHE--")) {
                        path.append(" - ").append(horaire);
                    }
                    path.append("\n");
                } else {
                    if (i != 1) {
                        if (lineRes.get(i) != lineRes.get(i - 1) && lineRes.get(i) != null) {
                            MutablePair<Double, Long> tempDistTime = itinerary.getDistTimeForALine(stationRes, lineRes, i - 1);
                            path.append(purple_bold).append("Ligne ").append(lineRes.get(i).getLineNameWithoutVariant()).append(": ").append("     ").append(yellow_bold).append(tempDistTime.getRight()).append("min. ").append(normalColor).append("~ ").append(yellow_bold).append(tempDistTime.getLeft()).append("km.\n");
                            path.append(purple_bold).append("|     ").append(blue_bold).append(stationRes.get(i - 1).getName());
                            LocalTime horaire = itinerary.getItineraryTimes().get(stationRes.get(i - 1));
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
     * Search all schedules for a station in a line
     *
     * @param m       the map we use for data
     * @param scanner the scanner for terminal
     */
    private static void searchSchedule(Map m, Scanner scanner) throws QuitException {

        // demande la station
        ArrayList<Station> stationChoice = askChoiceStation(scanner, m);

        // Afficher les lignes
        ArrayList<String> linesNames = printLines(stationChoice);

        // demande la ligne
        List<String> lineChoice = askChoiceLine(scanner, linesNames);

        // afficher les horaires de la station
        printSchedules(m, lineChoice, stationChoice);
    }

    /**
     * Ask the choice of the station
     *
     * @param scanner the scanner for terminal
     * @param m       the map
     * @return the station
     */
    private static ArrayList<Station> askChoiceStation(Scanner scanner, Map m) throws QuitException {

        System.out.println("\n\u001B[34mSelect your station :\u001B[0m");
        String choice = "";
        boolean choiceIsOk = false;

        while (!choiceIsOk) {

            choice = scanner.nextLine().trim();

            if (isChoiceStationCorrect(choice, m)) {
                choiceIsOk = true;
            } else {
                ArrayList<Station> list_2 = similar_names(StringUtils.stripAccents(choice), m.getStations());
                if (!list_2.isEmpty()) return multi_choice_similar(m, list_2, scanner);
                System.out.println("Try again !");
            }

        }

        if (choice.equals("quit")) {
            throw new QuitException();
        } else {
            return m.getStationsByName(choice);
        }
    }

    /**
     * Check if the choice of the station is correct
     *
     * @param choice the choice of the station
     * @param m      the map
     * @return true if the choice is correct
     */
    private static boolean isChoiceStationCorrect(String choice, Map m) {
        return m.getStationsByName(choice).size() != 0;
    }

    /**
     * Print all possibles lines and return all the line names
     *
     * @param stations les stations dont nous voulons voir les lignes qu'elles désservent
     * @return all the lines names
     */
    private static ArrayList<String> printLines(ArrayList<Station> stations) {

        System.out.println("\n\u001B[34mWe have the lines :\u001B[0m");
        ArrayList<String> linesNames = new ArrayList<>();
        ArrayList<String> linesNamesWithoutVariant = new ArrayList<>();

        for (Station station : stations) {
            ArrayList<String> names = station.getNeighboringLines();
            for (String name : names) {
                if (!linesNames.contains(name)) {
                    linesNames.add(name);
                    String lineNameWithoutVariant = name.split("\\.")[0];
                    if (!linesNamesWithoutVariant.contains(lineNameWithoutVariant)) {
                        System.out.println("\u001B[34m--\u001B[0m " + lineNameWithoutVariant);
                        linesNamesWithoutVariant.add(lineNameWithoutVariant);
                    }

                }
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
    private static List<String> askChoiceLine(Scanner scanner, ArrayList<String> linesNames) throws QuitException {

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
            String finalChoice = choice;
            return linesNames.stream().filter(s -> s.split("\\.")[0].equals(finalChoice)).toList();
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
        for (String name : linesNames) {
            if (name.split("\\.")[0].equals(choice)) {
                return true;
            }
        }
        return choice.equals("quit");
    }

    /**
     * print all the schedules for a station and a line
     *
     * @param m         the map
     * @param lineNames names of the line (because of variants it's a list)
     * @param stations  stations we want the schedules
     */
    private static void printSchedules(Map m, List<String> lineNames, ArrayList<Station> stations) {

        ArrayList<LocalTime> schedules = new ArrayList<>();

        for (Station station : stations) {
            ArrayList<String> neighborLines = station.getNeighboringLines();

            for (String name : lineNames) {
                if (neighborLines.contains(name)) {
                    Line l = m.searchLine(name);
                    ArrayList<LocalTime> schedulesForAStation = station.getSchedulesOfLine(l);
                    if (schedulesForAStation != null) { // in case it's a terminus: no schedules
                        for (LocalTime localTime : schedulesForAStation) {
                            if (!schedules.contains(localTime)) {
                                schedules.add(localTime);
                            }
                        }
                    }
                }
            }

        }

        System.out.println("\n\u001B[34mFor the Station \"" + stations.get(0).getName() + "\" on the Line " + lineNames.get(0).split("\\.")[0] + ", we have theses schedules :\u001B[0m");

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
            System.out.print(localTime + "  --  ");
            if (count % 10 == 0) {
                System.out.println("\n");
            }
            count++;
        }

        System.out.println();

    }

}