package platform.ui.actions.edit;

import org.eclipse.swt.SWT;

import platform.model.IRoot;
import platform.ui.actions.APlatformAction;
import platform.ui.actions.IUndoHandler;

public class CopyRelationAction
        extends APlatformAction {
    
    private final IRoot  root;
    private final String context;
    
    public CopyRelationAction(final IRoot root, final String context) {
        super("Copy"); //$NON-NLS-1$
        this.root = root;
        this.context = context;
    }
    
    @Override
    public int getAccelerator() {
        return SWT.MOD1 + 'C';
    }
    
    @Override
    public boolean isEnabled() {
        return this.root.getSelection() != null;
    }
    
    @Override
    protected IUndoHandler internalRun() {
        Clipboard.getClipboard(this.context).copy(this.root.getSelections());
        return null;
    }
    
}
