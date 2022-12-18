package ctrmap.editor.gui.editors.text;

import ctrmap.editor.gui.editors.text.alias.ITextAliasManager;
import ctrmap.editor.CTRMap;
import ctrmap.formats.pokemon.text.MsgStr;
import ctrmap.editor.gui.editors.common.AbstractTabbedEditor;
import ctrmap.editor.gui.editors.text.loaders.AbstractTextLoader;
import ctrmap.editor.gui.editors.text.loaders.ITextArcType;
import ctrmap.formats.pokemon.text.TextFileFriendlizer;
import xstandard.fs.FSFile;
import xstandard.gui.DialogUtils;
import xstandard.text.FormattingUtils;
import xstandard.gui.file.XFileDialog;
import xstandard.gui.file.CommonExtensionFilters;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.swing.CellEditor;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.TableModelEvent;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;

public class TextEditor extends javax.swing.JPanel implements AbstractTabbedEditor {

	private CTRMap cm;
	private AbstractTextLoader ldr;
	private ITextAliasManager aliasMng;
	private boolean loaded = false;

	private boolean changed = false;

	private int COL_MSGID = 0;
	private int COL_ALIAS = 1;
	private int COL_WRITE9BIT = 2;
	private int COL_MSGSTR = 3;

	private TableColumn write9BitColumn;

	public TextEditor(CTRMap cm) {
		this();
		this.cm = cm;
	}

	public TextEditor() {
		initComponents();

		textFileBox.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				if (ldr != null) {
					int tf = textFileBox.getValueCB();
					loadTextFileData(tf);
				}
			}
		});

		getTableModel().addTableModelListener((TableModelEvent e) -> {
			if (ldr != null && loaded) {
				if (e.getType() == TableModelEvent.UPDATE) {
					for (int row = e.getFirstRow(); row <= e.getLastRow(); row++) {
						int msgid = (Integer) textTable.getValueAt(row, COL_MSGID);
						String text = (String) textTable.getValueAt(row, COL_MSGSTR);

						boolean is9bit = false;
						if (ldr.getMsgHandler().isMsgDataSupports9Bit()) {
							is9bit = (Boolean) textTable.getValueAt(row, COL_WRITE9BIT);
						}
						MsgStr ms = ldr.getMsgStrs().get(row);
						ITextAliasManager.MessageTag tag = new ITextAliasManager.MessageTag(ldr.getArcType(), ldr.getTextFileId(), msgid);
						if (ldr.setTextLineContent(msgid, text) || ms.encode9Bit != is9bit) {
							ms.encode9Bit = is9bit;
							changed = true;
						}

						String alias = (String) textTable.getValueAt(row, COL_ALIAS);
						String saneAlias = FormattingUtils.getStrWithoutNonAlphanumeric(alias);
						if (!alias.equals(saneAlias)) {
							textTable.setValueAt(saneAlias, row, COL_ALIAS);
						}
						alias = saneAlias;
						if (aliasMng != null) {
							String nowAlias = getAliasOrDefault(msgid);
							if (!Objects.equals(nowAlias, alias)) {
								aliasMng.setMsgidAlias(tag, alias);
							}
							aliasMng.setMsgidAliasContent(tag, text);
						}
					}
				}
			}
		});

		JPopupMenu tableResizeMenu = new JPopupMenu();

		JMenuItem removeRowItem = new JMenuItem("Remove");

		removeRowItem.addActionListener(((e) -> {
			DefaultTableModel mdl = getTableModel();
			boolean didRangeCheck = false;
			List<Integer> rows = getSelectedRows();
			rows.sort((Integer o1, Integer o2) -> o2 - o1);
			for (Integer row : rows) {
				if (row != textTable.getRowCount() - 1) {
					if (!didRangeCheck) {
						if (rangeCheck(-1)) {
							didRangeCheck = true;
						} else {
							break;
						}
					}
				}
				ldr.removeTextLine(row);
				loaded = false;
				mdl.removeRow(row);
				updateMsgIds(row);
				loaded = true;
				changed = true;
			}
		}));

		JMenuItem appendRowItem = new JMenuItem();
		JMenuItem prependRowItem = new JMenuItem("Add row before");

		appendRowItem.addActionListener(((e) -> {
			List<Integer> selectedRows = getSelectedRows();
			int addind;
			if (!selectedRows.isEmpty()) {
				addind = selectedRows.get(selectedRows.size() - 1) + 1;
			} else {
				addind = textTable.getRowCount();
			}
			if (rangeCheck(addind)) {
				insertBlankRowAt(addind);
			}
		}));

		prependRowItem.addActionListener(((e) -> {
			int addind = textTable.getSelectedRow();
			if (rangeCheck(addind)) {
				insertBlankRowAt(addind);
			}
		}));

		textTable.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (SwingUtilities.isRightMouseButton(e)) {
					int row = textTable.rowAtPoint(e.getPoint());

					if (!textTable.isRowSelected(row)) {
						textTable.setRowSelectionInterval(row, row);
					}

					tableResizeMenu.removeAll();
					if (textTable.getSelectedRow() != -1) {
						tableResizeMenu.add(appendRowItem);
						tableResizeMenu.add(prependRowItem);
						appendRowItem.setText("Add row after");
						tableResizeMenu.add(removeRowItem);

					} else {
						appendRowItem.setText("Add row");
						tableResizeMenu.add(appendRowItem);
					}

					tableResizeMenu.show(textTable, e.getX(), e.getY());
				}
			}
		});

		textTableSP.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (SwingUtilities.isRightMouseButton(e)) {
					tableResizeMenu.removeAll();
					appendRowItem.setText("Add row");
					tableResizeMenu.add(appendRowItem);

					tableResizeMenu.show(textTableSP, e.getX(), e.getY());
				}
			}
		});

		write9BitColumn = textTable.getColumnModel().getColumn(COL_WRITE9BIT);
	}

	public void load(CTRMap cm) {
		this.cm = cm;
	}

	public void setMsgIdManager(ITextAliasManager mng) {
		this.aliasMng = mng;
	}

	private boolean rangeCheck(int addind) {
		if (addind != textTable.getRowCount()) {
			return DialogUtils.showYesNoDialog("Warning", "Performing this operation will shift the order of rows. Proceed anyway?");
		}
		return true;
	}

	private void insertBlankRowAt(int addind) {
		loaded = false;
		String text = ldr.getBlankLineText(addind);
		ldr.insertTextLineContent(addind, text);

		DefaultTableModel mdl = getTableModel();

		mdl.insertRow(addind, new Object[]{
			addind, //msgid
			getAliasOrDefault(addind), //alias
			ldr.getMsgStrs().get(addind).encode9Bit, //encoding
			text //data
		});

		textTable.getSelectionModel().setSelectionInterval(addind, addind);

		if (aliasMng != null) {
			aliasMng.setSavedataEnable(false);
			List<String> aliases = getAllAliases();
			aliases.add(addind, null);
			for (int i = 0; i < aliases.size(); i++) {
				ITextAliasManager.MessageTag tag = new ITextAliasManager.MessageTag(ldr.getArcType(), ldr.getTextFileId(), i);
				aliasMng.setMsgidAlias(tag, aliases.get(i));
			}
			aliasMng.setSavedataEnable(true);
			aliasMng.saveDataManual(ldr.getArcType(), ldr.getTextFileId());
		}

		updateMsgIds(addind);
		loaded = true;
		changed = true;
	}

	private List<String> getAllAliases() {
		List<String> aliases = new ArrayList<>();
		if (aliasMng != null) {
			ITextAliasManager.MessageTag tag = new ITextAliasManager.MessageTag(ldr.getArcType(), ldr.getTextFileId(), 0);
			for (int i = 0; i < textTable.getRowCount(); i++) {
				tag.msgId = i;
				String alias = aliasMng.getMsgidAlias(tag);
				aliases.add(alias);
			}
		}
		return aliases;
	}

	private String getAliasOrDefault(int msgId) {
		String a = null;
		if (aliasMng != null) {
			ITextAliasManager.MessageTag tag = new ITextAliasManager.MessageTag(ldr.getArcType(), ldr.getTextFileId(), msgId);
			a = aliasMng.getMsgidAlias(tag);
		}
		if (a == null) {
			a = getDefaultAlias(msgId);
		}
		return a;
	}

	private String getDefaultAlias(int msgId) {
		return "MSG" + FormattingUtils.getIntWithLeadingZeros(4, msgId);
	}

	private boolean isDefaultAlias(int msgId) {
		if (aliasMng != null) {
			return aliasMng.getMsgidAlias(new ITextAliasManager.MessageTag(ldr.getArcType(), ldr.getTextFileId(), msgId)) == null;
		}
		return true;
	}

	private void updateMsgIds(int startIdx) {
		for (int i = startIdx; i < textTable.getRowCount(); i++) {
			textTable.setValueAt(i, i, COL_MSGID);
			if (isDefaultAlias(i)) {
				textTable.setValueAt(getDefaultAlias(i), i, COL_ALIAS);
			}
		}
	}

	private List<Integer> getSelectedRows() {
		int[] rows = textTable.getSelectedRows();
		List<Integer> l = new ArrayList<>();
		for (int i = 0; i < rows.length; i++) {
			l.add(rows[i]);
		}
		return l;
	}

	public final void initialize(AbstractTextLoader loader) {
		ldr = loader;
		textArcTypeBox.removeAllItems();
		for (ITextArcType at : ldr.getArcTypes()) {
			textArcTypeBox.addItem(at.friendlyName());
		}

		textTable.createDefaultColumnsFromModel();
		COL_MSGID = 0;
		COL_ALIAS = 1;
		if (!ldr.getMsgHandler().isMsgDataSupports9Bit()) {
			if (COL_WRITE9BIT != -1) {
				textTable.getColumnModel().removeColumn(textTable.getColumnModel().getColumn(write9BitColumn.getModelIndex()));
				//idk what fuckery there is in java that I can't remove it by the ptr, but have to get the index instead
				COL_WRITE9BIT = -1;
				COL_MSGSTR = 2;
			}
		} else {
			if (COL_WRITE9BIT != 2) {
				textTable.getColumnModel().addColumn(write9BitColumn);
				textTable.getColumnModel().moveColumn(write9BitColumn.getModelIndex(), 2);
				COL_WRITE9BIT = 2;
			}
			textTable.getColumnModel().getColumn(COL_WRITE9BIT).setMaxWidth(35);
			COL_MSGSTR = 3;
		}
		textTable.doLayout();
		textTable.getColumnModel().getColumn(COL_MSGID).setMaxWidth(100);
		textTable.getColumnModel().getColumn(COL_ALIAS).setPreferredWidth(150);
		textTable.getColumnModel().getColumn(COL_ALIAS).setMaxWidth(300);

		textArcTypeBox.setSelectedIndex(0);
	}

	private boolean loadTextData(ITextArcType type) {
		cancelEditing();
		if (ldr.setArcType(type)) {
			int max = ldr.getTextArcMax();
			textFileBox.makeComboBoxValuesInt(max);
			btnAddTextFile.setEnabled(ldr.checkCanExpandTextArc(type));
			return true;
		}
		return false;
	}

	public void forceMsgFileLoad(ITextArcType arcType, int fileIdx) {
		if (arcType.ordinal() < textArcTypeBox.getItemCount()) {
			int nowFileIdx = textFileBox.getValueCB();
			textArcTypeBox.setSelectedIndex(arcType.ordinal());
			boolean arcChange = loadTextData(arcType);
			if (arcChange || fileIdx != nowFileIdx) {
				forceMsgFileLoadFromArc(fileIdx);
			}
		}
	}

	private void forceMsgFileLoadFromArc(int fileIdx) {
		textFileBox.setValue(fileIdx);
		loadTextFileData(fileIdx);
	}

	public void save() {
		if (changed) {
			ldr.writeCurrentTextFile();
			changed = false;
		}
	}
	
	@Override
	public boolean store(boolean dialog) {
		if (changed) {
			int result = dialog ? DialogUtils.showSaveConfirmationDialog(cm, "The message bank") : JOptionPane.YES_OPTION;
			switch (result) {
				case JOptionPane.CANCEL_OPTION:
					return false;
				case JOptionPane.YES_OPTION:
					save();
				case JOptionPane.NO_OPTION:
					break;
			}
		}
		return true;
	}

	private void loadTextFileData(int fileNo) {
		cancelEditing();
		save();
		changed = false;
		loaded = false;
		if (fileNo >= 0 && fileNo < ldr.getTextArcMax()) {
			ldr.setTextFile(fileNo);

			List<MsgStr> ld = ldr.getMsgStrs();
			DefaultTableModel tblModel = getTableModel();
			tblModel.setRowCount(0);

			for (int i = 0; i < ld.size(); i++) {
				String alias = getAliasOrDefault(i);

				tblModel.addRow(new Object[]{
					i,
					alias,
					ldr.getMsgStrs().get(i).encode9Bit,
					TextFileFriendlizer.getFriendlized(ld.get(i).value)
				});
			}
		}
		loaded = true;
	}

	private DefaultTableModel getTableModel() {
		return (DefaultTableModel) textTable.getModel();
	}

	@Override
	public String getTabName() {
		return "Text Editor";
	}

	private void cancelEditing() {
		if (textTable != null) {
			CellEditor e = textTable.getCellEditor();
			if (e != null) {
				e.cancelCellEditing();
			}
		}
	}

	/**
	 * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this
	 * code. The content of this method is always regenerated by the Form Editor.
	 */
	@SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        textArcGroup = new javax.swing.ButtonGroup();
        fancySep = new javax.swing.JSeparator();
        textArcSep = new javax.swing.JSeparator();
        textFileLabel = new javax.swing.JLabel();
        textFileBox = new xstandard.gui.components.combobox.ComboBoxAndSpinner();
        tableSeparator = new javax.swing.JSeparator();
        textTableSP = new javax.swing.JScrollPane();
        textTable = new javax.swing.JTable();
        btnAddTextFile = new javax.swing.JButton();
        btnSave = new javax.swing.JButton();
        btnDumpAllText = new javax.swing.JButton();
        textArcTypeBox = new javax.swing.JComboBox<>();

        fancySep.setOrientation(javax.swing.SwingConstants.VERTICAL);

        textArcSep.setOrientation(javax.swing.SwingConstants.VERTICAL);

        textFileLabel.setText("Text file:");

        textFileBox.setAllowOutOfBoxValues(false);
        textFileBox.setMaximumRowCount(30);

        textTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "MSGID", "Alias", "9-bit", "Text"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Integer.class, java.lang.String.class, java.lang.Boolean.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                false, true, true, true
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        textTable.setRowHeight(22);
        textTable.getTableHeader().setReorderingAllowed(false);
        textTableSP.setViewportView(textTable);
        if (textTable.getColumnModel().getColumnCount() > 0) {
            textTable.getColumnModel().getColumn(0).setMaxWidth(100);
            textTable.getColumnModel().getColumn(1).setPreferredWidth(150);
            textTable.getColumnModel().getColumn(1).setMaxWidth(300);
            textTable.getColumnModel().getColumn(2).setMaxWidth(35);
        }

        btnAddTextFile.setText("+");
        btnAddTextFile.setToolTipText("Add text file");
        btnAddTextFile.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddTextFileActionPerformed(evt);
            }
        });

        btnSave.setText("Save");
        btnSave.setToolTipText("Commit changes to the current text file");
        btnSave.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSaveActionPerformed(evt);
            }
        });

        btnDumpAllText.setText("Dump all");
        btnDumpAllText.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDumpAllTextActionPerformed(evt);
            }
        });

        textArcTypeBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                textArcTypeBoxActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(tableSeparator)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(fancySep, javax.swing.GroupLayout.PREFERRED_SIZE, 6, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(textArcTypeBox, javax.swing.GroupLayout.PREFERRED_SIZE, 172, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(textArcSep, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(textFileLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(textFileBox, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnAddTextFile, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(textTableSP, javax.swing.GroupLayout.DEFAULT_SIZE, 643, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(btnDumpAllText)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(btnSave)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(11, 11, 11)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(textArcSep)
                    .addComponent(textFileBox, javax.swing.GroupLayout.DEFAULT_SIZE, 22, Short.MAX_VALUE)
                    .addComponent(textFileLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(btnAddTextFile, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                    .addComponent(fancySep)
                    .addComponent(textArcTypeBox))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(tableSeparator, javax.swing.GroupLayout.PREFERRED_SIZE, 5, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(textTableSP, javax.swing.GroupLayout.DEFAULT_SIZE, 333, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnSave)
                    .addComponent(btnDumpAllText))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void btnAddTextFileActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddTextFileActionPerformed
		if (ldr.checkCanExpandTextArc(ldr.getArcType())) {
			int no = ldr.getTextArcMax();
			ldr.setTextFile(no);
			ldr.writeCurrentTextFile();
			textFileBox.getCB().addItem(String.valueOf(no));
			forceMsgFileLoad(ldr.getArcType(), no);
		}
    }//GEN-LAST:event_btnAddTextFileActionPerformed

    private void btnSaveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSaveActionPerformed
		ldr.writeCurrentTextFile();
    }//GEN-LAST:event_btnSaveActionPerformed

    private void btnDumpAllTextActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDumpAllTextActionPerformed
		FSFile target = XFileDialog.openSaveFileDialog(CommonExtensionFilters.PLAIN_TEXT);

		if (target != null) {
			int max = ldr.getTextArcMax();

			PrintStream out = new PrintStream(target.getNativeOutputStream());

			for (int i = 0; i < max; i++) {
				out.println("----- TEXT FILE " + i + "-----");
				ldr.setTextFile(i);

				int line = 0;
				for (MsgStr str : ldr.getMsgStrs()) {
					out.print(line);
					out.print(": ");
					out.println(TextFileFriendlizer.getFriendlized(str.value));
					line++;
				}
				out.println();
			}

			out.close();
		}
    }//GEN-LAST:event_btnDumpAllTextActionPerformed

    private void textArcTypeBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_textArcTypeBoxActionPerformed
		if (ldr != null) {
			int idx = textArcTypeBox.getSelectedIndex();
			if (idx != -1) {
				ITextArcType newArcType = ldr.getArcTypes()[idx];
				if (!ldr.isArcTypeNSFW(newArcType) || DialogUtils.showYesNoWarningDialog(
					cm,
					"Things are about to get fr*aking wild",
					"Warning! You are about to edit the profanity check database.\n"
					+ "Just in case you haven't yet readied yourself for what's to come, well, strap yourself in:\n"
					+ "These text files may contain... shhh.... naughty words.\n"
					+ "Knowing that, do you still want to continue?")) {
					loadTextData(newArcType);
					forceMsgFileLoadFromArc(0);
				} else {
					textArcTypeBox.setSelectedIndex(0);
				}
			}
		}
    }//GEN-LAST:event_textArcTypeBoxActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAddTextFile;
    private javax.swing.JButton btnDumpAllText;
    private javax.swing.JButton btnSave;
    private javax.swing.JSeparator fancySep;
    private javax.swing.JSeparator tableSeparator;
    private javax.swing.ButtonGroup textArcGroup;
    private javax.swing.JSeparator textArcSep;
    private javax.swing.JComboBox<String> textArcTypeBox;
    private xstandard.gui.components.combobox.ComboBoxAndSpinner textFileBox;
    private javax.swing.JLabel textFileLabel;
    private javax.swing.JTable textTable;
    private javax.swing.JScrollPane textTableSP;
    // End of variables declaration//GEN-END:variables
}
