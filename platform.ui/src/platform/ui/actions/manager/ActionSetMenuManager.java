package platform.ui.actions.manager;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuListener2;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.swt.widgets.Control;

import platform.ui.actions.IActionSet;

public final class ActionSetMenuManager
        extends MenuManager
        implements IMenuListener2 {
    
    private final IActionSet actionSet;
    private final Control    control;
    
    public ActionSetMenuManager(final IActionSet actionSet, final Control control) {
        super();
        this.control = control;
        this.actionSet = actionSet;
        this.setRemoveAllWhenShown(true);
        this.addMenuListener(this);
        this.control.setMenu(this.createContextMenu(control));
    }
    
    @Override
    public void menuAboutToHide(final IMenuManager manager) {
        // do nothing
    }
    
    @Override
    public void menuAboutToShow(final IMenuManager manager) {
        for (final IAction action : this.actionSet.getActions()) {
            manager.add(action);
        }
    }
    
}
