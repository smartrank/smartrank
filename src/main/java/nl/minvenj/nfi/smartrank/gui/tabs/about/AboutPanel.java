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
package nl.minvenj.nfi.smartrank.gui.tabs.about;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.SystemColor;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Locale;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextPane;
import javax.swing.SwingConstants;
import javax.swing.UIManager;

import net.miginfocom.swing.MigLayout;
import nl.minvenj.nfi.smartrank.SmartRank;
import nl.minvenj.nfi.smartrank.gui.tabs.SmartRankPanel;

public class AboutPanel extends SmartRankPanel {

    private JProgressBar _memoryLoad;
    private JLabel _freeMemory;
    private JLabel _peakMemory;
    private JLabel _totalMemory;
    private Thread _memoryMonitor;

    /**
     * Creates new form AboutPanel
     */
    public AboutPanel() {

        setLayout(new MigLayout("", "[132px][18px][280px,grow,fill]", "[29px][70.00px][76.00px][115px,grow,fill]"));

        final JLabel titleLabel = new JLabel("SmartRank");
        titleLabel.setFont(new Font("Tahoma", 1, 24));
        add(titleLabel, "cell 0 0,alignx left,aligny top");

        final JLabel versionLabel = new JLabel(SmartRank.getRevision(), SwingConstants.TRAILING);
        versionLabel.setEnabled(false);
        add(versionLabel, "cell 2 0,alignx right,aligny bottom");

        add(new JLabel(new ImageIcon(getClass().getResource("/images/128x128/qubodup-bloodSplash.png"))), "cell 0 1 1 2,alignx center,aligny top");

        final JTextPane descriptionTextArea = new JTextPane();
        descriptionTextArea.setText("SmartRank allows DNA database searches for individuals whose profiles lead to high LRs in favor of the prosecution hypothesis.");
        descriptionTextArea.setBackground(SystemColor.control);
        descriptionTextArea.setFont(UIManager.getFont("EditorPane.font"));
        descriptionTextArea.setEditable(false);
        add(descriptionTextArea, "cell 2 1,growx,aligny top");

        final JTextPane signatureTextArea = new JTextPane();
        signatureTextArea.setContentType("text/html");
        signatureTextArea.setText(SmartRank.getSignatureInfo());
        signatureTextArea.setBackground(SystemColor.control);
        signatureTextArea.setFont(UIManager.getFont("EditorPane.font"));
        signatureTextArea.setEditable(false);
        add(signatureTextArea, "cell 2 2,grow");

        final JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("License", getLicensePane());
        tabbedPane.addTab("System Information", getSystemInfoPane());
        tabbedPane.addTab("Memory", getMemoryPanel());
        add(tabbedPane, "cell 0 3 3 1,grow");
    }

    private Component getMemoryPanel() {
        final JPanel memoryPanel = new JPanel();
        memoryPanel.setLayout(new MigLayout("", "[][grow]", "[][][][][][grow]"));

        final JLabel lblMaxMemory = new JLabel("Memory Load");
        memoryPanel.add(lblMaxMemory, "cell 0 0");

        _memoryLoad = new JProgressBar(0, 100);
        _memoryLoad.setStringPainted(true);
        memoryPanel.add(_memoryLoad, "cell 1 0,growx");

        memoryPanel.add(new JLabel("Free Memory"), "cell 0 1");

        _freeMemory = new JLabel();
        memoryPanel.add(_freeMemory, "cell 1 1");

        memoryPanel.add(new JLabel("Total Memory"), "cell 0 2");

        _totalMemory = new JLabel();
        memoryPanel.add(_totalMemory, "cell 1 2");

        final long maxMemory = Runtime.getRuntime().maxMemory();
        memoryPanel.add(new JLabel("Max Memory"), "cell 0 3");
        memoryPanel.add(new JLabel(String.format("%d bytes (%d MB)", maxMemory, maxMemory / (1024 * 1024))), "cell 1 3");

        _peakMemory = new JLabel();
        memoryPanel.add(new JLabel("Peak Allocated Memory"), "cell 0 4");
        memoryPanel.add(_peakMemory, "cell 1 4");

        _memoryMonitor = new Thread() {
            @Override
            public void run() {
                try {
                    int counter = 0;
                    int realPercentage = 0;
                    long peakAllocatedMemory = 0;
                    while (!isInterrupted()) {
                        sleep(50);
                        final long totalMemory = Runtime.getRuntime().totalMemory();
                        final long freeMemory = Runtime.getRuntime().freeMemory();
                        final long allocatedMemory = totalMemory - freeMemory;
                        if (allocatedMemory > peakAllocatedMemory) {
                            peakAllocatedMemory = allocatedMemory;
                        }
                        if (counter == 0) {
                            realPercentage = (int) ((allocatedMemory * 100L) / maxMemory);
                            final long peakAllocatedPercentage = (peakAllocatedMemory * 100L) / maxMemory;
                            _peakMemory.setText(String.format("%d bytes (%d MB, %d%%)", peakAllocatedMemory, peakAllocatedMemory / (1024 * 1024), Math.min(peakAllocatedPercentage, 100)));
                            _totalMemory.setText(String.format("%d bytes (%d MB)", totalMemory, totalMemory / (1024 * 1024)));
                            _freeMemory.setText(String.format("%d bytes (%d MB)", freeMemory, freeMemory / (1024 * 1024)));
                        }
                        counter = (counter + 1) % 20;
                        _memoryLoad.setForeground(getColor(_memoryLoad.getValue()));
                        if (_memoryLoad.getValue() > realPercentage) {
                            _memoryLoad.setValue(_memoryLoad.getValue() - 1);
                        }
                        else if (_memoryLoad.getValue() < realPercentage) {
                            _memoryLoad.setValue(_memoryLoad.getValue() + 1);
                        }
                    }
                }
                catch (final InterruptedException ie) {
                }
            }

            private Color getColor(final int value) {
                final Color[] colors = new Color[]{
                    new Color(0x00008800),
                    new Color(0x00118800),
                    new Color(0x00228800),
                    new Color(0x00338800),
                    new Color(0x00448800),
                    new Color(0x00558800),
                    new Color(0x00668800),
                    new Color(0x00778800),
                    new Color(0x00888800),
                    new Color(0x00887700),
                    new Color(0x00886600),
                    new Color(0x00885500),
                    new Color(0x00884400),
                    new Color(0x00883300),
                    new Color(0x00882200),
                    new Color(0x00881100),
                    new Color(0x00880000)
                };
                return colors[(value * (colors.length - 1) / 100)];
            };
        };
        _memoryMonitor.setDaemon(true);
        _memoryMonitor.setName("MemoryMonitor");
        _memoryMonitor.start();

        return memoryPanel;
    }

    private JPanel getSystemInfoPane() {
        final JPanel root = new JPanel(new MigLayout("", "[grow,fill]", "[grow,fill][]"));

        final JScrollPane systemInfoScrollPane = new JScrollPane();
        final JTextPane systemInfoTextArea = new JTextPane();
        systemInfoTextArea.setEditable(false);
        systemInfoScrollPane.setViewportView(systemInfoTextArea);
        final ArrayList<String> properties = new ArrayList<>();
        properties.add("runtime.availableProcessors = " + Runtime.getRuntime().availableProcessors());
        properties.add(String.format("runtime.maxMemory = %d bytes (%d MB)", Runtime.getRuntime().maxMemory(), Runtime.getRuntime().maxMemory() / (1024 * 1024)));
        properties.add("locale.default = " + Locale.getDefault());
        final Enumeration<?> propertyNames = System.getProperties().propertyNames();
        while (propertyNames.hasMoreElements()) {
            final String name = "" + propertyNames.nextElement();
            final String value = System.getProperty(name);
            properties.add(name + " = " + value);
        }
        Collections.sort(properties);
        final StringBuilder sb = new StringBuilder();
        for (final String property : properties) {
            sb.append(property).append("\r\n");
        }
        systemInfoTextArea.setText(sb.toString());
        systemInfoTextArea.setCaretPosition(0);
        root.add(systemInfoScrollPane, "cell 0 0");

        return root;
    }

    private JScrollPane getLicensePane() {
        final JScrollPane licenseScrollPane = new JScrollPane();
        final JTextArea licenseTextArea = new JTextArea();
        licenseTextArea.setEditable(false);
        licenseScrollPane.setViewportView(licenseTextArea);
        try {
            final LineNumberReader bis = new LineNumberReader(new InputStreamReader(getClass().getResourceAsStream("/gpl-3.0.txt")));
            final StringBuilder licenseBuilder = new StringBuilder();
            String line = bis.readLine();
            while (line != null) {
                licenseBuilder.append(line).append("\n");
                line = bis.readLine();
            }
            licenseTextArea.setText(licenseBuilder.toString());
            licenseTextArea.setCaretPosition(0);
        }
        catch (final IOException ex) {
            licenseTextArea.setText("License could not be loaded!" + ex.getClass().getName() + ": " + ex.getMessage());
        }
        return licenseScrollPane;
    }

    public void shutDown() {
        _memoryMonitor.interrupt();
    }
}
