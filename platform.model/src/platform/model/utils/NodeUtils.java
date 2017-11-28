package platform.model.utils;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.eclipse.core.runtime.Assert;

import platform.model.Attribute;
import platform.model.INode;
import platform.model.IObject;
import platform.model.IRelation;
import platform.model.factory.RelationFactories;
import platform.utils.Strings;

public class NodeUtils {
    
    public static int cunt(final INode node, final TraversalContext context) {
        if (NodeUtils.checkContext(context, node)) {
            int cunt = 1;
            final Collection<IRelation> relations = node.getRelations();
            for (final IRelation relation : relations) {
                cunt += NodeUtils.cunt(relation.getTarget(), context);
            }
            return cunt;
        }
        return 0;
    }
    
    public static final INode find(final INode node, final TraversalContext context, final String id) {
        if (NodeUtils.checkContext(context, node)) {
            if (node.getId().equals(id)) {
                return node;
            }
            for (final IRelation relation : node.getRelations()) {
                if (relation.getTarget().getId().equals(id)) {
                    return relation.getTarget();
                }
            }
            for (final IRelation relation : node.getRelations()) {
                final INode found = NodeUtils.find(relation.getTarget(), context, id);
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }
    
    public static final IRelation findParentRelation(final INode node, final TraversalContext context, final String id) {
        if (NodeUtils.checkContext(context, node)) {
            for (final IRelation relation : node.getRelations()) {
                if (relation.getTarget().getId().equals(id)) {
                    return relation;
                }
            }
            for (final IRelation relation : node.getRelations()) {
                final IRelation found = NodeUtils.findParentRelation(relation.getTarget(), context, id);
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }
    
    public static final IRelation findRelation(final INode node, final TraversalContext context, final String id) {
        if (NodeUtils.checkContext(context, node)) {
            for (final IRelation relation : node.getRelations()) {
                if (relation.getId().equals(id)) {
                    return relation;
                }
            }
            for (final IRelation relation : node.getRelations()) {
                final IRelation found = NodeUtils.findRelation(relation.getTarget(), context, id);
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }
    
    public static void merge(final INode in, final TraversalContext context, final INode toMerge) {
        Assert.isTrue(in != toMerge && in.equals(toMerge), "in and toMerge must not be the same and must be equals"); //$NON-NLS-1$
        if (NodeUtils.checkContext(context, in)) {
            NodeUtils.updateAttributes(in, toMerge.getAttributes(), true);
            final Collection<IRelation> relations = in.getRelations();
            final Collection<IRelation> toMergeRelations = toMerge.getRelations();
            final Collection<IRelation> relationsToMerge = new ArrayList<>(toMergeRelations.size());
            for (final Iterator<IRelation> it = toMergeRelations.iterator(); it.hasNext();) {
                final IRelation relationToMerge = it.next();
                for (final IRelation relation : relations) {
                    if (relation.getId().equals(relationToMerge.getId())) {
                        NodeUtils.mergeRelation(relation, context, relationToMerge);
                        it.remove();
                        break;
                    }
                }
                relationsToMerge.add(RelationFactories.INSTANCE.create(
                        relationToMerge.getType(),
                        relationToMerge.getId(),
                        relationToMerge.getAttributes(),
                        in,
                        relationToMerge.getTarget()));
            }
            in.addRelations(relationsToMerge);
        }
    }
    
    public static void mergeRelation(final IRelation relation, final TraversalContext context, final IRelation toMerge) {
        Assert.isTrue(relation != toMerge && relation.equals(toMerge), "in and toMerge must not be the same and must be equals"); //$NON-NLS-1$
        if (!relation.getSource().getId().equals(toMerge.getSource().getId())
                || !relation.getTarget().getId().equals(toMerge.getTarget().getId())) {
            System.err.println("source or target are not equal, ignoring"); //$NON-NLS-1$
            return;
        }
        NodeUtils.updateAttributes(relation, toMerge.getAttributes(), true);
        NodeUtils.merge(relation.getTarget(), context, toMerge.getTarget());
    }
    
    public static void print(final INode node, final TraversalContext context, final String space, final PrintStream out) {
        if (NodeUtils.checkContext(context, node)) {
            out.print(space + node.toString() + Strings.NEW_LINE);
            for (final IRelation relation : node.getRelations()) {
                out.print(space + Strings.SPACE + relation.toString() + Strings.NEW_LINE);
                if (context == null || !context.contains(relation.getTarget())) {
                    NodeUtils.print(relation.getTarget(), context, space + Strings.SPACE, out);
                }
            }
        }
    }
    
    public static void updateAttributes(final IObject object, final Collection<Attribute> toMergeAttributes, final boolean removeNotFound) {
        final Collection<Attribute> currentAttributes = object.getAttributes();
        final int size = toMergeAttributes.size() + (removeNotFound ? currentAttributes.size() : 0);
        final Collection<Attribute> newAttributes = new ArrayList<>(size);
        if (removeNotFound) {
            for (final Attribute attiribute : currentAttributes) {
                newAttributes.add(new Attribute(attiribute.getDescriptor(), null));
            }
        }
        newAttributes.addAll(toMergeAttributes);
        object.addAttributes(newAttributes);
    }
    
    private static boolean checkContext(final TraversalContext context, final INode node) {
        return context == null || context.add(node) && context.traverseNextLevel();
    }
    
}
