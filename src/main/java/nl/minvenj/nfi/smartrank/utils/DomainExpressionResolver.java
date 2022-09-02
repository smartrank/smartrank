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

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;

import nl.minvenj.nfi.smartrank.domain.DefenseHypothesis;
import nl.minvenj.nfi.smartrank.domain.ProsecutionHypothesis;
import nl.minvenj.nfi.smartrank.domain.Sample;
import nl.minvenj.nfi.smartrank.messages.data.CrimeSceneProfilesMessage;
import nl.minvenj.nfi.smartrank.messages.data.DefenseHypothesisMessage;
import nl.minvenj.nfi.smartrank.messages.data.ProsecutionHypothesisMessage;
import nl.minvenj.nfi.smartrank.raven.messages.MessageBus;

/**
 * Resolves an expression containing placeholders referencing domain values. Placeholders are contained in {},
 * and can reference domain values or system properties, e.g:
 * <UL>
 *   <LI>{CASEFILES_FOLDER} refers to the folder in which the case files are stored</LI>
 *   <LI>{user.home} refers to the location of the current user's home folder</LI>
 * </UL>
 * Domain placeholders are not case sensitive. The following domain placeholders are defined:
 * <UL>
 *   <LI>CASEFILES_FOLDER - the name of the folder where casefiles (crimescene samples etc) are stored</LI>
 *   <LI>CRIMESCENE_PROFILES - a comma-separated list of the names of enabled crimescene samples</LI>
 *   <LI>HD - a brief description of Hd</LI>
 *   <LI>HP - a brief description of Hp</LI>
 *   <LI>DATE - the current date in yyyyMMdd format</LI>
 *   <LI>TIME - the current time in HHmmss format</LI>
 * </UL>
 */
public class DomainExpressionResolver {

    private DomainExpressionResolver() {
    }

    /**
     * Resolves domain references in the supplied template.
     *
     * @param template a String optionally containing domain references enclosed in {} characters
     * @param dateTime DATE and TIME are resolved to this value
     * @return a String containing the template with all references expanded to their actual values
     */
    public static String resolve(final String template, final long dateTime) {
        String resolvedValue = template;

        while (resolvedValue.matches(".*\\{.+\\}.*")) {
            final int start = resolvedValue.indexOf("{");
            final int end = resolvedValue.indexOf("}", start);
            if (start != -1 && end != -1) {
                resolvedValue = resolvedValue.substring(0, start) + resolveVariable(resolvedValue.substring(start + 1, end), dateTime) + resolvedValue.substring(end + 1);
            }
        }
        return resolvedValue;
    }

    private static String resolveVariable(final String variable, final long dateTime) {
        if ("CASEFILES_FOLDER".equalsIgnoreCase(variable)) {
            final String file = getFirstEnabledCrimesceneProfileFilename();
            return new File(file).getParent();
        }
        if ("CRIMESCENE_PROFILES".equalsIgnoreCase(variable)) {
            String traceIDs = "";
            for (final Sample replicate : MessageBus.getInstance().query(CrimeSceneProfilesMessage.class)) {
                if (replicate.isEnabled()) {
                    final String repId = replicate.getName().replaceAll("Rep\\d+$", "");
                    if (!traceIDs.contains(repId)) {
                        if (!traceIDs.isEmpty()) {
                            traceIDs += ",";
                        }
                        traceIDs += repId;
                    }
                }
            }
            return FileNameSanitizer.sanitize(traceIDs);
        }
        if ("HD".equalsIgnoreCase(variable)) {
            final DefenseHypothesis hd = MessageBus.getInstance().query(DefenseHypothesisMessage.class);
            return "Hd-" + hd.getUnknownCount() + "U";
        }
        if ("HP".equalsIgnoreCase(variable)) {
            final ProsecutionHypothesis hp = MessageBus.getInstance().query(ProsecutionHypothesisMessage.class);
            return "Hp-" + hp.getUnknownCount() + "U";
        }
        if ("DATE".equalsIgnoreCase(variable)) {
            final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            return sdf.format(new Date(dateTime));
        }
        if ("TIME".equalsIgnoreCase(variable)) {
            final SimpleDateFormat sdf = new SimpleDateFormat("HHmmss");
            return sdf.format(new Date(dateTime));
        }
        return System.getProperty(variable, variable);
    }

    private static String getFirstEnabledCrimesceneProfileFilename() {
        final Collection<Sample> crimesceneProfiles = MessageBus.getInstance().query(CrimeSceneProfilesMessage.class);
        final Sample first = null;
        final Iterator<Sample> iterator = crimesceneProfiles.iterator();
        while (first == null && iterator.hasNext()) {
            final Sample sample = iterator.next();
            if (sample.isEnabled()) {
                return sample.getSourceFile();
            }
        }
        return "";
    }
}
