package platform.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

import org.eclipse.core.runtime.Assert;

import platform.utils.collections.CollectionsUtils;

@SuppressWarnings("nls")
public abstract class ANode
        extends AObject
        implements INode {
    
    private Map<Descriptor<? extends IRelation>, List<IRelation>> relations;
    private Collection<IRelationListener>                         listeners;
    
    protected ANode(final Descriptor<? extends INode> type, final String id, final Collection<Attribute> attributes, final IRoot root) {
        super(type, id, attributes, root);
    }
    
    @Override
    public final void addRelations(final Collection<IRelation> add) {
        if (CollectionsUtils.isNullOrEmpty(add)) {
            return;
        }
        for (final IRelation relation : add) {
            Assert.isTrue(relation != null && relation.getSource() == this, "relation must be not null and source must be this");
            final List<IRelation> typedRelations = this.relations == null ? null : this.relations.get(relation.getType());
            Assert.isTrue(typedRelations == null || !typedRelations.contains(relation), "already contains relation");
        }
        if (this.relations == null) {
            this.relations = new HashMap<>();
        }
        for (final IStrategy strategy : this.getRoot().getStrategies()) {
            strategy.addRelations(this, add);
        }
        final Collection<IRelation> addedRelations = new ArrayList<>(add.size());
        for (final IRelation relation : add) {
            List<IRelation> typedRelations = this.relations.get(relation.getType());
            if (typedRelations == null) {
                typedRelations = new ArrayList<>(1);
                this.relations.put(relation.getType(), typedRelations);
            }
            typedRelations.add(relation);
            addedRelations.add(relation);
        }
        if (addedRelations.isEmpty()) {
            return;
        }
        for (final IStrategy strategy : this.getRoot().getStrategies()) {
            strategy.relationsAdded(this, addedRelations);
        }
        if (this.listeners != null) {
            for (final IRelationListener listener : this.listeners) {
                listener.relationsAdded(this, addedRelations);
            }
        }
    }
    
    @Override
    public List<IRelation> getRelations() {
        if (this.relations == null) {
            return Collections.emptyList();
        }
        final List<IRelation> allRelations = new ArrayList<>(0);
        for (final Collection<IRelation> typedRelations : this.relations.values()) {
            if (typedRelations != null) {
                allRelations.addAll(typedRelations);
            }
        }
        return allRelations;
    }
    
    @Override
    public final List<IRelation> getRelations(final Set<Descriptor<IRelation>> descriptors) {
        if (descriptors == null) {
            return this.getRelations();
        }
        if (this.relations == null || descriptors.isEmpty()) {
            return Collections.emptyList();
        }
        final List<IRelation> allRelations = new ArrayList<>(0);
        for (final Descriptor<? extends IRelation> descriptor : descriptors) {
            final Collection<IRelation> typedRelations = this.relations.get(descriptor);
            if (typedRelations != null) {
                allRelations.addAll(typedRelations);
            }
        }
        return allRelations;
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public Descriptor<? extends INode> getType() {
        return (Descriptor<? extends INode>) super.getType();
    }
    
    @Override
    public final void registerRelationListener(final IRelationListener nodeListener) {
        if (nodeListener == null) {
            return;
        }
        if (this.listeners == null) {
            this.listeners = new CopyOnWriteArrayList<>();
        }
        this.listeners.add(nodeListener);
    }
    
    @Override
    public final void removeRelations(final Collection<IRelation> remove) {
        if (remove == null) {
            return;
        }
        if (this.relations == null) {
            remove.clear();
            return;
        }
        for (final IRelation relation : remove) {
            Assert.isTrue(relation != null && relation.getSource() == this, "relation must be not null and source must be this");
        }
        for (final IStrategy strategy : this.getRoot().getStrategies()) {
            strategy.removeRelations(this, remove);
        }
        for (final Iterator<IRelation> it = remove.iterator(); it.hasNext();) {
            final IRelation relation = it.next();
            final Collection<IRelation> typedRelations = this.relations.get(relation.getType());
            if (typedRelations == null) {
                continue;
            }
            if (!typedRelations.remove(relation)) {
                it.remove();
            }
            if (typedRelations.isEmpty()) {
                this.relations.remove(relation.getType());
            }
        }
        if (!remove.isEmpty()) {
            for (final IStrategy strategy : this.getRoot().getStrategies()) {
                strategy.relationsRemoved(this, remove);
            }
            if (this.listeners != null) {
                for (final IRelationListener listener : this.listeners) {
                    listener.relationsRemoved(this, remove);
                }
            }
        }
        if (this.getRoot() != null) {
            final Collection<IRelation> selection = this.getRoot().getSelections();
            selection.removeAll(remove);
            this.getRoot().setSelection(this, selection);
        }
        if (this.relations.isEmpty()) {
            this.relations = null;
        }
    }
    
    @Override
    public final void unregisterRelationListener(final IRelationListener nodeListener) {
        if (this.listeners == null || nodeListener == null) {
            return;
        }
        this.listeners.remove(nodeListener);
        if (this.listeners.isEmpty()) {
            this.listeners = null;
        }
    }
    
}
