package platform.ui.controls;

import org.eclipse.swt.widgets.Control;

import platform.model.INode;

public interface INodeControl<C extends Control> {
    
    C getControl();
    
    INode getInput();
    
}
