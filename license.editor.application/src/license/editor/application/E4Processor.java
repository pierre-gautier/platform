package license.editor.application;

import org.eclipse.e4.core.di.annotations.Execute;

public class E4Processor {
    
    public E4Processor() {
        System.out.println("E4Processor.Processor()"); //$NON-NLS-1$
    }
    
    @Execute
    public void execute() {
        System.out.println("E4Processor.execute()"); //$NON-NLS-1$
    }
    
}
