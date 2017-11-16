package platform.ui.preferences;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.ColorFieldEditor;
import org.eclipse.jface.preference.ComboFieldEditor;
import org.eclipse.jface.preference.DirectoryFieldEditor;
import org.eclipse.jface.preference.FileFieldEditor;
import org.eclipse.jface.preference.FontFieldEditor;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.jface.preference.PathEditor;
import org.eclipse.jface.preference.RadioGroupFieldEditor;
import org.eclipse.jface.preference.ScaleFieldEditor;
import org.eclipse.jface.preference.StringFieldEditor;

public final class PreferencePreferencePage
        extends APreferencePage {
    
    private static final String[][] VALUES = {
            { "Truc", "TrucValue" }, //$NON-NLS-1$ //$NON-NLS-2$
            { "Bidule", "BiduleValue" }, //$NON-NLS-1$ //$NON-NLS-2$
            { "Chouette", "ChouetteValue" } //$NON-NLS-1$ //$NON-NLS-2$
    };
    
    public PreferencePreferencePage() {
        super("Exemples"); //$NON-NLS-1$
    }
    
    @Override
    protected void createFieldEditors() {
        
        this.addField(new BooleanFieldEditor("BooleanFieldEditor", "Oui/Non", this.getFieldEditorParent())); //$NON-NLS-1$ //$NON-NLS-2$
        
        this.addField(new StringFieldEditor("StringFieldEditor", "Texte", this.getFieldEditorParent())); //$NON-NLS-1$ //$NON-NLS-2$
        
        final IntegerFieldEditor ife = new IntegerFieldEditor("IntegerFieldEditor", "Entier", this.getFieldEditorParent()); //$NON-NLS-1$ //$NON-NLS-2$
        ife.setValidRange(Integer.MIN_VALUE, Integer.MAX_VALUE);
        this.addField(ife);
        
        this.addField(new ScaleFieldEditor("ScaleFieldEditor", "Proportion", this.getFieldEditorParent())); //$NON-NLS-1$ //$NON-NLS-2$
        
        this.addField(new ComboFieldEditor("ComboFieldEditor", "Choix valeur unique (combo)", PreferencePreferencePage.VALUES, this.getFieldEditorParent())); //$NON-NLS-1$ //$NON-NLS-2$
        
        this.addField(new RadioGroupFieldEditor("RadioGroupFieldEditor", "Choix valeur unique (group)", 1, PreferencePreferencePage.VALUES, this.getFieldEditorParent())); //$NON-NLS-1$ //$NON-NLS-2$
        
        this.addField(new ColorFieldEditor("ColorFieldEditor", "Couleur", this.getFieldEditorParent())); //$NON-NLS-1$ //$NON-NLS-2$
        
        this.addField(new FontFieldEditor("FontFieldEditor", "Police", this.getFieldEditorParent())); //$NON-NLS-1$ //$NON-NLS-2$
        
        this.addField(new FileFieldEditor("FileFieldEditor", "Fichier", this.getFieldEditorParent())); //$NON-NLS-1$ //$NON-NLS-2$
        
        this.addField(new DirectoryFieldEditor("DirectoryFieldEditor", "Dossier", this.getFieldEditorParent())); //$NON-NLS-1$ //$NON-NLS-2$
        
        this.addField(new PathEditor("PathEditor", "Chemin", "Choisissez un chemin", this.getFieldEditorParent())); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        
    }
}
