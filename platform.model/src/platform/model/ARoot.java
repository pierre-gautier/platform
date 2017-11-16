package platform.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public abstract class ARoot
        extends ANode
        implements IRoot {
    
    private final List<IRelation>          selection;
    
    private List<IStrategy>                strategies;
    private Collection<ISelectionListener> listeners;
    
    protected ARoot(final Descriptor<? extends IRoot> type, final String id, final Collection<Attribute> attributes, final List<IStrategy> strategies) {
        super(type, id, attributes, null);
        this.selection = new ArrayList<>();
        this.strategies = strategies == null ? null : new ArrayList<>(strategies);
    }
    
    @Override
    public void addStrategy(final IStrategy strategy) {
        if (strategy != null) {
            if (this.strategies == null) {
                this.strategies = new ArrayList<>();
            }
            this.strategies.add(strategy);
        }
    }
    
    @Override
    public final IRoot getRoot() {
        return this;
    }
    
    @Override
    public final IRelation getSelection() {
        if (this.selection.isEmpty()) {
            return null;
        }
        return this.selection.get(0);
    }
    
    @Override
    public final List<IRelation> getSelections() {
        final List<IRelation> result = new ArrayList<>(this.selection);
        for (final IStrategy strategy : this.getStrategies()) {
            strategy.getSelection(result);
        }
        return result;
    }
    
    @Override
    public final List<IStrategy> getStrategies() {
        if (this.strategies == null) {
            return Collections.emptyList();
        }
        return Collections.unmodifiableList(this.strategies);
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public final Descriptor<? extends IRoot> getType() {
        return (Descriptor<? extends IRoot>) super.getType();
    }
    
    @Override
    public final void registerSelectionListener(final ISelectionListener listener) {
        if (listener == null) {
            return;
        }
        if (this.listeners == null) {
            this.listeners = new CopyOnWriteArrayList<>();
        }
        this.listeners.add(listener);
    }
    
    @Override
    public final void setSelection(final Object source, final Collection<IRelation> selection) {
        if (selection != null && this.selection.size() == selection.size() && this.selection.containsAll(selection)) {
            return;
        }
        this.selection.clear();
        if (selection != null) {
            for (final IStrategy strategy : this.getStrategies()) {
                strategy.setSelection(selection);
            }
            this.selection.addAll(selection);
        }
        this.sendEvent(source);
    }
    
    @Override
    public final void unregisterSelectionListener(final ISelectionListener listener) {
        if (this.listeners == null || listener == null) {
            return;
        }
        this.listeners.remove(listener);
        if (this.listeners.isEmpty()) {
            this.listeners = null;
        }
    }
    
    private void sendEvent(final Object source) {
        if (this.listeners != null) {
            for (final ISelectionListener listener : this.listeners) {
                listener.selectionChanged(source, this);
            }
        }
    }
    
}
