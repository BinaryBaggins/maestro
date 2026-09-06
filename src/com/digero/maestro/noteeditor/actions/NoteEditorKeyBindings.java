package com.digero.maestro.noteeditor.actions;

import com.digero.maestro.noteeditor.components.NoteGridPanel;
import java.awt.GraphicsEnvironment;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.KeyStroke;

public final class NoteEditorKeyBindings {

    private NoteEditorKeyBindings() {}

    private static final int shortcutMask = getShortcutMask();

    private static int getShortcutMask() {
        if (GraphicsEnvironment.isHeadless()) {
            return InputEvent.CTRL_DOWN_MASK;
        }

        return Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx();
    }

    public static void install(NoteGridPanel noteGridPanel) {
        bind(
            noteGridPanel,
            KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0),
            "deleteSelectedNotes",
            noteGridPanel::deleteSelectedNotes
        );

        bind(noteGridPanel, KeyStroke.getKeyStroke(KeyEvent.VK_Z, shortcutMask), "undo", noteGridPanel::undo);

        bind(noteGridPanel, KeyStroke.getKeyStroke(KeyEvent.VK_Y, shortcutMask), "redo", noteGridPanel::redo);
    }

    private static void bind(JComponent component, KeyStroke keyStroke, String actionName, Runnable action) {
        component.getInputMap(JComponent.WHEN_FOCUSED).put(keyStroke, actionName);

        component.getActionMap().put(
            actionName,
            new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    action.run();
                }
            }
        );
    }
}
