package license.editor.application.parts;

import java.util.Collection;

import javax.annotation.PreDestroy;
import javax.inject.Inject;

import org.eclipse.e4.ui.di.Focus;
import org.eclipse.swt.widgets.Composite;

import platform.model.Attribute;
import platform.model.IAttributeListener;
import platform.model.IObject;
import platform.model.IRelation;
import platform.model.IRoot;
import platform.model.ISelectionListener;
import platform.model.license.LicenseRoot;
import platform.ui.workbench.properties.PropertySheetEntry;
import platform.ui.workbench.properties.PropertySheetViewer;
import platform.ui.workbench.properties.model.RelationPropertySource;

public class LicensePropertiesPart
        implements ISelectionListener,
        IAttributeListener {
    
    private final PropertySheetViewer viewer;
    private final IRoot               root;
    private IRelation                 input;
    
    @Inject
    public LicensePropertiesPart(final Composite parent, final LicenseRoot root) {
        this.root = root;
        this.viewer = new PropertySheetViewer(parent);
        this.viewer.setRootEntry(new PropertySheetEntry());
        this.selectionChanged(this, root);
    }
    
    @Override
    public void attributesChanged(final IObject object, final Collection<Attribute> attributes) {
        // this.viewer.setInput(this.input == null ? null : new Object[] { new RelationPropertySource(this.input) });
    }
    
    @Focus
    public void focus() {
        if (this.viewer != null && !this.viewer.getControl().isDisposed()) {
            this.viewer.getControl().setFocus();
        }
    }
    
    @PreDestroy
    public void preDestroy() {
        if (this.input != null) {
            this.input.unregisterAttributeListener(this);
            this.input.getTarget().unregisterAttributeListener(this);
        }
        this.root.unregisterSelectionListener(this);
    }
    
    @Override
    public void selectionChanged(final Object source, final IRoot modifiedRoot) {
        this.preDestroy();
        this.root.registerSelectionListener(this);
        this.input = this.root.getSelection();
        if (this.input != null && this.root.getSelections().size() == 1) {
            this.input.registerAttributeListener(this);
            this.input.getTarget().registerAttributeListener(this);
            this.viewer.setInput(new Object[] { new RelationPropertySource(this.input) });
        } else {
            this.viewer.setInput(new Object[] {});
        }
    }
}