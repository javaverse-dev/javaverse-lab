package dev.javaverse.decision;

import org.tinylog.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

///
/// Eagerly load, parses and report error if any found in the csv file.
///
/// @author Javaverse
class Csv {

    private final List<Map<String, String>> data;

    Csv(String filename) {
        this.data = parse(filename);
    }

    List<String> getAll(String header) {
        return data.stream()
                .map(l -> l.get(header))
                .toList();
    }

    List<String> getTop(String header, int limit) {
        return data.stream()
                .limit(limit)
                .map(l -> l.get(header))
                .toList();
    }

    @Override
    public String toString() {
        return Objects.toString(data);
    }

    private List<Map<String, String>> parse(String filename) {
        var is = getClass().getResourceAsStream(filename);
        if (is == null) {
            throw new IllegalArgumentException("'%s' not found".formatted(filename));
        }

        try (var reader = new BufferedReader(new InputStreamReader(is))) {

            var lines = reader.lines().toList();
            if (lines.isEmpty()) {
                return List.of();
            }

            var headers = headers(lines.getFirst());
            var body = body(lines);

            Logger.debug("Headers: {}", () -> Arrays.toString(headers));
            return createData(headers, body);

        } catch (IOException e) {
            throw new RuntimeException("Failed to parse: " + filename, e);
        }
    }


    private List<Map<String, String>> createData(String[] headers, List<String[]> body) {

        var data = new ArrayList<Map<String, String>>();

        for (String[] line : body) {
            var map = new LinkedHashMap<String, String>();

            for (int i = 0; i < headers.length; i++) {
                var value = i < line.length ? line[i] : "";
                map.put(headers[i], value);
            }

            data.add(map);
        }

        return data;
    }

    private String[] headers(String headerStr) {
        return Arrays.stream(headerStr.replace("\uFEFF", "").split(",", -1))
                .map(String::trim)
                .toArray(String[]::new);
    }

    private List<String[]> body(List<String> lines) {
        return lines.stream()
                .skip(1)
                .map(line -> line.split(",", -1))
                .toList();

    }

}
