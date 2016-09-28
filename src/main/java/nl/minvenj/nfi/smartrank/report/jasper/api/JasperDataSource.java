/**
 * Copyright (C) 2013-2015 Netherlands Forensic Institute
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package nl.minvenj.nfi.smartrank.report.jasper.api;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRField;
import nl.minvenj.nfi.smartrank.SmartRank;
import nl.minvenj.nfi.smartrank.analysis.SearchResults;
import nl.minvenj.nfi.smartrank.domain.AnalysisParameters;
import nl.minvenj.nfi.smartrank.domain.DNADatabase;
import nl.minvenj.nfi.smartrank.domain.LikelihoodRatio;
import nl.minvenj.nfi.smartrank.domain.Locus;
import nl.minvenj.nfi.smartrank.domain.PopulationStatistics;
import nl.minvenj.nfi.smartrank.domain.Sample;
import nl.minvenj.nfi.smartrank.messages.data.CrimeSceneProfilesMessage;
import nl.minvenj.nfi.smartrank.messages.data.DatabaseMessage;
import nl.minvenj.nfi.smartrank.messages.data.DefenseHypothesisMessage;
import nl.minvenj.nfi.smartrank.messages.data.EnabledLociMessage;
import nl.minvenj.nfi.smartrank.messages.data.KnownProfilesMessage;
import nl.minvenj.nfi.smartrank.messages.data.LikelihoodRatiosMessage;
import nl.minvenj.nfi.smartrank.messages.data.PopulationStatisticsMessage;
import nl.minvenj.nfi.smartrank.messages.data.ProsecutionHypothesisMessage;
import nl.minvenj.nfi.smartrank.messages.data.SearchResultsMessage;
import nl.minvenj.nfi.smartrank.raven.messages.MessageBus;
import nl.minvenj.nfi.smartrank.utils.OrderMergedList;

public class JasperDataSource implements JRDataSource {

    private static final Logger LOG = LoggerFactory.getLogger(JasperDataSource.class);
    public static final String PROGRAM_VERSION = "ProgramVersion";
    public static final String CASE_NUMBER = "CaseNumber";
    public static final String USER_NAME = "UserName";
    public static final String DATE_TIME = "DateTime";
    public static final String TRACEIDS = "TraceIDs";

    public static final String THETA_CORRECTION = "ThetaCorrection";
    public static final String DROP_IN_PROBABILITY = "DropInProbability";
    public static final String POPULATION_STATISTICS_FILE_NAME = "PopulationStatisticsFileName";
    public static final String RARE_ALLELE_FREQUENCY = "RareAlleleFrequency";

    public static final String DROPOUT_ESTIMATION_DATA = "DropoutEstimationData";
    public static final String DROPOUT_ESTIMATION_ITERATIONS = "DropoutEstimationIterations";
    public static final String DROPOUT_ESTIMATION_5PERCENT = "DropoutEstimation5Percent";
    public static final String DROPOUT_ESTIMATION_95PERCENT = "DropoutEstimation95Percent";

    public static final String DATABASE_LOCATION = "DatabaseLocation";
    public static final String DATABASE_HASH = "DatabaseHash";
    public static final String DATABASE_RECORD_COUNT = "DatabaseRecordCount";
    public static final String DATABASE_COMPOSITION = "DatabaseComposition";

    public static final String EXCLUDED_PROFILES = "ExcludedProfiles";
    public static final String CRIMESAMPLES = "CrimesceneSamples";
    public static final String CRIMESAMPLES_LOCI = "CrimesceneSamplesLoci";

    public static final String KNOWNPROFILES = "KnownProfiles";
    public static final String KNOWNPROFILES_LOCI = "KnownProfilesLoci";

    public static final String PROSECUTION_UNKNOWNS = "ProsecutionUnknowns";
    public static final String PROSECUTION_CONTRIBUTORS = "ProsecutionContributors";
    public static final String PROSECUTION_UNKNOWNS_DROPOUT_PROBABILITY = "ProsecutionUnknownsDropoutProbability";

    public static final String DEFENSE_UNKNOWNS = "DefenseUnknowns";
    public static final String DEFENSE_CONTRIBUTORS = "DefenseContributors";
    public static final String DEFENSE_UNKNOWNS_DROPOUT_PROBABILITY = "DefenseUnknownsDropoutProbability";

    public static final String RESULTS = "Results";
    public static final String ALL_LRS = "AllLRs";
    public static final String RESULT_SAMPLES_LOCI = "ResultSamplesLoci";
    public static final String POSITIVE_LRS = "PositiveLRs";
    public static final String LRS_OVER_THRESHOLD = "LRsOverThreshold";
    public static final String NUMBER_OF_LRS_OVER_THRESHOLD = "NumberOfLRsOverThreshold";
    public static final String LR_THRESHOLD = "LRThreshold";
    public static final String NUMBEROFCALCULATEDLRS = "NumberOfCalculatedLRs";
    public static final String MAXLR = "MaximumLR";
    public static final String MINLR = "MinimumLR";
    public static final String LR_1_PERCENT = "LR1Percent";
    public static final String LR_50_PERCENT = "LR50Percent";
    public static final String LR_99_PERCENT = "LR99Percent";
    public static final String RESULTS_PER_NUMBER_OF_LOCI = "ResultsPerNumberOfLoci";

    private final AtomicBoolean _hasNext = new AtomicBoolean(true);

    private static final ArrayList<JRField> FIELDS = new ArrayList<>();

    {
        FIELDS.add(new JasperField(PROGRAM_VERSION, "The version of SmartRank used to generate the report", String.class));
//        FIELDS.add(new JasperField(CASE_NUMBER, "The case number", String.class));
        FIELDS.add(new JasperField(USER_NAME, "The name of the user running the analysis", String.class));
        FIELDS.add(new JasperField(DATE_TIME, "The date and time when the analysis was run", String.class));
        FIELDS.add(new JasperField(TRACEIDS, "A string describing the trace or traces under analysis", String.class));

        FIELDS.add(new JasperField(THETA_CORRECTION, "The value for the Theta correction", Double.class));
        FIELDS.add(new JasperField(DROP_IN_PROBABILITY, "The probability of Drop-In", Double.class));
        FIELDS.add(new JasperField(POPULATION_STATISTICS_FILE_NAME, "The file from which the population statistics (a.k.a. the Allele Frequencies) were read.", String.class));
        FIELDS.add(new JasperField(RARE_ALLELE_FREQUENCY, "The frequency assigned to the alleles that were detected as rare", Double.class));

        FIELDS.add(new JasperField(DATABASE_LOCATION, "The location of the DNA database in which the search was performed.", String.class));
        FIELDS.add(new JasperField(DATABASE_HASH, "The hash value for the database contents.", String.class));
        FIELDS.add(new JasperField(DATABASE_RECORD_COUNT, "The number of profiles in the database.", String.class));
        FIELDS.add(new JasperField(DATABASE_COMPOSITION, "The number of profiles in the database reported per number of loci.", Collection.class));

        FIELDS.add(new JasperField(EXCLUDED_PROFILES, "The profiles in the database that were excluded from the search.", Collection.class));
        FIELDS.add(new JasperField(CRIMESAMPLES, "The replicates", Collection.class));
//        FIELDS.add(new JasperField(CRIMESAMPLE_LOCI, "The contents of the crime samples", Collection.class));

        FIELDS.add(new JasperField(KNOWNPROFILES, "The reference profiles", Collection.class));
        FIELDS.add(new JasperField(KNOWNPROFILES_LOCI, "The contents of the known profiles", Collection.class));

        FIELDS.add(new JasperField(PROSECUTION_UNKNOWNS, "The number of unknowns in the prosecution hypothesis", Integer.class));
        FIELDS.add(new JasperField(PROSECUTION_CONTRIBUTORS, "The contributors according to the prosecution hypothesis", Collection.class));
        FIELDS.add(new JasperField(PROSECUTION_UNKNOWNS_DROPOUT_PROBABILITY, "The probability of Drop-Out for the unknowns according to the prosecution hypothesis", Double.class));

        FIELDS.add(new JasperField(DEFENSE_UNKNOWNS, "The number of unknowns in the defense hypothesis", Integer.class));
        FIELDS.add(new JasperField(DEFENSE_CONTRIBUTORS, "The contributors according to the defense hypothesis", Collection.class));
        FIELDS.add(new JasperField(DEFENSE_UNKNOWNS_DROPOUT_PROBABILITY, "The probability of Drop-Out for the unknowns according to the defense hypothesis", Double.class));

        FIELDS.add(new JasperField(RESULTS, "A collection of Likelihood Ratios representing the profiles with an LR > 1", Collection.class));

        FIELDS.add(new JasperField(NUMBEROFCALCULATEDLRS, "A collection of Likelihood Ratios representing the profiles with an LR > 1", Collection.class));
        FIELDS.add(new JasperField(ALL_LRS, "A collection of all Likelihood Ratios calculated during the search", Collection.class));

        FIELDS.add(new JasperField(MAXLR, "The highest calculated LR", Collection.class));
        FIELDS.add(new JasperField(MINLR, "The lowest calculated LR", Collection.class));
        FIELDS.add(new JasperField(LR_1_PERCENT, "The 1-percent qualtile of the distribution of calculated LRs", Collection.class));
        FIELDS.add(new JasperField(LR_50_PERCENT, "The 50-percent qualtile of the distribution of calculated LRs", Collection.class));
        FIELDS.add(new JasperField(LR_99_PERCENT, "The 99-percent qualtile of the distribution of calculated LRs", Collection.class));
        FIELDS.add(new JasperField(RESULTS_PER_NUMBER_OF_LOCI, "The number of calculated LRs > 1 broken up by number of evaluated loci", Collection.class));
    }

    @Override
    public boolean next() throws JRException {
        return _hasNext.getAndSet(false);
    }

    @Override
    public Object getFieldValue(final JRField jrf) throws JRException {
        switch (jrf.getName()) {
            case USER_NAME:
                return System.getProperty("user.name");
            case DATE_TIME:
                final SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                return sdf.format(new Date());
            case DATABASE_LOCATION:
                return getSafeConnectString();
            case DATABASE_RECORD_COUNT:
                return getSafeRecordCount();
            case DATABASE_HASH:
                return getSafeFileHash();
            case DATABASE_COMPOSITION:
                return getSafeComposition();
            case PROSECUTION_UNKNOWNS:
                return MessageBus.getInstance().query(ProsecutionHypothesisMessage.class).getUnknownCount();
            case PROSECUTION_CONTRIBUTORS:
                return MessageBus.getInstance().query(ProsecutionHypothesisMessage.class).getContributors();
            case DEFENSE_CONTRIBUTORS:
                return MessageBus.getInstance().query(DefenseHypothesisMessage.class).getContributors();
            case DEFENSE_UNKNOWNS:
                return MessageBus.getInstance().query(DefenseHypothesisMessage.class).getUnknownCount();
            case EXCLUDED_PROFILES:
                return getSearchResults().getExcludedProfileStatistics();
            case PROGRAM_VERSION:
                return SmartRank.getVersion();
            case THETA_CORRECTION:
                return MessageBus.getInstance().query(DefenseHypothesisMessage.class).getThetaCorrection();
            case DROP_IN_PROBABILITY:
                return MessageBus.getInstance().query(ProsecutionHypothesisMessage.class).getDropInProbability();
            case PROSECUTION_UNKNOWNS_DROPOUT_PROBABILITY:
                return MessageBus.getInstance().query(ProsecutionHypothesisMessage.class).getUnknownDropoutProbability();
            case DEFENSE_UNKNOWNS_DROPOUT_PROBABILITY:
                return MessageBus.getInstance().query(DefenseHypothesisMessage.class).getUnknownDropoutProbability();
            case POPULATION_STATISTICS_FILE_NAME:
                return MessageBus.getInstance().query(PopulationStatisticsMessage.class).getFileName();
            case RARE_ALLELE_FREQUENCY:
                return MessageBus.getInstance().query(PopulationStatisticsMessage.class).getRareAlleleFrequency();
            case NUMBEROFCALCULATEDLRS:
                return getSearchResults().getNumberOfLRs();
            case ALL_LRS:
                return getAllLRs();
            case POSITIVE_LRS:
                return getSafePositiveLRs(1);
            case LRS_OVER_THRESHOLD:
                return getSafePositiveLRs(getSearchResults().getParameters().getLrThreshold());
            case NUMBER_OF_LRS_OVER_THRESHOLD:
                return getSafePositiveLRs(getSearchResults().getParameters().getLrThreshold()).size();
            case LR_THRESHOLD:
                return getSearchResults().getParameters().getLrThreshold();
            case MAXLR:
                return getSearchResults().getMaxRatio();
            case MINLR:
                return getSearchResults().getMinRatio();
            case LR_1_PERCENT:
                return getSearchResults().getPercentile(1);
            case LR_50_PERCENT:
                return getSearchResults().getPercentile(50);
            case LR_99_PERCENT:
                return getSearchResults().getPercentile(99);
            case RESULTS_PER_NUMBER_OF_LOCI:
                return getSearchResults().getResultsPerNumberOfLoci(getSearchResults().getParameters().getLrThreshold());
            case RESULTS:
                return buildResultsList();
            case RESULT_SAMPLES_LOCI:
                return buildResultsLociList();

            case CRIMESAMPLES:
                final ArrayList<Sample> usedSamplesList = new ArrayList<>();

                for (final Sample sample : MessageBus.getInstance().query(CrimeSceneProfilesMessage.class)) {
                    if (sample.isEnabled()) {
                        usedSamplesList.add(sample);
                    }
                }
                return usedSamplesList;

            case CRIMESAMPLES_LOCI:
                return buildSampleContentList(MessageBus.getInstance().query(CrimeSceneProfilesMessage.class));
            case DROPOUT_ESTIMATION_DATA:
                return getSearchResults().getParameters().getDropoutEstimation() == null ? null : getSearchResults().getParameters().getDropoutEstimation().getData();
            case DROPOUT_ESTIMATION_ITERATIONS:
                return getSearchResults().getParameters().getDropoutEstimation() == null ? 0 : getSearchResults().getParameters().getDropoutEstimation().getIterations();
            case DROPOUT_ESTIMATION_5PERCENT:
                return getSearchResults().getParameters().getDropoutEstimation() == null ? 0 : getSearchResults().getParameters().getDropoutEstimation().getMinimum();
            case DROPOUT_ESTIMATION_95PERCENT:
                return getSearchResults().getParameters().getDropoutEstimation() == null ? 0 : getSearchResults().getParameters().getDropoutEstimation().getMaximum();
            case KNOWNPROFILES:
                final ArrayList<Sample> usedKnownProfilesList = new ArrayList<>();
                if (MessageBus.getInstance().query(KnownProfilesMessage.class) != null) {
                    for (final Sample sample : MessageBus.getInstance().query(KnownProfilesMessage.class)) {
                        if (sample.isEnabled()) {
                            usedKnownProfilesList.add(sample);
                        }
                    }
                }
                return usedKnownProfilesList;
            case KNOWNPROFILES_LOCI:
                return buildSampleContentList(MessageBus.getInstance().query(KnownProfilesMessage.class));

            case TRACEIDS:
                String traceIDs = "";
                for (final Sample replicate : MessageBus.getInstance().query(CrimeSceneProfilesMessage.class)) {
                    if (replicate.isEnabled()) {
                        final String repId = replicate.getName().replaceAll("Rep\\d+$", "");
                        if (!traceIDs.contains(repId)) {
                            if (!traceIDs.isEmpty()) {
                                traceIDs += ", ";
                            }
                            traceIDs += repId;
                        }
                    }
                }
                return traceIDs;
            default:
                LOG.error("Unknown data element: {}", jrf.getName());
                System.out.println("Unknown data element: " + jrf.getName());
                return null;
        }
    }

    private List<SampleContents> buildSampleContentList(final List<Sample> sampleList) {
        final ArrayList<SampleContents> sampleContentsList = new ArrayList<>();
        if (sampleList == null || sampleList.isEmpty()) {
            return sampleContentsList;
        }

        final OrderMergedList<String> oml = new OrderMergedList<String>();
        for (final Sample sample : sampleList) {
            for (final Locus locus : sample.getLoci()) {
                oml.add(locus.getName());
            }
        }

        for (final String locusName : oml) {
            for (final Sample sample : sampleList) {
                final Locus locus = sample.getLocus(locusName);
                if (locus != null) {
                    sampleContentsList.add(new SampleContents(sample.getName(), locusName, ("" + locus.getAlleles()).replaceAll("[\\'\\[\\]]", "")));
                }
            }
        }

        return sampleContentsList;
    }

    private SearchResults getSearchResults() {
        return MessageBus.getInstance().query(SearchResultsMessage.class);
    }

    private List<PlainResult> getAllLRs() {
        final ArrayList<PlainResult> retval = new ArrayList<>();
        final List<Double> allLRs = new ArrayList<>(getSearchResults().getLRs());
        Collections.sort(allLRs);
        int idx = 0;
        for (final Double d : allLRs) {
                retval.add(new PlainResult(d, idx++));
        }
        return retval;
    }

    private List<PlainResult> getSafePositiveLRs(final int threshold) {
        LOG.debug("getSafePositiveLRs({})", threshold);
        final ArrayList<PlainResult> retval = new ArrayList<>();
        final List<Double> allLRs = new ArrayList<>();
        for (final LikelihoodRatio lr : getSearchResults().getPositiveLRs()) {
            allLRs.add(lr.getOverallRatio().getRatio());
        }

        LOG.debug("allLRs has {} elements", allLRs.size());
        Collections.sort(allLRs);
        int idx = 0;
        for (final Double d : allLRs) {
            if (d > threshold) {
                retval.add(new PlainResult(d, idx++));
            }
        }
        LOG.debug("getSafePositiveLRs({}): {} values returned", threshold, retval.size());
        return retval;
    }

    private Object getSafeComposition() {
        final DNADatabase dnaDatabase = MessageBus.getInstance().query(DatabaseMessage.class);
        final ArrayList<CountPerLocus> retval = new ArrayList<>();
        if (dnaDatabase != null) {
            final List<Integer> specimenCountPerNumberOfLoci = dnaDatabase.getSpecimenCountPerNumberOfLoci();
            for (int locusCount = 0; locusCount < specimenCountPerNumberOfLoci.size(); locusCount++) {
                final Integer recordCount = specimenCountPerNumberOfLoci.get(locusCount);
                if (recordCount > 0) {
                    retval.add(new CountPerLocus(locusCount, recordCount, new Double(((recordCount * 10000L) / dnaDatabase.getRecordCount()) / 100F)));
                }
            }
        }
        return retval;
    }

    private Object getSafeFileHash() {
        final DNADatabase dnaDatabase = MessageBus.getInstance().query(DatabaseMessage.class);
        return dnaDatabase == null ? "" : dnaDatabase.getFileHash();
    }

    private Object getSafeRecordCount() {
        final DNADatabase dnaDatabase = MessageBus.getInstance().query(DatabaseMessage.class);
        return dnaDatabase == null ? "" : dnaDatabase.getRecordCount();
    }

    private Object getSafeConnectString() {
        final DNADatabase dnaDatabase = MessageBus.getInstance().query(DatabaseMessage.class);
        return dnaDatabase == null ? "" : dnaDatabase.getConnectString();
    }

    public static JRField[] getFields() {
        return FIELDS.toArray(new JRField[FIELDS.size()]);
    }

    private ArrayList<Result> buildResultsList() {
        final List<LikelihoodRatio> lrs = MessageBus.getInstance().query(LikelihoodRatiosMessage.class);
        final AnalysisParameters parameters = getSearchResults().getParameters();
        final PopulationStatistics statistics = MessageBus.getInstance().query(PopulationStatisticsMessage.class);
        final int lrThreshold = parameters.getLrThreshold();
        final int numberOfResults = parameters.getMaximumNumberOfResults();
        final ArrayList<Locus> crimesampleLoci = new ArrayList<>();
        for (final Sample sample : parameters.getEnabledCrimesceneProfiles()) {
            for (final Locus locus : sample.getLoci()) {
                crimesampleLoci.add(locus);
            }
        }

        final ArrayList<Result> results = new ArrayList<>();

        int rank = 1;
        for (final LikelihoodRatio lr : lrs) {
            if (lr.getOverallRatio().getRatio() > lrThreshold) {
                String comments = reportUnusedLoci("Specimen ", lr.getProfile().getLoci(), lr.getLoci());
                comments += reportUnusedLoci("Crime Sample ", crimesampleLoci, lr.getLoci());
                comments += reportLociNotInStatistics(statistics.getLoci(), lr.getProfile().getLoci());
                results.add(new Result(rank++, lr.getProfile(), lr.getLocusCount(), lr.getOverallRatio().getRatio(), comments));
            }
            if (rank > numberOfResults)
                break;
        }

        return results;
    }

    private ArrayList<SampleContents> buildResultsLociList() {
        final List<LikelihoodRatio> lrs = MessageBus.getInstance().query(LikelihoodRatiosMessage.class);
        final AnalysisParameters parameters = getSearchResults().getParameters();
        final int lrThreshold = parameters.getLrThreshold();
        final int numberOfResults = parameters.getMaximumNumberOfResults();
        final ArrayList<Sample> sampleList = new ArrayList<>();

        for (final LikelihoodRatio lr : lrs) {
            if (lr.getOverallRatio().getRatio() > lrThreshold) {
                sampleList.add(lr.getProfile());
            }
            if (sampleList.size() == numberOfResults)
                break;
        }

        final ArrayList<SampleContents> sampleContentsList = new ArrayList<>();

        if (sampleList == null || sampleList.isEmpty()) {
            return sampleContentsList;
        }

        final ArrayList<String> locusNames = new ArrayList<String>();
        final List<String> loci = MessageBus.getInstance().query(EnabledLociMessage.class);

        // Add enabled loci where at least one specimen contains that locus
        for (final String locus : loci) {
            boolean present = false;
            for (final Sample sample : sampleList) {
                present |= sample.getLocus(locus) != null;
            }
            if (present)
                locusNames.add(locus);
        }

        for (final Sample sample : sampleList) {
            for (final Locus locus : sample.getLoci()) {
                // Add loci from the specimens that are not present in the crime-scene profiles
                if (!locusNames.contains(locus.getName())) {
                    locusNames.add(locus.getName());
                }
            }
        }

        for (final String locusName : locusNames) {
            int rank = 1;
            for (final Sample sample : sampleList) {
                final Locus locus = sample.getLocus(locusName);
                String alleles = "";
                if (locus != null) {
                    alleles = locus.getAlleles().toString().replaceAll("[\\'\\[\\]]", "");
                }
                sampleContentsList.add(new SampleContents(String.format("Ranked % 3d: ", rank++) + sample.getName(), locusName, alleles));
            }
        }

        return sampleContentsList;
    }

    private String reportUnusedLoci(final String prefix, final Collection<Locus> referenceLoci, final Set<String> evaluatedLoci) {
        final ArrayList<String> unusedLoci = new ArrayList<>();
        final ArrayList<String> usedLoci = new ArrayList<>();
        for (final Locus locus : referenceLoci) {
            if (!usedLoci.contains(locus.getName()) && !evaluatedLoci.contains(locus.getName())) {
                unusedLoci.add(locus.getName());
            }
            usedLoci.add(locus.getName());
        }

        if (unusedLoci.isEmpty()) {
            return "";
        }

        final StringBuilder stringBuilder = new StringBuilder(prefix);
        if (unusedLoci.size() > 1) {
            stringBuilder.append("loci");
        }
        else {
            stringBuilder.append("locus");
        }
        stringBuilder.append(" not evaluated: ");
        for (int idx = 0; idx < unusedLoci.size(); idx++) {
            stringBuilder.append(unusedLoci.get(idx));
            if (idx < unusedLoci.size() - 2) {
                stringBuilder.append(", ");
            }
            else if (idx == unusedLoci.size() - 2) {
                stringBuilder.append(" and ");
            }
        }
        return stringBuilder.toString() + "\n";
    }

    private String reportLociNotInStatistics(final Collection<String> statsLoci, final Collection<Locus> profileLoci) {
        final ArrayList<String> unusedLoci = new ArrayList<>();
        for (final Locus locus : profileLoci) {
            if (!statsLoci.contains(locus.getName())) {
                unusedLoci.add(locus.getName());
            }
        }

        if (unusedLoci.isEmpty()) {
            return "";
        }

        final StringBuilder stringBuilder = new StringBuilder();
        if (unusedLoci.size() > 1) {
            stringBuilder.append("Loci");
        }
        else {
            stringBuilder.append("Locus");
        }
        stringBuilder.append(" not in population statistics: ");
        for (int idx = 0; idx < unusedLoci.size(); idx++) {
            stringBuilder.append(unusedLoci.get(idx));
            if (idx < unusedLoci.size() - 2) {
                stringBuilder.append(", ");
            }
            else if (idx == unusedLoci.size() - 2) {
                stringBuilder.append(" and ");
            }
        }
        return stringBuilder.toString() + "\n";
    }

    public static class CountPerLocus {
        private final Integer _locusCount;
        private final Integer _recordCount;
        private final Double _percentage;

        public CountPerLocus(final int locusCount, final int recordCount, final Double percentage) {
            _locusCount = locusCount;
            _recordCount = recordCount;
            _percentage = percentage;
        }

        public Integer getLocusCount() {
            return _locusCount;
        }

        public Integer getRecordCount() {
            return _recordCount;
        }

        public Double getPercentage() {
            return _percentage;
        }
    }

    public static class PlainResult {
        private final Double _lr;
        private final Integer _index;

        public PlainResult(final Double lr, final int index) {
            _lr = lr;
            _index = index;
        }

        public Double getLr() {
            return _lr;
        }

        public Integer getIndex() {
            return _index;
        }
    }

    public static class SampleContents {
        private final String _name;
        private final String _locus;
        private final String _alleles;

        public SampleContents(final String name, final String locus, final String alleles) {
            _name = name;
            _locus = locus;
            _alleles = alleles;
        }

        public String getAlleles() {
            return _alleles;
        }

        public String getLocus() {
            return _locus;
        }

        public String getName() {
            return _name;
        }
    }

    public static class Result {

        private final int _rank;
        private final Sample _profile;
        private final double _lr;
        private final String _comments;
        private final int _evaluatedLoci;

        Result(final int rank, final Sample profile, final int evaluatedLoci, final double lr, final String comments) {
            _rank = rank;
            _profile = profile;
            _evaluatedLoci = evaluatedLoci;
            _lr = lr;
            _comments = comments;
        }

        public int getRank() {
            return _rank;
        }

        public String getProfile() {
            return _profile.getName();
        }

        public double getLr() {
            return _lr;
        }

        public int getLocusCount() {
            return _profile.getLoci().size();
        }

        public String getComments() {
            return _comments;
        }

        public int getEvaluatedLoci() {
            return _evaluatedLoci;
        }
    }
}
