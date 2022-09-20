package ctrmap.editor.gui;

import java.awt.Frame;
import java.util.ArrayList;
import java.util.List;
import xstandard.util.JARUtils;

/**
 * The about dialog and that's about it.
 */
public class AboutDialog extends javax.swing.JDialog {
	
	public static final String GEVENT_OPEN_ID = "OpenAboutDialog";
	private static final String PROGRAM_NAME = "CTRMap CE";

	public AboutDialog(Frame parent, boolean modal) {
		super(parent, modal);
		initComponents();
		
		String version = JARUtils.getBuildDate();
		if (version != null) {
			versionLabel.setText(PROGRAM_NAME + " " + version);
		}
		else {
			versionLabel.setText(PROGRAM_NAME + " development build");
		}
	}
	
	private String joinCredits(List<String> credits) {
		StringBuilder text = new StringBuilder();
		List<String> sorted = new ArrayList<>(credits.size());
		for (String c : credits) {
			if (c != null) {
				sorted.add(c);
			}
		}
		sorted.sort(String.CASE_INSENSITIVE_ORDER);
		text.append("<html><center>");
		for (String line : sorted) {
			text.append(line);
			text.append("<br/>");
		}
		text.append("</html></center>");
		return text.toString();
	}
	
	public void setCredits(List<String> credits) {
		creditsContainer.setText(joinCredits(credits));
		pack();
	}
	
	public void setSpecialThanks(List<String> specialThanks) {
		specialThanksContainer.setText(joinCredits(specialThanks));
		pack();
	}
	
	/**
	 * This method is called from within the constructor to initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is always
	 * regenerated by the Form Editor.
	 */
	@SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        versionLabel = new javax.swing.JLabel();
        sep1 = new javax.swing.JSeparator();
        hostLabel = new javax.swing.JLabel();
        sep2 = new javax.swing.JSeparator();
        creditsLabel = new javax.swing.JLabel();
        specialThanksLabel = new javax.swing.JLabel();
        creditsContainer = new javax.swing.JLabel();
        specialThanksContainer = new javax.swing.JLabel();
        endSep = new javax.swing.JSeparator();
        jLabel1 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("About");
        setResizable(false);

        versionLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        versionLabel.setText("CTRMap CE");

        hostLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        hostLabel.setText("<html>\n<center>\nDeveloped by HelloOO7\n</center>\n</html>");
        hostLabel.setAlignmentX(0.5F);
        hostLabel.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);

        creditsLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        creditsLabel.setText("<html> <center> <h3>Additional credits: </h3></center> </html>");
        creditsLabel.setPreferredSize(new java.awt.Dimension(124, 25));

        specialThanksLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        specialThanksLabel.setText("<html> <center> <h3>Special thanks: </h3></center> </html>");
        specialThanksLabel.setPreferredSize(new java.awt.Dimension(103, 25));

        creditsContainer.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);

        specialThanksContainer.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);

        jLabel1.setFont(new java.awt.Font("Georgia", 0, 11)); // NOI18N
        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setText("GAME FREAK");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(sep1, javax.swing.GroupLayout.Alignment.TRAILING)
            .addComponent(sep2)
            .addComponent(endSep)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(creditsContainer, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(hostLabel, javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(versionLabel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(creditsLabel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 333, Short.MAX_VALUE)
                    .addComponent(specialThanksLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 333, Short.MAX_VALUE)
                    .addComponent(specialThanksContainer, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(versionLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(sep1, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(hostLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(sep2, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(creditsLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(creditsContainer, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(specialThanksLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(specialThanksContainer, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(5, 5, 5)
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(endSep, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel creditsContainer;
    private javax.swing.JLabel creditsLabel;
    private javax.swing.JSeparator endSep;
    private javax.swing.JLabel hostLabel;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JSeparator sep1;
    private javax.swing.JSeparator sep2;
    private javax.swing.JLabel specialThanksContainer;
    private javax.swing.JLabel specialThanksLabel;
    private javax.swing.JLabel versionLabel;
    // End of variables declaration//GEN-END:variables
}
