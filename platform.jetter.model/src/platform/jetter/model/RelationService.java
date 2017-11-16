package platform.jetter.model;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;

import io.swagger.annotations.Api;
import io.swagger.jaxrs.PATCH;
import platform.jetter.model.mapper.NodeDtoMapper;
import platform.jetter.model.mapper.RelationDtoMapper;
import platform.model.INode;
import platform.model.IRelation;
import platform.model.IRoot;
import platform.model.commons.Root;
import platform.model.utils.NodeUtils;
import platform.model.utils.TraversalContext;
import platform.utils.collections.CollectionsUtils;

@Api(value = "relations", produces = MediaType.APPLICATION_JSON, consumes = MediaType.APPLICATION_JSON)
@Path("/relations")
public class RelationService {
    
    private final IRoot             root;
    private final RelationDtoMapper relationMapper;
    
    public RelationService() {
        this(new Root("node-service")); //$NON-NLS-1$
    }
    
    public RelationService(final IRoot root) {
        super();
        this.root = root;
        final NodeDtoMapper nodeMapper = new NodeDtoMapper(this.root);
        this.relationMapper = new RelationDtoMapper(nodeMapper);
        nodeMapper.setMapper(this.relationMapper);
    }
    
    @DELETE
    @Consumes(MediaType.APPLICATION_JSON)
    @SuppressWarnings("unchecked")
    public void delete(final Collection<String> ids) {
        final Map<INode, Collection<IRelation>> relations = CollectionsUtils.createMultiMap(1, LinkedList.class);
        for (final String id : ids) {
            final IRelation relation = NodeUtils.findRelation(this.root, new TraversalContext(), id);
            if (relation != null) {
                relations.get(relation.getSource()).add(relation);
            }
        }
        for (final Entry<INode, Collection<IRelation>> entry : relations.entrySet()) {
            entry.getKey().removeRelations(entry.getValue());
        }
    }
    
    @PATCH
    @Consumes(MediaType.APPLICATION_JSON)
    public void patch(final Collection<RelationDto> entities) {
        for (final RelationDto entity : entities) {
            final IRelation relation = NodeUtils.findRelation(this.root, new TraversalContext(), entity.getId());
            if (relation != null) {
                NodeUtils.merge(relation, new TraversalContext(), this.relationMapper.toModel(entity, relation.getSource()));
            }
        }
    }
    
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @SuppressWarnings("unchecked")
    public void post(final Collection<RelationDto> entities) {
        final Map<String, Collection<RelationDto>> sourceIdToRelations = CollectionsUtils.createMultiMap(1, LinkedList.class);
        for (final RelationDto entity : entities) {
            sourceIdToRelations.get(entity.getSourceId()).add(entity);
        }
        final Map<INode, Collection<IRelation>> sourceToRelations = CollectionsUtils.createMultiMap(sourceIdToRelations.size(), LinkedList.class);
        for (final Entry<String, Collection<RelationDto>> entry : sourceIdToRelations.entrySet()) {
            final INode source = NodeUtils.find(this.root, new TraversalContext(), entry.getKey());
            if (source != null) {
                for (final RelationDto relation : entry.getValue()) {
                    sourceToRelations.get(source).add(this.relationMapper.toModel(relation, source));
                }
            }
        }
    }
    
}