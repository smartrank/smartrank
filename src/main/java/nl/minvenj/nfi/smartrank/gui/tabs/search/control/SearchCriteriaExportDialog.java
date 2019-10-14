package nl.minvenj.nfi.smartrank.gui.tabs.search.control;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.DefaultTableModel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.miginfocom.swing.MigLayout;
import nl.minvenj.nfi.smartrank.domain.AnalysisParameters;
import nl.minvenj.nfi.smartrank.domain.DefenseHypothesis;
import nl.minvenj.nfi.smartrank.domain.ProsecutionHypothesis;
import nl.minvenj.nfi.smartrank.domain.Sample;
import nl.minvenj.nfi.smartrank.gui.SmartRankGUISettings;
import nl.minvenj.nfi.smartrank.io.searchcriteria.SearchCriteriaWriter;
import nl.minvenj.nfi.smartrank.messages.data.AnalysisParametersMessage;
import nl.minvenj.nfi.smartrank.messages.data.DefenseHypothesisMessage;
import nl.minvenj.nfi.smartrank.messages.data.ProsecutionHypothesisMessage;
import nl.minvenj.nfi.smartrank.messages.status.ErrorStringMessage;
import nl.minvenj.nfi.smartrank.raven.messages.MessageBus;
import nl.minvenj.nfi.smartrank.utils.FileNameSanitizer;

@SuppressWarnings("serial")
public class SearchCriteriaExportDialog extends JDialog {
    private static final Logger LOG = LoggerFactory.getLogger(SearchCriteriaExportDialog.class);

    private JTextField _caseNumber;
    private JTable _propertiesTable;
    private MessageBus _messageBus;
    private JCheckBox _exportPopulationStatisticsCheckbox;
    private JCheckBox _automaticDropoutEstimationCheckbox;
    private JTextField _userName;
    private JCheckBox _alwaysUseThisNameCheckbox;

    /**
     * Launch the application.
     */
    public static void main(final String[] args) {
        try {
            final SearchCriteriaExportDialog dialog = new SearchCriteriaExportDialog();
            dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
            dialog.setVisible(true);
        }
        catch (final Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Create the dialog.
     */
    public SearchCriteriaExportDialog() {
        _messageBus = MessageBus.getInstance();

        final ArrayList<Image> icons = new ArrayList<>();
        icons.add(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/images/16x16/qubodup-bloodSplash.png")));
        icons.add(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/images/32x32/qubodup-bloodSplash.png")));
        icons.add(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/images/64x64/qubodup-bloodSplash.png")));
        icons.add(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/images/128x128/qubodup-bloodSplash.png")));
        setIconImages(icons);

        setModalityType(ModalityType.APPLICATION_MODAL);
        setTitle("Export search criteria");
        setBounds(100, 100, 561, 430);
        getContentPane().setLayout(new BorderLayout());
        final JPanel contentPanel = new JPanel();
        contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
        getContentPane().add(contentPanel, BorderLayout.CENTER);
        contentPanel.setLayout(new MigLayout("", "[][grow][66.00][44.00]", "[][][][][][][][][grow]"));
        contentPanel.add(new JLabel("Case number"), "cell 0 1,alignx trailing");
        _caseNumber = new JTextField();
        contentPanel.add(_caseNumber, "cell 1 1,growx");
        _caseNumber.setColumns(10);

        final JLabel lblUsername = new JLabel("Username");
        contentPanel.add(lblUsername, "cell 0 2,alignx trailing");

        _userName = new JTextField(SmartRankGUISettings.getExportUserName().isEmpty() ? System.getProperty("user.name") : SmartRankGUISettings.getExportUserName());
        contentPanel.add(_userName, "cell 1 2,growx");
        _userName.setColumns(10);

        _alwaysUseThisNameCheckbox = new JCheckBox("Always use this name");
        contentPanel.add(_alwaysUseThisNameCheckbox, "cell 2 2 2 1");
        _exportPopulationStatisticsCheckbox = new JCheckBox("Export population statistics", SmartRankGUISettings.isExportPopulationStatistics());
        contentPanel.add(_exportPopulationStatisticsCheckbox, "cell 1 3");
        _automaticDropoutEstimationCheckbox = new JCheckBox("Automatic dropout estimation", SmartRankGUISettings.isAutomaticDropoutEstimation());
        contentPanel.add(_automaticDropoutEstimationCheckbox, "cell 1 4");
        contentPanel.add(new JLabel("Additional properties"), "cell 0 5 1 4,alignx trailing,aligny top");
        final JScrollPane scrollPane = new JScrollPane();
        contentPanel.add(scrollPane, "cell 1 5 2 4,grow");
        _propertiesTable = new JTable();
        _propertiesTable.setModel(new DefaultTableModel(
                                                        new Object[][]{
                                                        },
                                                        new String[]{
                                                            "Name", "Value"
                                                        }) {
            @Override
            public Class getColumnClass(final int columnIndex) {
                return String.class;
            }
        });
        scrollPane.setViewportView(_propertiesTable);

        final JButton btnAdd = new JButton("Add");
        btnAdd.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                addProperty();
            }
        });
        contentPanel.add(btnAdd, "cell 3 5,growx");
        final JButton btnRemove = new JButton("Remove");
        btnRemove.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                removeProperty();
            }
        });
        contentPanel.add(btnRemove, "cell 3 7,growx");
        final JPanel buttonPane = new JPanel();
        buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));

        getContentPane().add(buttonPane, BorderLayout.SOUTH);
        final JButton okButton = new JButton("OK");
        okButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                if (onOk()) {
                    dispose();
                }
            }
        });
        okButton.setActionCommand("OK");
        buttonPane.add(okButton);
        getRootPane().setDefaultButton(okButton);
        final JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                dispose();
            }
        });
        cancelButton.setActionCommand("Cancel");
        buttonPane.add(cancelButton);
    }

    protected void removeProperty() {
        ((DefaultTableModel) _propertiesTable.getModel()).removeRow(_propertiesTable.convertRowIndexToModel(_propertiesTable.getSelectedRow()));
    }

    protected void addProperty() {
        ((DefaultTableModel) _propertiesTable.getModel()).addRow(new Object[]{"", ""});
        _propertiesTable.grabFocus();
    }

    protected boolean onOk() {
        final AnalysisParameters parameters = _messageBus.query(AnalysisParametersMessage.class);
        final DefenseHypothesis defenseHypothesis = _messageBus.query(DefenseHypothesisMessage.class);
        final ProsecutionHypothesis prosecutionHypothesis = _messageBus.query(ProsecutionHypothesisMessage.class);
        final String caseNumber = _caseNumber.getText();
        final boolean exportStatistics = _exportPopulationStatisticsCheckbox.isSelected();
        final boolean automaticDropoutEstimation = _automaticDropoutEstimationCheckbox.isSelected();
        final HashMap<String, String> properties = getProperties();

        final String fileName = selectFilename();
        if (fileName.isEmpty()) {
            return false;
        }
        try {
            new SearchCriteriaWriter(_userName.getText(), parameters, defenseHypothesis, prosecutionHypothesis, caseNumber, exportStatistics, automaticDropoutEstimation, properties)
                .write(new File(fileName));
        }
        catch (final Throwable t) {
            LOG.error("Error showing report!", t);
            _messageBus.send(this, new ErrorStringMessage("Error saving search criteria:\n" + t.getMessage()));
            return false;
        }

        // Persist settings
        SmartRankGUISettings.setExportPopulationStatistics(exportStatistics);
        SmartRankGUISettings.setExportAutomaticDropoutEstimation(automaticDropoutEstimation);
        SmartRankGUISettings.setLastSelectedSearchCriteriaExportPath(new File(fileName).getParent());
        if (_alwaysUseThisNameCheckbox.isSelected()) {
            SmartRankGUISettings.setExportUserName(_userName.getText());
        }
        return true;
    }

    private HashMap<String, String> getProperties() {
        final HashMap<String, String> properties = new HashMap<>();

        for (int row = 0; row < _propertiesTable.getRowCount(); row++) {
            final String name = (String) _propertiesTable.getValueAt(_propertiesTable.convertRowIndexToView(row), _propertiesTable.convertColumnIndexToView(0));
            final String value = (String) _propertiesTable.getValueAt(_propertiesTable.convertRowIndexToView(row), _propertiesTable.convertColumnIndexToView(1));
            properties.put(name, value);
        }
        return properties;
    }

    private String selectFilename() {
        final JFileChooser chooser = new JFileChooser(SmartRankGUISettings.getLastSelectedSearchCriteriaExportPath());
        chooser.setDialogTitle("Please select the location and name of the file to be exported");
        chooser.addChoosableFileFilter(new FileFilter() {
            @Override
            public boolean accept(final File f) {
                return f.getName().toLowerCase().endsWith(".json") || f.getName().toLowerCase().endsWith(".xml");
            }

            @Override
            public String getDescription() {
                return "All supported filetypes (*.json;*.xml)";
            }
        });
        chooser.addChoosableFileFilter(new FileFilter() {
            @Override
            public boolean accept(final File f) {
                return f.getName().toLowerCase().endsWith(".json");
            }

            @Override
            public String getDescription() {
                return "JSON files (*.json)";
            }
        });
        chooser.addChoosableFileFilter(new FileFilter() {
            @Override
            public boolean accept(final File f) {
                return f.getName().toLowerCase().endsWith(".xml");
            }

            @Override
            public String getDescription() {
                return "XML files (*.xml)";
            }
        });
        chooser.setAcceptAllFileFilterUsed(true);

        final AnalysisParameters parameters = _messageBus.query(AnalysisParametersMessage.class);
        final Sample[] samples = parameters.getEnabledCrimesceneProfiles().toArray(new Sample[0]);
        if (samples.length == 0) {
            return "";
        }
        final Sample sample = samples[0];

        chooser.setSelectedFile(new File(FileNameSanitizer.sanitize(_caseNumber.getText()) + "-" + FileNameSanitizer.sanitize(sample.getName()) + "-" + new SimpleDateFormat("yyyyMMdd-hhmmss").format(new Date())));
        if (JFileChooser.APPROVE_OPTION == chooser.showSaveDialog(this)) {
            String absolutePath = chooser.getSelectedFile().getAbsolutePath();

            final String description = chooser.getFileFilter().getDescription();
            if ((description.contains("json") || description.contains("*.*")) && !absolutePath.toLowerCase().endsWith("json")) {
                absolutePath = absolutePath + ".json";
            }
            else {
                if (description.contains("xml") && !absolutePath.toLowerCase().endsWith("xml")) {
                    absolutePath = absolutePath + ".xml";
                }
            }
            return absolutePath;
        }
        return "";
    }
}
