package fr.uparis.beryllium.model;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
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
     * Static method that is used to parse a file containing
     * the network data (CSV file)
     *
     * @param file String of the path to the file
     * @return A map of the network described by 
     * @throws FormatException if the appropriate format isn't respected
     */
    public static Map readMap(String csvFile) throws FormatException {

        Map map = new Map();
        try {

            Iterator<CSVRecord> it = getCsvRecordIterator(csvFile);
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

        // Search for the stations and line
        // If they exist, search function return their object in map's lists
        // Else, it create a new object, put it in map's lists and return it
        Station stat1 = map.searchStation(firstStation, new Localisation(Double.parseDouble(gpsFirstStation[0]), Double.parseDouble(gpsFirstStation[1])));
        Station stat2 = map.searchStation(secondStation, new Localisation(Double.parseDouble(gpsSecondStation[0]), Double.parseDouble(gpsSecondStation[1])));
        // Add line with variant in the name
        Line line = map.searchLine(lineInCsv[0] + "." + lineInCsv[2]);

        // Add station to line's list
        // addStation verify if the station is already in the list or not
        line.addStation(stat1);
        line.addStation(stat2);

        // Add neighbours
        stat1.addNextStation(stat2, line, duration, Double.parseDouble(distance));
        stat2.addNextStation(stat1, line, duration, Double.parseDouble(distance));
    }

    private static Iterator<CSVRecord> getCsvRecordIterator(String file) throws IOException, FormatException {
        FileReader reader = new FileReader(file);
        //Build format of the csv file
        CSVFormat format = CSVFormat.DEFAULT.builder()
                .setHeader("station1", "gps1", "station2", "gps2", "line", "duration", "dist")
                .setDelimiter(";")
                .build();


        CSVParser parser = new CSVParser(reader, format);
        Iterable<CSVRecord> records = parser.getRecords();

        checkCsvFileFormat(records, format);


        return records.iterator();
    }

    private static void checkCsvFileFormat(Iterable<CSVRecord> records, CSVFormat format) throws FormatException {
        for (CSVRecord record : records) {
            if (record.size() != format.getHeader().length) {
                throw new FormatException("The file is not in the correct format");
            }
        }
    }
}
