/*
 * Copyright (c) 2020, Netherlands Forensic Institute
 * All rights reserved.
 */
package nl.minvenj.nfi.smartrank.utils;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.number.IsCloseTo.closeTo;
import static org.junit.Assert.*;

import org.junit.Test;

public class AverageTest {

    @Test
    public void testSizeEmpty() {
        assertThat(new Average().size(), is(0L));
    }

    @Test
    public void testValueEmpty() {
        assertThat(new Average().get(), closeTo(0.0, 0.0000001));
    }
    
    @Test
    public void testSizeOne() {
        Average average = new Average();
        average.add(5);
        assertThat(average.size(), is(1L));
        assertThat(average.get(), closeTo(5.0, 0.0000001));
    }

    @Test
    public void testSizeOneInConstructor() {
        Average average = new Average(5);
        assertThat(average.size(), is(1L));
        assertThat(average.get(), closeTo(5.0, 0.0000001));
    }

    @Test
    public void testSizeTwo() {
        Average average = new Average();
        average.add(5);
        average.add(10);
        assertThat(average.size(), is(2L));
        assertThat(average.get(), closeTo(7.5, 0.0000001));
    }

    @Test
    public void testSizeTwoInConstructor() {
        Average average = new Average(5, 10);
        assertThat(average.size(), is(2L));
        assertThat(average.get(), closeTo(7.5, 0.0000001));
    }
    
    @Test
    public void testSizeThree() {
        Average average = new Average();
        average.add(5);
        average.add(10);
        average.add(38);
        assertThat(average.size(), is(3L));
        assertThat(average.get(), closeTo(17.66666666, 0.0000001));
    }

    @Test
    public void testSizeThreeInConstructor() {
        Average average = new Average(5, 10, 38);
        assertThat(average.size(), is(3L));
        assertThat(average.get(), closeTo(17.66666666, 0.0000001));
    }
    
    @Test
    public void testOneTimeMillis() {
        Average average = new Average();
        average.add(1603101000138L);
        assertThat(average.size(), is(1L));
        assertThat(average.get(), closeTo(1603101000138L, 0.0000001));
    }
    
    @Test
    public void testTwoTimesMillis() {
        Average average = new Average();
        average.add(1603101000138L);
        average.add(1603101000272L);
        assertThat(average.size(), is(2L));
        assertThat(average.get(), closeTo(1603101000205L, 0.0000001));
    }
    
    @Test
    public void testReset() {
        Average average = new Average(1,2,3);
        assertThat(average.size(), is(3L));
        assertThat(average.get(), closeTo(2.0, 0.0000001));
        average.reset();
        assertThat(average.size(), is(0L));
        assertThat(average.get(), closeTo(0.0, 0.0000001));
    }
}
