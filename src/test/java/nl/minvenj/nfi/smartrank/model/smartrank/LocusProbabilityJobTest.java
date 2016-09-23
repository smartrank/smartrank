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

package nl.minvenj.nfi.smartrank.model.smartrank;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collection;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import nl.minvenj.nfi.smartrank.domain.Allele;
import nl.minvenj.nfi.smartrank.domain.Contributor;
import nl.minvenj.nfi.smartrank.domain.Hypothesis;
import nl.minvenj.nfi.smartrank.domain.Locus;
import nl.minvenj.nfi.smartrank.domain.PopulationStatistics;
import nl.minvenj.nfi.smartrank.domain.Sample;

@RunWith(MockitoJUnitRunner.class)
public class LocusProbabilityJobTest {

    private Collection<Sample> _profiles;

    @Mock
    private Sample _profile1;

    @Mock
    private Sample _profile12;

    @Mock
    private Sample _profile33;

    @Mock
    private Sample _profile123;

    @Mock
    private Hypothesis _hypothesis;

    @Mock
    private PopulationStatistics _statistics;

    @Mock
    private Locus _vwa12;

    @Mock
    private Locus _vwa123;

    @Mock
    private Locus _vwa33;

    @Mock
    private Allele _allele1;

    @Mock
    private Allele _allele2;

    @Mock
    private Allele _allele3;

    @Mock
    private Locus _vwa1;

    @Mock
    private Contributor _contributor1;

    @Before
    public void setup() {
        Allele.getId("1");
        Allele.getId("2");
        Allele.getId("3");

        when(_allele1.getId()).thenReturn(0);
        when(_allele1.getAllele()).thenReturn("1");
        when(_allele1.getLocus()).thenReturn(_vwa12);

        when(_allele2.getId()).thenReturn(1);
        when(_allele2.getAllele()).thenReturn("2");
        when(_allele2.getLocus()).thenReturn(_vwa12);

        when(_allele3.getId()).thenReturn(2);
        when(_allele3.getAllele()).thenReturn("3");
        when(_allele2.getLocus()).thenReturn(_vwa33);

        when(_statistics.getProbability(any(Locus.class), eq(_allele1))).thenReturn(0.01);
        when(_statistics.getProbability(any(Locus.class), eq(_allele2))).thenReturn(0.035);

        when(_vwa12.getName()).thenReturn(LOCUSNAME);
        when(_vwa12.getAlleles()).thenReturn(Arrays.asList(_allele1, _allele2));

        when(_contributor1.getDropoutProbability()).thenReturn(0.03);
        when(_contributor1.getSample()).thenReturn(_profile1);

        when(_hypothesis.getPopulationStatistics()).thenReturn(_statistics);
        when(_hypothesis.getDropInProbability()).thenReturn(0.05);
        when(_hypothesis.getContributor(any(Allele.class))).thenReturn(_contributor1);
        when(_hypothesis.getContributors()).thenReturn(Arrays.asList(_contributor1));

        when(_vwa1.getName()).thenReturn(LOCUSNAME);
        when(_vwa1.getAlleles()).thenReturn(Arrays.asList(_allele1));

        when(_vwa33.getName()).thenReturn(LOCUSNAME);
        when(_vwa33.getAlleles()).thenReturn(Arrays.asList(_allele3, _allele3));

        when(_vwa123.getName()).thenReturn(LOCUSNAME);
        when(_vwa123.getAlleles()).thenReturn(Arrays.asList(_allele1, _allele2, _allele3));

        when(_profile12.getLoci()).thenReturn(Arrays.asList(_vwa12));

        when(_profile1.getLoci()).thenReturn(Arrays.asList(_vwa1));
        when(_profile33.getLoci()).thenReturn(Arrays.asList(_vwa33));
        when(_profile123.getLoci()).thenReturn(Arrays.asList(_vwa123));

        _profiles = Arrays.asList(_profile1, _profile12, _profile123);
    }
    private static final String LOCUSNAME = "VWA";

    private static final String INVALIDLOCUSNAME = "DOESNOTEXIST";

    @Test(expected = IllegalArgumentException.class)
    public void testLocusNameNull() throws Exception {
        new LocusProbabilityJob(null, _profiles, _hypothesis);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testLocusProfilesNull() throws Exception {
        new LocusProbabilityJob(LOCUSNAME, null, _hypothesis);
    }

    @Test(expected = NullPointerException.class)
    public void testLocusHypothesisNull() throws Exception {
        new LocusProbabilityJob(LOCUSNAME, _profiles, null);
    }
}