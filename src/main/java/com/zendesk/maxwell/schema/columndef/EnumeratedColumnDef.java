package com.zendesk.maxwell.schema.columndef;

import com.fasterxml.jackson.annotation.JsonProperty;

abstract public class EnumeratedColumnDef extends ColumnDef  {
	@JsonProperty("enum_values")
	protected String[] enumValues;

	public String[] getEnumValues() {
		return enumValues;
	}

	public void setEnumValues(String[] v) {
		enumValues = v;
	}
}
