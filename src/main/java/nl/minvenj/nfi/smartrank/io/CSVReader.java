/*
* Copyright (c) 2016, Netherlands Forensic Institute
* All rights reserved.
*/
package nl.minvenj.nfi.smartrank.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;

import com.opencsv.CSVParser;

public class CSVReader {
    private static char[] _separators = {',', '\t', ';'};
    private final HashingReader _hashingReader;
    private final BufferedReader _reader;
    private final HashMap<Character, CSVParser> _parserHash;
    private final boolean _returnEmptyFields;

    private File _file;

    /**
     * Constructor creating a CSV reader object that does not return empty
     * fields.
     *
     * @param fileName The name of the source file
     * @throws IOException
     */
    public CSVReader(final String fileName) throws IOException {
        this(fileName, false);
    }

    /**
     * Constructor
     *
     * @param fileName The name of the source file
     * @param returnEmptyfields If true, the reader will return empty field
     * found in the input file as empty strings. If false, the reader will skip
     * empty fields.
     * @throws IOException
     */
    public CSVReader(final String fileName, final boolean returnEmptyfields) throws IOException {
        this(new FileReader(resolveFileName(fileName)), returnEmptyfields);
    }

    /**
     * Constructor. Empty fields are not returned.
     *
     * @param inputFile A File object representing the input file
     * @throws IOException
     */
    public CSVReader(final File inputFile) throws IOException {
        this(inputFile, false);
    }

    /**
     * Constructor
     *
     * @param inputFile A File object representing the input file
     * @param returnEmptyFields true if empty fields are to be returned by the
     * {@link:getFields} method.
     * @throws IOException
     */
    public CSVReader(final File inputFile, final boolean returnEmptyFields) throws IOException {
        this(new FileInputStream(inputFile), returnEmptyFields);
        _file = inputFile;
    }

    /**
     * Constructor
     *
     * @param is An InputStream opened on the input file
     * @param returnEmptyFields true
     * @throws IOException
     */
    public CSVReader(final InputStream is, final boolean returnEmptyFields) throws IOException {
        this(new InputStreamReader(is, "UTF-8"), returnEmptyFields);
    }

    /**
     * Private utility constructor
     *
     * @param reader the BufferedReader from which to read.
     * @param returnEmptyFields true if empty fields are to be returned
     * @throws IOException
     */
    private CSVReader(final Reader reader, final boolean returnEmptyFields) throws IOException {
        this._hashingReader = new HashingReader(reader);
        this._reader = new BufferedReader(_hashingReader);
        this._parserHash = new HashMap<>();

        this._returnEmptyFields = returnEmptyFields;
    }

    private static String resolveFileName(final String fileName) {
        String resolvedFileName;
        final URL url = CSVReader.class.getResource(fileName);
        if (url == null) {
            final URI uri = new File(fileName).toURI();
            try {
                resolvedFileName = URLDecoder.decode(uri.getRawPath(), "UTF-8");
            }
            catch (final UnsupportedEncodingException ex) {
                resolvedFileName = uri.getRawPath();
            }
        }
        else {
            resolvedFileName = url.getFile();
        }
        return resolvedFileName;
    }

    /**
     * Reads the next line from the input file and converts to an array of
     * strings
     *
     * @return An array of strings containing the fields found in the next line
     * of the input file, or null if the end of the file is reached.
     * @throws IOException If there was an error reading from the file.
     */
    public String[] readFields() throws IOException {
        String line;
        do {
            line = _reader.readLine();
            if (line == null) {
                return null;
            }
        } while (line.length() == 0);

        return parse(line);
    }

    /**
     * Obtains a signature value for the input file. The general form of this
     * string is ALGORITHM/HASHVALUE, where ALGORITHM is the name of the
     * algorithm used (either XOR or SHA-1) and the value is the hex of the
     * generated hash.
     *
     * @return A String containing the signature of the file contents
     */
    public String getFileHash() {
        return _hashingReader.getHash();
    }

    public long getOffset() {
        return _hashingReader.getOffset();
    }

    public String getFileName() {
        return "" + _file;
    }

    /**
     * Determines the separator used for the string. Upon encountering a new
     * separator, it is added to a hashmap containing a parser for that specific
     * separator.
     *
     * @param csvText The string for which to determine the separator
     */
    private Character determineSeparator(final String csvText) throws IOException {
        final int separatorIndex = Integer.MAX_VALUE;
        Character separator = null;

        for (int idx = 0; idx < _separators.length; idx++) {
            if (csvText.contains(String.valueOf(_separators[idx]))) {
                final int occurrenceIndex = csvText.indexOf(_separators[idx]);
                if (occurrenceIndex < separatorIndex) {
                    separator = _separators[idx];
                }
            }
        }
        if (separator == null) {
            throw new IllegalArgumentException("Unknown file format! Only Comma Separated and Tab Separated files are supported.");
        }
        return separator;
    }

    public String[] parse(final String csvLine) throws IOException {
        final Character separator = determineSeparator(csvLine);
        final ArrayList<String> fields = new ArrayList<>();

        if (!_parserHash.containsKey(separator)) {
            _parserHash.put(separator, new CSVParser(separator));
        }

        final String[] parsedLine = _parserHash.get(separator).parseLine(csvLine);

        for (int idx = 0; idx < parsedLine.length; idx++) {
            String fieldValue = parsedLine[idx].trim().replaceAll("\"", "");
            if (fieldValue.startsWith("\ufffe") || fieldValue.startsWith("\ufeff")) {
                fieldValue = fieldValue.substring(1);
            }

            if (fieldValue.matches("")) {
                if (_returnEmptyFields) {
                    fields.add(fieldValue);
                }
            }

            else {
                fields.add(fieldValue);
            }

        }

        return fields.toArray(new String[fields.size()]);
    }
}