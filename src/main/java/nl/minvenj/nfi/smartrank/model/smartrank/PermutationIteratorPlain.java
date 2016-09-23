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

import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nl.minvenj.nfi.smartrank.domain.Locus;

public class PermutationIteratorPlain implements PermutationIterator {

    private static final Logger LOG = LoggerFactory.getLogger(PermutationIteratorPlain.class);
    private static final int[] FACTORIALS = {1, 1, 2, 6, 24, 120, 720, 5040, 40320, 362880, 3628800, 39916800, 479001600};
    private final int[] _indices;
    private final Locus[] _loci;
    private final int _batchEnd;
    private final int[] _locusCount;
    private final int _n;
    private final long _size;

    public PermutationIteratorPlain(int unknownCount, Locus[] possibleAlleleCombinations, int batchStart) {
        _indices = new int[unknownCount];
        Arrays.fill(_indices, batchStart);
        _loci = possibleAlleleCombinations;
        _locusCount = new int[_loci.length];

        // Get the number of permutations for the total n!
        _n = FACTORIALS[_indices.length];

        _batchEnd = batchStart + 1;
        if (_indices.length > 0) {
            _indices[0] = batchStart;
        }

        _size = doSum(_indices.length, _loci.length - batchStart);
    }

    private long doSum(int depth, int range) {
        if (depth == 0) {
            return 0;
        }
        if (depth == 1) {
            return 1;
        }
        if (depth == 2) {
            return range;
        }
        long size = 0;
        for (int count = 1; count <= range; count++) {
            size += doSum(depth - 1, count);
        }
        return size;
    }

    @Override
    public long size() {
        return _size;
    }

    @Override
    public boolean hasNext() {
        if (_indices.length == 0) {
            return false;
        }
        return _indices[0] < _batchEnd;
    }

    @Override
    public synchronized Permutation next() {
        if (_indices.length == 0 || _indices[0] >= _batchEnd) {
            return null;
        }

        // Prepare return value
        Locus[] permutationLoci = new Locus[_indices.length];
        Arrays.fill(_locusCount, 0);
        for (int idx = 0; idx < _indices.length; idx++) {
            _locusCount[_indices[idx]]++;
            permutationLoci[idx] = _loci[_indices[idx]];
        }

        // Update the index values for the next iteration
        int indicesIndex = _indices.length - 1;

        // Handle rollover
        while ((indicesIndex > 0) && (_indices[indicesIndex] >= (_loci.length - 1))) {
            _indices[indicesIndex--] = 0;
        }
        _indices[indicesIndex]++;

        // Do not return equivalent combinations of loci
        Arrays.fill(_indices, indicesIndex + 1, _indices.length, _indices[indicesIndex]);

        int permutationFactor = calculatePermutationFactor();
//        LOG.info("Indices: {} factor {}", signature, permutationFactor);
        return new Permutation(permutationLoci, permutationFactor);
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException("Remove is not supported for the PermutationIterator.");
    }

    /**
     * Calculates the number of permutations for the current collection of loci
     *
     * @return The number of permutations for the current collection of loci
     */
    private int calculatePermutationFactor() {
        // Divide the total number of permutations by the number of equivalent permutations of each of the components
        // n! / PRODUCT<i=1;k>(Ci!) where n is the number of elements in the signature, k is the number of distinct elements in the signature and Ci is the number of times element i of k is used.
        int k = 1;

        for (int idx = 0; idx < _locusCount.length; idx++) {
            k *= FACTORIALS[_locusCount[idx]];
        }

        return _n / k;
    }
}
