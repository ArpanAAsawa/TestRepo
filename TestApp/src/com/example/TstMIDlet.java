package com.example;

import java.util.Vector;

import javax.microedition.midlet.MIDlet;
import javax.microedition.midlet.MIDletStateChangeException;

import com.sun.lwuit.Display;
import com.sun.lwuit.Form;
import com.sun.lwuit.plaf.UIManager;
import com.sun.lwuit.tree.Tree;
import com.sun.lwuit.tree.TreeModel;
import com.sun.lwuit.util.Resources;

public class TstMIDlet extends MIDlet {

	public TstMIDlet() {
	}

	protected void destroyApp(boolean arg0) throws MIDletStateChangeException {
	}

	protected void pauseApp() {
	}

	protected void startApp() throws MIDletStateChangeException {
		Display.init(this);
		try {
			Resources r = Resources.open("/gidiTheme.res");
			UIManager.getInstance().setThemeProps(
					r.getTheme(r.getThemeResourceNames()[0]));
		} catch (java.io.IOException e) {

		}
		Form f = new Form();
		Tree t = new Tree(new FileTreeModel());
		f.addComponent(t);
		f.show();

	}
}

class FileTreeModel implements TreeModel {

	public Vector getChildren(Object parent) {
		Vector response = new Vector();
		if (parent == null) {
			response.addElement("One");
			response.addElement("Two");
			response.addElement("Three");
		} else {
			if (!isLeaf(parent)) {
				response.addElement("Child One");
				response.addElement("Child Two");
				response.addElement("Child Three");
			}
		}
		return response;
	}

	public boolean isLeaf(Object node) {
		return ((String) node).indexOf("Child") > -1;
	}
}