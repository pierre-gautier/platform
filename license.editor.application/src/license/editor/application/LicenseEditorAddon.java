package license.editor.application;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;

import platform.model.INode;
import platform.model.IRelation;
import platform.model.commons.AttributesStrategy;
import platform.model.license.LicenseRoot;
import platform.rest.client.model.NodeDtoClient;
import platform.rest.client.model.RelationDtoClient;
import platform.utils.Configuration;
import platform.utils.interfaces.IService;
import platform.whatsup.WhatsupStrategy;
import platform.ws.WSStrategy;
import platform.xml.XMLStrategy;

public class LicenseEditorAddon {
    
    private static final String   URL = "http://localhost:8080/api/"; //$NON-NLS-1$
    
    private final LicenseRoot     root;
    private final XMLStrategy     xmlStrategy;
    private final WSStrategy      wsStrategy;
    private final WhatsupStrategy whatsupStrategy;
    
    @Inject
    public LicenseEditorAddon(final LicenseRoot root) {
        this.root = root;
        // xml
        this.xmlStrategy = new XMLStrategy(this.root, Configuration.file("licenses.xml"), false); //$NON-NLS-1$
        // whatsup
        this.whatsupStrategy = new WhatsupStrategy(1000);
        // ws
        final IService<INode> nodeService = new NodeDtoClient(root, LicenseEditorAddon.URL + "nodes/", true); //$NON-NLS-1$
        final IService<IRelation> relationService = new RelationDtoClient(root, LicenseEditorAddon.URL + "relations/", true); //$NON-NLS-1$
        this.wsStrategy = new WSStrategy(root, nodeService, relationService);
    }
    
    @PostConstruct
    public void postConstruct() {
        this.wsStrategy.load();
        this.root.addStrategy(new AttributesStrategy());
        this.root.addStrategy(this.wsStrategy);
        this.root.addStrategy(this.xmlStrategy);
        this.root.addStrategy(this.whatsupStrategy);
    }
    
    @PreDestroy
    public void preDestroy() {
        this.xmlStrategy.save();
    }
}
