import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;
import java.util.*;
import minpq.UnsortedArrayMinPQ;

import minpq.MinPQ;
import minpq.OptimizedHeapMinPQ;

/**
 * Display the most commonly-reported WCAG recommendations.
 */
public class ReportAnalyzer {
    public static void main(String[] args) throws IOException {
        File inputFile = new File("data/wcag.tsv");
        Map<String, String> wcagDefinitions = new LinkedHashMap<>();
        Scanner scanner = new Scanner(inputFile);
        while (scanner.hasNextLine()) {
            String[] line = scanner.nextLine().split("\t", 2);
            String index = "wcag" + line[0].replace(".", "");
            String title = line[1];
            wcagDefinitions.put(index, title);
        }

        Pattern re = Pattern.compile("wcag\\d{3,4}");
        List<String> wcagTags = Files.walk(Paths.get("data/reports"))
                .map(path -> {
                    try {
                        return Files.readString(path);
                    } catch (IOException e) {
                        return "";
                    }
                })
                .flatMap(contents -> re.matcher(contents).results())
                .map(MatchResult::group)
                .toList();

        Map<String, Integer> tagCounts = new HashMap<>();

        for (String tag : wcagTags) {
            tagCounts.put(tag, tagCounts.getOrDefault(tag, 1) + 1);
        }

        UnsortedArrayMinPQ<String> pq = new UnsortedArrayMinPQ<>();

        for (Map.Entry<String, Integer> tag : tagCounts.entrySet()) {
            if (pq.contains(tag.getKey())) {
                pq.changePriority(tag.getKey(), -tag.getValue());
            } else {
                if (pq.size() < 3) {
                    pq.add(tag.getKey(), -tag.getValue());
                } else if (-tag.getValue() > pq.getPriority(pq.peekMin())) {
                    pq.removeMin();
                    pq.add(tag.getKey(), -tag.getValue());
                }
            }
        }

        List<String> topTags = new ArrayList<>();
        while (!pq.isEmpty()) {
            topTags.add(pq.removeMin());
        }

        Collections.reverse(topTags);
        int i = 0;
        for (String tag : topTags) {
            i++;
            System.out.println(i + ". " + wcagDefinitions.get(tag));
        }
    }
}