package org.talend.dataquality.statistics.datetime;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.chrono.IsoChronology;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class DateTimePatternListGenerator {

    static List<LocaledPattern> knownLocaledPatternList = new ArrayList<LocaledPattern>();

    static List<String> knownPatternList = new ArrayList<String>();

    private final static ZonedDateTime ZONED_DATE_TIME = ZonedDateTime.of(2222, 3, 11, 5, 6, 7, 888, ZoneId.of("Europe/Paris"));

    private final static FormatStyle[] FORMAT_STYLES = new FormatStyle[] { FormatStyle.SHORT, FormatStyle.MEDIUM,
            FormatStyle.LONG, FormatStyle.FULL };

    private static final boolean PRINT_DETAILED_RESULTS = false;

    private static final boolean PRINT_SAMPLE_TABLE = false;

    private static final boolean PRINT_PATTERN_LIST = true;

    private static List<LocaledPattern> OTHER_COMMON_PATTERNS = new ArrayList<LocaledPattern>() {

        private static final long serialVersionUID = 1L;

        {
            add(new LocaledPattern("M/d/yyyy", Locale.US, "OTHER", false));// 6/18/2009
            add(new LocaledPattern("MMM d yyyy", Locale.US, "OTHER", false));// Jan 18 2012
            add(new LocaledPattern("MMM.d.yyyy", Locale.US, "OTHER", false));// Jan.12.2010
            add(new LocaledPattern("MMMM d yyyy", Locale.US, "OTHER", false));// January 18 2012
            add(new LocaledPattern("yyyy-M-d H:mm:ss.S", Locale.US, "OTHER", true));// 2013-2-14 13:40:51.1
            add(new LocaledPattern("d/MMM/yyyy H:mm:ss Z", Locale.US, "OTHER", true));// 14/Feb/2013 13:40:51 +0100
            // add(new LocaledPattern("dd-MMM-yy hh.mm.ss.S a",//
            // Locale.US, "OTHER", true));// 18-Nov-86 01.00.00.000000000 AM

        }
    };

    private static List<LocaledPattern> processBaseDateTimePatternsByLocales() {

        Locale[] localeArray = new Locale[] { Locale.US, //
                Locale.FRANCE, //
                Locale.GERMANY, //
                Locale.UK,//
                Locale.ITALY, //
                Locale.CANADA, Locale.CANADA_FRENCH, //
                Locale.JAPAN, //
                Locale.CHINA, //
        };

        // Set<String> dateTimePatternsList = new LinkedHashSet<String>();
        List<LocaledPattern> dateTimePatterns = new ArrayList<LocaledPattern>();
        for (Locale l : localeArray) {
            getFormatsOfLocale(l, true);
        }
        dateTimePatterns.removeAll(knownPatternList);
        // return new ArrayList<String>(dateTimePatterns);
        return dateTimePatterns;

    }

    private static void getFormatsOfLocale(Locale locale, boolean keepLongMonth) {
        if (PRINT_DETAILED_RESULTS) {
            System.out.println("--------------------Locale: " + locale + "-----------------------");
        }
        getFormatByStyle(true, false, locale, keepLongMonth); // Date Only
        getFormatByStyle(true, true, locale, keepLongMonth); // Date & Time

        // getFormatByStyle(false, true, locale, keepLongMonth); //Time Only
    }

    private static void getFormatByStyle(boolean isDateRequired, boolean isTimeRequired, Locale locale, boolean keepLongMonth) {
        for (FormatStyle style : FORMAT_STYLES) {
            String pattern = DateTimeFormatterBuilder.getLocalizedDateTimePattern(//
                    isDateRequired ? style : null, isTimeRequired ? style : null, IsoChronology.INSTANCE, locale);//

            // ignore patterns with long month for additional languages
            if (!keepLongMonth
                    && (pattern.contains("MMMM") || pattern.contains("MMM") || pattern.contains(" a") || pattern.contains("'"))) {
                continue;
            }

            if (!knownPatternList.contains(pattern)) {

                LocaledPattern lp = new LocaledPattern(pattern, locale, style.name(), isTimeRequired);
                knownLocaledPatternList.add(lp);
                knownPatternList.add(pattern); // update list of pattern strings without locale
                if (PRINT_DETAILED_RESULTS) {
                    System.out.println(lp);
                }
            } else {
                if (pattern.contains("MMMM") || pattern.contains("MMM")) {
                    if (PRINT_DETAILED_RESULTS) {
                        System.out.print("!!!duplicated pattern with different locale!!! ");
                    }
                    LocaledPattern lp = new LocaledPattern(pattern, locale, style.name(), isTimeRequired);
                    knownLocaledPatternList.add(lp);
                    if (PRINT_DETAILED_RESULTS) {
                        System.out.println(lp);
                    }

                }

            }
        }
    }

    private static void processAdditionalDateTimePatternsByLocales() {

        for (String lang : Locale.getISOLanguages()) {
            getFormatsOfLocale(new Locale(lang), false);
        }
        // dateTimePatternsList.removeAll(knownPatternList);

    }

    private static void processISOAndRFCDateTimePatternList() {

        List<LocaledPattern> patternList = new ArrayList<LocaledPattern>();

        // 1. BASIC_ISO_DATE
        patternList.add(new LocaledPattern("yyyyMMddZ", Locale.US, "BASIC_ISO_DATE", false));
        patternList.add(new LocaledPattern("yyyyMMdd", Locale.US, "BASIC_ISO_DATE", false));
        // 2. ISO_DATE
        patternList.add(new LocaledPattern("yyyy-MM-ddZZZZZ", Locale.US, "ISO_DATE", false));
        patternList.add(new LocaledPattern("yyyy-MM-dd", Locale.US, "ISO_DATE", false));
        // 3. ISO_DATE_TIME
        patternList.add(new LocaledPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'['VV']'", Locale.US, "ISO_DATE_TIME", true));
        patternList.add(new LocaledPattern("yyyy-MM-dd'T'HH:mm:ss.SSS", Locale.US, "ISO_DATE_TIME", true));
        patternList.add(new LocaledPattern("yyyy-MM-dd'T'HH:mm:ss", Locale.US, "ISO_DATE_TIME", true));
        // 4. ISO_INSTANT
        patternList.add(new LocaledPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US, "ISO_INSTANT", true));
        // 5. ISO_LOCAL_DATE
        patternList.add(new LocaledPattern("yyyy-MM-dd", Locale.US, "ISO_LOCAL_DATE", false));
        // 6. ISO_LOCAL_DATE_TIME
        patternList.add(new LocaledPattern("yyyy-MM-dd'T'HH:mm:ss.SSS", Locale.US, "ISO_LOCAL_DATE_TIME", true));
        patternList.add(new LocaledPattern("yyyy-MM-dd'T'HH:mm:ss", Locale.US, "ISO_LOCAL_DATE_TIME", true));// 1970-01-01T00:32:43
        // 7. ISO_OFFSET_DATE
        patternList.add(new LocaledPattern("yyyy-MM-ddZZZZZ", Locale.US, "ISO_OFFSET_DATE", false));
        // 8. ISO_OFFSET_DATE_TIME
        patternList.add(new LocaledPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZZZZZ", Locale.US, "ISO_OFFSET_DATE_TIME", true));
        patternList.add(new LocaledPattern("yyyy-MM-dd'T'HH:mm:ssZZZZZ", Locale.US, "ISO_OFFSET_DATE_TIME", true));
        // 9. ISO_ORDINAL_DATE
        patternList.add(new LocaledPattern("yyyy-DZZZZZ", Locale.US, "ISO", false));
        // 10. ISO_WEEK_DATE
        patternList.add(new LocaledPattern("yyyy-'W'w-WZZZZZ", Locale.US, "ISO", false));
        // 11. ISO_ZONED_DATE_TIME
        patternList.add(new LocaledPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZZZZZ'['VV']'", Locale.US, "ISO_ZONED_DATE_TIME", true));
        patternList.add(new LocaledPattern("yyyy-MM-dd'T'HH:mm:ssZZZZZ'['VV']'", Locale.US, "ISO_ZONED_DATE_TIME", true));
        // 12. RFC_1123_DATE_TIME
        patternList.add(new LocaledPattern("EEE, d MMM yyyy HH:mm:ss Z", Locale.US, "RFC1123_WITH_DAY", true));
        patternList.add(new LocaledPattern("d MMM yyyy HH:mm:ss Z", Locale.US, "RFC1123", true));

        for (LocaledPattern lp : patternList) {
            if (!knownPatternList.contains(lp.pattern)) {
                knownLocaledPatternList.add(lp);
                knownPatternList.add(lp.getPattern());
                if (PRINT_DETAILED_RESULTS) {
                    System.out.println(lp);
                }
            }
        }
    }

    @SuppressWarnings("unused")
    private static void validateISOPattens(List<String> isoPatternList) {

        Set<String> formattedDateTimeSet = new HashSet<String>();
        for (String pattern : isoPatternList) {
            formattedDateTimeSet.add(getFormattedDateTime(pattern, Locale.US));
        }

        DateTimeFormatter[] formatters = new DateTimeFormatter[] { DateTimeFormatter.BASIC_ISO_DATE, // 1
                DateTimeFormatter.ISO_DATE, // 2
                DateTimeFormatter.ISO_DATE_TIME, // 3
                // DateTimeFormatter.ISO_TIME, //
                DateTimeFormatter.ISO_INSTANT, // 4
                DateTimeFormatter.ISO_LOCAL_DATE, // 5
                DateTimeFormatter.ISO_LOCAL_DATE_TIME,// 6
                // DateTimeFormatter.ISO_LOCAL_TIME, //
                DateTimeFormatter.ISO_OFFSET_DATE, // 7
                DateTimeFormatter.ISO_OFFSET_DATE_TIME, // 8
                // DateTimeFormatter.ISO_OFFSET_TIME, //
                DateTimeFormatter.ISO_ORDINAL_DATE, // 9
                DateTimeFormatter.ISO_WEEK_DATE, // 10
                DateTimeFormatter.ISO_ZONED_DATE_TIME, // 11
                DateTimeFormatter.RFC_1123_DATE_TIME, // 12
        };

        System.out.println("-------------Validate ISO PattenText-------------");
        for (int i = 0; i < formatters.length; i++) {

            System.out.print((i + 1) + "\t");
            try {
                String formattedDateTime = ZONED_DATE_TIME.format(formatters[i]);
                System.out.print(formattedDateTimeSet.contains(formattedDateTime) ? "YES\t" : "NO\t");
                System.out.println(formattedDateTime);
            } catch (Throwable t) {
                System.out.println(t.getMessage());
            }
        }

    }

    // TODO Sort not only by length but also by other conditions: DecimalStyle, hasZone? etc.
    private static void sortDatePattern(List<String> list) {
        Collections.sort(list, new Comparator<String>() {

            @Override
            public int compare(String s1, String s2) {

                Integer s1Length = s1.length();
                Integer s2length = s2.length();
                int lComp = s1Length.compareTo(s2length);

                if (lComp != 0) {
                    return lComp;
                } else {
                    int sComp = ("_" + s1).compareTo("_" + s2);
                    return sComp;
                }
            }
        });
    }

    private static String getFormattedDateTime(String pattern, Locale locale) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern, locale);
        try {
            String formattedDateTime = ZONED_DATE_TIME.format(formatter);
            return formattedDateTime;
        } catch (Throwable t) {
            return t.getMessage();
        }
    }

    public static void main(String[] args) {

        int currentLocaledPatternSize = 0;

        // 1. Base Localized DateTimePatterns (java8 DateTimeFormatterBuilder)
        processBaseDateTimePatternsByLocales();
        int basePatternCount = knownLocaledPatternList.size() - currentLocaledPatternSize;
        if (PRINT_DETAILED_RESULTS) {
            System.out.println("#basePatterns = " + basePatternCount + "\n");
        }
        currentLocaledPatternSize = knownLocaledPatternList.size();

        // 2. Other common DateTime patterns
        for (LocaledPattern lp : OTHER_COMMON_PATTERNS) {
            if (!knownPatternList.contains(lp.pattern)) {
                knownLocaledPatternList.add(lp);
                knownPatternList.add(lp.getPattern());
                if (PRINT_DETAILED_RESULTS) {
                    System.out.println(lp);
                }
            }
        }

        // 3. ISO and RFC DateTimePatterns
        processISOAndRFCDateTimePatternList();
        // knownPatternList.addAll(isoPatternList);
        int isoPatternCount = knownLocaledPatternList.size() - currentLocaledPatternSize;
        if (PRINT_DETAILED_RESULTS) {
            System.out.println("#DateTimePattern(ISO&RFC) = " + isoPatternCount + "\n");
        }
        currentLocaledPatternSize = knownLocaledPatternList.size();

        // 4. Additional Localized DateTimePatterns (java8 DateTimeFormatterBuilder)
        processAdditionalDateTimePatternsByLocales();
        // knownPatternList.addAll(additionalPatternList);
        int additionalPatternCount = knownLocaledPatternList.size() - currentLocaledPatternSize;
        if (PRINT_DETAILED_RESULTS) {
            System.out.println("#additionalPatternList = " + additionalPatternCount + "\n");
        }
        currentLocaledPatternSize = knownLocaledPatternList.size();

        // 5. add legacy DateTimePatterns
        // getNonExistentPatternsInLegacyFile(knownPatternList);

        if (PRINT_DETAILED_RESULTS) {
            System.out.println("#Total = " + knownLocaledPatternList.size() + //
                    " (#basePatterns = " + basePatternCount + //
                    ", #isoPatterns = " + isoPatternCount + //
                    ", #additionalPatterns = " + additionalPatternCount + ")\n");//
        }

        if (PRINT_SAMPLE_TABLE) {// table header
            System.out.println("Sample\tPattern\tLocale\tFormatStyle\tIsWithTime");
        }
        for (LocaledPattern lp : knownLocaledPatternList) {

            if (PRINT_PATTERN_LIST) {
                System.out.println(lp);
            }
            if (PRINT_SAMPLE_TABLE) {
                System.out.println(ZONED_DATE_TIME.format(DateTimeFormatter.ofPattern(lp.getPattern(), lp.getLocale())) + "\t"
                        + lp.getPattern() + "\t" + lp.getLocale() + "\t" + lp.getFormatStyle() + "\t" + lp.isWithTime());
            }
        }

    }
}

class LocaledPattern {

    String pattern;

    Locale locale;

    String formatStyle;

    boolean withTime;

    public LocaledPattern(String pattern, Locale locale, String formatStyle, boolean withTime) {
        this.pattern = pattern;
        this.locale = locale;
        this.formatStyle = formatStyle;
        this.withTime = withTime;
    }

    public String getPattern() {
        return pattern;
    }

    public Locale getLocale() {
        return locale;
    }

    public String getFormatStyle() {
        return formatStyle;
    }

    public boolean isWithTime() {
        return withTime;
    }

    public String toString() {
        return locale + "\t" + pattern;

    }

}
