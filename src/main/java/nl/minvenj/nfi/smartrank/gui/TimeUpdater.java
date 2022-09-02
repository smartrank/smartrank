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
package nl.minvenj.nfi.smartrank.gui;

import java.util.ArrayList;

import javax.swing.JLabel;

public class TimeUpdater extends Thread {

    private static final long INTERVAL = 490;
    private static final String[] TIME_REMAINING_LABELS = {"- :  :  ", " -:  :  ", "  :- :  ", "  : -:  ", "  :  :- ", "  :  : -"};
    private final ArrayList<JLabel> _timeRemainingLabels;
    private final ArrayList<JLabel> _runningTimeLabels;
    private int _percent;
    private boolean _needRetrain;
    private long _startTime;
    private boolean _interrupted;
    private static TimeUpdater _me;

    private TimeUpdater() {
        _runningTimeLabels = new ArrayList<>();
        _timeRemainingLabels = new ArrayList<>();
        _percent = 0;
        _needRetrain = true;
        setDaemon(true);
        setName("TimeUpdater");
    }

    public void addLabels(final JLabel runningTimeLabel, final JLabel timeRemainingLabel) {
        synchronized (_me) {
            _runningTimeLabels.add(runningTimeLabel);
            _timeRemainingLabels.add(timeRemainingLabel);
        }
    }

    public void addRunningTimeLabel(final JLabel runningTimeLabel) {
        _runningTimeLabels.add(runningTimeLabel);
    }

    public static synchronized TimeUpdater getInstance() {
        if (_me == null || !_me.isAlive()) {
            _me = new TimeUpdater();
            _me.start();
        }
        return _me;
    }

    @Override
    public void run() {
        long estimatedTimeLeft = 0;
        while (!_interrupted && !isInterrupted()) {
            final long elapsed = System.currentTimeMillis() - _startTime;
            synchronized (_me) {
                for (final JLabel runningTimeLabel : _runningTimeLabels) {
                    runningTimeLabel.setText(formatTimeInterval(elapsed));
                }

                if (_needRetrain) {
                    if (_percent > 0) {
                        final long newEstimatedTimeLeft = (elapsed * (100 - _percent)) / _percent;
                        if (estimatedTimeLeft == Long.MAX_VALUE) {
                            estimatedTimeLeft = Math.max(newEstimatedTimeLeft, 0);
                        }
                        else {
                            final long diff = (newEstimatedTimeLeft - estimatedTimeLeft) * _percent / 100;
                            estimatedTimeLeft += diff;
                        }
                    }
                    else {
                        estimatedTimeLeft = Long.MAX_VALUE;
                    }
                    _needRetrain = false;
                }

                if (estimatedTimeLeft == Long.MAX_VALUE || estimatedTimeLeft <= -1000L) {
                    for (final JLabel timeRemainingLabel : _timeRemainingLabels) {
                        timeRemainingLabel.setText(TIME_REMAINING_LABELS[((int) (elapsed / 1000)) % TIME_REMAINING_LABELS.length]);
                    }
                }
                else {
                    for (final JLabel timeRemainingLabel : _timeRemainingLabels) {
                        timeRemainingLabel.setText(formatTimeInterval(estimatedTimeLeft));
                    }
                    estimatedTimeLeft -= INTERVAL;
                }
            }

            try {
                sleep(INTERVAL);
            }
            catch (final InterruptedException ex) {
                interrupt();
            }

        }
    }

    @Override
    public synchronized void start() {
        _startTime = System.currentTimeMillis();
        super.start();
    }

    private String formatTimeInterval(final long timeInterval) {
        final long hours = timeInterval / 3600000;
        final long minutes = (timeInterval % 3600000) / 60000;
        final long seconds = (timeInterval % 60000) / 1000;
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    public void setPercentReady(final int percent) {
        if (!_needRetrain && percent != _percent) {
            _needRetrain = true;
        }
        _percent = percent;
    }

    @Override
    public void interrupt() {
        super.interrupt();
        _interrupted = true;
    }
}
