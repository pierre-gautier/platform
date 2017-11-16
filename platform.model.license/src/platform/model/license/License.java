package platform.model.license;

import java.time.LocalDate;
import java.util.Collection;

import platform.model.Attribute;
import platform.model.IRoot;
import platform.model.commons.Col;
import platform.model.commons.Descriptors;
import platform.model.commons.Img;
import platform.model.commons.Node;
import platform.model.commons.Pos;

public class License
        extends Node {
    
    public License(final String id, final Collection<Attribute> attributes, final IRoot root) {
        super(LicenseDescriptors.LICENSE, id, attributes, root);
    }
    
    @Override
    public Collection<Attribute> getDefaultAttributes() {
        final Collection<Attribute> defaultAttributes = super.getDefaultAttributes();
        defaultAttributes.add(new Attribute(LicenseDescriptors.USER, "user")); //$NON-NLS-1$
        defaultAttributes.add(new Attribute(LicenseDescriptors.TOKEN, this.getId()));
        defaultAttributes.add(new Attribute(LicenseDescriptors.MAIL, "user@mail.com")); //$NON-NLS-1$
        defaultAttributes.add(new Attribute(LicenseDescriptors.END, LocalDate.now().plusMonths(1)));
        defaultAttributes.add(new Attribute(Descriptors.IMAGE, new Img("platform:/plugin/platform.ui/icons/filter_ps.png"))); //$NON-NLS-1$
        defaultAttributes.add(new Attribute(Descriptors.COLOR, new Col(255, 0, 0, 255)));
        defaultAttributes.add(new Attribute(Descriptors.SIZE, Pos.create(45d, 98d)));
        defaultAttributes.add(new Attribute(Descriptors.ACTIVE, Boolean.TRUE));
        return defaultAttributes;
    }
    
}
