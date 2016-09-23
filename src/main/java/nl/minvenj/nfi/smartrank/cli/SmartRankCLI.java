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
package nl.minvenj.nfi.smartrank.cli;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;

import nl.minvenj.nfi.smartrank.messages.data.AddCrimeSceneFilesMessage;
import nl.minvenj.nfi.smartrank.messages.data.AddKnownFilesMessage;
import nl.minvenj.nfi.smartrank.messages.data.DatabaseFileMessage;
import nl.minvenj.nfi.smartrank.messages.data.PopulationStatisticsFileMessage;
import nl.minvenj.nfi.smartrank.raven.messages.MessageBus;
import nl.minvenj.nfi.smartrank.raven.messages.RavenMessage;

public class SmartRankCLI {

    public static void process(final String[] args) throws IOException {
        final String[] effectiveParameters = handleSessionFileReferences(args);
        configureDatabase(effectiveParameters);
        configureCrimesceneProfiles(effectiveParameters);
        configureKnownProfiles(effectiveParameters);
        configureParameters(effectiveParameters);
//        configureProsecution(effectiveParameters);
//        configureDefense(effectiveParameters);
    }

    private static void configureDatabase(final String[] args) {
        for (int idx = 0; idx < args.length; idx++) {
            if (args[idx].equalsIgnoreCase("-db")) {
                MessageBus.getInstance().send("SmartRankCLI", new DatabaseFileMessage(new File(args[idx + 1])));
            }
        }
    }

    private static void configureCrimesceneProfiles(final String[] args) {
        try {
            collectFiles(args, AddCrimeSceneFilesMessage.class, "-mix");
        }
        catch (final Exception e) {
            throw new IllegalArgumentException("Error configuring crimescene profiles", e);
        }
    }

    private static void configureKnownProfiles(final String[] args) {
        try {
            collectFiles(args, AddKnownFilesMessage.class, "-known");
        }
        catch (final Exception e) {
            throw new IllegalArgumentException("Error configuring known profiles", e);
        }
    }

    private static void collectFiles(final String[] args, final Class<? extends RavenMessage<List<File>>> messageClass, final String parm) throws Exception {
        final ArrayList<File> files = new ArrayList<>();
        for (int idx = 0; idx < args.length; idx++) {
            if (args[idx].equalsIgnoreCase(parm)) {
                files.add(new File(args[idx + 1]));
            }
        }
        if (!files.isEmpty()) {
            final Constructor<? extends RavenMessage<List<File>>> c = messageClass.getConstructor(List.class);
            MessageBus.getInstance().send("SmartRankCLI", c.newInstance(files));
        }
    }

    private static void configureParameters(final String[] args) {
        for (int idx = 0; idx < args.length; idx++) {
            if (args[idx].equalsIgnoreCase("-freq")) {
                MessageBus.getInstance().send("SmartRankCLI", new PopulationStatisticsFileMessage(new File(args[idx + 1])));
            }
        }
    }

    private static void configureProsecution(final String[] args) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private static void configureDefense(final String[] args) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private static String[] handleSessionFileReferences(final String[] args) throws IOException {
        final ArrayList<String> newArgs = new ArrayList<>();

        for (int idx = 0; idx < args.length; idx++) {
            final String curArg = args[idx];
            if (curArg.equalsIgnoreCase("-cfg")) {
                newArgs.addAll(loadConfigFile(args[idx + 1]));
                idx++;
            } else {
                newArgs.add(curArg);
            }
        }
        return newArgs.toArray(new String[newArgs.size()]);
    }

    private static Collection<? extends String> loadConfigFile(final String fileName) throws IOException {
        final ArrayList<String> additionalArguments = new ArrayList<>();
        final Properties props = new Properties();
        try (FileInputStream fis = new FileInputStream(fileName)) {
            props.load(fis);
        }
        for (final String key : props.stringPropertyNames()) {
            additionalArguments.add(key.startsWith("-") ? key : "-" + key);
            additionalArguments.add(props.getProperty(key));
        }
        return additionalArguments;
    }
}
