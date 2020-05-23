package labmda.csv2json.handler;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class Csv2JsonConverterTest {

    private Csv2JsonConverter testee = new Csv2JsonConverter();

    @Test
    void toListOfMaps_oneLineWithHeader_returnsListOfMaps() {
        // arrange
        String text = "fruit;color;weight\r\norange;yellow;250";
        // act
        List<Map<String, String>> result = testee.toListOfMaps(text.getBytes(), ';');
        // assert
        assertEquals("orange", result.get(0).get("fruit"));
        assertEquals("yellow", result.get(0).get("color"));
        assertEquals("250", result.get(0).get("weight"));
    }

    @Test
    void toJson_oneLineWithHeader_returnsJson() {
        // arrange
        String text = "fruit;color;weight\r\norange;yellow;250";
        // act
        String result = testee.toJson(text.getBytes(), ';');
        // assert
        assertEquals("[{\"fruit\":\"orange\",\"color\":\"yellow\",\"weight\":\"250\"}]", result);
    }
}