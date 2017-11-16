package platform.ui.workbench.properties.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import platform.model.Attribute;
import platform.model.Descriptor;
import platform.model.IObject;
import platform.model.IRelation;
import platform.model.commons.Descriptors;
import platform.ui.actions.edit.AttributesAction;
import platform.ui.workbench.properties.IPropertyDescriptor;
import platform.ui.workbench.properties.IPropertySource;
import platform.utils.Strings;

public class RelationPropertySource
        implements IPropertySource {
    
    private static final String RELATION_PREFIX = "Relation "; //$NON-NLS-1$
    
    private static Collection<? extends IPropertyDescriptor> createDescriptors(final IObject object) {
        final boolean isRelation = object instanceof IRelation;
        final Collection<Attribute> values = object.getAttributes();
        final Collection<IPropertyDescriptor> result = new ArrayList<>(values.size());
        final Set<Descriptor<?>> descriptors = new HashSet<>(values.size());
        for (final Attribute pv : values) {
            descriptors.add(pv.getDescriptor());
        }
        for (final Descriptor<?> pd : descriptors) {
            result.add(new PropertyDescriptor(pd, isRelation));
        }
        result.add(new PropertyDescriptor(Descriptors.ID, isRelation));
        result.add(new PropertyDescriptor(Descriptors.TYPE, isRelation));
        return result;
    }
    
    private static Descriptor<?> getDescriptor(final Object id) {
        return Descriptor.getDescriptor(id.toString().replaceFirst(RelationPropertySource.RELATION_PREFIX, Strings.EMPTY));
    }
    
    private static boolean isRelation(final Object id) {
        return id.toString().startsWith(RelationPropertySource.RELATION_PREFIX);
    }
    
    private final IRelation relation;
    
    public RelationPropertySource(final IRelation relation) {
        this.relation = relation;
    }
    
    @Override
    public Object getEditableValue() {
        return null;
    }
    
    @Override
    public IPropertyDescriptor[] getPropertyDescriptors() {
        if (this.relation == null) {
            return new IPropertyDescriptor[0];
        }
        final Collection<IPropertyDescriptor> descriptors = new ArrayList<>();
        descriptors.addAll(RelationPropertySource.createDescriptors(this.relation));
        descriptors.addAll(RelationPropertySource.createDescriptors(this.relation.getTarget()));
        return descriptors.toArray(new IPropertyDescriptor[descriptors.size()]);
    }
    
    @Override
    public Object getPropertyValue(final Object id) {
        final Descriptor<?> descriptor = RelationPropertySource.getDescriptor(id);
        if (RelationPropertySource.isRelation(id)) {
            return this.relation.getAttribute(descriptor);
        }
        return this.relation.getTarget().getAttribute(descriptor);
    }
    
    @Override
    public boolean isPropertyEditable(final Object id) {
        final Descriptor<?> descriptor = RelationPropertySource.getDescriptor(id);
        return !(descriptor == Descriptors.ID || descriptor == Descriptors.TYPE);
    }
    
    @Override
    public void setPropertyValue(final Object id, final Object value) {
        final boolean isRelation = id.toString().startsWith(RelationPropertySource.RELATION_PREFIX);
        final String actualId = id.toString().replaceFirst(RelationPropertySource.RELATION_PREFIX, Strings.EMPTY);
        final Descriptor<?> pd = Descriptor.getDescriptor(actualId);
        final IObject object = isRelation ? this.relation : this.relation.getTarget();
        new AttributesAction(Arrays.asList(object), Arrays.asList(Attribute.unchecked(pd, value))).run();
    }
    
}
