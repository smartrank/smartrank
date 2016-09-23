/*
 * Copyright (C) 2016 Netherlands Forensic Institute
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
package nl.minvenj.nfi.smartrank.utils;

import java.util.ArrayList;
import java.util.Collection;

/**
 * A list that is intended to assist in composing compound lists where the order of the initial list is preserved.
 * <P>
 * <b>Example:</b><br>
 * merging list A C into existing list A B results in A C B
 * merging list B C into existing list A results in A B C
 * merging list B A into existing list A C results in A C B
 *
 * Initial content | Item Added  | Result
 * ======================================
 * empty           | A           | A
 * A               | B           | A B
 * A B             | A           | A B
 * A B             | A, C        | A C B
 */
public class OrderMergedList<T> extends ArrayList<T> {

    private int _nextIndexToAdd = 0;

    /**
     * Constructs an empty list.
     */
    public OrderMergedList() {
        super();
    }

    /**
     * Constructs a list containing the elements of the specified
     * collection, in the order they are returned by the collection's
     * iterator.
     *
     * @param c the collection whose elements are to be placed into this list
     * @throws NullPointerException if the specified collection is null
     */
    public OrderMergedList(final Collection<? extends T> c) {
        super(c);
    }

    /**
     * Constructs an empty list with the specified initial capacity.
     *
     * @param  initialCapacity  the initial capacity of the list
     * @throws IllegalArgumentException if the specified initial capacity
     *         is negative
     */
    public OrderMergedList(final int x) {
        super(x);
    }

    @Override
    public boolean add(final T newItem) {

        final int itemIndex = indexOf(newItem);
        if (itemIndex == -1) {
            add(_nextIndexToAdd, newItem);
            _nextIndexToAdd++;
        }
        else {
            _nextIndexToAdd = itemIndex;
        }
        return itemIndex == -1;
    }
}
