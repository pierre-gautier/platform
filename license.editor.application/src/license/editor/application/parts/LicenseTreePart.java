package license.editor.application.parts;

import java.util.Arrays;

import javax.inject.Inject;

import org.eclipse.e4.ui.di.Focus;
import org.eclipse.e4.ui.di.Persist;
import org.eclipse.swt.widgets.Composite;

import license.editor.application.actions.LicenseActionSet;
import platform.model.commons.Descriptors;
import platform.model.license.LicenseRoot;
import platform.ui.actions.IActionSet;
import platform.ui.actions.manager.ActionSetMenuManager;
import platform.ui.controls.filter.NodeFilterText;
import platform.ui.controls.tree.ModelTreeViewer;
import platform.ui.controls.tree.ModelTreeViewerFilter;
import platform.ui.swt.SWTUtils;

public class LicenseTreePart {
    
    private final ModelTreeViewer viewer;
    
    @Inject
    public LicenseTreePart(final Composite parent, final LicenseRoot root) {
        
        parent.setLayout(SWTUtils.createGridLayout(1, true, 0, 0));
        
        final ModelTreeViewerFilter filter = new ModelTreeViewerFilter(Arrays.asList(Descriptors.LABEL), null, null);
        
        new NodeFilterText(parent, false, filter);
        
        this.viewer = new ModelTreeViewer(parent, root, filter, null);
        
        final IActionSet actionSet = new LicenseActionSet(root);
        
        this.viewer.setActionSetDescriptor(actionSet);
        
        new ActionSetMenuManager(actionSet, this.viewer.getControl());
        
    }
    
    @Persist
    public void save() {
        System.out.println("LicenseTreePart.save()"); //$NON-NLS-1$
    }
    
    @Focus
    public void setFocus() {
        this.viewer.getControl().setFocus();
    }
}