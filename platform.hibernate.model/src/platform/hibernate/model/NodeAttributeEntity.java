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
@Table(name = "node_attribute", uniqueConstraints = @UniqueConstraint(columnNames = { "node_id", "name" }))
public class NodeAttributeEntity
        implements java.io.Serializable {
    
    private static final long     serialVersionUID = 4863036506422143020L;
    
    private NodeAttributeIdEntity id;
    private NodeEntity            node;
    private String                value;
    
    public NodeAttributeEntity() {
        super();
    }
    
    public NodeAttributeEntity(final String nodeId, final String name, final String value) {
        this.id = new NodeAttributeIdEntity(nodeId, name);
        this.value = value;
    }
    
    @EmbeddedId
    @AttributeOverrides({
            @AttributeOverride(name = "id", column = @Column(name = "node_id", nullable = false, length = 45)),
            @AttributeOverride(name = "name", column = @Column(name = "name", nullable = false, length = 45)) })
    public NodeAttributeIdEntity getId() {
        return this.id;
    }
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "node_id", nullable = false, insertable = false, updatable = false)
    public NodeEntity getNode() {
        return this.node;
    }
    
    @Column(name = "value", nullable = false, length = 45)
    public String getValue() {
        return this.value;
    }
    
    public void setId(final NodeAttributeIdEntity id) {
        this.id = id;
    }
    
    public void setNode(final NodeEntity node) {
        this.node = node;
    }
    
    public void setValue(final String value) {
        this.value = value;
    }
    
}
