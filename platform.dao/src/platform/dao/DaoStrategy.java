package platform.dao;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;

import platform.model.AStrategy;
import platform.model.Attribute;
import platform.model.INode;
import platform.model.IObject;
import platform.model.IRelation;
import platform.model.IRoot;
import platform.model.utils.NodeUtils;
import platform.utils.interfaces.IDao;

public class DaoStrategy
        extends AStrategy {
    
    private final IRoot                   root;
    private final IDao<INode, String>     nodeDao;
    private final IDao<IRelation, String> relationDao;
    
    public DaoStrategy(final IRoot root, final IDao<INode, String> nodeDao, final IDao<IRelation, String> relationDao) {
        this.relationDao = relationDao;
        this.nodeDao = nodeDao;
        this.root = root;
    }
    
    @Override
    public void attributesChanged(final IObject object, final Collection<Attribute> attributes) {
        if (object instanceof INode) {
            this.nodeDao.update(Arrays.asList((INode) object));
        } else {
            this.relationDao.update(Arrays.asList((IRelation) object));
        }
    }
    
    public void load() {
        final Collection<INode> nodes = this.nodeDao.retrieve(Arrays.asList(this.root.getId()));
        if (nodes == null || nodes.isEmpty()) {
            this.nodeDao.create(Arrays.asList(this.root));
            return;
        }
        for (final INode node : nodes) {
            if (node.equals(this.root)) {
                NodeUtils.merge(this.root, node);
            }
        }
    }
    
    @Override
    public void relationsAdded(final INode node, final Collection<IRelation> added) {
        final Collection<INode> nodes = new HashSet<>(added.size());
        final Collection<IRelation> relations = new HashSet<>(added.size());
        for (final IRelation relation : added) {
            // TODO bug here when targets are not leafs
            this.extract(relation, nodes, relations);
        }
        this.nodeDao.create(nodes);
        this.relationDao.create(relations);
    }
    
    @Override
    public void relationsRemoved(final INode node, final Collection<IRelation> removed) {
        this.relationDao.delete(removed);
    }
    
    private void extract(final IRelation relation, final Collection<INode> nodes, final Collection<IRelation> relations) {
        relations.add(relation);
        nodes.add(relation.getTarget());
        for (final IRelation child : relation.getTarget().getRelations()) {
            this.extract(child, nodes, relations);
        }
    }
    
}
