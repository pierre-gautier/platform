package platform.jetter.model;

import java.util.Map;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import io.swagger.annotations.ApiModelProperty;

@XmlRootElement
public class RelationDto {
    
    private @XmlAttribute String            id;
    private @XmlAttribute String            type;
    private @XmlAttribute String            sourceId;
    private @XmlElement Map<String, String> attributes;
    private @XmlElement NodeDto             target;
    
    public RelationDto() {
        super();
    }
    
    public RelationDto(final String id, final String type, final String sourceId, final NodeDto target, final Map<String, String> attributes) {
        super();
        this.setId(id);
        this.setType(type);
        this.setTarget(target);
        this.setSourceId(sourceId);
        this.setAttributes(attributes);
    }
    
    public Map<String, String> getAttributes() {
        return this.attributes;
    }
    
    @ApiModelProperty(example = "this-is-an-example-relation-id")
    public String getId() {
        return this.id;
    }
    
    @ApiModelProperty(example = "this-is-an-example-node-id")
    public String getSourceId() {
        return this.sourceId;
    }
    
    @ApiModelProperty(example = "{id:this-is-an-example-node-id-2,type:node}")
    public NodeDto getTarget() {
        return this.target;
    }
    
    @ApiModelProperty(example = "relation")
    public String getType() {
        return this.type;
    }
    
    public void setAttributes(final Map<String, String> attributes) {
        this.attributes = attributes;
    }
    
    public void setId(final String id) {
        this.id = id;
    }
    
    public void setSourceId(final String sourceId) {
        this.sourceId = sourceId;
    }
    
    public void setTarget(final NodeDto target) {
        this.target = target;
    }
    
    public void setType(final String type) {
        this.type = type;
    }
    
    @Override
    @SuppressWarnings("nls")
    public String toString() {
        return "RelationDto [id=" + this.id + ", type=" + this.type + ", sourceId=" + this.sourceId + ", target=" + this.target + ", attributes=" + this.attributes + "]";
    }
    
}
