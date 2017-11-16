/*
 * Handler to open up a configured preferences dialog.
 * Written by Brian de Alwis, Manumitting Technologies.
 * Placed in the public domain.
 */
package platform.ui.preferences;

import java.io.File;
import java.io.IOException;

import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.preference.PreferenceManager;
import org.eclipse.jface.preference.PreferenceStore;
import org.eclipse.jface.viewers.ViewerComparator;

import platform.ui.swt.SWTUtils;
import platform.utils.Configuration;

public final class PreferencesHandler {
    
    @Execute
    public static void execute(final IEclipseContext context, @Optional final IPreferenceStore store) {
        final IPreferenceStore actualStore = PreferencesHandler.getActualStore(store);
        final PreferenceManager manager = PreferenceParser.parsePreferences(actualStore, context);
        final PreferenceDialog dialog = new PreferenceDialog(SWTUtils.getDisplay().getActiveShell(), manager);
        dialog.setPreferenceStore(actualStore);
        dialog.setHelpAvailable(false);
        dialog.create();
        dialog.getTreeViewer().setComparator(new ViewerComparator());
        dialog.open();
    }
    
    private static IPreferenceStore getActualStore(final IPreferenceStore store) {
        if (store != null) {
            return store;
        }
        final File preferences = new File(Configuration.application(), "preferences.properties"); //$NON-NLS-1$
        final PreferenceStore actualStore = new PreferenceStore(preferences.getAbsolutePath());
        try {
            actualStore.load();
        } catch (final IOException e) {
            e.printStackTrace();
        }
        return actualStore;
    }
    
}
