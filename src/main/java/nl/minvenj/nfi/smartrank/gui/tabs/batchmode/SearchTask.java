package nl.minvenj.nfi.smartrank.gui.tabs.batchmode;

import java.awt.EventQueue;

/**
 * A task that starts a new search on the EDT.
 */
final class SearchTask implements Runnable {
    /**
     * 
     */
    private final BatchModePanel _batchModePanel;

    /**
     * @param batchModePanel
     */
    SearchTask(BatchModePanel batchModePanel) {
        _batchModePanel = batchModePanel;
    }

    @Override
    public void run() {
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                SearchTask.this._batchModePanel.startSearch();
            }
        });
    }
}