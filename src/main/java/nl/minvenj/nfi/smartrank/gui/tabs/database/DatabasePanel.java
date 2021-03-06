/*
 * Copyright (C) 2015,2017 Netherlands Forensic Institute
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
package nl.minvenj.nfi.smartrank.gui.tabs.database;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.DefaultTableModel;

import net.miginfocom.swing.MigLayout;
import nl.minvenj.nfi.smartrank.domain.DNADatabase;
import nl.minvenj.nfi.smartrank.domain.ProblemLocation;
import nl.minvenj.nfi.smartrank.gui.SmartRankGUISettings;
import nl.minvenj.nfi.smartrank.gui.tabs.SmartRankPanel;
import nl.minvenj.nfi.smartrank.gui.tabs.database.config.DBSettingsDialog;
import nl.minvenj.nfi.smartrank.messages.data.DatabaseConnectionMessage;
import nl.minvenj.nfi.smartrank.messages.data.DatabaseFileMessage;
import nl.minvenj.nfi.smartrank.messages.data.DatabaseMessage;
import nl.minvenj.nfi.smartrank.messages.status.ApplicationStatusMessage;
import nl.minvenj.nfi.smartrank.messages.status.DatabaseFormatProblemMessage;
import nl.minvenj.nfi.smartrank.raven.ApplicationStatus;
import nl.minvenj.nfi.smartrank.raven.annotations.ExecuteOnSwingEventThread;
import nl.minvenj.nfi.smartrank.raven.annotations.RavenMessageHandler;
import nl.minvenj.nfi.smartrank.raven.components.zebra.ZebraTable;
import nl.minvenj.nfi.smartrank.raven.messages.MessageBus;

/**
 * This panel contains GUI elements for uploading the DNA database.
 */
public class DatabasePanel extends SmartRankPanel {
    private static final long serialVersionUID = 2107955085886692693L;

    private JButton _browseButton;
    private JTextField _databaseFileName;
    private JLabel _databaseConnectionLabel;
    private JLabel _databaseFormat;
    private JLabel _databaseFormatLabel;
    private JLabel _databaseRecordCount;
    private JLabel _databaseRecordCountLabel;
    private ZebraTable _formattingProblemsTable;
    private JLabel _formattingProblemsLabel;
    private JLabel _iconLabel;
    private JScrollPane _formattingProblemsScrollPane;
    private JLabel _formatProblemCount;
    private DBStatisticsPanel _statisticsPanel;
    private JSplitPane _splitPane;
    private JButton _connectButton;

    public DatabasePanel() {
        initComponents();
        registerAsListener();
        acceptSingleDroppedFile(DatabaseFileMessage.class);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    private void initComponents() {
        setLayout(new MigLayout("", "[1px][pref!,shrink 0][17.00px,fill][269.00px,grow,fill][][pref!,right]", "[14.00px][22.00px][21.00px][14px][14px][18.00px][110px,grow]"));

        _databaseFormatLabel = new JLabel();
        _databaseFormatLabel.setText("Database format:");
        _databaseRecordCountLabel = new JLabel();
        _databaseRecordCountLabel.setText("Number of records:");
        _formattingProblemsLabel = new JLabel();
        _formattingProblemsLabel.setText("Formatting problems:");
        _iconLabel = new JLabel();
        _iconLabel.setIcon(new ImageIcon(getClass().getResource("/images/64x64/db.png")));
        _databaseFileName = new JTextField();
        _databaseFileName.setEditable(false);
        _databaseFileName.setDropTarget(getDropTarget());
        _browseButton = new JButton("Browse...", new ImageIcon(getClass().getResource("/images/16x16/folder.png")));
        _browseButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent evt) {
                browseButtonActionPerformed(evt);
            }
        });
        _databaseFormat = new JLabel();
        _databaseRecordCount = new JLabel();
        _formatProblemCount = new JLabel();
        _formatProblemCount.setVerticalAlignment(SwingConstants.TOP);

        _iconLabel.setName("databaseIcon");
        _browseButton.setName("browseButton");
        _databaseFormatLabel.setLabelFor(_databaseFormat);
        _databaseRecordCountLabel.setLabelFor(_databaseRecordCount);
        _formattingProblemsLabel.setLabelFor(_formatProblemCount);

        _databaseConnectionLabel = new JLabel("Database");

        add(_iconLabel, "cell 0 0 1 3,aligny top");
        add(_databaseConnectionLabel, "cell 1 0,alignx trailing");
        add(_databaseFileName, "cell 3 0,growx");

        _connectButton = new JButton("Connect...", new ImageIcon(getClass().getResource("/images/16x16/database_go.png")));
        _connectButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                connectButtonActionPerformed();
            }
        });

        add(_connectButton, "cell 4 0,aligny center");
        add(_browseButton, "cell 5 0,alignx center");
        add(_databaseFormat, "cell 2 3,grow");
        add(_databaseRecordCountLabel, "cell 0 4 2 1,alignx left,aligny top");
        add(_databaseFormatLabel, "cell 0 3 2 1,alignx left,aligny top");
        add(_databaseRecordCount, "cell 2 4,grow");
        add(_formattingProblemsLabel, "cell 0 5 2 1,alignx left,aligny top");
        add(_formatProblemCount, "cell 2 5,grow");

        _splitPane = new JSplitPane();
        _splitPane.setResizeWeight(1);
        _splitPane.setOneTouchExpandable(true);
        _splitPane.setContinuousLayout(true);
        add(_splitPane, "cell 0 6 6 1,grow");
        _formattingProblemsScrollPane = new JScrollPane();
        _splitPane.setLeftComponent(_formattingProblemsScrollPane);
        _formattingProblemsTable = new ZebraTable();
        _formattingProblemsTable.setAutoCreateRowSorter(true);
        _formattingProblemsTable.setModel(new DefaultTableModel(new Object[][]{},
                                                                new String[]{"Specimen", "Locus", "Description"}) {
            private static final long serialVersionUID = 6843817182458249187L;
            Class<?>[] _types = new Class[]{
                java.lang.Object.class, java.lang.Object.class, java.lang.String.class
            };

            @Override
            public Class<?> getColumnClass(final int columnIndex) {
                return _types[columnIndex];
            }

            @Override
            public boolean isCellEditable(final int rowIndex, final int columnIndex) {
                return false;
            }
        });
        _formattingProblemsTable.setName("dbFormattingProblem");
        _formattingProblemsScrollPane.setViewportView(_formattingProblemsTable);

        _iconLabel.setName("databaseIcon");
        _databaseFileName.setName("databaseFileName");
        _browseButton.setName("browseButton");
        _databaseFormat.setName("databaseFormat");
        _databaseFormatLabel.setLabelFor(_databaseFormat);
        _databaseRecordCount.setName("databaseRecordCount");
        _databaseRecordCountLabel.setLabelFor(_databaseRecordCount);
        _formatProblemCount.setName("formatProblemCount");
        _formattingProblemsLabel.setLabelFor(_formatProblemCount);
        _formattingProblemsTable.setName("formattingProblemsTable");

        _statisticsPanel = new DBStatisticsPanel();
        _splitPane.setRightComponent(_statisticsPanel);
        _splitPane.getRightComponent().setMinimumSize(new Dimension());
        _splitPane.setDividerSize(10);
    }

    private void connectButtonActionPerformed() {
        final DBSettingsDialog dlg = new DBSettingsDialog((JFrame) SwingUtilities.getWindowAncestor(this), false);
        dlg.setLocationRelativeTo(this);
        dlg.setVisible(true);
        if(dlg.isOk()) {
            MessageBus.getInstance().send(this, new DatabaseConnectionMessage(dlg.getDBSettings()));
        }
    }

    private void browseButtonActionPerformed(final java.awt.event.ActionEvent evt) {
        final JFileChooser chooser = new JFileChooser("");

        final FileFilter xmlCsvAndTxtFilter = new FileFilter() {
            @Override
            public String getDescription() {
                return "Codis database files (*.csv|*.txt|*.xml)";
            }

            @Override
            public boolean accept(final File f) {
                final String fileName = f.getName().toLowerCase();
                return f.isDirectory() || (f.isFile() && (fileName.endsWith(".csv") || fileName.endsWith(".txt") || fileName.endsWith(".xml")));
            }
        };
        chooser.setAcceptAllFileFilterUsed(true);
        chooser.addChoosableFileFilter(xmlCsvAndTxtFilter);
        chooser.setFileFilter(xmlCsvAndTxtFilter);

        chooser.setDialogTitle("Select a database file");
        chooser.setSelectedFile(new File(SmartRankGUISettings.getLastSelectedDatabaseFileName()));
        if (JFileChooser.APPROVE_OPTION == chooser.showOpenDialog(this)) {
            MessageBus.getInstance().send(this, new DatabaseFileMessage(chooser.getSelectedFile()));
        }
    }

    @RavenMessageHandler(DatabaseFormatProblemMessage.class)
    @ExecuteOnSwingEventThread
    void onDatabaseFormatProblem(final List<ProblemLocation> problems) {
        for (final ProblemLocation problem : problems) {
            _formattingProblemsTable.addRow(new Object[]{problem.getSpecimen(), problem.getLocus(), problem.getDescription()});
        }
        _formatProblemCount.setText("" + _formattingProblemsTable.getRowCount());
    }

    @RavenMessageHandler(DatabaseMessage.class)
    @ExecuteOnSwingEventThread
    public synchronized void onDatabaseChange(final DNADatabase db) {
        _databaseFileName.setText("");
        _statisticsPanel.clear();
        if (db != null) {
            if (db.getConnectString() != null && !db.getConnectString().equalsIgnoreCase(_databaseFileName.getText())) {
                _databaseFileName.setText(db.getConnectString());
            }
            _databaseFormat.setText(db.getFormatName());
            _databaseRecordCount.setText("" + db.getRecordCount());
            _statisticsPanel.setDatabase(db);
            if (db.getConfiguration().getDatabaseType().equalsIgnoreCase("File"))
                SmartRankGUISettings.setLastSelectedDatabaseFileName(db.getConnectString());
        }
    }

    @RavenMessageHandler({DatabaseFileMessage.class, DatabaseConnectionMessage.class})
    @ExecuteOnSwingEventThread
    public void onNewFile() {
        _databaseFormat.setText("");
        _databaseRecordCount.setText("");
        _formatProblemCount.setText("");
        _formattingProblemsTable.setRowCount(0);
        _statisticsPanel.clear();
    }

    @RavenMessageHandler(ApplicationStatusMessage.class)
    @ExecuteOnSwingEventThread
    public void onStatusChange(final ApplicationStatus newStatus) {
        if (newStatus == ApplicationStatus.VERIFYING_DB) {
            _formatProblemCount.setText("");
            _formattingProblemsTable.setRowCount(0);
        }

        final boolean enabled = !newStatus.isActive();
        _iconLabel.setEnabled(enabled);
        _browseButton.setEnabled(enabled);
        _connectButton.setEnabled(enabled);
        _databaseConnectionLabel.setEnabled(enabled);
        _databaseFormat.setEnabled(enabled);
        _databaseFormatLabel.setEnabled(enabled);
        _databaseRecordCount.setEnabled(enabled);
        _databaseRecordCountLabel.setEnabled(enabled);
    }

    /**
     * Loads the last-loaded database file.
     *
     * @return true if the file exists, false otherwise.
     */
    public boolean doLoad() {
        final File file = new File(SmartRankGUISettings.getLastSelectedDatabaseFileName());
        if (file.exists()) {
            MessageBus.getInstance().send(this, new DatabaseFileMessage(new File(SmartRankGUISettings.getLastSelectedDatabaseFileName())));
            return true;
        }
        return false;
    }

    /**
     * Connects to the database using the last-used settings.
     *
     * @return true if the settings were valid, false otherwise.
     */
    public boolean doConnect() {
        final DBSettingsDialog dlg = new DBSettingsDialog((JFrame) SwingUtilities.getWindowAncestor(this), true);
        dlg.setLocationRelativeTo(this);
        dlg.setVisible(true);
        if (dlg.isOk()) {
            MessageBus.getInstance().send(this, new DatabaseConnectionMessage(dlg.getDBSettings()));
            while (MessageBus.getInstance().query(ApplicationStatusMessage.class) != ApplicationStatus.BATCHMODE_IDLE) {
                try {
                    Thread.sleep(1000);
                }
                catch (final InterruptedException e) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }
}
