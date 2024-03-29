/*
 * Copyright (c) 2008, 2010, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores
 * CA 94065 USA or visit www.oracle.com if you need additional information or
 * have any questions.
 */
package com.sun.lwuit.list;

import java.util.Vector;

import com.sun.lwuit.Component;
import com.sun.lwuit.Container;
import com.sun.lwuit.Display;
import com.sun.lwuit.Graphics;
import com.sun.lwuit.events.ActionEvent;
import com.sun.lwuit.events.ActionListener;
import com.sun.lwuit.events.DataChangedListener;
import com.sun.lwuit.events.SelectionListener;
import com.sun.lwuit.geom.Dimension;
import com.sun.lwuit.layouts.Layout;
import com.sun.lwuit.util.EventDispatcher;

/**
 * This is a "list component" implemented as a container with a layout manager
 * which provides <b>some</b> of the ui advantages of a Container and some of a
 * list while pulling out some of the drawbacks of both.
 * This container uses the model/renderer approach for populating itself, adding/removing
 * entries will probably break it. It still provides most of the large size advantages
 * a list offers since the components within it are very simple and don't contain any
 * actual state other than layout information. The big advantage with this class is
 * the ability to leverage elaborate LWUIT layouts such as Grid, Table &amp; flow layout
 * to provide other ways of rendering the content of a list model.
 *
 * @author Shai Almog
 */
public class ContainerList extends Container {
    private CellRenderer renderer = new DefaultListCellRenderer();
    private ListModel model;
    private Listeners listener;
    private EventDispatcher dispatcher = new EventDispatcher();

    /**
     * Default constructor
     */
    public ContainerList() {
        this(new DefaultListModel());
    }

    /**
     * Constructs a container list with the given model
     *
     * @param m the model
     */
    public ContainerList(ListModel m) {
        init(m);
    }

    private void init(ListModel m) {
        setModel(m);
        setUIID("ContainerList");
        setScrollableY(true);
    }

    /**
     * Constructs a container list with the given model and layout
     *
     * @param l layout manager
     * @param m the model
     */
    public ContainerList(Layout l, ListModel m) {
        super(l);
        init(m);
    }

    /**
     * The renderer used to draw the container list elements
     * 
     * @param r renderer instance
     */
    public void setRenderer(CellRenderer r) {
        renderer = r;
        repaint();
    }

    /**
     * The renderer used to draw the container list elements
     */
    public CellRenderer getRenderer() {
        return renderer;
    }

    private void updateComponentCount() {
        int cc = getComponentCount();
        int modelCount = model.getSize();
        if(cc != modelCount) {
            if(cc < modelCount) {
                for(int iter = cc ; iter < modelCount ; iter++) {
                    addComponent(new Entry(iter));
                }
            } else {
                while(getComponentCount() > modelCount) {
                    removeComponent(getComponentAt(getComponentCount() - 1));
                }
            }
        }
    }

    /**
     * Returns the list model
     * 
     * @return the list model
     */
    public ListModel getModel() {
        return model;
    }

    /**
     * Allows binding a listener to user selection actions
     *
     * @param l the action listener to be added
     */
    public void addActionListener(ActionListener l) {
        dispatcher.addListener(l);
    }

    /**
     * This method allows extracting the action listeners from the current list
     *
     * @return vector containing the action listeners on the list
     */
    public Vector getActionListeners() {
        return dispatcher.getListenerVector();
    }

    /**
     * Allows binding a listener to user selection actions
     *
     * @param l the action listener to be removed
     */
    public void removeActionListener(ActionListener l) {
        dispatcher.removeListener(l);
    }

    /**
     * @inheritDoc
     */
    protected void initComponent() {
        if(model != null) {
            int i = model.getSelectedIndex();
            if(i > 0) {
                getComponentAt(i).requestFocus();
            }
        }
    }
    
    /**
     * Set the model for the container list
     *
     * @param model a model class that is mapped into the internal components
     */
    public void setModel(ListModel model) {
        if (this.model != null) {
            this.model.removeDataChangedListener(listener);
            this.model.removeSelectionListener(listener);
            listener = null;
        }
        this.model = model;
        updateComponentCount();
        if(model.getSelectedIndex() > 0) {
            getComponentAt(model.getSelectedIndex()).requestFocus();
        }
        if (isInitialized()) {
            bindListeners();
        }
        repaint();
    }

    private void bindListeners() {
        if (listener == null) {
            listener = new Listeners();
            model.addDataChangedListener(listener);
            model.addSelectionListener(listener);
        }
    }

    /**
     * Returns the current/last selected item
     * @return selected item or null
     */
    public Object getSelectedItem() {
        int i = model.getSelectedIndex();
        if(i > -1) {
            return model.getItemAt(i);
        }
        return null;
    }


    /**
     * @inheritDoc
     */
    public String[] getPropertyNames() {
        return new String[] {"ListItems", "Renderer"};
    }

    /**
     * @inheritDoc
     */
    public Class[] getPropertyTypes() {
       return new Class[] {Object[].class, CellRenderer.class};
    }

    /**
     * @inheritDoc
     */
    public Object getPropertyValue(String name) {
        if(name.equals("ListItems")) {
            Object[] obj = new Object[model.getSize()];
            for(int iter = 0 ; iter < obj.length ; iter++) {
                obj[iter] = model.getItemAt(iter);
            }
            return obj;
        }
        if(name.equals("Renderer")) {
            return getRenderer();
        }
        return null;
    }

    /**
     * @inheritDoc
     */
    public String setPropertyValue(String name, Object value) {
        if(name.equals("ListItems")) {
            setModel(new DefaultListModel((Object[])value));
            return null;
        }
        if(name.equals("Renderer")) {
            setRenderer((CellRenderer)value);
            return null;
        }
        return super.setPropertyValue(name, value);
    }


    /**
     * This class is an internal implementation detail
     */
    class Entry extends Component {
        private int offset;

        Entry(int off) {
            setFocusable(true);
            offset = off;
        }

        public void initComponent() {
            offset = getParent().getComponentIndex(this);
        }

        protected void focusGained() {
            model.setSelectedIndex(offset);
        }

        public void paintBackground(Graphics g) {
        }
        
        public void paintBorder(Graphics g) {
        }

        public void paint(Graphics g) {
            Component cmp = renderer.getCellRendererComponent(ContainerList.this, model, model.getItemAt(offset), offset, hasFocus());
            cmp.setX(getX());
            cmp.setY(getY());
            cmp.setWidth(getWidth());
            cmp.setHeight(getHeight());
            cmp.paintComponent(g);
        }

        /**
         * @inheritDoc
         */
        public void pointerReleased(int x, int y) {
            super.pointerReleased(x, y);
            if (!isDragActivated()) {
                dispatcher.fireActionEvent(new ActionEvent(ContainerList.this, x, y));
            }
        }

        /**
         * @inheritDoc
         */
        public void keyReleased(int keyCode) {
            super.keyReleased(keyCode);
            if(Display.getInstance().getGameAction(keyCode) == Display.GAME_FIRE) {
                dispatcher.fireActionEvent(new ActionEvent(ContainerList.this, keyCode));
            }
        }
        
        public Dimension calcPreferredSize() {
            return renderer.getCellRendererComponent(ContainerList.this, model, model.getItemAt(offset), offset, hasFocus()).getPreferredSize();
        }
    }

    private class Listeners implements DataChangedListener, SelectionListener {

        public void dataChanged(int status, int index) {
            updateComponentCount();
        }

        public void selectionChanged(int oldSelected, int newSelected) {
            getComponentAt(newSelected).requestFocus();
        }
    }
}
