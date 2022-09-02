/*
 * Copyright (C) 2015 Netherlands Forensic Institute
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

package nl.minvenj.nfi.smartrank.domain;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Properties;

import nl.minvenj.nfi.smartrank.analysis.parameterestimation.DropoutEstimation;
import nl.minvenj.nfi.smartrank.gui.SmartRankRestrictions;

public class AnalysisParameters {
    private int _lrThreshold;
    private int _threadCount;
    private final Collection<Sample> _enabledCrimesceneProfiles;
    private DropoutEstimation _manualEstimate;
    private boolean _calculateHdOnce = true;
    private boolean _automaticParameterEstimationToBePerformed;
    private int _maximumNumberOfResults;
    private Properties _properties;

    public AnalysisParameters() {
        _enabledCrimesceneProfiles = new ArrayList<>();
        _maximumNumberOfResults = SmartRankRestrictions.getMaximumStoredResults();
    }

    public AnalysisParameters(final AnalysisParameters parameters) {
        _lrThreshold = parameters._lrThreshold;
        _enabledCrimesceneProfiles = new ArrayList<>(parameters._enabledCrimesceneProfiles);
        _manualEstimate = parameters._manualEstimate;
        _calculateHdOnce = parameters._calculateHdOnce;
        _automaticParameterEstimationToBePerformed = parameters._automaticParameterEstimationToBePerformed;
        _maximumNumberOfResults = parameters._maximumNumberOfResults;
    }

    public int getLrThreshold() {
        return _lrThreshold;
    }

    public void setLrThreshold(final int lrThreshold) {
        _lrThreshold = lrThreshold;
    }

    public int getThreadCount() {
        return _threadCount;
    }

    public void setThreadCount(final int threadCount) {
        _threadCount = threadCount;
    }

    public Collection<Sample> getEnabledCrimesceneProfiles() {
        return _enabledCrimesceneProfiles;
    }

    public void setEnabledCrimesceneProfiles(final Collection<Sample> crimesceneProfiles) {
        _enabledCrimesceneProfiles.clear();
        for (final Sample curProfile : crimesceneProfiles) {
            if (curProfile.isEnabled()) {
                _enabledCrimesceneProfiles.add(curProfile);
            }
        }
    }

    public DropoutEstimation getDropoutEstimation() {
        return _manualEstimate;
    }

    public void setDropoutEstimation(final DropoutEstimation maualEstimate) {
        _manualEstimate = maualEstimate;
    }

    public void setCalculateHdOnce(final boolean calculateHdOnce) {
        _calculateHdOnce = calculateHdOnce;
    }

    public boolean isCalculateHdOnce() {
        return _calculateHdOnce;
    }

    public void setParameterEstimation(final boolean automaticParameterEstimationToBePerformed) {
        _automaticParameterEstimationToBePerformed = automaticParameterEstimationToBePerformed;
    }

    public boolean isAutomaticParameterEstimationToBePerformed() {
        return _automaticParameterEstimationToBePerformed;
    }

    public void setMaxReturnedResults(final int maximumNumberOfResults) {
        _maximumNumberOfResults = maximumNumberOfResults;
    }

    public int getMaximumNumberOfResults() {
        return _maximumNumberOfResults;
    }

    public void setProperties(final Properties props) {
        _properties = new Properties();
        _properties.clear();
        if (props != null) {
            _properties.putAll(props);
        }
    }

    public Properties getProperties() {
        return _properties;
    }
}
