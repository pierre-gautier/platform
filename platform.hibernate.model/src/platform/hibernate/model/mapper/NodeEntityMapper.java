package platform.hibernate.model.mapper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import platform.hibernate.IEntityMapper;
import platform.hibernate.model.NodeAttributeEntity;
import platform.hibernate.model.NodeEntity;
import platform.hibernate.model.RelationEntity;
import platform.model.Attribute;
import platform.model.Descriptor;
import platform.model.INode;
import platform.model.IRelation;
import platform.model.IRoot;
import platform.model.factory.NodeFactories;
import platform.model.io.Serializer;
import platform.utils.Strings;

public class NodeEntityMapper
        implements IEntityMapper<NodeEntity, INode> {
    
    private static Set<NodeAttributeEntity> toEntities(final String id, final Collection<Attribute> attributes) {
        final Set<NodeAttributeEntity> daos = new HashSet<>(attributes.size());
        for (final Attribute attribute : attributes) {
            final String value = Serializer.serialize(attribute);
            if (!Strings.isNullEmptyOrBlank(value)) {
                daos.add(new NodeAttributeEntity(id, attribute.getDescriptor().getId(), value));
            }
        }
        return daos;
    }
    
    private static Collection<Attribute> toModels(final Set<NodeAttributeEntity> nodeAttributes) {
        final Collection<Attribute> values = new ArrayList<>(nodeAttributes.size());
        for (final NodeAttributeEntity nodeAttribute : nodeAttributes) {
            final Attribute attribute = Serializer.deserialize(nodeAttribute.getId().getName(), nodeAttribute.getValue());
            if (attribute != null) {
                values.add(attribute);
            }
        }
        return values;
    }
    
    private final IRoot          root;
    private RelationEntityMapper mapper;
    
    public NodeEntityMapper(final IRoot root) {
        this.root = root;
    }
    
    @Override
    public Class<NodeEntity> entityClass() {
        return NodeEntity.class;
    }
    
    @Override
    public String entityId() {
        return Strings.ID;
    }
    
    public void setMapper(final RelationEntityMapper mapper) {
        this.mapper = mapper;
    }
    
    @Override
    public NodeEntity toEntity(final INode node) {
        final NodeEntity entity = new NodeEntity(node.getId(), node.getType().getId(), NodeEntityMapper.toEntities(node.getId(), node.getAttributes()));
        if (this.mapper != null) {
            final Collection<IRelation> relations = node.getRelations();
            final Set<RelationEntity> entities = new HashSet<>(relations.size());
            for (final IRelation relation : relations) {
                entities.add(this.mapper.toEntity(relation, entity));
            }
            entity.setRelationsForTargetId(entities);
        }
        return entity;
    }
    
    @Override
    public INode toModel(final NodeEntity entity) {
        final INode node = NodeFactories.INSTANCE.create(Descriptor.getDescriptor(entity.getType()), entity.getId(), NodeEntityMapper.toModels(entity.getNodeAttributes()), this.root);
        if (this.mapper != null) {
            final Collection<RelationEntity> relationEntities = entity.getRelationsForSourceId();
            final Collection<IRelation> relationObjets = new ArrayList<>(relationEntities.size());
            for (final RelationEntity relationEntity : relationEntities) {
                final IRelation relation = this.mapper.toModel(relationEntity, node);
                if (relation != null) {
                    relationObjets.add(relation);
                }
            }
            node.addRelations(relationObjets);
        }
        return node;
    }
    
}
