package platform.ui.actions.edit;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;

import platform.model.INode;
import platform.model.IRelation;
import platform.ui.actions.APlatformAction;
import platform.ui.actions.IUndoHandler;
import platform.utils.collections.CollectionsUtils;

public abstract class ACreateRelationAction
        extends APlatformAction {
    
    protected ACreateRelationAction(final String text) {
        super(text);
    }
    
    protected abstract Collection<IRelation> getRelations();
    
    @Override
    @SuppressWarnings("unchecked")
    protected final IUndoHandler internalRun() {
        final Collection<IRelation> relations = this.getRelations();
        if (CollectionsUtils.isNullOrEmpty(relations)) {
            return null;
        }
        final Map<INode, Collection<IRelation>> nodeToRelations = CollectionsUtils.createMultiMap(1, LinkedList.class);
        for (final IRelation relation : relations) {
            nodeToRelations.get(relation.getSource()).add(relation);
        }
        for (final Entry<INode, Collection<IRelation>> entry : nodeToRelations.entrySet()) {
            entry.getKey().addRelations(entry.getValue());
        }
        return new CreateRelationUndoHandler(relations);
    }
    
}
