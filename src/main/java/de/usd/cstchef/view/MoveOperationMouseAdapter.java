package de.usd.cstchef.view;

import java.awt.Component;
import java.awt.Container;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;

import de.usd.cstchef.operations.Operation;

public class MoveOperationMouseAdapter extends OperationMouseAdapter {

    private JPopupMenu popupMenu;
    private RecipeStepPanel recipeStepPanel;

    public MoveOperationMouseAdapter(RecipeStepPanel source, Container target) {
        super(source.getOperationsPanel(), target);

        this.recipeStepPanel = source;

        popupMenu = new JPopupMenu();

        JMenuItem insertLeft = new JMenuItem("Insert lane on the left");
        JMenuItem insertRight = new JMenuItem("Insert lane on the right");
        JMenuItem deleteLane = new JMenuItem("Delete this lane");

        popupMenu.add(insertLeft);
        popupMenu.add(insertRight);
        popupMenu.add(deleteLane);

        insertLeft.addMouseListener(new MouseListener() {

            @Override
            public void mouseClicked(MouseEvent e) {
            }

            @Override
            public void mouseEntered(MouseEvent e) {
            }

            @Override
            public void mouseExited(MouseEvent e) {
            }

            @Override
            public void mousePressed(MouseEvent e) {
                recipeStepPanel.getRecipePanel().insertLaneAt(recipeStepPanel.getRecipePanel().getIndexOf(source));
            }

            @Override
            public void mouseReleased(MouseEvent e) {
            }
            
        });;

        insertRight.addMouseListener(new MouseListener() {

            @Override
            public void mouseClicked(MouseEvent e) {
            }

            @Override
            public void mouseEntered(MouseEvent e) {
            }

            @Override
            public void mouseExited(MouseEvent e) {
            }

            @Override
            public void mousePressed(MouseEvent e) {
                recipeStepPanel.getRecipePanel().insertLaneAt(recipeStepPanel.getRecipePanel().getIndexOf(source) + 1);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
            }
            
        });;

        deleteLane.addMouseListener(new MouseListener() {

            @Override
            public void mouseClicked(MouseEvent e) {
            }

            @Override
            public void mouseEntered(MouseEvent e) {
            }

            @Override
            public void mouseExited(MouseEvent e) {
            }

            @Override
            public void mousePressed(MouseEvent e) {
                recipeStepPanel.getRecipePanel().deleteLaneAt(recipeStepPanel.getRecipePanel().getIndexOf(source));
            }

            @Override
            public void mouseReleased(MouseEvent e) {
            }
            
        });;
    }

    @Override
    protected Operation getDraggedOperation(int x, int y) {
        Component comp = this.source.getComponentAt(x, y);
        comp.getParent().remove(comp);

        return comp instanceof Operation ? (Operation) comp : null;
    }


    @Override
    public void mouseClicked(MouseEvent e) {
        if(SwingUtilities.isRightMouseButton(e)) {
            popupMenu.show(e.getComponent(), e.getX(), e.getY());
        }
    }

}
