package classes;

public class OplTypeElement implements Comparable<OplTypeElement> {
	private String name;
	private long id;
	private String value;
	private OplType type;
	
	private int position;
	
	OplTypeElement(String name, long id) {
		this.name = name;
		this.id = id;
	}
	
	OplTypeElement() {
		this("", 0);
	}
	
	@Override
	public boolean equals(Object object) {
		if (object == null || !(object instanceof OplTypeElement)) return false;
		
		OplTypeElement element = (OplTypeElement) object;
		
		if (name.equals(element.name) && id == element.id && value == element.value) {
			return true;
		}
		
		return false;
	}
	
	@Override
	public int hashCode() {
		int code =  name.hashCode() + (int)id + value.hashCode();
		
		return code;
	}
	
	@Override
	public int compareTo(OplTypeElement element) {
		return getPosition() - ((OplTypeElement) element).getPosition();
	}
	
	@Override
	public String toString() {
		return name;
	}
	
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}

	public OplType getType() {
		return type;
	}

	public void setType(OplType type) {
		this.type = type;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public int getPosition() {
		return position;
	}

	public void setPosition(int order) {
		this.position = order;
	}
}