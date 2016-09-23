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
package nl.minvenj.nfi.smartrank.analysis;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import nl.minvenj.nfi.smartrank.domain.Hypothesis;

/**
 * Stores search results against the defining characteristics of the hypotheses that served as input.
 */
public class SearchResultCache {

    private static SearchResultCache _theInstance;
    private final Map<String, SearchResults> _theMap;

    /**
     * Private constructor to stop people from creating their own copy of this object.
     */
    private SearchResultCache() {
        _theMap = Collections.synchronizedMap(new HashMap<String, SearchResults>());
    }

    /**
     * Creates a new instance of the cache or returns a previously created instance.
     *
     * @return a {@link SearchResultCache}
     */
    public static synchronized SearchResultCache getInstance() {
        if (_theInstance == null) {
            _theInstance = new SearchResultCache();
        }
        return _theInstance;
    }

    /**
     * Gets the {@link SearchResults} for the combination of supplied hypotheses.
     *
     * @param hp the prosecution {@link Hypothesis}
     * @param hd the defense {@link Hypothesis}
     * @return a {@link SearchResults} object containing the results for the supplied hypotheses, or null if no previously calculated result is available for the supplied hypotheses
     */
    public SearchResults get(final Hypothesis hp, final Hypothesis hd) {
        if (hp == null || hd == null)
            return null;
        return _theMap.get(SearchResults.getGuid(hp, hd));
    }

    /**
     * Stores the supplied {@link SearchResults} in the cache.
     *
     * @param results the {@link SearchResults} to store
     */
    public void put(final SearchResults results) {
        _theMap.put(results.getGuid(), results);
    }

    /**
     * Clears the cache.
     */
    public void clear() {
        _theMap.clear();
    }
}
