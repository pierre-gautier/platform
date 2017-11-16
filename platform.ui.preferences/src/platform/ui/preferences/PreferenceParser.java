/** Class extracted from:PreferencesHandler**Handler to open up a configured preferences dialog.*Written by Brian de Alwis,Manumitting Technologies.*Placed in the public domain. */
package platform.ui.preferences;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.jface.preference.IPreferenceNode;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceManager;
import org.eclipse.jface.preference.PreferenceNode;
import org.eclipse.jface.preference.PreferencePage;
import org.osgi.framework.Bundle;

import platform.model.Activator;
import platform.utils.Strings;

public final class PreferenceParser {
    
    private static final String PREFS_PAGE_EXTENSION_POINT = "platform.ui.preferences.preferencePage"; //$NON-NLS-1$
    private static final String ATTR_CATEGORY              = "category";                               //$NON-NLS-1$
    private static final String ATTR_CLASS                 = "class";                                  //$NON-NLS-1$
    private static final String ELMT_PAGE                  = "page";                                   //$NON-NLS-1$
    
    public static PreferenceManager parsePreferences(final IPreferenceStore store, final IEclipseContext context) {
        
        final PreferenceManager manager = new PreferenceManager();
        
        final Map<String, PreferencePage> idToPage = new HashMap<>();
        final Map<String, String> idToCategory = new HashMap<>();
        
        final IExtensionRegistry registry = context.get(IExtensionRegistry.class);
        
        for (final IConfigurationElement configurationElement : registry.getConfigurationElementsFor(PreferenceParser.PREFS_PAGE_EXTENSION_POINT)) {
            
            final String clazzName = configurationElement.getAttribute(PreferenceParser.ATTR_CLASS);
            final String category = configurationElement.getAttribute(PreferenceParser.ATTR_CATEGORY);
            
            if (!configurationElement.getName().equals(PreferenceParser.ELMT_PAGE)) {
                System.err.println("Unexpected element " + configurationElement.getName()); //$NON-NLS-1$
                continue;
            } else if (Strings.isNullEmptyOrBlank(clazzName)) {
                System.err.println("Missing class " + configurationElement.getNamespaceIdentifier()); //$NON-NLS-1$
                continue;
            }
            
            final Bundle bundle = Activator.getBundleBySymbolicName(configurationElement.getNamespaceIdentifier());
            try {
                final Class<?> clazz = bundle.loadClass(clazzName);
                final Object object = ContextInjectionFactory.make(clazz, context);
                // final Object object = factory.create(prefPageURI, this.context);
                if (!(object instanceof PreferencePage)) {
                    System.err.println("Expected instance of PreferencePage instead of " + clazz); //$NON-NLS-1$
                    continue;
                }
                final PreferencePage page = (PreferencePage) object;
                page.setPreferenceStore(store);
                
                idToPage.put(clazzName, page);
                idToCategory.put(clazzName, category);
            } catch (final ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        
        while (!idToCategory.isEmpty()) {
            final Iterator<Entry<String, String>> iterator = idToCategory.entrySet().iterator();
            while (iterator.hasNext()) {
                final Entry<String, String> next = iterator.next();
                final String id = next.getKey();
                final String categoryId = next.getValue();
                if (Strings.isNullEmptyOrBlank(categoryId)
                        || !idToPage.containsKey(categoryId)) {
                    manager.addToRoot(new PreferenceNode(id, idToPage.get(id)));
                    iterator.remove();
                } else {
                    final IPreferenceNode node = PreferenceParser.findNode(manager, categoryId);
                    if (node != null) {
                        node.add(new PreferenceNode(id, idToPage.get(id)));
                        iterator.remove();
                    }
                }
            }
        }
        
        return manager;
    }
    
    private static final IPreferenceNode findNode(final PreferenceManager pm, final String categoryId) {
        if (Strings.isNullEmptyOrBlank(categoryId)) {
            return null;
        }
        for (final IPreferenceNode node : pm.getElements(PreferenceManager.PRE_ORDER)) {
            if (node.getId().equals(categoryId)) {
                return node;
            }
        }
        return null;
    }
    
}
