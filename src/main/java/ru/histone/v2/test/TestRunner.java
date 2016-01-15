package ru.histone.v2.test;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.apache.commons.io.IOUtils;
import ru.histone.v2.test.dto.HistoneTestCase;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by inv3r on 14/01/16.
 */
public class TestRunner {
    public static List<HistoneTestCase> loadTestCase(String testPath) {
        Stream<String> testStream = null;
        try {
            testStream = IOUtils.readLines(TestRunner.class.getResourceAsStream("/" + testPath), "UTF-8").stream();
        } catch (IOException e) {
            throw new RuntimeException("Failed load file from " + testPath);
        }
        Gson gson = new Gson();
        Type listType = new TypeToken<List<HistoneTestCase>>() {
        }.getType();
        List<HistoneTestCase> cases = gson.fromJson(testStream.collect(Collectors.joining()), listType);
        return cases;
    }
}
