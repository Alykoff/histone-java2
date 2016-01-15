package ru.histone.v2;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.invoke.MethodHandles;
import java.net.URL;
import java.nio.file.Paths;

/**
 *
 * Created by gali.alykoff on 14/01/16.
 */
public class HistoneV2StandardFromJs {
    public static final String RESOURCE_PATH_TO_RUNNER = "resources/v2/tools/histone2_js_runner.js";
    public static final String RELATIVE_RESOURCE_PATH_TO_RUNNER = "src/test/" + RESOURCE_PATH_TO_RUNNER;
    private static final String PATH_TO_HISTONE2_JS_RUNNER = getRunnerJsAbsolutePath(RESOURCE_PATH_TO_RUNNER, RELATIVE_RESOURCE_PATH_TO_RUNNER);

    public static void main(String[] args) throws IOException {
        System.out.println(getNodes("sdjhfsdjkbfdsksd {{if 1 = 1 && '2' != 'fdsds4'}}1231231{{else}}aaa{{/if}}sdklbjfsdlkfdsbfsdksfd"));
        System.out.println(getTpl("sdjhfsdjkbfdsksd {{if 1 = 1 && '2' != 'fdsds4'}}1231231{{else}}aaa{{/if}}sdklbjfsdlkfdsbfsdksfd"));
    }

    public static String getNodes(String tpl) throws IOException {
        tpl = tpl.replaceAll("\"", "\"");
        final ProcessBuilder processBuilder = new ProcessBuilder(
                "node", PATH_TO_HISTONE2_JS_RUNNER, "--tpl=\"" + tpl + "\"", "-n"
        );
        return getProcessResult(processBuilder);
    }

    public static String getTpl(String tpl) throws IOException {
        tpl = tpl.replaceAll("\"", "\"");
        final ProcessBuilder processBuilder = new ProcessBuilder(
                "node", PATH_TO_HISTONE2_JS_RUNNER, "--tpl=\"" + tpl + "\""
        );
        return getProcessResult(processBuilder);
    }

    private static String getProcessResult(ProcessBuilder builder) throws IOException {
        final Process process = builder.start();
        try (
                final InputStream is = process.getInputStream();
                final InputStreamReader inputStreamReader = new InputStreamReader(is);
                final BufferedReader bufferedReader = new BufferedReader(inputStreamReader)
        ) {
            String line;
            String processResult = "";
            while ((line = bufferedReader.readLine()) != null) {
                processResult += line;
            }
            return processResult;
        }
    }

    private static String getRunnerJsAbsolutePath(String resourcePath, String relativePath) {
        final URL uriScript = MethodHandles
                .lookup()
                .lookupClass()
                .getClassLoader()
                .getResource(resourcePath);
        try {
            if (uriScript == null) {
                return Paths.get(RELATIVE_RESOURCE_PATH_TO_RUNNER).toAbsolutePath().toString();
            } else {
                return Paths.get(uriScript.toURI()).toAbsolutePath().toString();
            }
        } catch (Exception e) {
            throw new RuntimeException("histone2 js runner script not found", e);
        }
    }
}
