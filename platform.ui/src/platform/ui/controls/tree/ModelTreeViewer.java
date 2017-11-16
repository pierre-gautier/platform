package platform.ui.controls.tree;

import java.util.Set;

import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Tree;

import platform.model.Descriptor;
import platform.model.INode;
import platform.model.IRelation;
import platform.ui.actions.IActionSet;
import platform.ui.controls.ANodeControl;
import platform.ui.controls.tree.Overlay.Position;
import platform.ui.swt.ActionSetListener;

public class ModelTreeViewer
        extends ANodeControl<Tree>
        implements Listener {
    
    private final ModelTreeViewerFilter filter;
    private TreeViewer                  viewer;
    
    public ModelTreeViewer(final Composite parent, final INode node) {
        this(parent, node, null, null);
    }
    
    public ModelTreeViewer(final Composite parent, final INode node, final ModelTreeViewerFilter filter, final Set<Descriptor<IRelation>> relationType) {
        super(parent, node, relationType);
        this.filter = filter;
        this.initControl();
    }
    
    public void addImageOverlay(final Descriptor<Boolean> pd, final Position position, final String imagePath) {
        this.getContentProvider().registerImageOverlay(pd, position, imagePath);
    }
    
    public void addRefreshProperty(final Descriptor<?> descriptor) {
        this.getContentProvider().addLabelDescriptor(descriptor);
    }
    
    @Override
    public void handleEvent(final Event e) {
        if (e.type == SWT.MouseUp) {
            final boolean itemNotFound = this.getControl().getItem(new Point(e.x, e.y)) == null;
            final boolean selectionNotEmpty = this.getInput().getRoot().getSelection() != null;
            if (itemNotFound && selectionNotEmpty) {
                this.viewer.setSelection(null);
                this.getInput().getRoot().setSelection(this.viewer, null);
            }
        } else if (e.type == SWT.KeyUp) {
            if (e.stateMask == SWT.MOD1 && e.keyCode == SWT.KEYPAD_ADD
                    || e.stateMask == (SWT.MOD3 | SWT.MOD2) && e.keyCode == 'p') {
                this.viewer.expandAll();
            } else if (e.stateMask == SWT.MOD1 && e.keyCode == SWT.KEYPAD_SUBTRACT
                    || e.stateMask == (SWT.MOD3 | SWT.MOD2) && e.keyCode == 'm') {
                this.viewer.collapseAll();
            }
        }
    }
    
    public final void setActionSetDescriptor(final IActionSet actionSet) {
        if (actionSet != null) {
            final ActionSetListener actionListener = new ActionSetListener(actionSet);
            this.getControl().addMouseListener(actionListener);
            this.getControl().addKeyListener(actionListener);
        }
    }
    
    @Override
    protected Tree createControl(final Composite parent) {
        final Tree tree = new Tree(parent, SWT.MULTI);
        tree.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        tree.addListener(SWT.MouseUp, this);
        tree.addListener(SWT.KeyUp, this);
        return tree;
    }
    
    @Override
    protected void initControl() {
        this.viewer = new TreeViewer(this.getControl());
        this.viewer.setUseHashlookup(true);
        final ModelTreeContentProvider provider = new ModelTreeContentProvider(this.viewer, this.getInput().getRoot(), this.getRelationTypes());
        this.viewer.addSelectionChangedListener(provider);
        this.viewer.setContentProvider(provider);
        this.viewer.setLabelProvider(provider);
        this.viewer.setInput(this.getInput());
        if (this.filter != null) {
            this.viewer.setExpandPreCheckFilters(true);
            this.viewer.setFilters(new ViewerFilter[] { this.filter });
            this.filter.setViewer(this.viewer);
        }
        provider.selectionChanged(this, null);
    }
    
    private ModelTreeContentProvider getContentProvider() {
        return (ModelTreeContentProvider) this.viewer.getContentProvider();
    }
}
