package platform.ui.actions;

import java.util.Collection;

import org.eclipse.jface.action.IAction;

public interface IActionSet {
    
    Collection<IAction> getActions();
    
    IAction getDefaultAction();
    
}
