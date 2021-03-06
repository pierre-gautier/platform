package platform.rest.model;

import platform.model.Descriptor;
import platform.model.INode;
import platform.model.IRelation;
import platform.model.factory.RelationFactories;
import platform.model.utils.NodeUtils;
import platform.utils.interfaces.IMapper;

public class RelationDtoMapper
        implements IMapper<RelationDto, IRelation> {
    
    private final NodeDtoMapper nodeMapper;
    
    public RelationDtoMapper(final NodeDtoMapper nodeMapper) {
        this.nodeMapper = nodeMapper;
    }
    
    @Override
    public RelationDto toEntity(final IRelation model) {
        return this.toEntity(model, this.nodeMapper.toEntity(model.getSource()));
    }
    
    public RelationDto toEntity(final IRelation model, final NodeDto source) {
        return new RelationDto(model.getId(), model.getType().getId(), source.getId(),
                this.nodeMapper.toEntity(model.getTarget()),
                NodeDtoMapper.toEntities(model.getAttributes()));
    }
    
    @Override
    public IRelation toModel(final RelationDto entity) {
        return this.toModel(entity, NodeUtils.find(this.nodeMapper.getRoot(), entity.getSourceId()));
    }
    
    public IRelation toModel(final RelationDto entity, final INode source) {
        return RelationFactories.INSTANCE.create(
                Descriptor.getDescriptor(entity.getType()), entity.getId(),
                NodeDtoMapper.toModels(entity.getAttributes()), source,
                this.nodeMapper.toModel(entity.getTarget()));
    }
    
}
