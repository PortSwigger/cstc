package de.usd.cstchef.view; 

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.SortedMap;
import java.util.TreeMap; 

import javax.swing.AbstractCellEditor;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn; 

import burp.api.montoya.core.ByteArray;
import de.usd.cstchef.VariableStore;
import de.usd.cstchef.operations.Operation; 

public class VariablesWindow extends JFrame {

    private static VariablesWindow instance;
    private static ImageIcon deleteVariableIcon = new ImageIcon(Operation.class.getResource("/delete-variable.png"));

    public static VariablesWindow getInstance() {
        if (VariablesWindow.instance == null) {
            VariablesWindow.instance = new VariablesWindow();
        }
        return VariablesWindow.instance;
    }

    private JLabel emptyLbl;
    private JTable table;

    private VariablesWindow() {
        super("Variables");
        this.setSize(new Dimension(600, 480));

        // Added third column for the Delete button
        DefaultTableModel model = new DefaultTableModel(new String[] { "Variable Name", "Content", "Delete" }, 0);
        this.table = new JTable(model) {
            // Only the delete column should be editable so its button can be clicked
            public boolean isCellEditable(int row, int column) {
                return column == 2;
            };
        };

        this.addComponentListener(new ComponentAdapter() {
            public void componentResized(ComponentEvent e) {
                if (table.getModel().getRowCount() == 0) {
                    setColumnWidth(new Dimension());
                }
            }
        });

        this.table.setLayout(new GridBagLayout());
        this.table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        this.table.getColumnModel().getColumn(0).setPreferredWidth(200);
        this.table.getColumnModel().getColumn(1).setCellRenderer(new WordWrapCellRenderer());

        // Setup the Delete button column renderer and editor
        TableColumn deleteColumn = this.table.getColumnModel().getColumn(2);
        deleteColumn.setCellRenderer(new ButtonRenderer());
        deleteColumn.setCellEditor(new ButtonEditor(this.table));
        deleteColumn.setPreferredWidth(80);

        this.table.getTableHeader().setReorderingAllowed(false);
        this.table.getTableHeader().setResizingAllowed(false);
        this.table.setFillsViewportHeight(true);
        ((DefaultTableCellRenderer) table.getTableHeader().getDefaultRenderer()).setHorizontalAlignment(JLabel.LEFT);

        this.emptyLbl = new JLabel("no variables defined");
        this.table.add(this.emptyLbl);

        JScrollPane scrollPane = new JScrollPane(this.table);
        this.add(scrollPane);
    }

    public void refresh(TreeMap<String, ByteArray> variables) {
        DefaultTableModel model = (DefaultTableModel) this.table.getModel();
        model.setRowCount(0);
        this.emptyLbl.setVisible(variables.isEmpty());
        SortedMap<String, ByteArray> sortedMap = new TreeMap<String, ByteArray>(variables);

        for (String key : sortedMap.keySet()) {
            // Third column holds the button label; actual button rendering/editor handles actions
            model.addRow(new Object[] { key, sortedMap.get(key).toString(), deleteVariableIcon });
        }
    }

    private void setColumnWidth(Dimension preferredSize) {
        TableColumn contentColumn = this.table.getColumnModel().getColumn(1);
        int parentWidth = 0;
        if (this.table.getParent() != null) {
            parentWidth = this.table.getParent().getWidth();
        }
        int width = Integer.max(preferredSize.width + WordWrapCellRenderer.MARGIN,
            parentWidth - this.table.getColumnModel().getColumn(0).getWidth() - this.table.getColumnModel().getColumn(2).getWidth());
        contentColumn.setPreferredWidth(Math.max(width, 50));
    }

    // Button renderer for Delete column
    private class ButtonRenderer extends JButton implements TableCellRenderer {
        public ButtonRenderer() {
            setIcon(deleteVariableIcon);
            //setText("Delete");
            setOpaque(true);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
            boolean hasFocus, int row, int column) {
            return this;
        }
    }

    // Button editor for Delete column
    private class ButtonEditor extends AbstractCellEditor implements TableCellEditor, ActionListener {
        private final JButton button;
        private final JTable tableRef;
        private int currentRow = -1;

        public ButtonEditor(JTable table) {
            this.tableRef = table;
            this.button = new JButton(deleteVariableIcon);
            this.button.addActionListener(this);
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row,
            int column) {
            this.currentRow = row;
            return this.button;
        }

        @Override
        public Object getCellEditorValue() {
            return "Delete";
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            // Stop editing first to commit changes and close editor
            stopCellEditing();
            DefaultTableModel model = (DefaultTableModel) tableRef.getModel();
            if (currentRow >= 0 && currentRow < model.getRowCount()) {
                // Remove variable from variable store
                VariableStore.getInstance().removeVariable((String) model.getValueAt(currentRow, 0));
                model.removeRow(currentRow);
                // Update empty label visibility if needed
                emptyLbl.setVisible(model.getRowCount() == 0);
            }
        }
    }

    class WordWrapCellRenderer extends JTextArea implements TableCellRenderer {
        private static final int MARGIN = 20;

        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
            boolean hasFocus, int row, int column) {
            // set text first then compute preferred size
            setText(value == null ? "" : value.toString());
            Dimension preferredSize = getPreferredSize();
            setSize(preferredSize.width, getPreferredSize().height);
            table.setRowHeight(30);
            table.setRowMargin(5);
            setColumnWidth(preferredSize);
            return this;
        }
    }
} 