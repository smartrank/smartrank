package nl.minvenj.nfi.smartrank.gui.tabs.batchmode;

import java.util.ArrayList;
import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.SpinnerModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

final class TimeSpinnerModel implements SpinnerModel {
    int _curTime = 0;
    long _lastNext = 0;
    int _nextCount = 0;
    long _lastPrev = 0;
    int _prevCount = 0;
    private final Collection<ChangeListener> _listeners = new ArrayList<>();

    public TimeSpinnerModel(final String initial) {
        setValue(initial);
    }

    @Override
    public Object getValue() {
        return String.format("%02d:%02d", _curTime / 60, _curTime % 60);
    }

    @Override
    public void setValue(final Object value) {
        final Pattern pattern = Pattern.compile("([01][0-9]|2[0-3])\\:?([0-5][0-9])");
        final Matcher matcher = pattern.matcher(value.toString().trim());
        if (matcher.matches()) {
            _curTime = Integer.parseInt(matcher.group(1)) * 60 + Integer.parseInt(matcher.group(2));
        }
        for (final ChangeListener l : _listeners) {
            l.stateChanged(new ChangeEvent(this));
        }
    }

    @Override
    public Object getNextValue() {
        final long now = System.currentTimeMillis();
        _nextCount = (_lastNext == 0 || ((now - _lastNext) < 100)) ? _nextCount + 1 : 0;
        final int delta = (_nextCount < 15 || _curTime % 15 != 0) ? 1 : 15;
        _lastNext = now;
        _nextCount++;
        _prevCount = 0;
        final int temp = (_curTime + delta) % 1440;
        return String.format("%02d:%02d", temp / 60, temp % 60);
    }

    @Override
    public Object getPreviousValue() {
        final long now = System.currentTimeMillis();
        _nextCount = (_lastPrev == 0 || ((now - _lastPrev) < 100)) ? _prevCount + 1 : 0;
        final int delta = (_prevCount < 15 || _curTime % 15 != 0) ? 1 : 15;
        _lastPrev = now;
        _nextCount = 0;
        _prevCount++;
        int temp = (_curTime - delta);
        if (temp < 0)
            temp = 1440 + temp;
        return String.format("%02d:%02d", temp / 60, temp % 60);
    }

    @Override
    public void addChangeListener(final ChangeListener l) {
        _listeners.add(l);
    }

    @Override
    public void removeChangeListener(final ChangeListener l) {
        _listeners.remove(l);
    }
}