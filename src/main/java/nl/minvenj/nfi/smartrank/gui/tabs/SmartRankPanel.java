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
package nl.minvenj.nfi.smartrank.gui.tabs;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDropEvent;
import java.io.File;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nl.minvenj.nfi.smartrank.messages.status.ApplicationStatusMessage;
import nl.minvenj.nfi.smartrank.messages.status.ErrorStringMessage;
import nl.minvenj.nfi.smartrank.raven.messages.MessageBus;
import nl.minvenj.nfi.smartrank.raven.messages.RavenMessage;

public abstract class SmartRankPanel extends javax.swing.JPanel {

    private static final Logger LOG = LoggerFactory.getLogger(SmartRankPanel.class);
    protected static final int SINGLE_FILE = 1;

    /**
     * Creates new form AbstractSmartRankPanel
     */
    public SmartRankPanel() {
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables

    protected void registerAsListener() {
        MessageBus.getInstance().registerSubscriber(this);
    }

    protected void acceptDroppedFiles(final Class<? extends RavenMessage<List<File>>> messageClass) {
        acceptDroppedFiles(messageClass, 0);
    }

    protected void acceptSingleDroppedFile(final Class<? extends RavenMessage<List<File>>> messageClass) {
        acceptDroppedFiles(messageClass, SINGLE_FILE);
    }

    private void acceptDroppedFiles(final Class<? extends RavenMessage<List<File>>> messageClass, final int modifier) {
        setDropTarget(new DropTarget() {
            @Override
            public synchronized void drop(final DropTargetDropEvent dtde) {
                try {
                    if (!MessageBus.getInstance().query(ApplicationStatusMessage.class).isActive()) {
                        final Transferable transfer = dtde.getTransferable();
                        if (transfer.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                            dtde.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
                            final List<?> objects = (List<?>) transfer.getTransferData(DataFlavor.javaFileListFlavor);

                            if (modifier == SINGLE_FILE && objects.size() != 1) {
                                MessageBus.getInstance().send(this, new ErrorStringMessage("Please only drop a single file"));
                            }
                            else {
                                final ArrayList<File> files = new ArrayList<>();
                                for (final Object object : objects) {
                                    if (object instanceof File) {
                                        files.add((File) object);
                                    }
                                }
                                if (!files.isEmpty()) {
                                    try {
                                        final Constructor<? extends RavenMessage<List<File>>> constructor = messageClass.getConstructor(List.class);
                                        final RavenMessage<List<File>> message = constructor.newInstance(files);
                                        MessageBus.getInstance().send(this, message);
                                    }
                                    catch (final Throwable t) {
                                        LOG.error("Droptarget Error: message class {} has no constructor for a list of files!", messageClass, t);
                                    }
                                }
                            }
                        }
                    }
                }
                catch (final Exception ex) {
                    MessageBus.getInstance().send(this, new ErrorStringMessage(ex.getLocalizedMessage()));
                }
                finally {
                    dtde.dropComplete(true);
                }
            }
        });

    }
}
