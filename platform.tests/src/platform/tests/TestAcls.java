package platform.tests;

import java.util.Arrays;
import java.util.Collection;

import org.junit.Assert;
import org.junit.Test;

import platform.acl.ACLDescriptors;
import platform.acl.ACLRoot;
import platform.acl.ACLStrategy;
import platform.model.Attribute;
import platform.model.INode;
import platform.model.IRoot;
import platform.model.commons.Descriptors;
import platform.model.commons.Root;
import platform.model.commons.Types;
import platform.model.factory.NodeFactories;

@SuppressWarnings("nls")
public class TestAcls {
    
    @Test
    public void test() {
        
        // data
        
        final IRoot dataroot = new Root("test-acl-root");
        
        Assert.assertEquals("test-acl-root", dataroot.getAttribute(Descriptors.LABEL));
        
        final INode dataNode = NodeFactories.INSTANCE.create(Types.NODE, null, null, dataroot);
        TestUtils.addRelation(dataroot, dataNode);
        
        // add should work because acls are not applied
        
        dataroot.addAttribute(Descriptors.LABEL, "tutu");
        Assert.assertEquals("tutu", dataroot.getAttribute(Descriptors.LABEL));
        
        dataNode.addAttribute(Descriptors.LABEL, "tutu");
        Assert.assertEquals("tutu", dataNode.getAttribute(Descriptors.LABEL));
        
        // UPDATE should be null because acls are not installed
        
        Assert.assertNull(dataNode.getAttribute(ACLDescriptors.UPDATE));
        
        // create acls root, strategy and install it
        
        final ACLRoot aclRoot = new ACLRoot();
        dataroot.addStrategy(new ACLStrategy(aclRoot));
        Assert.assertNull(dataNode.getAttribute(ACLDescriptors.UPDATE));
        
        // add should not work because UPDATE is not allowed
        
        dataroot.addAttribute(Descriptors.LABEL, "titi");
        Assert.assertEquals("tutu", dataroot.getAttribute(Descriptors.LABEL));
        
        dataNode.addAttribute(Descriptors.LABEL, "titi");
        Assert.assertEquals("tutu", dataNode.getAttribute(Descriptors.LABEL));
        
        // add acls rights
        
        final Collection<Attribute> values = Arrays.asList(new Attribute(ACLDescriptors.UPDATE, Boolean.TRUE));
        final INode aclNode = NodeFactories.INSTANCE.create(dataNode.getType(), dataNode.getId(), values, aclRoot);
        TestUtils.addRelation(aclRoot, aclNode);
        
        // UPDATE should be null because acls are not applied, then acls contain no rights
        
        Assert.assertSame(null, dataroot.getAttribute(ACLDescriptors.UPDATE));
        Assert.assertSame(Boolean.TRUE, dataNode.getAttribute(ACLDescriptors.UPDATE));
        
        // add should not work because UPDATE is not allowed
        
        dataroot.addAttribute(Descriptors.LABEL, "titi");
        Assert.assertEquals("tutu", dataroot.getAttribute(Descriptors.LABEL));
        
        // add should work because UPDATE is allowed
        dataNode.addAttribute(Descriptors.LABEL, "titi");
        Assert.assertEquals("titi", dataNode.getAttribute(Descriptors.LABEL));
        
    }
}
