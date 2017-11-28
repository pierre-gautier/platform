package platform.ws;

import java.util.ArrayList;
import java.util.Collection;

import platform.model.AStrategy;
import platform.model.INode;
import platform.model.IRelation;
import platform.model.IRoot;
import platform.model.utils.NodeUtils;
import platform.model.utils.TraversalContext;
import platform.utils.interfaces.IService;

public class WSStrategy
        extends AStrategy {
    
    private final IRoot               root;
    private final IService<INode>     nodeService;
    private final IService<IRelation> relationService;
    
    public WSStrategy(final IRoot root, final IService<INode> nodeService, final IService<IRelation> relationService) {
        super();
        this.root = root;
        this.nodeService = nodeService;
        this.relationService = relationService;
    }
    
    public boolean load() {
        final INode node = this.nodeService.getById(this.root.getId());
        if (node == null) {
            return false;
        }
        NodeUtils.merge(this.root, new TraversalContext(), node);
        return true;
    }
    
    @Override
    public void relationsAdded(final INode node, final Collection<IRelation> added) {
        final Collection<INode> nodes = new ArrayList<>(added.size());
        final Collection<IRelation> relations = new ArrayList<>(added.size());
        for (final IRelation relation : added) {
            if (relation.getSource().equals(relation.getRoot())) {
                nodes.add(relation.getTarget());
            } else {
                relations.add(relation);
            }
        }
        this.nodeService.post(nodes);
        this.relationService.post(relations);
    }
    
    @Override
    public void relationsRemoved(final INode node, final Collection<IRelation> removed) {
        final Collection<String> nodeIds = new ArrayList<>(removed.size());
        final Collection<String> relationIds = new ArrayList<>(removed.size());
        for (final IRelation relation : removed) {
            if (relation.getSource().equals(relation.getRoot())) {
                nodeIds.add(relation.getTarget().getId());
            } else {
                relationIds.add(relation.getId());
            }
        }
        this.relationService.delete(relationIds);
        this.nodeService.delete(nodeIds);
    }
    
}
