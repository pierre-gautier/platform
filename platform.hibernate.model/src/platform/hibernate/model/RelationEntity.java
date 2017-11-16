package platform.hibernate.model;

import java.util.Collections;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

@Entity
@Table(name = "relation")
public class RelationEntity
        implements java.io.Serializable {
    
    private static final long            serialVersionUID = 2092940265256917112L;
    
    private String                       id;
    private String                       type;
    private NodeEntity                   source;
    private NodeEntity                   target;
    private Set<RelationAttributeEntity> attributes;
    
    public RelationEntity() {
        super();
    }
    
    public RelationEntity(final String id, final String type, final NodeEntity source, final NodeEntity target, final Set<RelationAttributeEntity> attributes) {
        this.id = id;
        this.type = type;
        this.source = source;
        this.target = target;
        this.attributes = attributes;
    }
    
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, mappedBy = "relation")
    public Set<RelationAttributeEntity> getAttributes() {
        if (this.attributes == null) {
            return Collections.emptySet();
        }
        return this.attributes;
    }
    
    @Id
    @Column(name = "id", unique = true, nullable = false, length = 45)
    public String getId() {
        return this.id;
    }
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_id", nullable = false)
    public NodeEntity getSource() {
        return this.source;
    }
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_id", nullable = false)
    public NodeEntity getTarget() {
        return this.target;
    }
    
    @Column(name = "type", nullable = false, length = 45)
    public String getType() {
        return this.type;
    }
    
    public void setAttributes(final Set<RelationAttributeEntity> attributes) {
        this.attributes = attributes;
    }
    
    public void setId(final String id) {
        this.id = id;
    }
    
    public void setSource(final NodeEntity source) {
        this.source = source;
    }
    
    public void setTarget(final NodeEntity target) {
        this.target = target;
    }
    
    public void setType(final String type) {
        this.type = type;
    }
    
}
