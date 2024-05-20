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

        // Count the occurrences of each WCAG tag
        Map<String, Integer> tagCounts = new HashMap<>();
        for (String tag : wcagTags) {
            tagCounts.put(tag, tagCounts.getOrDefault(tag, 0) + 1);
        }
        //System.out.println(tagCounts);

        // Create a MinPQ to store the top 3 most commonly-reported WCAG tags
        MinPQ<String> tagCountPQ = new OptimizedHeapMinPQ<>();
        for (Map.Entry<String, Integer> entry : tagCounts.entrySet()) {
            String tag = entry.getKey();
            int count = entry.getValue();
            tagCountPQ.addOrChangePriority(tag, count);

            if (tagCountPQ.size() > 3) {
                tagCountPQ.removeMin();
            }
        }

        // Retrieve and display the top 3 WCAG tags
        List<String> topTags = new ArrayList<>();
        while (!tagCountPQ.isEmpty()) {
            topTags.add(tagCountPQ.removeMin());
        }
        Collections.reverse(topTags);

        System.out.println("Top 3 WCAG Tags:");
        for (String tag : topTags) {
            System.out.println(wcagDefinitions.get(tag) + " (" + tagCounts.get(tag) + " occurrences)");
        }
    }
}