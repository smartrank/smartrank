package nl.minvenj.nfi.smartrank.tools;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import nl.minvenj.nfi.smartrank.io.CSVReader;

public class JDBCInsert {

    public static void main(final String[] args) throws SQLException, IOException {
        if (args.length == 0)
            throw new IllegalArgumentException("Must supply input csv file");

        final CSVReader reader = new CSVReader(args[0]);
        final String[] headers = reader.readFields();

        try (Connection connection = DriverManager.getConnection("jdbc:jtds:sqlserver://localhost:17064/CODIS", "codis", "codis")) {
            final Statement statement = connection.createStatement();
            connection.setAutoCommit(false);

            final ResultSet idResultSet = statement.executeQuery("select max(Spec_CD)+1 from Specimen");
            idResultSet.next();
            int specimenKey = idResultSet.getInt(1);

            final ResultSet locusResultSet = statement.executeQuery("select * from Locus");
            final HashMap<String, Integer> locusKeys = new HashMap<>();
            while (locusResultSet.next()) {
                locusKeys.put(normalize(locusResultSet.getString(2)), locusResultSet.getInt(1));
            }

            String[] fields;
            while ((fields = reader.readFields()) != null) {
                try {
                    final String specimenInsert = "INSERT into Specimen (Spec_ID, Spec_CD, Spec_Type_CD) values ('" + fields[0].trim() + "', " + specimenKey + ", " + (((int) Math.random() * 4) + 1) + ")";

                    System.out.println("    " + specimenInsert);
                    final int rv = statement.executeUpdate(specimenInsert);
                    System.out.println("  > " + rv);

                    final List<String> loci = new ArrayList<>();

                    for (int idx = 1; idx < fields.length; idx++) {
                        final String allele = fields[idx];
                        final String locus = headers[idx].replaceFirst("_[1234]$", "");
                        final String[] parts = headers[idx].split("_");
                        final String band = parts[parts.length - 1];
                        final Integer locusKey = locusKeys.get(normalize(locus));

                        if (locusKey == null) {
                            System.out.println("No key found for locus " + locus);
                        }

                        if (!allele.equalsIgnoreCase("null")) {
                            if (!loci.contains(locus)) {
                                loci.add(locus);
                                final String sizingInsert = String.format("INSERT into Sizing (Spec_CD, Locus_CD) values (%s,%s)", specimenKey, locusKey);
                                System.out.println("    " + sizingInsert);
                                final int rvSizing = statement.executeUpdate(sizingInsert);
                                System.out.println("  > " + rvSizing);
                            }

                            final String alleleInsert = String.format("INSERT into PCR_Value (Spec_CD, Locus_CD, PCR_Value, Band_Num) values (%s,%s,%s,%s)", specimenKey, locusKey, allele, band);
                            System.out.println("    " + alleleInsert);
                            final int rvAllele = statement.executeUpdate(alleleInsert);
                            System.out.println("  > " + rvAllele);
                        }
                    }
                    specimenKey++;
                    if (!connection.getAutoCommit())
                        connection.commit();
                }
                catch (final Throwable t) {
                    connection.rollback();
                }

            }
        }
    }

    private static String normalize(final String locus) {
        return locus.toUpperCase().replaceAll("[ _]", "");
    }
}
