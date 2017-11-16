package platform.hibernate.model.mapper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import platform.hibernate.IEntityMapper;
import platform.hibernate.model.NodeEntity;
import platform.hibernate.model.RelationAttributeEntity;
import platform.hibernate.model.RelationEntity;
import platform.model.Attribute;
import platform.model.Descriptor;
import platform.model.INode;
import platform.model.IRelation;
import platform.model.factory.RelationFactories;
import platform.model.io.Serializer;
import platform.utils.Strings;

public class RelationEntityMapper
        implements IEntityMapper<RelationEntity, IRelation> {
    
    private static Set<RelationAttributeEntity> toEntities(final String id, final Collection<Attribute> models) {
        final Set<RelationAttributeEntity> entities = new HashSet<>(models.size());
        for (final Attribute model : models) {
            final String value = Serializer.serialize(model);
            if (!Strings.isNullEmptyOrBlank(value)) {
                entities.add(new RelationAttributeEntity(id, model.getDescriptor().getId(), value));
            }
        }
        return entities;
    }
    
    private static Collection<Attribute> toModels(final Set<RelationAttributeEntity> entities) {
        final Collection<Attribute> models = new ArrayList<>(entities.size());
        for (final RelationAttributeEntity entity : entities) {
            final Attribute attribute = Serializer.deserialize(entity.getId().getName(), entity.getValue());
            if (attribute != null) {
                models.add(attribute);
            }
        }
        return models;
    }
    
    private final NodeEntityMapper mapper;
    
    public RelationEntityMapper(final NodeEntityMapper mapper) {
        this.mapper = mapper;
    }
    
    @Override
    public Class<RelationEntity> entityClass() {
        return RelationEntity.class;
    }
    
    @Override
    public String entityId() {
        return Strings.ID;
    }
    
    @Override
    public RelationEntity toEntity(final IRelation model) {
        return this.toEntity(model, this.mapper.toEntity(model.getSource()));
    }
    
    public RelationEntity toEntity(final IRelation model, final NodeEntity source) {
        final NodeEntity target = this.mapper.toEntity(model.getTarget());
        return new RelationEntity(model.getId(), model.getType().getId(), source, target, RelationEntityMapper.toEntities(model.getId(), model.getAttributes()));
    }
    
    @Override
    public IRelation toModel(final RelationEntity entity) {
        return this.toModel(entity, this.mapper.toModel(entity.getSource()));
    }
    
    public IRelation toModel(final RelationEntity entity, final INode source) {
        final INode target = this.mapper.toModel(entity.getTarget());
        return RelationFactories.INSTANCE.create(Descriptor.getDescriptor(entity.getType()), entity.getId(), RelationEntityMapper.toModels(entity.getAttributes()), source, target);
    }
    
}
