package nl.minvenj.nfi.smartrank.gui.tabs.database;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileOutputStream;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JSplitPane;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.miginfocom.swing.MigLayout;
import nl.minvenj.nfi.smartrank.domain.DNADatabase;
import nl.minvenj.nfi.smartrank.messages.data.DatabaseMessage;
import nl.minvenj.nfi.smartrank.messages.status.ErrorStringMessage;
import nl.minvenj.nfi.smartrank.raven.messages.MessageBus;

public class DBStatisticsPanel extends JPanel {

    private static final Logger LOG = LoggerFactory.getLogger(DBStatisticsPanel.class);

    private final NumberOfLociPerSpecimenPane _noflociPane;
    private final NumberOfSpecimensPerLocusPanel _nofSpecimensPanel;

    public DBStatisticsPanel() {
        setLayout(new MigLayout("", "[631px,grow]", "[grow][]"));

        _noflociPane = new NumberOfLociPerSpecimenPane();
        _nofSpecimensPanel = new NumberOfSpecimensPerLocusPanel();

        final JSplitPane statsSplitPane = new JSplitPane();
        statsSplitPane.setResizeWeight(0.5);
        statsSplitPane.setOrientation(JSplitPane.HORIZONTAL_SPLIT);
        statsSplitPane.setLeftComponent(_noflociPane);
        statsSplitPane.setRightComponent(_nofSpecimensPanel);
        add(statsSplitPane, "cell 0 0,aligny top,grow");
        final JPanel buttonPanel = new JPanel();
        add(buttonPanel, "cell 0 1,alignx right,growy");
        buttonPanel.setLayout(new MigLayout("", "[631px,grow]", "[]"));

        final JButton saveButton = new JButton("Save...");
        buttonPanel.add(saveButton, "cell 0 0, alignx right");
        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                final JFileChooser chooser = new JFileChooser();
                final DNADatabase database = MessageBus.getInstance().query(DatabaseMessage.class);
                if (database != null) {
                    final String suggestedBaseName = getSuggestedBaseName(database);
                    chooser.setSelectedFile(new File(suggestedBaseName));
                    if (JFileChooser.APPROVE_OPTION == chooser.showSaveDialog(getTopLevelAncestor())) {
                        final File outFile = chooser.getSelectedFile();
                        try (FileOutputStream fos = new FileOutputStream(outFile)) {
                            fos.write("Database statistics\n".getBytes());
                            fos.write("===================\n\n".getBytes());
                            fos.write(String.format("Connect string: %s\n", database.getConnectString()).getBytes());
                            fos.write(String.format("Number of records: %d\n", database.getRecordCount()).getBytes());
                            fos.write("\n".getBytes());
                            _noflociPane.save(outFile.getAbsolutePath(), fos);
                            _nofSpecimensPanel.save(outFile.getAbsolutePath(), fos);
                        }
                        catch (final Throwable t) {
                            LOG.info("Error saving db statistics!", t);
                            MessageBus.getInstance().send(this, new ErrorStringMessage("Error saving Database statistics!\n" + t.getLocalizedMessage()));
                        }
                    }
                }
            }
        });
    }

    private String getSuggestedBaseName(final DNADatabase database) {
        return database.getConnectString() + "-statistics.txt";
    }

    public void setDatabase(final DNADatabase db) {
        _noflociPane.setDatabase(db);
        _nofSpecimensPanel.setDatabase(db);
    }

    public void clear() {
        _noflociPane.clear();
        _nofSpecimensPanel.clear();
    }
}
