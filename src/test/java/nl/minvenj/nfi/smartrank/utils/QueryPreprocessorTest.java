package nl.minvenj.nfi.smartrank.utils;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

import static nl.minvenj.nfi.smartrank.utils.QueryPreprocessor.process;

import java.io.IOException;
import java.io.StringReader;
import java.util.Properties;

import org.apache.commons.io.input.ReaderInputStream;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class QueryPreprocessorTest {

    @Parameters(name = "{0}")
    public static Object[][] getTestcases() {
        return new Object[][]{
            {"3 lines, 1 field", "First line\nSecond line is ${field}.\nThird line.", "First line\n-- Commented out for validation: Second line is ${field}.\nThird line.", "First line\nSecond line is 1234.\nThird line.", "field=1234", null},
            {"3 lines, 1 field illegal value", "First line\nSecond line is '${field}'.\nThird line.", "First line\n-- Commented out for validation: Second line is '${field}'.\nThird line.", "First line\nSecond line is 'Robert''); DROP TABLE students; --'.\nThird line.", "field=Robert'); DROP TABLE students; --", "Property 'field' has an illegal value for SQL expansion: 'Robert'); DROP TABLE students; --'"},
            {"3 lines, 2 fields", "First line\nSecond line is ${field}.\nThird line is ${anotherField}.", "First line\n-- Commented out for validation: Second line is ${field}.\n-- Commented out for validation: Third line is ${anotherField}.", "First line\nSecond line is 1234.\nThird line is hello.", "field=1234\nanotherField=hello", null},
            {"1 line, no fields", "This string has no fields.", "This string has no fields.", "This string has no fields.", "field=1234", null},
            {"1 line, 1 field", "This string has one ${field:default value}.", "-- Commented out for validation: This string has one ${field:default value}.", "This string has one 1234.", "field=1234", null},
            {"1 line, 1 unknown field", "This string has one ${field}.", "-- Commented out for validation: This string has one ${field}.", "This string has one field.", "otherField=1234", "The query references the property 'field' but this was not defined in the search criteria and no default was specified!"},
            {"1 line, 2 unknown fields", "This string has two ${field1} ${field2}.", "-- Commented out for validation: This string has two ${field1} ${field2}.", "This string has one field.", "otherField=1234", "The query references the properties 'field1' and 'field2' but these were not defined in the search criteria and no default was specified!"},
            {"2 lines, 1 known field, 1 unknown field with default", "This string has two ${field1}\nThe second line ${field2:has a default} value.", "-- Commented out for validation: This string has two ${field1}\n-- Commented out for validation: The second line ${field2:has a default} value.", "This string has two 1234\nThe second line has a default value.", "field1=1234", null},
            {"1 line, 1 unknown field with default", "This string has one ${field:default value}.", "-- Commented out for validation: This string has one ${field:default value}.", "This string has one default value.", "otherField=1234", null}
        };
    }

    @Parameter(0)
    public String name;

    @Parameter(1)
    public String input;

    @Parameter(2)
    public String expectedNoProperties;

    @Parameter(3)
    public String expectedWithProperties;

    @Parameter(4)
    public String properties;

    @Parameter(5)
    public String message;

    @Rule
    public ExpectedException expected = ExpectedException.none();

    @Test
    public final void testProcessStringWithoutProperties() {
        assertThat(process(input, null), equalTo(expectedNoProperties));
    }

    @Test
    public final void testProcessStringWithProperties() throws IOException {
        if (message != null) {
            expected.expectMessage(message);
        }
        final Properties props = new Properties();
        props.load(new ReaderInputStream(new StringReader(properties)));
        assertThat(process(input, props), equalTo(expectedWithProperties));
    }
}
