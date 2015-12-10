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
package org.talend.dataquality.statistics.type;

import org.talend.dataquality.statistics.datetime.CustomDatetimePatternManager;

/**
 * Date type analyzer with customized extension such as the date time pattern
 * 
 * @since 1.3.3
 * @author zhao
 *
 */
public class CustomDataTypeAnalyzer extends DataTypeAnalyzer {

    private static final long serialVersionUID = -9188435209256600268L;

    /**
     * Add a custom date time pattern.
     * @param customDateTimePattern the pattern to add.
     */
    public void addCustomDateTimePattern(String customDateTimePattern) {
        this.customDateTimePatterns.add(customDateTimePattern);
    }


    protected boolean isDate(String value) {
        return CustomDatetimePatternManager.isDate(value, customDateTimePatterns);
    }

    @Override
    protected boolean isTime(String value) {
        return CustomDatetimePatternManager.isTime(value, customDateTimePatterns);
    }
}
