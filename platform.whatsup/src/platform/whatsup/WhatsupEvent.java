package platform.whatsup;

public class WhatsupEvent {
    
    private final String type;
    private final String id;
    private final String action;
    
    public WhatsupEvent(final String type, final String id, final String action) {
        super();
        this.type = type;
        this.id = id;
        this.action = action;
    }
    
    public String getAction() {
        return this.action;
    }
    
    public String getId() {
        return this.id;
    }
    
    public String getType() {
        return this.type;
    }
    
}
