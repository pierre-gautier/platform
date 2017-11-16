package platform.ui.actions.edit;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;

import platform.model.INode;
import platform.model.IRelation;
import platform.ui.actions.IUndoHandler;
import platform.utils.collections.CollectionsUtils;

public class CreateRelationUndoHandler
        implements IUndoHandler {
    
    private final Collection<IRelation> relations;
    
    public CreateRelationUndoHandler(final Collection<IRelation> relations) {
        this.relations = relations;
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public IUndoHandler undo() {
        final Map<INode, Collection<IRelation>> sourceToRelations = CollectionsUtils.createMultiMap(1, LinkedList.class);
        for (final IRelation relation : this.relations) {
            sourceToRelations.get(relation.getSource()).add(relation);
        }
        for (final Entry<INode, Collection<IRelation>> entry : sourceToRelations.entrySet()) {
            entry.getKey().removeRelations(entry.getValue());
        }
        return new RemoveRelationUndoHandler(this.relations);
    }
    
}