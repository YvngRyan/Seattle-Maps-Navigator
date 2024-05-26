package minpq;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Random;
import java.util.Scanner;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Abstract class providing test cases for all {@link MinPQ} implementations.
 *
 * @see MinPQ
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class MinPQTests {
    /**
     * Returns an empty {@link MinPQ}.
     *
     * @return an empty {@link MinPQ}
     */
    public abstract <E> MinPQ<E> createMinPQ();

    @Test
    public void wcagIndexAsPriority() throws FileNotFoundException {
        File inputFile = new File("data/wcag.tsv");
        MinPQ<String> reference = new DoubleMapMinPQ<>();
        MinPQ<String> testing = createMinPQ();
        Scanner scanner = new Scanner(inputFile);
        while (scanner.hasNextLine()) {
            String[] line = scanner.nextLine().split("\t", 2);
            int index = Integer.parseInt(line[0].replace(".", ""));
            String title = line[1];
            reference.add(title, index);
            testing.add(title, index);
        }
        while (!reference.isEmpty()) {
            assertEquals(reference.removeMin(), testing.removeMin());
        }
        assertTrue(testing.isEmpty());
    }

    @Test
    public void randomPriorities() {
        int[] elements = new int[1000];
        for (int i = 0; i < elements.length; i = i + 1) {
            elements[i] = i;
        }
        Random random = new Random(373);
        int[] priorities = new int[elements.length];
        for (int i = 0; i < priorities.length; i = i + 1) {
            priorities[i] = random.nextInt(priorities.length);
        }

        MinPQ<Integer> reference = new DoubleMapMinPQ<>();
        MinPQ<Integer> testing = createMinPQ();
        for (int i = 0; i < elements.length; i = i + 1) {
            reference.add(elements[i], priorities[i]);
            testing.add(elements[i], priorities[i]);
        }

        for (int i = 0; i < elements.length; i = i+1) {
            int expected = reference.removeMin();
            int actual = testing.removeMin();

            if (expected != actual) {
                int expectedPriority = priorities[expected];
                int actualPriority = priorities[actual];
                assertEquals(expectedPriority, actualPriority);
            }
        }
    }

    private static class Tag implements Comparable<Tag> {
        String name;
        double priority;

        Tag(String name, double priority) {
            this.name = name;
            this.priority = priority;
        }

        @Override
        public int compareTo(Tag other) {
            if (this.priority != other.priority) {
                return Double.compare(this.priority, other.priority);
            } else {
                return this.name.compareTo(other.name);
            }
        }

        @Override
        public String toString() {
            return name; // Useful for debugging
        }
    }

    @Test
    public void testRandomWcagTagCounts() throws IOException {
        // Read WCAG tags from the file
        File wcagFile = new File("data/wcag.tsv");
        Scanner scanner = new Scanner(wcagFile);
        List<String> wcagTags = new ArrayList<>();
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            if (!line.isEmpty()) {
                String[] parts = line.split("\\t");
                String tag = parts[0].trim().replace(".", ""); // Remove dots for simplicity
                wcagTags.add("wcag" + tag); // Prefix with "wcag" to form a complete tag
            }
        }
        scanner.close();

        // Define the top commonly-reported tags (upweight these tags)
        List<String> frequentlyReportedTags = Arrays.asList("wcag111", "wcag121", "wcag122");
        int upweightFactor = 10; // Each of these tags will be 10 times more likely to be chosen

        MinPQ<Tag> reference = new OptimizedHeapMinPQ<>();
        MinPQ<Tag> testing = new OptimizedHeapMinPQ<>();
        Random random = new Random();
        Map<String, Integer> tagCounts = new HashMap<>();
        int totalTags = 10000;

        // Randomly add or update tag counts with upweighted frequently reported tags
        for (int i = 0; i < totalTags; i++) {
            String randomTag;
            if (random.nextInt(upweightFactor + 1) == 0) { // Random chance to pick a frequently reported tag
                randomTag = frequentlyReportedTags.get(random.nextInt(frequentlyReportedTags.size()));
            } else {
                randomTag = wcagTags.get(random.nextInt(wcagTags.size()));
            }
            int newCount = tagCounts.getOrDefault(randomTag, 0) + 1;
            tagCounts.put(randomTag, newCount);
            reference.addOrChangePriority(new Tag(randomTag, newCount), newCount);
            testing.addOrChangePriority(new Tag(randomTag, newCount), newCount);
        }

        // Remove all tags and check order
        while (!reference.isEmpty() && !testing.isEmpty()) {
            assertEquals(reference.removeMin().toString(), testing.removeMin().toString());
        }

        // Check that both queues are empty at the end
        assertTrue(reference.isEmpty());
        assertTrue(testing.isEmpty());
    }
}
