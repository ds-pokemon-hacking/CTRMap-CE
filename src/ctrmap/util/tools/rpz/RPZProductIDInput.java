package ctrmap.util.tools.rpz;

import xstandard.gui.components.listeners.DocumentAdapterEx;
import javax.swing.event.DocumentEvent;

public class RPZProductIDInput extends javax.swing.JPanel {

	/**
	 * Creates new form RPZProductIDInput
	 */
	public RPZProductIDInput() {
		initComponents();
		
		textField.getDocument().addDocumentListener(new DocumentAdapterEx() {
			@Override
			public void textChangedUpdate(DocumentEvent e) {
				updateHint();
			}
		});
	}
	
	public void setProductId(String val){
		textField.setText(val);
		updateHint();
	}
	
	public boolean isProductIdValid(){
		String ht = hint.getText();
		return ht == null || ht.isEmpty();
	}
	
	public String getProductId(){
		return textField.getText();
	}
	
	private void updateHint(){
		String hintText = "";
		String s = textField.getText();
		if (s.isEmpty()){
			hintText = "Product ID may not be empty.";
		}
		else if (s.contains(" ")){
			hintText = "Product ID should not contain spaces.";
		}
		hint.setText(hintText);
	}

	/**
	 * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The content of this method is always regenerated by the Form Editor.
	 */
	@SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        textField = new javax.swing.JTextField();
        hint = new javax.swing.JLabel();

        hint.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(textField, javax.swing.GroupLayout.DEFAULT_SIZE, 180, Short.MAX_VALUE)
            .addComponent(hint, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(textField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(hint, javax.swing.GroupLayout.DEFAULT_SIZE, 14, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel hint;
    private javax.swing.JTextField textField;
    // End of variables declaration//GEN-END:variables
}
