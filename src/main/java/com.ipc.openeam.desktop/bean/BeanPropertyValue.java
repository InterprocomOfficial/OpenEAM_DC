package com.ipc.openeam.desktop.bean;


import javax.persistence.*;
import com.ipc.openeam.desktop.util.Utils;

@Entity
@Inheritance(strategy=InheritanceType.TABLE_PER_CLASS)
public abstract class BeanPropertyValue extends SystemBean {
	@Id
	@GeneratedValue(generator="bean_property_gen")
	@TableGenerator(name="bean_property_gen", table="eam_sequence",
		pkColumnName="name", valueColumnName="seq",
		pkColumnValue="bean_property", initialValue=1, allocationSize=1)
	private Long id;
	
	private String alnValue;
	private Double numericValue;
	private Boolean yornValue;
	
	public Long getId() {
		return id;
	}
	
	public void setId(Long id) {
		this.id = id;
	}
	
	public abstract BeanPropertyType getType();

	public Object getValue(EntityManager em) {

			if (getType().equals(BeanPropertyType.ALN)) {
				return getAlnValue();
			} else if (getType().equals(BeanPropertyType.YORN)) {
				return getYornValue();
			}  else {
				return getNumericValue();
			}
	}

	public boolean setValue(Object value) {

		if (getType().equals(BeanPropertyType.ALN)) {
			return setAlnValue(value);
		} else if (getType().equals(BeanPropertyType.NUMERIC) || getType().equals(BeanPropertyType.INTEGER)) {
			return setNumericValue(value);
		} else if (getType().equals(BeanPropertyType.YORN)) {
			return setYournValue(value);
		}  else {
			throw new IllegalArgumentException("Value of bean property should have domain");
		}
	}

	public boolean isValueSet() {
		if (getType() == null) {
			return false;
		} else
		return true;
	}


	public String getAlnValue() {
		return alnValue;
	}

	public boolean setAlnValue(String alnValue) {
		boolean changed = (this.alnValue == null) ? 
				!(alnValue == null) : !this.alnValue.equals(alnValue);
		this.alnValue = alnValue;
		return getId() == null || changed;
	}
	
	public boolean setAlnValue(Object value) {
		if (value instanceof String) {
			return setAlnValue((String) value);
		} else if (value == null) {
			return setAlnValue(null);
		} else {
			throw new IllegalArgumentException("Value of asset aln field should be String");
		}
	}

	public Double getNumericValue() {
		return numericValue;
	}

	public boolean setNumericValue(Double numericValue) {
		boolean changed = (this.numericValue == null) ? 
				!(numericValue == null) : !this.numericValue.equals(numericValue);
		this.numericValue = numericValue;
		return getId() == null || changed;
	}
	
	public boolean setNumericValue(Object value) {
		if (value instanceof Double) {
			return setNumericValue((Double) value);
		} else if (value instanceof Integer) {
			return setNumericValue(new Double((Integer) value));
		} else if (value instanceof Long) {
			return setNumericValue(new Double((Long) value));
		} else {
			try {
				if (value == null || value.equals("")) {
					return setNumericValue(null);
				} else {
					return setNumericValue(Double.parseDouble(((String) value).replace(",", ".")));
				}
			} catch (NumberFormatException e) {
				throw new IllegalArgumentException("Value of numeric propertie should be Long or Integer");
			}
		}
	}

	public Boolean getYornValue() {
		if (yornValue == null) {
			return false;
		}
		return yornValue;
	}

	public boolean setYornValue(Boolean yornValue) {
		boolean changed = (this.yornValue == null) ? 
				!(yornValue == null) : !this.yornValue.equals(yornValue);
		this.yornValue = yornValue;
		return getId() == null || changed;
	}
	
	public boolean setYournValue(Object value) {
		if (value instanceof Boolean) {
			return setYornValue((Boolean) value);
		} else if (value instanceof String) {
			if (value.equals("1")) {
				return setYornValue(true);
			} else if (value.equals("0")) {
				return setYornValue(false);
			} else {
				return setYornValue(Boolean.parseBoolean((String) value));
			}
		} else if (value == null) {
			return setYornValue(false);
		} else {
			throw new IllegalArgumentException("Value of yorn propertie should be Boolean");
		}
	}

	public String getValueAsString(EntityManager em) {
			if (getType().equals(BeanPropertyType.ALN)) {
				return getAlnValue();
			} else if (getType().equals(BeanPropertyType.NUMERIC)) {
				return (getNumericValue() != null) ? Utils.decimalFormat.format(getNumericValue()) : null;
			} else if (getType().equals(BeanPropertyType.INTEGER)) {
				return (getNumericValue() != null) ? Utils.integerFormat.format(getNumericValue()) : null;
			} else if (getType().equals(BeanPropertyType.YORN)) {
				if (getYornValue() != null && getYornValue()) {
					return "Да";
				} else {
					return "Нет";
				}
			}
			return null;
	}


}
