package platform.ws;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import platform.model.AStrategy;
import platform.model.Attribute;
import platform.model.INode;
import platform.model.IObject;
import platform.model.IRelation;
import platform.model.IRoot;
import platform.model.utils.NodeUtils;
import platform.utils.collections.CollectionsUtils;
import platform.utils.interfaces.IService;

public class WSStrategy
        extends AStrategy
        implements Runnable {
    
    private final IRoot               root;
    private final IService<INode>     nodeService;
    
    private final IService<IRelation> relationService;
    private final Set<INode>          nodeUpdated;
    private final Set<IRelation>      relationUpdated;
    private final Set<IRelation>      relationsRemoved;
    
    private DelayerThread             thread;
    
    public WSStrategy(final IRoot root, final IService<INode> nodeService, final IService<IRelation> relationService) {
        super();
        this.root = root;
        this.nodeService = nodeService;
        this.relationService = relationService;
        this.nodeUpdated = new HashSet<>(32);
        this.relationUpdated = new HashSet<>(32);
        this.relationsRemoved = new HashSet<>(32);
    }
    
    @Override
    public void attributesChanged(final IObject object, final Collection<Attribute> attributes) {
        if (object instanceof INode) {
            synchronized (this.nodeUpdated) {
                this.nodeUpdated.add((INode) object);
            }
        } else {
            synchronized (this.relationUpdated) {
                this.relationUpdated.add((IRelation) object);
            }
        }
        this.scheddule();
    }
    
    public void load() {
        final INode node = this.nodeService.getById(this.root.getId());
        if (node == null) {
            this.nodeService.merge(this.root);
            return;
        }
        NodeUtils.merge(this.root, node);
    }
    
    @Override
    public void relationsAdded(final INode node, final Collection<IRelation> added) {
        synchronized (this.relationUpdated) {
            this.relationUpdated.addAll(added);
        }
        this.scheddule();
    }
    
    @Override
    public void relationsRemoved(final INode node, final Collection<IRelation> removed) {
        synchronized (this.relationsRemoved) {
            this.relationsRemoved.addAll(removed);
        }
        this.scheddule();
    }
    
    @Override
    public synchronized void run() {
        if (!this.nodeUpdated.isEmpty()) {
            final Collection<INode> copied = CollectionsUtils.synchronizedCopy(this.nodeUpdated, true);
            for (final INode node : copied) {
                this.nodeService.merge(node);
            }
        }
        if (!this.relationUpdated.isEmpty()) {
            final Collection<IRelation> copied = CollectionsUtils.synchronizedCopy(this.relationUpdated, true);
            for (final IRelation relation : copied) {
                this.relationService.merge(relation);
            }
        }
        if (!this.relationsRemoved.isEmpty()) {
            final Collection<IRelation> copied = CollectionsUtils.synchronizedCopy(this.relationsRemoved, true);
            for (final IRelation relation : copied) {
                this.relationService.delete(relation.getId());
            }
        }
    }
    
    private void scheddule() {
        if (this.thread == null || !this.thread.isAlive()) {
            this.thread = new DelayerThread("WSStrategy", this, true, 250); //$NON-NLS-1$
            this.thread.start();
        } else {
            this.thread.interrupt();
        }
    }
    
}
