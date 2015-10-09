// ============================================================================
//
// Copyright (C) 2006-2015 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.dataquality.statistics.quality;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.junit.Test;
import org.talend.dataquality.semantic.classifier.SemanticCategoryEnum;
import org.talend.dataquality.semantic.statistics.SemanticQualityAnalyzer;
import org.talend.datascience.common.inference.ValueQualityStatistics;
import org.talend.datascience.common.inference.type.DataType;
import org.talend.datascience.common.inference.type.DataType.Type;

/**
 * created by talend on 2015-07-28 Detailled comment.
 *
 */
public class ValueQualityAnalyzerTest {

    public static List<String[]> getRecords(InputStream inputStream, String separator) {
        if (inputStream == null) {
            throw new IllegalArgumentException("Input stream cannot be null.");
        }
        try {
            List<String[]> records = new ArrayList<String[]>();
            final List<String> lines = IOUtils.readLines(inputStream);
            for (String line : lines) {
                String[] record = StringUtils.splitByWholeSeparatorPreserveAllTokens(line, separator);
                records.add(record);
            }
            return records;
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                inputStream.close();
            } catch (IOException e) {
                // Silent ignore
                e.printStackTrace();
            }
        }

    }

    public static List<String[]> getRecords(InputStream inputStream) {
        return getRecords(inputStream, ";");
    }

    @Test
    public void testValueQualityAnalyzerWithoutSemanticQuality() {

        DataTypeQualityAnalyzer dataTypeQualityAnalyzer = new DataTypeQualityAnalyzer(new Type[] { DataType.Type.INTEGER,
                Type.STRING, Type.STRING, Type.STRING, Type.DATE, Type.STRING, Type.DATE, Type.INTEGER, Type.DOUBLE });
        String[] semanticTypes = new String[] { SemanticCategoryEnum.UNKNOWN.name(), SemanticCategoryEnum.UNKNOWN.name(),
                SemanticCategoryEnum.UNKNOWN.name(), SemanticCategoryEnum.UNKNOWN.name(), SemanticCategoryEnum.UNKNOWN.name(),
                SemanticCategoryEnum.UNKNOWN.name(), SemanticCategoryEnum.UNKNOWN.name(), SemanticCategoryEnum.UNKNOWN.name(),
                SemanticCategoryEnum.UNKNOWN.name() };
        SemanticQualityAnalyzer semanticQualityAnalyzer = new SemanticQualityAnalyzer(semanticTypes);

        ValueQualityAnalyzer valueQualityAnalyzer = new ValueQualityAnalyzer(dataTypeQualityAnalyzer, semanticQualityAnalyzer);
        valueQualityAnalyzer.init();

        final List<String[]> records = getRecords(this.getClass().getResourceAsStream("../data/customers_100.csv"));

        for (String[] record : records) {
            valueQualityAnalyzer.analyze(record);
        }

        for (int i = 0; i < semanticTypes.length; i++) {
            ValueQualityStatistics dataTypeQualityResult = dataTypeQualityAnalyzer.getResult().get(i);
            ValueQualityStatistics aggregatedResult = valueQualityAnalyzer.getResult().get(i);
            assertEquals("unexpected ValidCount on Column " + i, dataTypeQualityResult.getValidCount(),
                    aggregatedResult.getValidCount());
            assertEquals("unexpected InvalidCount on Column " + i, dataTypeQualityResult.getInvalidCount(),
                    aggregatedResult.getInvalidCount());
            assertEquals("unexpected EmptyCount on Column " + i, dataTypeQualityResult.getEmptyCount(),
                    aggregatedResult.getEmptyCount());
        }

        try {
            valueQualityAnalyzer.close();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Test
    public void testValueQualityAnalyzerWithSemanticQuality() {

        final List<String[]> records = new ArrayList<String[]>() {

            private static final long serialVersionUID = 1L;

            {
                add(new String[] { "1", "UT" });
                add(new String[] { "2", "MN" });
                add(new String[] { "3", "MO" });
                add(new String[] { "4", "" });
                add(new String[] { "5", "IL" });
                add(new String[] { "6", "ORZ" });
                add(new String[] { "7", " " });
                add(new String[] { "8", "LOL" });
            }
        };

        final int[] EXPECTED_VALID_COUNT = { 8, 4 };
        final int[] EXPECTED_EMPTY_COUNT = { 0, 2 };
        final int[] EXPECTED_INVALID_COUNT = { 0, 2 };
        final List<Set<String>> EXPECTED_INVALID_VALUES = new ArrayList<Set<String>>() {

            private static final long serialVersionUID = 1L;

            {
                add(new HashSet<String>());
                add(new HashSet<String>() {

                    private static final long serialVersionUID = 1L;

                    {
                        add("LOL");
                        add("ORZ");
                    }
                });
            }
        };

        DataTypeQualityAnalyzer dataTypeQualityAnalyzer = new DataTypeQualityAnalyzer(new Type[] { DataType.Type.INTEGER,
                Type.STRING });
        SemanticQualityAnalyzer semanticQualityAnalyzer = new SemanticQualityAnalyzer(new String[] {
                SemanticCategoryEnum.UNKNOWN.name(), SemanticCategoryEnum.US_STATE_CODE.name() });

        ValueQualityAnalyzer valueQualityAnalyzer = new ValueQualityAnalyzer(dataTypeQualityAnalyzer, semanticQualityAnalyzer);
        valueQualityAnalyzer.init();

        for (String[] record : records) {
            valueQualityAnalyzer.analyze(record);
        }

        for (int i = 0; i < EXPECTED_INVALID_VALUES.size(); i++) {
            ValueQualityStatistics aggregatedResult = valueQualityAnalyzer.getResult().get(i);
            assertEquals("unexpected ValidCount on Column " + i, EXPECTED_VALID_COUNT[i], aggregatedResult.getValidCount());
            assertEquals("unexpected EmptyCount on Column " + i, EXPECTED_EMPTY_COUNT[i], aggregatedResult.getEmptyCount());
            assertEquals("unexpected InvalidCount on Column " + i, EXPECTED_INVALID_COUNT[i], aggregatedResult.getInvalidCount());
            assertEquals("unexpected InvalidValues on Column " + i, EXPECTED_INVALID_VALUES.get(i),
                    aggregatedResult.getInvalidValues());
        }

        try {
            valueQualityAnalyzer.close();
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }
}