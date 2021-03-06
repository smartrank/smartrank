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

import java.awt.Component;
import java.awt.Desktop;
import java.awt.Frame;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.Executors;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JTabbedPane;
import javax.swing.WindowConstants;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.miginfocom.swing.MigLayout;
import nl.minvenj.nfi.smartrank.SmartRank;
import nl.minvenj.nfi.smartrank.domain.AnalysisParameters;
import nl.minvenj.nfi.smartrank.domain.DefenseHypothesis;
import nl.minvenj.nfi.smartrank.domain.ProsecutionHypothesis;
import nl.minvenj.nfi.smartrank.gui.status.StatusPanel;
import nl.minvenj.nfi.smartrank.gui.tabs.about.AboutPanel;
import nl.minvenj.nfi.smartrank.gui.tabs.batchmode.BatchModePanel;
import nl.minvenj.nfi.smartrank.gui.tabs.crimesceneprofiles.CrimeSceneProfilesPanel;
import nl.minvenj.nfi.smartrank.gui.tabs.database.DatabasePanel;
import nl.minvenj.nfi.smartrank.gui.tabs.knownprofiles.KnownProfilesPanel;
import nl.minvenj.nfi.smartrank.gui.tabs.search.SearchPanel;
import nl.minvenj.nfi.smartrank.io.WritableFileSource;
import nl.minvenj.nfi.smartrank.messages.commands.WritableFileSourceMessage;
import nl.minvenj.nfi.smartrank.messages.data.AnalysisParametersMessage;
import nl.minvenj.nfi.smartrank.messages.data.DefenseHypothesisMessage;
import nl.minvenj.nfi.smartrank.messages.data.ProsecutionHypothesisMessage;
import nl.minvenj.nfi.smartrank.messages.status.ApplicationStatusMessage;
import nl.minvenj.nfi.smartrank.messages.status.ErrorStringMessage;
import nl.minvenj.nfi.smartrank.raven.ApplicationStatus;
import nl.minvenj.nfi.smartrank.raven.annotations.ExecuteOnSwingEventThread;
import nl.minvenj.nfi.smartrank.raven.annotations.RavenMessageHandler;
import nl.minvenj.nfi.smartrank.raven.messages.MessageBus;
import nl.minvenj.nfi.smartrank.utils.FilenameLengthChecker;
import nl.minvenj.nfi.smartrank.utils.OnlyNewFilesFilter;

public class SmartRankGUI extends javax.swing.JFrame implements WritableFileSource {

    private final class BatchModeWindowCloseHandler extends WindowAdapter {
        private final BatchModePanel _panel;

        public BatchModeWindowCloseHandler(final BatchModePanel batchModePanel) {
            _panel = batchModePanel;
        }

        @Override
        public void windowClosing(final WindowEvent e) {
            if (_panel.isRunning() && SmartRankRestrictions.isWindowCloseBlockedInBatchMode()) {
                setState(Frame.ICONIFIED);
            }
            else {
                if (JOptionPane.OK_OPTION == JOptionPane.showConfirmDialog(e.getWindow(), "Are you sure you want to close the application?", "Close SmartRank?", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE)) {
                    LOG.info("SmartRank closed.");
                    _aboutPanel.shutDown();
                    _tabEnabler.interrupt();
                    System.exit(0);
                }
            }
        }
    }

    private final class InteractiveWindowCloseHandler extends WindowAdapter {
        @Override
        public void windowClosing(final WindowEvent e) {
            if (JOptionPane.OK_OPTION == JOptionPane.showConfirmDialog(e.getWindow(), "Are you sure you want to close the application?", "Close SmartRank?", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE)) {
                LOG.info("SmartRank closed.");
                _aboutPanel.shutDown();
                _tabEnabler.interrupt();
                System.exit(0);
            }
        }
    }

    private static final Logger LOG = LoggerFactory.getLogger(SmartRankGUI.class);
    private AboutPanel _aboutPanel;
    private CrimeSceneProfilesPanel _crimeSceneProfilesPanel;
    private DatabasePanel _databasePanel;
    private KnownProfilesPanel _knownProfilesPanel;
    private JTabbedPane _mainTabbedPane;
    private JMenuBar _menuBar;
    private SearchPanel _searchPanel;
    private StatusPanel _statusPanel;
    private JMenuItem _aboutMenuItem;
    private JMenuItem _contentsMenuItem;
    private JMenu _helpMenu;
    private JMenu _optimizationsMenu;
    private JCheckBoxMenuItem _shutdownQforHp;
    private JCheckBoxMenuItem _shutdownQforHd;
    private JCheckBoxMenuItem _calculateHdOnce;
    private BatchModePanel _batchModePanel;
    private Thread _tabEnabler;

    /**
     * Creates new form GUI
     */
    public SmartRankGUI() {

        initComponentsCommon();
        if (SmartRankRestrictions.isBatchMode()) {
            LOG.info("Starting GUI in Batch Mode");
            initComponentsBatchMode();
        }
        else {
            LOG.info("Starting GUI in Interactive Mode");
            initComponentsInteractive();
        }

        createMenuBar();

        MessageBus.getInstance().registerSubscriber(this);

        MessageBus.getInstance().send(this, new WritableFileSourceMessage(this));

        pack();
        setLocationRelativeTo(null);

        if (SmartRankRestrictions.isBatchMode()) {
            Executors.newSingleThreadExecutor().submit(new Runnable() {
                @Override
                public void run() {
                    boolean stat = false;
                    if (SmartRankRestrictions.getBatchAutoStartMode().equalsIgnoreCase("file")) {
                        stat = _databasePanel.doLoad();
                    }
                    if (SmartRankRestrictions.getBatchAutoStartMode().equalsIgnoreCase("db")) {
                        stat = _databasePanel.doConnect();
                    }
                    if (stat) {
                        _mainTabbedPane.setSelectedComponent(_batchModePanel);
                        _batchModePanel.doRun();
                    }
                }
            });
        }
    }

    private void initComponentsCommon() {
        _mainTabbedPane = new javax.swing.JTabbedPane();
        _databasePanel = new nl.minvenj.nfi.smartrank.gui.tabs.database.DatabasePanel();
        _aboutPanel = new nl.minvenj.nfi.smartrank.gui.tabs.about.AboutPanel();
        _statusPanel = new nl.minvenj.nfi.smartrank.gui.status.StatusPanel();

        setTitle("SmartRank " + SmartRank.getVersion() + (SmartRankGUISettings.getWindowTitle().isEmpty() ? "" : " - " + SmartRankGUISettings.getWindowTitle()));

        getContentPane().setLayout(new MigLayout("insets 1", "[788px,grow,fill]", "[655px,grow,fill][17px:17px:17px]"));
        getContentPane().add(_mainTabbedPane, "cell 0 0,alignx center,aligny center,gapy 0");
        getContentPane().add(_statusPanel, "cell 0 1,alignx left,aligny top,gapy 0");

        _mainTabbedPane.addTab("DNA database", new javax.swing.ImageIcon(getClass().getResource("/images/16x16/db.png")), _databasePanel); // NOI18N
        _mainTabbedPane.addTab("About", new javax.swing.ImageIcon(getClass().getResource("/images/16x16/qubodup-bloodSplash.png")), _aboutPanel); // NOI18N

        // This to allow the WindowStateListener to actually block the window from
        // closing if more than a configurable amount of processing time remains unexported.
        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

        _tabEnabler = new Thread() {
            @Override
            public void run() {
                setName("GUI TabEnabler Thread");
                while (true) {
                    final int tabCount = _mainTabbedPane.getTabCount();
                    for (int idx = 0; idx < tabCount; idx++) {
                        final Component tabComponent = _mainTabbedPane.getComponentAt(idx);
                        _mainTabbedPane.setEnabledAt(idx, tabComponent != null && tabComponent.isEnabled());
                    }
                    try {
                        sleep(200);
                    }
                    catch (final InterruptedException ex) {
                        return;
                    }
                }
            }
        };

        _tabEnabler.setDaemon(true);
        _tabEnabler.start();
    }

    private void initComponentsBatchMode() {
        _batchModePanel = new BatchModePanel();
        _mainTabbedPane.insertTab("Batch Mode", new javax.swing.ImageIcon(getClass().getResource("/images/16x16/script.png")), _batchModePanel, null, 1);

        // Set application icon
        final ArrayList<Image> icons = new ArrayList<>();
        icons.add(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/images/16x16/qubodup-bloodSplashBatch.png")));
        icons.add(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/images/32x32/qubodup-bloodSplashBatch.png")));
        icons.add(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/images/64x64/qubodup-bloodSplashBatch.png")));
        icons.add(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/images/128x128/qubodup-bloodSplashBatch.png")));
        setIconImages(icons);

        addWindowListener(new BatchModeWindowCloseHandler(_batchModePanel));
    }

    private void initComponentsInteractive() {
        _crimeSceneProfilesPanel = new nl.minvenj.nfi.smartrank.gui.tabs.crimesceneprofiles.CrimeSceneProfilesPanel();
        _knownProfilesPanel = new nl.minvenj.nfi.smartrank.gui.tabs.knownprofiles.KnownProfilesPanel();
        _searchPanel = new nl.minvenj.nfi.smartrank.gui.tabs.search.SearchPanel();

        _crimeSceneProfilesPanel.setEnabled(false);
        _mainTabbedPane.insertTab("Crime-scene profiles", new javax.swing.ImageIcon(getClass().getResource("/images/16x16/red-palmprint.png")), _crimeSceneProfilesPanel, null, 1); // NOI18N

        _knownProfilesPanel.setEnabled(false);
        _mainTabbedPane.insertTab("Known profiles", new javax.swing.ImageIcon(getClass().getResource("/images/16x16/eppendorf-color-closed.png")), _knownProfilesPanel, null, 2); // NOI18N

        _searchPanel.setEnabled(false);
        _mainTabbedPane.insertTab("Search", new javax.swing.ImageIcon(getClass().getResource("/images/16x16/magnifier.png")), _searchPanel, null, 3); // NOI18N

        // Set application icon
        final ArrayList<Image> icons = new ArrayList<>();
        icons.add(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/images/16x16/qubodup-bloodSplash.png")));
        icons.add(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/images/32x32/qubodup-bloodSplash.png")));
        icons.add(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/images/64x64/qubodup-bloodSplash.png")));
        icons.add(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/images/128x128/qubodup-bloodSplash.png")));
        setIconImages(icons);

        addWindowListener(new InteractiveWindowCloseHandler());
    }

    private void createMenuBar() {
        _menuBar = new javax.swing.JMenuBar();
        _helpMenu = new javax.swing.JMenu();
        _contentsMenuItem = new javax.swing.JMenuItem();
        _aboutMenuItem = new javax.swing.JMenuItem();

        _helpMenu.setMnemonic('h');
        _helpMenu.setText("Help");

        _contentsMenuItem.setMnemonic('m');
        _contentsMenuItem.setText("Open Manual in a Browser");
        _helpMenu.add(_contentsMenuItem);
        _contentsMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                if (Desktop.isDesktopSupported()) {
                    try {
                        Desktop.getDesktop().browse(new File("manual/smartrank-manual.html").toURI());
                    }
                    catch (final IOException e1) {
                        MessageBus.getInstance().send(SmartRankGUI.this, new ErrorStringMessage(e1.getMessage()));
                    }
                }
            }
        });

        _aboutMenuItem.setMnemonic('a');
        _aboutMenuItem.setText("About");
        _helpMenu.add(_aboutMenuItem);
        _aboutMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                _mainTabbedPane.setSelectedComponent(_aboutPanel);
            }
        });

        _optimizationsMenu = new JMenu("Optimizations");
        _optimizationsMenu.setVisible(SmartRankRestrictions.isOptimizationsMenuShown());
        _menuBar.add(_optimizationsMenu);

        _shutdownQforHp = new JCheckBoxMenuItem("Shutdown Q designation for Hp", true);
        _optimizationsMenu.add(_shutdownQforHp);
        _shutdownQforHp.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                ProsecutionHypothesis prosecutionHypothesis = MessageBus.getInstance().query(ProsecutionHypothesisMessage.class);
                if (prosecutionHypothesis == null) {
                    prosecutionHypothesis = new ProsecutionHypothesis();
                }
                prosecutionHypothesis.setQDesignationShutdown(_shutdownQforHp.isSelected());
                MessageBus.getInstance().send(this, new ProsecutionHypothesisMessage(prosecutionHypothesis));
            }
        });

        _shutdownQforHd = new JCheckBoxMenuItem("Shutdown Q designation for Hd", false);
        _optimizationsMenu.add(_shutdownQforHd);
        _shutdownQforHd.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                DefenseHypothesis h = MessageBus.getInstance().query(DefenseHypothesisMessage.class);
                if (h == null) {
                    h = new DefenseHypothesis();
                }
                h.setQDesignationShutdown(_shutdownQforHd.isSelected());
                MessageBus.getInstance().send(this, new DefenseHypothesisMessage(h));
            }
        });

        _calculateHdOnce = new JCheckBoxMenuItem("Calculate Pr(E|Hd) only once", true);
        _optimizationsMenu.add(_calculateHdOnce);
        _calculateHdOnce.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                final AnalysisParameters params = MessageBus.getInstance().query(AnalysisParametersMessage.class);
                if (params != null) {
                    params.setCalculateHdOnce(_calculateHdOnce.isSelected());
                }
            }
        });

        _menuBar.add(_helpMenu);

        setJMenuBar(_menuBar);
    }

    @Override
    public String getWritableFile(final String fileName) {
        File fileWithCheckedName = new File(FilenameLengthChecker.check(new File(fileName).getAbsolutePath(), new OnlyNewFilesFilter(), SmartRankRestrictions.getMaximumPathLength()));
        while (!checkFile(fileWithCheckedName)) {
            final JFileChooser chooser = new JFileChooser();
            chooser.setDialogTitle("Cannot write file to " + fileWithCheckedName.getParent() + ". \nPlease select another location...");
            chooser.setSelectedFile(fileWithCheckedName);
            if (JOptionPane.OK_OPTION == chooser.showOpenDialog(this)) {
                fileWithCheckedName = new File(FilenameLengthChecker.check(chooser.getSelectedFile().getAbsolutePath(), new OnlyNewFilesFilter(), SmartRankRestrictions.getMaximumPathLength()));
            }
            else {
                return "";
            }
        }

        return fileWithCheckedName.getAbsolutePath();
    }

    @RavenMessageHandler(ApplicationStatusMessage.class)
    @ExecuteOnSwingEventThread
    void menuEnabler(final ApplicationStatus status) {
        _optimizationsMenu.setEnabled(!status.isActive());
    }

    private boolean checkFile(final File file) {
        final File parent = file.getParentFile();
        if (parent == null) {
            LOG.info("Parent for {} is null!", file);
            return false;
        }
        if (!parent.exists() && !parent.mkdirs()) {
            LOG.info("Directory {} does not exist and cannot be created!", parent);
            return false;
        }
        try {
            if (!file.exists() && !file.createNewFile()) {
                LOG.info("File {} cannot be created!", file);
                return false;
            }
        }
        catch (final Throwable t) {
            LOG.info("File {} cannot be created due to the following exception:", file, t);
            return false;
        }

        if (!file.canWrite()) {
            LOG.info("File {} is not writable!", file);
            return false;
        }

        return true;
    }
}
