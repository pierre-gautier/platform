package platform.ui.controls;

import java.util.Set;

import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import platform.model.Descriptor;
import platform.model.INode;
import platform.model.IRelation;
import platform.model.IRoot;
import platform.model.ISelectionListener;

public abstract class ANodeControl<C extends Control>
        implements INodeControl<C>,
        ISelectionListener,
        DisposeListener {
    
    private final Set<Descriptor<IRelation>> relationTypes;
    private final C                          control;
    private final INode                      input;
    
    protected ANodeControl(final Composite parent, final INode node, final Set<Descriptor<IRelation>> relationType) {
        this.relationTypes = relationType;
        this.control = this.createControl(parent);
        this.input = node;
        if (this.input != null) {
            this.control.addDisposeListener(this);
            this.input.getRoot().registerSelectionListener(this);
        }
    }
    
    @Override
    public final C getControl() {
        return this.control;
    }
    
    @Override
    public final INode getInput() {
        return this.input;
    }
    
    @Override
    public void selectionChanged(final Object source, final IRoot repository) {
        // do nothing
    }
    
    @Override
    public void widgetDisposed(final DisposeEvent e) {
        if (this.input != null) {
            this.input.getRoot().unregisterSelectionListener(this);
        }
    }
    
    protected abstract C createControl(final Composite parent);
    
    protected Set<Descriptor<IRelation>> getRelationTypes() {
        return this.relationTypes;
    }
    
    protected abstract void initControl();
    
}
