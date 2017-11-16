package platform.ui.actions.edit;

import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.swt.SWT;

import platform.model.INode;
import platform.model.IRelation;
import platform.model.IRoot;
import platform.model.factory.NodeFactories;
import platform.model.factory.RelationFactories;

public class PasteRelationAction
        extends ACreateRelationAction {
    
    private static INode copy(final INode node) {
        final INode copiedNode = NodeFactories.INSTANCE.create(node.getType(), null, node.getAttributes(), node.getRoot());
        final Collection<IRelation> relations = node.getRelations();
        final Collection<IRelation> copiedRelations = new ArrayList<>(relations.size());
        for (final IRelation relation : relations) {
            copiedRelations.add(PasteRelationAction.copy(relation, copiedNode));
        }
        copiedNode.addRelations(copiedRelations);
        return copiedNode;
    }
    
    private static IRelation copy(final IRelation relation, final INode source) {
        return RelationFactories.INSTANCE.create(relation.getType(), null, relation.getAttributes(), source,
                PasteRelationAction.copy(relation.getTarget()));
    }
    
    private final IRoot  root;
    private final String context;
    
    public PasteRelationAction(final IRoot root, final String context) {
        super("Paste"); //$NON-NLS-1$
        this.root = root;
        this.context = context;
    }
    
    @Override
    public int getAccelerator() {
        return SWT.MOD1 + 'V';
    }
    
    @Override
    protected Collection<IRelation> getRelations() {
        final IRelation selection = this.root.getSelection();
        final INode target = selection == null ? this.root : selection.getTarget();
        final Collection<IRelation> relations = Clipboard.getClipboard(this.context).getCopied();
        final Collection<IRelation> copiedRelations = new ArrayList<>(relations.size());
        for (final IRelation relation : relations) {
            copiedRelations.add(PasteRelationAction.copy(relation, target));
        }
        return copiedRelations;
    }
    
}
