package foo;

import java.util.ArrayList;


import com.google.appengine.api.datastore.Key;


public class Petition {
	Key Key;
	String contenu;
	String titre;
	String owner;
	ArrayList<String> signataire;
	int nbSignataires;
	
	
	public String getContenu() {
		return contenu;
	}

	public void setContenu(String contenu) {
		this.contenu = contenu;
	}

	public String getParent() {
		return owner;
	}
	
	
	public void setParent(String parent) {
		this.owner = parent;
	}

	
}