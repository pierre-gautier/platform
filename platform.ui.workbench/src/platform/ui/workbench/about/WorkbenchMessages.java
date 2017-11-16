/*******************************************************************************
 * Copyright (c) 2005, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * Contributors:
 * IBM - Initial API and implementation
 * Sebastian Davids - bug 128529
 * Semion Chichelnitsky (semion@il.ibm.com) - bug 278064
 * Tristan Hume - <trishume@gmail.com> -
 * Fix for Bug 2369 [Workbench] Would like to be able to save workspace without exiting
 * Implemented workbench auto-save to correctly restore state in case of crash.
 * Andrey Loskutov <loskutov@gmx.de> - Bug 388476, 445538, 463262
 *******************************************************************************/
package platform.ui.workbench.about;

import org.eclipse.osgi.util.NLS;

/**
 * Message class for workbench messages. These messages are used
 * throughout the workbench.
 */
public class WorkbenchMessages {
    
    public static String AboutDialog_shellTitle;
    public static String AboutDialog_defaultProductName;
    public static String AboutDialog_DetailsButton;
    
    public static String AboutPluginsDialog_pluginName;
    public static String AboutPluginsDialog_pluginId;
    public static String AboutPluginsDialog_version;
    public static String AboutPluginsDialog_signed;
    public static String AboutPluginsDialog_provider;
    public static String AboutPluginsDialog_state_installed;
    public static String AboutPluginsDialog_state_resolved;
    public static String AboutPluginsDialog_state_starting;
    public static String AboutPluginsDialog_state_stopping;
    public static String AboutPluginsDialog_state_uninstalled;
    public static String AboutPluginsDialog_state_active;
    public static String AboutPluginsDialog_state_unknown;
    public static String AboutPluginsDialog_moreInfo;
    public static String AboutPluginsDialog_signingInfo_show;
    public static String AboutPluginsDialog_signingInfo_hide;
    public static String AboutPluginsDialog_columns;
    public static String AboutPluginsDialog_errorTitle;
    public static String AboutPluginsDialog_unableToOpenFile;
    public static String AboutPluginsDialog_filterTextMessage;
    
    public static String AboutSystemDialog_browseErrorLogName;
    public static String AboutSystemDialog_copyToClipboardName;
    public static String AboutSystemDialog_noLogTitle;
    public static String AboutSystemDialog_noLogMessage;
    public static String AboutSystemPage_FetchJobTitle;
    public static String AboutSystemPage_RetrievingSystemInfo;
    
    public static String BundleSigningTray_Cant_Find_Service;
    public static String BundleSigningTray_Determine_Signer_For;
    public static String BundleSigningTray_Signing_Certificate;
    public static String BundleSigningTray_Signing_Date;
    public static String BundleSigningTray_Unget_Signing_Service;
    public static String BundleSigningTray_Unknown;
    public static String BundleSigningTray_Unsigned;
    public static String BundleSigningTray_Working;
    
    public static String InstallationDialog_ShellTitle;
    
    public static String SystemSummary_descriptorIdVersionState;
    public static String SystemSummary_pluginRegistry;
    public static String SystemSummary_sectionTitle;
    public static String SystemSummary_systemProperties;
    public static String SystemSummary_timeStamp;
    
    static {
        NLS.initializeMessages(WorkbenchMessages.class.getPackage().getName() + ".messages", WorkbenchMessages.class); //$NON-NLS-1$
    }
    
}
