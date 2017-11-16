package platform.ui.actions.edit;

import java.util.Arrays;
import java.util.Collection;

import org.eclipse.swt.SWT;

import platform.model.IRelation;
import platform.model.IRoot;
import platform.ui.actions.APlatformAction;
import platform.ui.actions.IUndoHandler;

public class RemoveRelationAction
        extends APlatformAction {
    
    private final IRoot root;
    
    public RemoveRelationAction(final IRoot root) {
        super("Remove"); //$NON-NLS-1$
        this.root = root;
    }
    
    @Override
    public int getAccelerator() {
        return SWT.DEL;
    }
    
    @Override
    public boolean isEnabled() {
        return !this.root.getSelections().isEmpty();
    }
    
    @Override
    protected IUndoHandler internalRun() {
        final Collection<IRelation> relations = this.root.getSelections();
        for (final IRelation relation : relations) {
            relation.getSource().removeRelations(Arrays.asList(relation));
        }
        if (!relations.isEmpty()) {
            return new RemoveRelationUndoHandler(relations);
        }
        return null;
    }
    
}
