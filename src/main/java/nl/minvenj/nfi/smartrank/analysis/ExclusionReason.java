package nl.minvenj.nfi.smartrank.analysis;

import java.util.regex.Pattern;

import nl.minvenj.nfi.smartrank.gui.SmartRankRestrictions;

/**
 * Defines the possible reasons for ignoring a database sample.
 */
public enum ExclusionReason {
    REQUIRES_DROPOUT("dropout required to explain evidence, but dropout set to 0.00 in Hp"),
    NOT_ENOUGH_LOCI("insufficient loci in common with crimescene samples (minimum=SmartRankRestrictions.getMinimumNumberOfLoci())"),
    UNEXPECTED_NUMBER_OF_FIELDS("number of fields in record not consistent with file header"),
    OTHER("record could not be processed");

    private final String _description;

    private ExclusionReason(final String description) {
        _description = description;
    }

    public String getDescription() {
        return _description.replaceAll(Pattern.quote("SmartRankRestrictions.getMinimumNumberOfLoci()"), "" + SmartRankRestrictions.getMinimumNumberOfLoci());
    }
}
