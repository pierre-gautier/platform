package platform.hibernate.model;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

@Entity
@Table(name = "relation_attribute", uniqueConstraints = @UniqueConstraint(columnNames = { "relation_id", "name" }))
public class RelationAttributeEntity
        implements java.io.Serializable {
    
    private static final long         serialVersionUID = 3633539268450135971L;
    
    private RelationAttributeIdEntity id;
    private RelationEntity            relation;
    private String                    value;
    
    public RelationAttributeEntity() {
        super();
    }
    
    public RelationAttributeEntity(final String relationId, final String name, final String value) {
        this.id = new RelationAttributeIdEntity(relationId, name);
        this.value = value;
    }
    
    @EmbeddedId
    @AttributeOverrides({
            @AttributeOverride(name = "id", column = @Column(name = "relation_id", nullable = false, length = 45)),
            @AttributeOverride(name = "name", column = @Column(name = "name", nullable = false, length = 45)) })
    public RelationAttributeIdEntity getId() {
        return this.id;
    }
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "relation_id", nullable = false, insertable = false, updatable = false)
    public RelationEntity getRelation() {
        return this.relation;
    }
    
    @Column(name = "value", nullable = false, length = 45)
    public String getValue() {
        return this.value;
    }
    
    public void setId(final RelationAttributeIdEntity id) {
        this.id = id;
    }
    
    public void setRelation(final RelationEntity relation) {
        this.relation = relation;
    }
    
    public void setValue(final String value) {
        this.value = value;
    }
    
}
