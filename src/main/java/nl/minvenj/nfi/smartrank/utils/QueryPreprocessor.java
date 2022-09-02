package nl.minvenj.nfi.smartrank.utils;

import java.util.ArrayList;
/*
 * Copyright (C) 2019 Netherlands Forensic Institute
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class QueryPreprocessor {

    private static final Logger LOG = LoggerFactory.getLogger(QueryPreprocessor.class);
    private static final String VARIABLE_START = "${";
    private static final String VARIABLE_END = "}";
    private static final String COMMENT_HEADER = "-- Commented out for validation: ";
    private static final Pattern FIELD_PATTERN = Pattern.compile("\\$\\{([^}]+)\\}");

    /**
     * Quotes out lines containing variable references
     * @param sql The original SQL query
     * @return a version of the input sql with all lines containing variable references quoted out
     */
    public static String quoteOutVariableLines(final String sql) {
        String newSql = sql;

        int startIdx = newSql.indexOf(VARIABLE_START);
        while (startIdx >= 0) {
            // Backtrack to find the start of the line
            int lineStartIdx = startIdx;
            while (lineStartIdx >= 0 && newSql.charAt(lineStartIdx) != '\n') {
                lineStartIdx--;
            }
            newSql = newSql.substring(0, lineStartIdx + 1) + COMMENT_HEADER + newSql.substring(lineStartIdx + 1);
            final int endOfLine = newSql.indexOf("\n", startIdx + 1);
            if (endOfLine >= 0) {
                startIdx = newSql.indexOf(VARIABLE_START, endOfLine);
            }
            else {
                startIdx = -1;
            }
        }
        return newSql;
    }

    /**
     * Replaces variable references with their values
     * @param sql the original SQL query
     * @param props a {@link Properties} object containing the available variable values.
     * @return a version of the input sql with all references to variables replaced with their values. Values are SQL-safequoted.
     */
    public static String process(final String sql, final Properties props) {
        if (props == null || props.isEmpty()) {
            return quoteOutVariableLines(sql);
        }

        String newSql = sql;

        for (final Object key : props.keySet()) {
            final String value = props.getProperty((String) key);
            if (!value.matches("^[a-zA-Z0-9 _-]+$")) {
                throw new IllegalArgumentException("Property '" + key + "' has an illegal value for SQL expansion: '" + value + "'");
            }

            newSql = newSql.replaceAll("\\$\\{" + key + "(\\:.*)?\\}", value);
        }

        // Replace uspecified varialbes with their default values
        newSql = newSql.replaceAll("\\$\\{[^}]+\\:(.+)\\}", "$1");

        final Matcher matcher = FIELD_PATTERN.matcher(newSql);
        final ArrayList<String> unmatchedProperties = new ArrayList<>();
        while (matcher.find()) {
            final String propertyName = matcher.group(1);
            if (!unmatchedProperties.contains(propertyName)) {
                unmatchedProperties.add(propertyName);
            }
        }
        if (unmatchedProperties.isEmpty()) {
            return newSql;
        }

        LOG.error("Cannot resolve variables {}!", unmatchedProperties);
        LOG.error("SQL query: {}", sql);
        LOG.error("Available variables: {}", props);
        throw new IllegalArgumentException("The query references the propert" + (unmatchedProperties.size() == 1 ? "y" : "ies") + " " + humanReadable(unmatchedProperties) + " but th" + (unmatchedProperties.size() == 1 ? "is was" : "ese were") + " not defined in the search criteria and no default was specified!");
    }

    private static String humanReadable(final ArrayList<String> unmatchedProperties) {
        String processed = unmatchedProperties.toString().replaceAll("[\\[\\]]", "'").replaceAll(", ", "', '");
        final int lastComma = processed.lastIndexOf(',');
        if (lastComma >= 0) {
            processed = processed.substring(0, lastComma) + " and" + processed.substring(lastComma + 1);
        }
        return processed;
    }
}
