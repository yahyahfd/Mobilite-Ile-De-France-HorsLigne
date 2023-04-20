package fr.uparis.beryllium.model;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalTime;
import java.util.Iterator;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import fr.uparis.beryllium.exceptions.FormatException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Parser {

    private static final Logger LOGGER = LogManager.getLogger(Parser.class);

    /**
     * Static method that is used to parse the map data file containing
     * the network data (CSV file)
     *
     * @param csvFile String of the path to the file
     * @return A map of the network described by
     * @throws FormatException if the appropriate format isn't respected
     */
    public static Map readMap(String csvFile) throws FormatException {

        Map map = new Map();
        try {

            String[] HEADERS = {"station1", "gps1", "station2", "gps2", "line", "duration", "dist"};

            Iterator<CSVRecord> it = getCsvRecordIterator(csvFile, HEADERS);
            if (!it.hasNext()) {
                LOGGER.warn("The file is empty");
            }

            while (it.hasNext()) {
                fillMapInformationsWithExtractedFields(map, it);
            }

        } catch (IllegalArgumentException e) {
            // the StackTrace doesn't tell where is the error, only lines of error in our code like line 12 in Controller, 127 in apache csv
            LOGGER.error("Csv format incorrect, the map is incomplete.", e);
        } catch (FileNotFoundException e) {
            LOGGER.error("File not found", e);
        } catch (IOException e) {
            LOGGER.error("Error while reading the file", e);
        }

        return map;

    }

    /**
     * Static method that is used to parse the timetables data file containing
     * @param csvFile
     * @param map
     * @return
     * @throws FormatException
     */

    public static Map readMapHorraire(String csvFile, Map map) throws FormatException{

        try {

            String[] HEADERS = {"line", "station", "time","variant"};

            Iterator<CSVRecord> it = getCsvRecordIterator(csvFile, HEADERS);
            if (!it.hasNext()) {
                LOGGER.warn("The file is empty");
            }

            while (it.hasNext()) {
                fillMapWithHorraires(map, it);
            }

        } catch (IllegalArgumentException e) {
            // the StackTrace doesn't tell where is the error, only lines of error in our code like line 12 in Controller, 127 in apache csv
            LOGGER.error("Csv format incorrect, the map is incomplete.", e);
        } catch (FileNotFoundException e) {
            LOGGER.error("File not found", e);
        } catch (IOException e) {
            LOGGER.error("Error while reading the file", e);
        }

        return map;

    }

    /**
     * static method that is used to fill each station of the lines of the map with times
     * @param map the map to fill
     * @param it iterator of the csv file
     */

    private static void fillMapWithHorraires(Map map, Iterator<CSVRecord> it){

        CSVRecord record = it.next();

        String lineString = record.get("line");
        String stationString = record.get("station");
        String time[] = record.get("time").split(":");
        String variant = record.get("variant");

        Line line = map.searchLine(lineString + "." + variant);
        Station station = map.searchStation(stationString, map.getStationByName(stationString).getLocalisations().get(lineString), lineString);

        line.addStationTime(station, LocalTime.of(Integer.parseInt(time[0]), Integer.parseInt(time[1])));
    }

    /**
     * Static method that is used to fill the map with the data extracted from the csv file
     * @param map the map to fill
     * @param it iterator of the csv file
     */

    private static void fillMapInformationsWithExtractedFields(Map map, Iterator<CSVRecord> it) {
        CSVRecord record = it.next();
        // Creating map
        String firstStation = record.get("station1");
        String secondStation = record.get("station2");
        String[] lineInCsv = record.get("line").split(" ");
        String[] gpsFirstStation = record.get("gps1").split(",");
        String[] gpsSecondStation = record.get("gps2").split(",");
        String[] duration = record.get("duration").split(":");
        String distance = record.get("dist");

        Localisation firstStationLocalisation = new Localisation(Double.parseDouble(gpsFirstStation[1]), Double.parseDouble(gpsFirstStation[0]));
        Localisation secondStationLocalisation = new Localisation(Double.parseDouble(gpsSecondStation[1]), Double.parseDouble(gpsSecondStation[0]));
        // Search for the stations and line
        // Add line with variant in the name
        Line line = map.searchLine(lineInCsv[0] + "." + lineInCsv[2]);

        // If they exist, search function returns their object in map's lists
        // Else, it creates a new object, put it in map's lists and return it
        Station stat1 = map.searchStation(firstStation, firstStationLocalisation, lineInCsv[0]);
        Station stat2 = map.searchStation(secondStation, secondStationLocalisation, lineInCsv[0]);

        // Add station to line's list
        // addStation verify if the station is already in the list or not
        line.addStation(stat1,firstStationLocalisation);
        line.addStation(stat2, secondStationLocalisation);

        // Add neighbours
        Line walkingLine = new Line("--MARCHE--");
        int radius1km = 1;
        stat1.addNextStation(stat2, line, duration, Double.parseDouble(distance) / 10, false, null);
        stat1.addWalkingNeighbours(walkingLine, map.getAllStations(), radius1km, false, firstStationLocalisation);
        stat2.addWalkingNeighbours(walkingLine, map.getAllStations(), radius1km, false, secondStationLocalisation);
    }


    /**
     * Static method that is used to get an iterator of the csv file
     * @param file the path to the file
     * @param headers the headers of the csv file
     * @return an iterator of the csv file
     * @throws IOException
     * @throws FileNotFoundException
     * @throws FormatException
     */
    private static Iterator<CSVRecord> getCsvRecordIterator(String file, String[] headers) throws IOException, FileNotFoundException, FormatException {
        FileReader reader = new FileReader(file);
        //Build a format of the csv file
        CSVFormat format = CSVFormat.DEFAULT.builder()
                .setHeader(headers)
                .setDelimiter(";")
                .build();


        CSVParser parser = new CSVParser(reader, format);
        Iterable<CSVRecord> records = parser.getRecords();

        checkCsvFileFormat(records, format);

        parser.close();
        return records.iterator();
    }

    /**
     * Static method that is used to check if the csv file is in the correct format
     * @param records
     * @param format
     * @throws FormatException
     */
    private static void checkCsvFileFormat(Iterable<CSVRecord> records, CSVFormat format) throws FormatException {
        for (CSVRecord record : records) {
            if (record.size() != format.getHeader().length) {
                throw new FormatException("The file is not in the correct format");
            }
        }
    }
}
