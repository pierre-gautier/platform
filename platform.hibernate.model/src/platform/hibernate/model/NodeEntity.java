package platform.hibernate.model;

import java.util.Collections;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

@Entity
@Table(name = "node")
public class NodeEntity
        implements java.io.Serializable {
    
    private static final long        serialVersionUID = -3870353672945389001L;
    
    private String                   id;
    private String                   type;
    private Set<NodeAttributeEntity> attributes;
    private Set<RelationEntity>      relationsForTargetId;
    private Set<RelationEntity>      relationsForSourceId;
    
    public NodeEntity() {
        super();
    }
    
    public NodeEntity(final String id, final String type, final Set<NodeAttributeEntity> attributes) {
        this.id = id;
        this.type = type;
        this.attributes = attributes;
    }
    
    @Id
    @Column(name = "id", unique = true, nullable = false, length = 45)
    public String getId() {
        return this.id;
    }
    
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, mappedBy = "node")
    public Set<NodeAttributeEntity> getNodeAttributes() {
        if (this.attributes == null) {
            return Collections.emptySet();
        }
        return this.attributes;
    }
    
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "source")
    public Set<RelationEntity> getRelationsForSourceId() {
        if (this.relationsForSourceId == null) {
            return Collections.emptySet();
        }
        return this.relationsForSourceId;
    }
    
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "target")
    public Set<RelationEntity> getRelationsForTargetId() {
        if (this.relationsForTargetId == null) {
            return Collections.emptySet();
        }
        return this.relationsForTargetId;
    }
    
    @Column(name = "type", nullable = false, length = 45)
    public String getType() {
        return this.type;
    }
    
    public void setId(final String id) {
        this.id = id;
    }
    
    public void setNodeAttributes(final Set<NodeAttributeEntity> attributes) {
        this.attributes = attributes;
    }
    
    public void setRelationsForSourceId(final Set<RelationEntity> relationsForSourceId) {
        this.relationsForSourceId = relationsForSourceId;
    }
    
    public void setRelationsForTargetId(final Set<RelationEntity> relationsForTargetId) {
        this.relationsForTargetId = relationsForTargetId;
    }
    
    public void setType(final String type) {
        this.type = type;
    }
    
}
