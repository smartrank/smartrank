/*
 * Copyright (c) 2020, Netherlands Forensic Institute
 * All rights reserved.
 */
package nl.minvenj.nfi.smartrank.utils;

public class Average {
    private double _value;
    private long _count;
    
    public Average() {
    }

    public Average(long... values) {
        for(long value : values) {
            add(value);
        }
    }
    
    public long size() {
        return _count;
    }

    public double get() {
        return _value;
    }
    
    public void add(long val) {
        _value = (_value*_count + val) /( _count+1);
        _count++;
    }
    
    public void reset() {
        _value = 0;
        _count = 0;
    }
}
