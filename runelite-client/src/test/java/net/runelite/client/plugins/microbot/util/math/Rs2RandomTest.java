package net.runelite.client.plugins.microbot.util.math;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import net.runelite.api.Point;
import java.awt.Rectangle;
//import net.runelite.api.geometry.RectangleUnion.Rectangle;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.statistics.HistogramDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;



class Rs2RandomTest {
    private static final String TEST_RESOURCES_PATH = "src/test/resources";
    private static final String IMAGE_OUTPUT_DIR = TEST_RESOURCES_PATH + "/Rs2RandomTest";
    @BeforeAll
    static void setUp() throws IOException {
        // Create the output directory if it doesn't exist
        Files.createDirectories(Paths.get(IMAGE_OUTPUT_DIR));
    }
    private void saveChart(JFreeChart chart, String fileName) throws IOException {
        File outputFile = new File(IMAGE_OUTPUT_DIR, fileName);
        ChartUtils.saveChartAsPNG(outputFile, chart, 600, 400);
    }

    @Test
    void testTruncatedNormalSample() throws IOException {
        double lowerBound = 0;
        double upperBound = 100;
        int sampleSize = 100000;

        HistogramDataset dataset = new HistogramDataset();
        double[] values = new double[sampleSize];

        for (int i = 0; i < sampleSize; i++) {
            values[i] = Rs2Random.truncatedNormalSample(lowerBound, upperBound, null, null);
        }

        dataset.addSeries("Truncated Normal", values, 100);

        JFreeChart histogram = ChartFactory.createHistogram(
                "Truncated Normal Distribution",
                "Value",
                "Frequency",
                dataset,
                PlotOrientation.VERTICAL,
                true,
                true,
                false
        );

        saveChart(histogram, "truncated_normal_histogram.png");
    }

    @Test
    void testFancyNormalSample() throws IOException {
        double lowerBound = 0;
        double upperBound = 100;
        int sampleSize = 100000;

        HistogramDataset dataset = new HistogramDataset();
        double[] values = new double[sampleSize];

        for (int i = 0; i < sampleSize; i++) {
            values[i] = Rs2Random.fancyNormalSample(lowerBound, upperBound);
        }

        dataset.addSeries("Fancy Normal", values, 100);

        JFreeChart histogram = ChartFactory.createHistogram(
                "Fancy Normal Distribution",
                "Value",
                "Frequency",
                dataset,
                PlotOrientation.VERTICAL,
                true,
                true,
                false
        );

        saveChart(histogram, "fancy_normal_histogram.png");
    }

    @Test
    void testRandomPointIn() throws IOException {
        int xMin = 100;
        int yMin = 100;
        int width = 200;
        int height = 150;
        int sampleSize = 10000;

        XYSeries series = new XYSeries("Random Points");
        List<double[]> seeds = Rs2Random.randomSeeds(0, 8, 12);

        for (int i = 0; i < sampleSize; i++) {
            int[] point = Rs2Random.randomPointIn(xMin, yMin, width, height, seeds);
            series.add(point[0], point[1]);
        }

        XYSeriesCollection dataset = new XYSeriesCollection(series);

        JFreeChart scatterPlot = ChartFactory.createScatterPlot(
                "Random Points in Rectangle",
                "X",
                "Y",
                dataset,
                PlotOrientation.VERTICAL,
                true,
                true,
                false
        );

        saveChart(scatterPlot, "random_points_scatter.png");
    }
     @Test
    void testChiSquaredSample() throws IOException {
        int df = 3;
        double min = 0;
        double max = Double.POSITIVE_INFINITY;
        int sampleSize = 100000;

        HistogramDataset dataset = new HistogramDataset();
        double[] values = new double[sampleSize];

        for (int i = 0; i < sampleSize; i++) {
            values[i] = Rs2Random.chiSquaredSample(df, min, max);
        }

        dataset.addSeries("Chi-Squared", values, 100);

        JFreeChart histogram = ChartFactory.createHistogram(
                "Chi-Squared Distribution (df = " + df + ")",
                "Value",
                "Frequency",
                dataset,
                PlotOrientation.VERTICAL,
                true,
                true,
                false
        );

        saveChart(histogram, "chi_squared_histogram.png");
    }

    @Test
    void testRandomPointEx() throws IOException {
        Point from = new Point(150, 150);
        Rectangle rect = new Rectangle(100, 100, 200, 150);
        double force = 0.5;
        int sampleSize = 10000;

        XYSeries series = new XYSeries("Random Points");

        for (int i = 0; i < sampleSize; i++) {
            Point point = Rs2Random.randomPointEx(from, rect, force);
            series.add(point.getX(), point.getY());
        }

        XYSeriesCollection dataset = new XYSeriesCollection(series);

        JFreeChart scatterPlot = ChartFactory.createScatterPlot(
                "Random Points Ex in Rectangle",
                "X",
                "Y",
                dataset,
                PlotOrientation.VERTICAL,
                true,
                true,
                false
        );

        saveChart(scatterPlot, "random_points_ex_scatter.png");
    }

    @Test
    void testRandomWeightedChoiceWithProbability() throws IOException {
        List<String> items = Arrays.asList("A", "B", "C", "D", "E");
        int sampleSize = 100000;

        Map<String, Integer> counts = items.stream().collect(Collectors.toMap(item -> item, item -> 0));
        Map<String, Double> probabilities = items.stream().collect(Collectors.toMap(item -> item, item -> 0.0));

        for (int i = 0; i < sampleSize; i++) {
            Rs2Random.WeightedResult<String> result = Rs2Random.randomWeightedChoiceWithProbability(items);
            String selectedItem = result.getItem();
            counts.put(selectedItem, counts.get(selectedItem) + 1);
            probabilities.put(selectedItem, result.getProbability());
        }

        XYSeries countSeries = new XYSeries("Counts");
        XYSeries probabilitySeries = new XYSeries("Probabilities");

        for (int i = 0; i < items.size(); i++) {
            String item = items.get(i);
            countSeries.add(i, counts.get(item));
            probabilitySeries.add(i, probabilities.get(item));
        }

        XYSeriesCollection dataset = new XYSeriesCollection();
        dataset.addSeries(countSeries);
        dataset.addSeries(probabilitySeries);

        JFreeChart barChart = ChartFactory.createXYBarChart(
                "Random Weighted Choice Results",
                "Item",
                false,
                "Count / Probability",
                dataset,
                PlotOrientation.VERTICAL,
                true,
                true,
                false
        );

        saveChart(barChart, "random_weighted_choice_results.png");

        // Additional assertions
        for (String item : items) {
            double expectedProbability = (double) counts.get(item) / sampleSize;
            double actualProbability = probabilities.get(item);
            assertEquals(expectedProbability, actualProbability, 0.01, "Probability for " + item + " should be close to the observed frequency");
        }
    }
}