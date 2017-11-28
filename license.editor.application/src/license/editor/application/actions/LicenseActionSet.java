package license.editor.application.actions;

import java.util.Collection;

import org.eclipse.jface.action.IAction;

import platform.model.license.LicenseDescriptors;
import platform.model.license.LicenseRoot;
import platform.ui.actions.IActionSet;
import platform.ui.actions.edit.CopyRelationAction;
import platform.ui.actions.edit.CreateRelationAction;
import platform.ui.actions.edit.PasteRelationAction;
import platform.ui.actions.edit.RemoveRelationAction;
import platform.utils.collections.CollectionsUtils;

public class LicenseActionSet
        implements IActionSet {
    
    private final LicenseRoot root;
    
    public LicenseActionSet(final LicenseRoot root) {
        this.root = root;
    }
    
    @Override
    public Collection<IAction> getActions() {
        return CollectionsUtils.asList(
                new CreateRelationAction(this.root, LicenseDescriptors.LICENSE, null),
                new CreateMultiRelationAction(this.root, LicenseDescriptors.LICENSE, null),
                new CopyRelationAction(this.root, LicenseDescriptors.LICENSE.getId()),
                new PasteRelationAction(this.root, LicenseDescriptors.LICENSE.getId()),
                new RemoveRelationAction(this.root));
    }
    
    @Override
    public IAction getDefaultAction() {
        return null;
    }
    
}
