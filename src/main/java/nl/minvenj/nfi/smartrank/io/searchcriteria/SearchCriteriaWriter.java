package nl.minvenj.nfi.smartrank.io.searchcriteria;

import java.io.File;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import javax.xml.bind.JAXB;

import com.fasterxml.jackson.databind.ObjectMapper;

import nl.minvenj.nfi.smartrank.domain.Allele;
import nl.minvenj.nfi.smartrank.domain.AnalysisParameters;
import nl.minvenj.nfi.smartrank.domain.Contributor;
import nl.minvenj.nfi.smartrank.domain.DefenseHypothesis;
import nl.minvenj.nfi.smartrank.domain.Hypothesis;
import nl.minvenj.nfi.smartrank.domain.Locus;
import nl.minvenj.nfi.smartrank.domain.PopulationStatistics;
import nl.minvenj.nfi.smartrank.domain.ProsecutionHypothesis;
import nl.minvenj.nfi.smartrank.domain.Sample;
import nl.minvenj.nfi.smartrank.io.smartrankimport.jaxb.AlleleStatisticsType;
import nl.minvenj.nfi.smartrank.io.smartrankimport.jaxb.AlleleType;
import nl.minvenj.nfi.smartrank.io.smartrankimport.jaxb.ContributorType;
import nl.minvenj.nfi.smartrank.io.smartrankimport.jaxb.ContributorsType;
import nl.minvenj.nfi.smartrank.io.smartrankimport.jaxb.HDType;
import nl.minvenj.nfi.smartrank.io.smartrankimport.jaxb.HPType;
import nl.minvenj.nfi.smartrank.io.smartrankimport.jaxb.LocusStatisticsType;
import nl.minvenj.nfi.smartrank.io.smartrankimport.jaxb.LocusType;
import nl.minvenj.nfi.smartrank.io.smartrankimport.jaxb.NonContributorsType;
import nl.minvenj.nfi.smartrank.io.smartrankimport.jaxb.PropertiesType;
import nl.minvenj.nfi.smartrank.io.smartrankimport.jaxb.PropertyType;
import nl.minvenj.nfi.smartrank.io.smartrankimport.jaxb.ReplicateType;
import nl.minvenj.nfi.smartrank.io.smartrankimport.jaxb.ReplicatesType;
import nl.minvenj.nfi.smartrank.io.smartrankimport.jaxb.SmartRankImportFile;
import nl.minvenj.nfi.smartrank.io.smartrankimport.jaxb.SpecimenType;
import nl.minvenj.nfi.smartrank.io.smartrankimport.jaxb.StatisticsType;
import nl.minvenj.nfi.smartrank.io.smartrankimport.jaxb.UnknownsType;
import nl.minvenj.nfi.smartrank.raven.NullUtils;

public class SearchCriteriaWriter {

    private final AnalysisParameters _parameters;
    private final DefenseHypothesis _defenseHypothesis;
    private final ProsecutionHypothesis _prosecutionHypothesis;
    private final boolean _exportStatistics;
    private final boolean _automaticDropoutEstimation;
    private final String _caseNumber;
    private final String _userName;
    private final HashMap<String, String> _properties;

    public SearchCriteriaWriter(final String userName,
                                final AnalysisParameters parameters,
                                final DefenseHypothesis defenseHypothesis,
                                final ProsecutionHypothesis prosecutionHypothesis,
                                final String caseNumber,
                                final boolean exportStatistics,
                                final boolean automaticDropoutEstimation,
                                final HashMap<String, String> properties) {
        _userName = NullUtils.getValue(userName, "");
        _parameters = parameters;
        _defenseHypothesis = defenseHypothesis;
        _prosecutionHypothesis = prosecutionHypothesis;
        _caseNumber = caseNumber;
        _exportStatistics = exportStatistics;
        _automaticDropoutEstimation = automaticDropoutEstimation;
        _properties = properties;
    }

    public void write(final File file) throws Exception {
        final SmartRankImportFile xml = new SmartRankImportFile();
        xml.setUserid(_userName);
        xml.setDateTime(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").format(new Date()));
        xml.setCaseFolder(_caseNumber);
        xml.setDropin(new BigDecimal(_defenseHypothesis.getDropInProbability()));
        xml.setTheta(new BigDecimal(_defenseHypothesis.getThetaCorrection()));
        xml.setLrThreshold(_parameters.getLrThreshold());
        xml.setMaximumNumberOfResults("" + _parameters.getMaximumNumberOfResults());
        xml.setRareAlleleFrequency(_defenseHypothesis.getPopulationStatistics().getRareAlleleFrequency());

        final List<Sample> specimens = new ArrayList<>();

        final HPType hpType = new HPType();
        hpType.setCandidateDropout(_automaticDropoutEstimation ? "automatic" : "" + _prosecutionHypothesis.getCandidateDropout());
        hpType.setContributors(getContributors(_prosecutionHypothesis, specimens));
        hpType.setNonContributors(getNonContributors(_prosecutionHypothesis, specimens));
        hpType.setUnknowns(getUnknowns(_prosecutionHypothesis));
        xml.setHP(hpType);

        final HDType hdType = new HDType();
        hdType.setContributors(getContributors(_defenseHypothesis, specimens));
        hdType.setNonContributors(getNonContributors(_defenseHypothesis, specimens));
        hdType.setUnknowns(getUnknowns(_defenseHypothesis));
        xml.setHD(hdType);

        xml.setReplicates(getReplicates(_parameters, specimens));

        addSpecimens(xml, specimens);

        if (_exportStatistics) {
            addStatistics(_prosecutionHypothesis.getPopulationStatistics(), xml);
        }

        addProperties(xml);
        xml.setVersion(BigDecimal.ONE);

        doExport(xml, file);
    }

    private void addProperties(final SmartRankImportFile xml) {
        final PropertiesType propertiesType = new PropertiesType();
        for (final String name : _properties.keySet()) {
            final PropertyType propertyType = new PropertyType();
            propertyType.setName(name);
            propertyType.setValue(_properties.get(name));
            propertiesType.getProperty().add(propertyType);
        }
        xml.setProperties(propertiesType);
    }

    private UnknownsType getUnknowns(final Hypothesis defenseHypothesis) {
        final UnknownsType hdUnkowns = new UnknownsType();
        hdUnkowns.setCount(defenseHypothesis.getUnknownCount());
        hdUnkowns.setDropout(_automaticDropoutEstimation ? "automatic" : "" + defenseHypothesis.getUnknownDropoutProbability());
        return hdUnkowns;
    }

    private ReplicatesType getReplicates(final AnalysisParameters parameters, final List<Sample> specimens) {
        final ReplicatesType replicates = new ReplicatesType();
        for (final Sample trace : parameters.getEnabledCrimesceneProfiles()) {
            final ReplicateType replicate = new ReplicateType();
            replicate.setName(trace.getName());
            replicates.getReplicate().add(replicate);
            if (!specimens.contains(trace)) {
                specimens.add(trace);
            }
        }
        return replicates;
    }

    private void addSpecimens(final SmartRankImportFile xml, final List<Sample> specimens) {
        for (final Sample sample : specimens) {
            final SpecimenType specimen = new SpecimenType();
            specimen.setName(sample.getName());
            for (final Locus locus : sample.getLoci()) {
                final LocusType locusType = new LocusType();
                locusType.setName(locus.getName());
                for (final Allele allele : locus.getAlleles()) {
                    final AlleleType alleleType = new AlleleType();
                    alleleType.setValue(allele.getAllele());
                    locusType.getAllele().add(alleleType);
                }
                specimen.getLocus().add(locusType);
            }
            xml.getSpecimen().add(specimen);
        }
    }

    private NonContributorsType getNonContributors(final Hypothesis hypothesis, final List<Sample> specimens) {
        final NonContributorsType nonContributorsType = new NonContributorsType();
        for (final Contributor donor : hypothesis.getNonContributors()) {
            final ContributorType cont = new ContributorType();
            cont.setName(donor.getSample().getName());
            cont.setDropout(_automaticDropoutEstimation ? "automatic" : "" + donor.getDropoutProbability());
            nonContributorsType.getNonContributor().add(cont);
            if (!specimens.contains(donor.getSample())) {
                specimens.add(donor.getSample());
            }
        }
        return nonContributorsType;
    }

    private ContributorsType getContributors(final Hypothesis hypothesis, final List<Sample> specimens) {
        final ContributorsType contributorsType = new ContributorsType();
        for (final Contributor donor : hypothesis.getContributors()) {
            if (!donor.isCandidate()) {
                final ContributorType cont = new ContributorType();
                cont.setName(donor.getSample().getName());
                cont.setDropout(_automaticDropoutEstimation ? "automatic" : "" + donor.getDropoutProbability());
                contributorsType.getContributor().add(cont);
                if (!specimens.contains(donor.getSample())) {
                    specimens.add(donor.getSample());
                }
            }
        }
        return contributorsType;
    }

    private void addStatistics(final PopulationStatistics popStats, final SmartRankImportFile xml) {
        final StatisticsType statsType = new StatisticsType();
        statsType.setRareAlleleFrequency(popStats.getRareAlleleFrequency());
        final List<LocusStatisticsType> locusList = statsType.getLocus();
        for (final String locusName : popStats.getLoci()) {
            final LocusStatisticsType locusStatisticsType = new LocusStatisticsType();

            locusStatisticsType.setName(locusName);
            for (final String alleleName : popStats.getAlleles(locusName)) {
                final AlleleStatisticsType alleleStatType = new AlleleStatisticsType();
                alleleStatType.setValue(alleleName);
                alleleStatType.setProbability(popStats.getProbability(locusName, alleleName));
                locusStatisticsType.getAllele().add(alleleStatType);
            }

            locusList.add(locusStatisticsType);
        }
        xml.setStatistics(statsType);
    }

    private void doExport(final SmartRankImportFile xml, final File file) throws Exception {
        if (file.getName().toLowerCase().endsWith("json")) {
            new ObjectMapper().writeValue(file, xml);
        }
        if (file.getName().toLowerCase().endsWith("xml")) {
            JAXB.marshal(xml, file);
        }
    }
}
