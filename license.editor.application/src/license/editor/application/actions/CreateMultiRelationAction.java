package license.editor.application.actions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import org.eclipse.swt.SWT;

import platform.model.Attribute;
import platform.model.Descriptor;
import platform.model.INode;
import platform.model.IRelation;
import platform.model.IRoot;
import platform.model.commons.Descriptors;
import platform.model.commons.Types;
import platform.model.factory.NodeFactories;
import platform.model.factory.RelationFactories;
import platform.ui.actions.edit.ACreateRelationAction;

public class CreateMultiRelationAction
        extends ACreateRelationAction {
    
    private final IRoot                           root;
    private final Descriptor<? extends INode>     nodeType;
    private final Descriptor<? extends IRelation> relationType;
    
    public CreateMultiRelationAction(final IRoot root) {
        this(root, null, null);
    }
    
    public CreateMultiRelationAction(final IRoot root, final Descriptor<? extends INode> nodeType, final Descriptor<? extends IRelation> relationType) {
        super("Create"); //$NON-NLS-1$
        this.root = root;
        this.nodeType = nodeType == null ? Types.NODE : nodeType;
        this.relationType = relationType == null ? Types.RELATION : relationType;
    }
    
    @Override
    public int getAccelerator() {
        return SWT.MOD1 + 'N';
    }
    
    @Override
    protected Collection<IRelation> getRelations() {
        final IRelation selection = this.root.getSelection();
        final INode source = selection == null ? this.root : selection.getTarget();
        final Collection<Attribute> attributes = Arrays.asList(new Attribute(Descriptors.LABEL, this.nodeType.getLabel()));
        final INode target = NodeFactories.INSTANCE.create(this.nodeType, null, attributes, this.root);
        if (target == null) {
            return null;
        }
        final IRelation relation = RelationFactories.INSTANCE.create(this.relationType, null, null, source, target);
        if (relation == null) {
            return null;
        }
        this.createRelations(target, 3, 2);
        return Arrays.asList(relation);
    }
    
    private void createRelations(final INode source, final int w, final int h) {
        if (h == 0 || w == 0) {
            return;
        }
        System.out.println(h + " - CreateMultiRelationAction.createRelations() " + source); //$NON-NLS-1$
        final Collection<IRelation> relations = new ArrayList<>(w);
        for (int i = 0; i < w; i++) {
            final Collection<Attribute> attributes = Arrays.asList(new Attribute(Descriptors.LABEL, this.nodeType.getLabel()));
            final INode target = NodeFactories.INSTANCE.create(this.nodeType, null, attributes, this.root);
            relations.add(RelationFactories.INSTANCE.create(this.relationType, null, null, source, target));
        }
        source.addRelations(relations);
        
        for (final IRelation relation : relations) {
            this.createRelations(relation.getTarget(), w, h - 1);
        }
    }
    
}
