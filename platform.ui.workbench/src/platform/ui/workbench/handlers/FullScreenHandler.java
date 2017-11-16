package platform.ui.workbench.handlers;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;

public final class FullScreenHandler {
    
    private static Map<Shell, Menu> menus = new HashMap<>(0);
    
    @Execute
    public static void execute(final Shell shell) {
        final boolean fullScreen = !shell.getFullScreen();
        if (fullScreen) {
            shell.setMenuBar(FullScreenHandler.menus.put(shell, shell.getMenuBar()));
        } else {
            shell.setMenuBar(FullScreenHandler.menus.remove(shell));
        }
        shell.setFullScreen(fullScreen);
    }
    
}
