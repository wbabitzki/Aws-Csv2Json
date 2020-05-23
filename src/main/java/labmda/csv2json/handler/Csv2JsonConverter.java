package labmda.csv2json.handler;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Csv2JsonConverter {
    private final static Gson gson =new Gson();

    public String toJson(byte[] text, char separator) {
        final List<Map<String, String>> list = toListOfMaps(text, separator);
        return gson.toJson(list, new TypeToken<List<Map<String, String>>>() {}.getType());
    }

    List<Map<String, String>> toListOfMaps(byte[] text, char separator) {
        final List<String[]> entries = readList(text, separator);
        final Function<String[], Map<String, String>> stringsToMap = strings -> {
            final Map<String, String> map = new LinkedHashMap<>();
            for (int i = 0; i < strings.length; i++) {
                map.put(entries.get(0)[i], strings[i]);
            }
            return map;
        };
        return entries.stream()
                .skip(1)
                .map(stringsToMap)
                .collect(Collectors.toList());
    }

    private List<String[]> readList(byte[] text, char separator) {
        List<String[]> entries = null;
        CSVParser csvParser = new CSVParserBuilder().withSeparator(separator).build();
        try (InputStreamReader input = new InputStreamReader(new ByteArrayInputStream(text))) {
            final CSVReader csvReader = new CSVReaderBuilder(input).withCSVParser(csvParser).build();
            entries = csvReader.readAll();
        } catch (IOException | CsvException e) {
            throw new IllegalArgumentException(e);
        }
        return entries;
    }
}
