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

package nl.minvenj.nfi.smartrank.raven.timeformat;

public class TimeUtils {

    private TimeUtils() {
    }

    public static String formatDuration(long duration) {
        long milliseconds = duration % 1000;
        long seconds = (duration % 60000) / 1000;
        long minutes = (duration % 3600000) / 60000;
        long hours = duration / 3600000;

        StringBuilder sb = new StringBuilder();
        if (hours > 0) {
            sb.append(hours).append(" hour");
            if (hours > 1) {
                sb.append("s");
            }
        }
        if (minutes > 0) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append(minutes).append(" minute");
            if (minutes > 1) {
                sb.append("s");
            }
        }
        if (seconds > 0) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append(seconds).append(" second");
            if (seconds > 1) {
                sb.append("s");
            }
        }
        if (milliseconds > 0) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append(milliseconds).append(" millisecond");
            if (milliseconds > 1) {
                sb.append("s");
            }
        }
        return sb.toString();
    }
}
