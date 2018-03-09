package nl.minvenj.nfi.smartrank.gui.utils;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.KeyStroke;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.text.JTextComponent;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoManager;

public class UndoDecorator {

    public static class UndoListener implements UndoableEditListener {
        private final UndoManager _undo;

        public UndoListener(final UndoManager undo) {
            _undo = undo;
        }

        @Override
        public void undoableEditHappened(final UndoableEditEvent e) {
            _undo.addEdit(e.getEdit());
        }
    }

    public static class UndoAction extends AbstractAction {
        private final UndoManager _undo;

        public UndoAction(final UndoManager undo) {
            super("Undo");
            _undo = undo;
        }

        @Override
        public void actionPerformed(final ActionEvent e) {
            try {
                if (_undo.canUndo()) {
                    _undo.undo();
                }
            }
            catch (final CannotUndoException cue) {
            }
        }
    }

    public static class RedoAction extends AbstractAction {
        private final UndoManager _undo;

        public RedoAction(final UndoManager undo) {
            super("Redo");
            _undo = undo;
        }

        @Override
        public void actionPerformed(final ActionEvent e) {
            try {
                if (_undo.canRedo()) {
                    _undo.redo();
                }
            }
            catch (final CannotRedoException cue) {
            }
        }
    }

    public static void register(final JTextComponent comp) {
        final UndoManager undo = new UndoManager();
        final UndoListener undoListener = new UndoListener(undo);
        comp.getDocument().addUndoableEditListener(undoListener);
        comp.getActionMap().put("Undo", new UndoAction(undo));
        comp.getActionMap().put("Redo", new RedoAction(undo));
        comp.getInputMap().put(KeyStroke.getKeyStroke("control Z"), "Undo");
        comp.getInputMap().put(KeyStroke.getKeyStroke("control Y"), "Redo");
    }

}
