package platform.hibernate.model;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class RelationAttributeIdEntity
        implements java.io.Serializable {
    
    private static final long serialVersionUID = 4582754798303007335L;
    
    private String            id;
    private String            name;
    
    public RelationAttributeIdEntity() {
        super();
    }
    
    public RelationAttributeIdEntity(final String id, final String name) {
        this.id = id;
        this.name = name;
    }
    
    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (this.getClass() != obj.getClass()) {
            return false;
        }
        final RelationAttributeIdEntity other = (RelationAttributeIdEntity) obj;
        if (this.name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!this.name.equals(other.name)) {
            return false;
        }
        if (this.id == null) {
            if (other.id != null) {
                return false;
            }
        } else if (!this.id.equals(other.id)) {
            return false;
        }
        return true;
    }
    
    @Column(name = "relation_id", nullable = false, length = 45)
    public String getId() {
        return this.id;
    }
    
    @Column(name = "name", nullable = false, length = 45)
    public String getName() {
        return this.name;
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (this.name == null ? 0 : this.name.hashCode());
        result = prime * result + (this.id == null ? 0 : this.id.hashCode());
        return result;
    }
    
    public void setId(final String id) {
        this.id = id;
    }
    
    public void setName(final String name) {
        this.name = name;
    }
    
}
