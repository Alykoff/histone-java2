package ru.histone.v2.test;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import ru.histone.v2.test.dto.HistoneTestCase;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URISyntaxException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by inv3r on 14/01/16.
 */
public class TestRunner {
    public static List<HistoneTestCase> loadTestCases() throws URISyntaxException, IOException {
        DirectoryStream<Path> stream = Files.newDirectoryStream(Paths.get(TestRunner.class.getResource("/acceptance").toURI()));

        Gson gson = new Gson();
        Type listType = new TypeToken<List<HistoneTestCase>>() {}.getType();

        List<HistoneTestCase> cases = new ArrayList<>();
        for (Path path : stream) {
            List<Path> paths = getFiles(path);
            for (Path p : paths) {
                Stream<String> stringStream = Files.lines(p);
                List<HistoneTestCase> histoneCases = gson.fromJson(stringStream.collect(Collectors.joining()), listType);
                cases.addAll(histoneCases);
            }
        }
        return cases;
    }

    private static List<Path> getFiles(Path path) throws IOException {
        List<Path> files = new ArrayList<>();
        if (Files.isDirectory(path)) {
            for (Path p : Files.newDirectoryStream(path)) {
                files.addAll(getFiles(p));
            }
        } else {
            files.add(path);
        }
        return files;
    }
}
