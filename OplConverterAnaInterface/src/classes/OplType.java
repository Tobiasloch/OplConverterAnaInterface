package classes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

public class OplType {
	
	private String type;
	private int position;
	private LinkedHashMap<Long, OplTypeElement> elements;
	private HashMap<Integer, OplTypeElement> order;
	
	OplType(String type, int position, OplTypeElement element) {
		this.type = type;
		elements = new LinkedHashMap<Long, OplTypeElement>();
		
		this.setPosition(position);
		order = new HashMap<Integer, OplTypeElement>();
		
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
	
	public void swapTypes(OplTypeElement a, OplTypeElement b) {
		order.put(b.getPosition(), a);
		order.put(a.getPosition(), b);
		
		int pos = b.getPosition();
		
		b.setPosition(a.getPosition());
		a.setPosition(pos);
	}

	public void swapTypes(int a, int b) {
		swapTypes(order.get(a), order.get(b));
	}
	
	public void addElement(OplTypeElement element) {
		elements.put(element.getId(), element);
		order.put(order.size(), element);
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
	
	public OplTypeElement getElement(long key) {
		return elements.get(key);
	}
	
	public ArrayList<OplTypeElement> getElements() {
		ArrayList<OplTypeElement> list = new ArrayList<OplTypeElement>();
		
		for (long key : elements.keySet()) list.add(elements.get(key));
		
		return list;
	}
	
	public LinkedHashMap<Long, OplTypeElement> getHash() {
		return elements;
	}

	public int getPosition() {
		return position;
	}

	public void setPosition(int position) {
		this.position = position;
	}
}