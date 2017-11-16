package platform.ui.actions;

import java.util.Deque;
import java.util.concurrent.ConcurrentLinkedDeque;

public class ActionHistory {
    
    private static final int                 ACTION_UNDO_QUEUE_SIZE = 10;
    
    private static final Deque<IUndoHandler> UNDO_STACK             = new ConcurrentLinkedDeque<>();
    private static final Deque<IUndoHandler> REDO_STACK             = new ConcurrentLinkedDeque<>();
    
    public static boolean canRedo() {
        return !ActionHistory.REDO_STACK.isEmpty();
    }
    
    public static boolean canUndo() {
        return !ActionHistory.UNDO_STACK.isEmpty();
    }
    
    public static void historize(final IUndoHandler undoHandler) {
        ActionHistory.historize(undoHandler, ActionHistory.UNDO_STACK);
    }
    
    public static final void redo() {
        final IUndoHandler lastAction = ActionHistory.REDO_STACK.pollLast();
        if (lastAction != null) {
            final IUndoHandler redoHandler = lastAction.undo();
            if (redoHandler != null) {
                ActionHistory.historize(redoHandler, ActionHistory.UNDO_STACK);
            }
        }
    }
    
    public static final void undo() {
        final IUndoHandler lastAction = ActionHistory.UNDO_STACK.pollLast();
        if (lastAction != null) {
            final IUndoHandler redoHandler = lastAction.undo();
            if (redoHandler != null) {
                ActionHistory.historize(redoHandler, ActionHistory.REDO_STACK);
            }
        }
    }
    
    private static void historize(final IUndoHandler undoHandler, final Deque<IUndoHandler> stack) {
        if (undoHandler == null) {
            return;
        }
        stack.addLast(undoHandler);
        while (stack.size() > ActionHistory.ACTION_UNDO_QUEUE_SIZE) {
            stack.removeFirst();
        }
    }
    
}
