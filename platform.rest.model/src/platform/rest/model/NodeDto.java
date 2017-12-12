package platform.rest.model;

import java.util.Collection;
import java.util.Map;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import io.swagger.annotations.ApiModelProperty;

@XmlRootElement
public class NodeDto {
    
    private @XmlAttribute String                id;
    private @XmlAttribute String                type;
    private @XmlElement Map<String, String>     attributes;
    private @XmlElement Collection<RelationDto> relationships;
    
    public NodeDto() {
        super();
    }
    
    public NodeDto(final String id, final String type, final Map<String, String> attributes) {
        super();
        this.setId(id);
        this.setType(type);
        this.setAttributes(attributes);
    }
    
    public Map<String, String> getAttributes() {
        return this.attributes;
    }
    
    @ApiModelProperty(example = "this-is-an-example-node-id")
    public String getId() {
        return this.id;
    }
    
    public Collection<RelationDto> getRelationships() {
        return this.relationships;
    }
    
    @ApiModelProperty(example = "node")
    public String getType() {
        return this.type;
    }
    
    public void setAttributes(final Map<String, String> attributes) {
        this.attributes = attributes;
    }
    
    public void setId(final String id) {
        this.id = id;
    }
    
    public void setRelationships(final Collection<RelationDto> relationships) {
        this.relationships = relationships;
    }
    
    public void setType(final String type) {
        this.type = type;
    }
    
    @Override
    @SuppressWarnings("nls")
    public String toString() {
        return "NodeDto [id=" + this.id + ", type=" + this.type + ", attributes=" + this.attributes + ", relationships=" + this.relationships + "]";
    }
    
}
