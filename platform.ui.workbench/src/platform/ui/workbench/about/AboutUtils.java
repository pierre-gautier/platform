package platform.ui.workbench.about;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Shell;

/**
 * Manages links in styled text.
 */

public class AboutUtils {
    
    private static final String LOG_FILE = "";         //$NON-NLS-1$
    
    private static final String FILE     = "file:///"; //$NON-NLS-1$
    
    public static void openErrorLogBrowser(final Shell shell) {
        final String filename = Platform.getLogFileLocation().toOSString();
        
        final File log = new File(filename);
        if (log.exists()) {
            // Make a copy of the file with a temporary name.
            // Working around an issue with windows file associations/browser
            // malfunction whereby the browser doesn't open on ".log" and we
            // aren't returned an error.
            // See https://bugs.eclipse.org/bugs/show_bug.cgi?id=97783
            final File copy = new File(AboutUtils.LOG_FILE);
            try {
                Files.copy(log.toPath(), copy.toPath());
                Program.launch(AboutUtils.FILE + copy.getAbsolutePath());
            } catch (final IOException e) {
                // Couldn't make copy, try to open the original log.
                // We try the original in this case rather than putting up an error,
                // because the copy could fail due to an I/O or out of space
                // problem.
                // In that case we may still be able to show the original log,
                // depending on the platform. The risk is that users with
                // configurations that have bug #97783 will still get nothing
                // (vs. an error) but we'd rather
                // try again than put up an error dialog on platforms where the
                // ability to view the original log works just fine.
                Program.launch(AboutUtils.FILE + filename);
                e.printStackTrace();
            }
            return;
        }
        MessageDialog.openInformation(shell, WorkbenchMessages.AboutSystemDialog_noLogTitle, NLS.bind(WorkbenchMessages.AboutSystemDialog_noLogMessage, filename));
    }
    
    /**
     * Open a link
     */
    public static void openLink(final String href) {
        // format the href for an html file (file:///<filename.html> required for Mac only.
        String url = href;
        if (url.startsWith("file:")) { //$NON-NLS-1$
            url = url.substring(5);
            while (url.startsWith("/")) { //$NON-NLS-1$
                url = url.substring(1);
            }
            url = AboutUtils.FILE + url;
        }
        Program.launch(href);
    }
    
    private AboutUtils() {
        // hide constructor
    }
    
}
