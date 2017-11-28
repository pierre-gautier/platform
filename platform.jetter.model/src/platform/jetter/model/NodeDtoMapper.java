package platform.jetter.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import platform.model.Attribute;
import platform.model.Descriptor;
import platform.model.INode;
import platform.model.IRelation;
import platform.model.IRoot;
import platform.model.factory.NodeFactories;
import platform.model.io.Serializer;
import platform.utils.Strings;
import platform.utils.interfaces.IMapper;

public class NodeDtoMapper
        implements IMapper<NodeDto, INode> {
    
    public static Map<String, String> toEntities(final Collection<Attribute> attributes) {
        final Map<String, String> keyToValue = new HashMap<>(attributes.size());
        for (final Attribute attribute : attributes) {
            final String value = Serializer.serialize(attribute);
            if (!Strings.isNullEmptyOrBlank(value)) {
                keyToValue.put(attribute.getDescriptor().getId(), value);
            }
        }
        return keyToValue;
    }
    
    public static Collection<Attribute> toModels(final Map<String, String> keyToValue) {
        if (keyToValue == null) {
            return Collections.emptyList();
        }
        final Collection<Attribute> attributes = new ArrayList<>(keyToValue.size());
        for (final Entry<String, String> entry : keyToValue.entrySet()) {
            final Attribute attribute = Serializer.deserialize(entry.getKey(), entry.getValue());
            if (attribute != null) {
                attributes.add(attribute);
            }
        }
        return attributes;
    }
    
    private final IRoot             root;
    private final RelationDtoMapper relationMapper;
    
    public NodeDtoMapper(final IRoot root) {
        this.root = root;
        this.relationMapper = new RelationDtoMapper(this);
    }
    
    public IRoot getRoot() {
        return this.root;
    }
    
    @Override
    public NodeDto toEntity(final INode node) {
        final NodeDto entity = new NodeDto(node.getId(), node.getType().getId(), NodeDtoMapper.toEntities(node.getAttributes()));
        if (this.relationMapper != null) {
            final Collection<IRelation> relations = node.getRelations();
            final Set<RelationDto> relationships = new HashSet<>(relations.size());
            for (final IRelation relation : relations) {
                final RelationDto relationDto = this.relationMapper.toEntity(relation, entity);
                if (relationDto != null) {
                    relationships.add(relationDto);
                }
            }
            entity.setRelationships(relationships);
        }
        return entity;
    }
    
    @Override
    public INode toModel(final NodeDto entity) {
        final Descriptor<INode> type = Descriptor.getDescriptor(entity.getType());
        final INode node = NodeFactories.INSTANCE.create(type, entity.getId(), NodeDtoMapper.toModels(entity.getAttributes()), this.root);
        if (this.relationMapper != null) {
            final Collection<RelationDto> relationships = entity.getRelationships();
            if (relationships != null) {
                final Collection<IRelation> relations = new ArrayList<>(relationships.size());
                for (final RelationDto relationEntity : relationships) {
                    if (relationEntity != null) {
                        final IRelation relation = this.relationMapper.toModel(relationEntity, node);
                        if (relation != null) {
                            relations.add(relation);
                        }
                    }
                }
                node.addRelations(relations);
            }
        }
        return node;
    }
    
}
