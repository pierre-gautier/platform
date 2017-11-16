package platform.jetter.model.mapper;

import platform.jetter.model.NodeDto;
import platform.jetter.model.RelationDto;
import platform.model.Descriptor;
import platform.model.INode;
import platform.model.IRelation;
import platform.model.factory.RelationFactories;
import platform.model.utils.NodeUtils;
import platform.model.utils.TraversalContext;
import platform.utils.interfaces.IMapper;

public class RelationDtoMapper
        implements IMapper<RelationDto, IRelation> {
    
    private final NodeDtoMapper mapper;
    
    public RelationDtoMapper(final NodeDtoMapper mapper) {
        this.mapper = mapper;
    }
    
    @Override
    public RelationDto toEntity(final IRelation model) {
        return this.toEntity(model, this.mapper.toEntity(model.getSource()));
    }
    
    public RelationDto toEntity(final IRelation model, final NodeDto source) {
        return new RelationDto(model.getId(), model.getType().getId(), source.getId(),
                this.mapper.toEntity(model.getTarget()),
                NodeDtoMapper.toEntities(model.getAttributes()));
    }
    
    @Override
    public IRelation toModel(final RelationDto entity) {
        return this.toModel(entity, NodeUtils.find(this.mapper.getRoot(), new TraversalContext(), entity.getSourceId()));
    }
    
    public IRelation toModel(final RelationDto entity, final INode source) {
        return RelationFactories.INSTANCE.create(
                Descriptor.getDescriptor(entity.getType()), entity.getId(),
                NodeDtoMapper.toModels(entity.getAttributes()), source,
                this.mapper.toModel(entity.getTarget()));
    }
    
}
