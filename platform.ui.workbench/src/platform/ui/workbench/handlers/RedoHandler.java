package platform.ui.workbench.handlers;

import org.eclipse.e4.core.di.annotations.CanExecute;
import org.eclipse.e4.core.di.annotations.Execute;

import platform.ui.actions.ActionHistory;

public class RedoHandler {
    
    @CanExecute
    public boolean canExecute() {
        return ActionHistory.canUndo();
    }
    
    @Execute
    public void execute() {
        ActionHistory.undo();
    }
    
}
