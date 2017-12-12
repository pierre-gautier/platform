package platform.rest.whatsup;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import io.swagger.annotations.ApiModelProperty;

@XmlRootElement
public class WhatsupEventDto {
    
    private @XmlAttribute String id;
    private @XmlAttribute String type;
    private @XmlAttribute String action;
    
    public WhatsupEventDto(final String type, final String id, final String action) {
        super();
        this.setId(id);
        this.setType(type);
        this.setAction(action);
    }
    
    @ApiModelProperty(example = "attributesChanged")
    public String getAction() {
        return this.action;
    }
    
    @ApiModelProperty(example = "this-is-an-example-node-id")
    public String getId() {
        return this.id;
    }
    
    @ApiModelProperty(example = "node")
    public String getType() {
        return this.type;
    }
    
    public void setAction(final String action) {
        this.action = action;
    }
    
    public void setId(final String id) {
        this.id = id;
    }
    
    public void setType(final String type) {
        this.type = type;
    }
    
    @Override
    @SuppressWarnings("nls")
    public String toString() {
        return "NodeDto [id=" + this.id + ", type=" + this.type + ", action=" + this.action + "]";
    }
    
}
