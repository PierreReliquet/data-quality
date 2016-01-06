package org.talend.dataquality.statistics.datetime;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

public class PerformanceComparer {

    private static final Logger LOGGER = Logger.getLogger(PerformanceComparer.class);

    private static final List<String> CUSTOM_PATTERN_LIST = Arrays.asList(new String[] {//
            "yyyy/MMM/d", "yyyy/MMM/dd", "yyyy/MMMM/dd" });

    private List<String> LINES = new ArrayList<>();

    {

        InputStream stream = SystemDateTimePatternManager.class.getResourceAsStream("DateSampleTable.txt");
        try {
            List<String> allLines = IOUtils.readLines(stream);
            for (int i = 1; i < 11; i++) {
                LINES.add(allLines.get(i));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public long testIsDateWithCustomPatterns(boolean useNewImpl, int replicate) {

        SystemDateTimePatternManager.isDate("12/02/99");
        CustomDateTimePatternManager.isDate("12/02/99", CUSTOM_PATTERN_LIST);
        Date begin = new Date();
        LOGGER.debug("Detect date start at: " + begin);

        for (int i = 0; i < LINES.size(); i++) {
            for (int n = 0; n < replicate; n++) {
                String line = LINES.get(i);
                if (!"".equals(line.trim())) {
                    String[] sampleLine = line.trim().split("\t");
                    String sample = sampleLine[0];
                    if (useNewImpl) {
                        CustomDateTimePatternManager.isDate(sample, CUSTOM_PATTERN_LIST);
                    } else {
                        CustomDateTimePatternManagerOld.isDate(sample, CUSTOM_PATTERN_LIST);
                    }
                }
            }
        }

        Date end = new Date();
        LOGGER.debug("Detect date end at: " + end);
        long difference = end.getTime() - begin.getTime();

        LOGGER.debug("Detect date time diff: " + difference + " ms.");
        // System.out.println("Total duration with " + (useNewImpl ? "NEW" : "OLD") + " solution on " + count +
        // " samples: " + difference + "ms");
        return difference;
    }

    public static void main(String[] args) throws IOException {
        PerformanceComparer comparer = new PerformanceComparer();

        comparer.runTest(1234); // warm-up
        for (int i = 0; i < 11; i++) {
            comparer.runTest(1000 * (int) Math.pow(2, i));
        }

    }

    private void runTest(int replicate) {
        final double trialCount = 3;
        long oldSum = 0;
        for (int i = 0; i < trialCount; i++) {
            oldSum += testIsDateWithCustomPatterns(false, replicate);
        }
        double oldAverage = oldSum / trialCount;
        System.out.println("Average Duration with old solution: " + oldAverage);

        long newSum = 0;
        for (int i = 0; i < trialCount; i++) {
            newSum += testIsDateWithCustomPatterns(true, replicate);
        }
        double newAverage = newSum / trialCount;
        System.out.println("Average Duration with new solution: " + newAverage);

        double rate = (oldAverage - newAverage) * 10000 / newAverage;
        System.out.println("On " + replicate * LINES.size() + " records, the new solution is " + rate / 100 + "% faster.\n");

    }
}
