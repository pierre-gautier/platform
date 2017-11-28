package license.editor.application;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;

import platform.jersey.NodeDtoClient;
import platform.jersey.RelationDtoClient;
import platform.model.INode;
import platform.model.IRelation;
import platform.model.commons.AttributesStrategy;
import platform.model.license.LicenseRoot;
import platform.utils.Configuration;
import platform.utils.interfaces.IService;
import platform.ws.WSStrategy;
import platform.xml.XMLStrategy;

public class LicenseEditorAddon {
    
    private static final String URL = "http://localhost:8080/api/"; //$NON-NLS-1$
    
    private final LicenseRoot   root;
    private final XMLStrategy   xmlStrategy;
    private final WSStrategy    wsStrategy;
    
    @Inject
    public LicenseEditorAddon(final LicenseRoot root) {
        this.root = root;
        this.xmlStrategy = new XMLStrategy(this.root, Configuration.file("licenses.xml"), false); //$NON-NLS-1$
        
        final IService<INode> nodeService = new NodeDtoClient(root, LicenseEditorAddon.URL + "nodes/", true); //$NON-NLS-1$
        final IService<IRelation> relationService = new RelationDtoClient(root, LicenseEditorAddon.URL + "relations/", true); //$NON-NLS-1$
        this.wsStrategy = new WSStrategy(root, nodeService, relationService);
        this.root.addStrategy(new AttributesStrategy());
        this.root.addStrategy(this.xmlStrategy);
        root.addStrategy(this.wsStrategy);
    }
    
    @PostConstruct
    public void postConstruct() {
        this.wsStrategy.load();
    }
    
    @PreDestroy
    public void preDestroy() {
        this.xmlStrategy.save();
    }
}
