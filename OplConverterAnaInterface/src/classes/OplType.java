package classes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

public class OplType {
	
	private String type;
	private int position;
	private ArrayList<OplTypeElement> elements;
	
	OplType(String type, int position, OplTypeElement element) {
		this.type = type;
		elements = new ArrayList<OplTypeElement>();
		
		this.setPosition(position);
		
		if (element != null) addElement(element);
	}
	
	OplType(String type, int position) {
		this(type, position, null);
	}
	
	OplType(String type) {
		this(type, -1);
	}
	
	OplType() {
		this("");
	}
	
	@Override
	public boolean equals(Object object) {
		if (object == null || !(object instanceof OplType)) return false;
		
		OplType type = (OplType) object;
		
		if (this.type.equals(type.getType())) {
			if (elements.equals(type.elements)) return true;
		}
		
		return false;
	}
	
	public void addElement(OplTypeElement element) {
		elements.add(element);
	}
	
	public void addElement() {
		addElement(new OplTypeElement());
	}
	
	public void removeElement(OplTypeElement elem) {
		elements.remove(elem.getId());
	}
	
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	
	public OplTypeElement getElement(int key) {
		return elements.get(key);
	}
	
	public ArrayList<OplTypeElement> getElements() {
		return elements;
	}

	public int getPosition() {
		return position;
	}

	public void setPosition(int position) {
		this.position = position;
	}
}