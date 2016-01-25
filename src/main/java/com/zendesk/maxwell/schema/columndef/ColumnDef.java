package com.zendesk.maxwell.schema.columndef;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.DatabindContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonTypeIdResolver;
import com.fasterxml.jackson.databind.jsontype.impl.TypeIdResolverBase;

@JsonTypeInfo(use=JsonTypeInfo.Id.CUSTOM, include=JsonTypeInfo.As.PROPERTY, property="type")
@JsonTypeIdResolver( ColumnDefResolver.class )


public abstract class ColumnDef {
	protected String name;
	protected String type;

	public String encoding;

	@JsonProperty("enums")
	protected String[] enumValues;

	@JsonIgnore
	private int pos;

	public boolean signed;

	public ColumnDef() { }

	public ColumnDef(String name, String type, int pos) {
		this.name = name.toLowerCase();
		this.type = type;
		this.pos = pos;
		this.signed = false;
	}

	public abstract boolean matchesMysqlType(int type);
	public abstract String toSQL(Object value);

	public Object asJSON(Object value) {
		return value;
	}

	public ColumnDef copy() {
		return build(this.name, this.encoding, this.type, this.pos, this.signed, this.enumValues);
	}

	public static ColumnDef build(String name, String encoding, String type, int pos, boolean signed, String enumValues[]) {
		switch(type) {
		case "tinyint":
		case "smallint":
		case "mediumint":
		case "int":
			return new IntColumnDef(name, type, pos, signed);
		case "bigint":
			return new BigIntColumnDef(name, type, pos, signed);
		case "tinytext":
		case "text":
		case "mediumtext":
		case "longtext":
		case "varchar":
		case "char":
			return new StringColumnDef(name, type, pos, encoding);
		case "tinyblob":
		case "blob":
		case "mediumblob":
		case "longblob":
		case "binary":
		case "varbinary":
			return new StringColumnDef(name, type, pos, "binary");
		case "geometry":
		case "geometrycollection":
		case "linestring":
		case "multilinestring":
		case "multipoint":
		case "multipolygon":
		case "polygon":
		case "point":
			return new GeometryColumnDef(name, type, pos);
		case "float":
		case "double":
			return new FloatColumnDef(name, type, pos);
		case "decimal":
			return new DecimalColumnDef(name, type, pos);
		case "date":
			return new DateColumnDef(name, type, pos);
		case "datetime":
		case "timestamp":
			return new DateTimeColumnDef(name, type, pos);
		case "year":
			return new YearColumnDef(name, type, pos);
		case "time":
			return new TimeColumnDef(name, type, pos);
		case "enum":
			return new EnumColumnDef(name, type, pos, enumValues);
		case "set":
			return new SetColumnDef(name, type, pos, enumValues);
		case "bit":
			return new BitColumnDef(name, type, pos);
		default:
			throw new IllegalArgumentException("unsupported column type " + type);
		}
	}

	static private String charToByteType(String type) {
		switch (type) {
			case "char":
			case "character":
				return "binary";
			case "varchar":
			case "varying":
				return "varbinary";
			case "tinytext":
				return "tinyblob";
			case "text":
				return "blob";
			case "mediumtext":
				return "mediumblob";
			case "longtext":
				return "longblob";
			case "long":
				return "mediumblob";
			default:
				throw new RuntimeException("Unknown type with BYTE flag: " + type);
		}
	}

	static public String unalias_type(String type, boolean longStringFlag, Long columnLength, boolean byteFlagToStringColumn) {
		if ( byteFlagToStringColumn )
			type = charToByteType(type);

		if ( longStringFlag ) {
			switch (type) {
				case "varchar":
					return "mediumtext";
				case "varbinary":
					return "mediumblob";
				case "binary":
					return "mediumtext";
			}
		}

		switch(type) {
			case "character":
			case "nchar":
				return "char";
			case "text":
			case "blob":
				if ( columnLength == null )
					return type;

				if ( columnLength < (1 << 8) )
					return "tiny" + type;
				else if ( columnLength < ( 1 << 16) )
					return type;
				else if ( columnLength < ( 1 << 24) )
					return "medium" + type;
				else
					return "long" + type;
			case "nvarchar":
			case "varying":
				return "varchar";
			case "bool":
			case "boolean":
			case "int1":
				return "tinyint";
			case "int2":
				return "smallint";
			case "int3":
				return "mediumint";
			case "int4":
			case "integer":
				return "int";
			case "int8":
				return "bigint";
			case "real":
			case "numeric":
				return "double";
			case "long":
				return "mediumtext";
			default:
				return type;
		}
	}

	public String getName() {
		return name;
	}

	public String getType() {
		return type;
	}

	public int getPos() {
		return pos;
	}

	public void setPos(int i) {
		this.pos = i;
	}

	public String getEncoding() {
		return this.encoding;
	}

	public boolean getSigned() {
		return this.signed;
	}

	public String[] getEnumValues() {
		return enumValues;
	}
}

class ColumnDefResolver extends TypeIdResolverBase {
	@Override
	public String idFromValue(Object value) {
		return ((ColumnDef) value).getType();
	}

	@Override
	public String idFromValueAndType(Object value, Class<?> suggestedType) {
		return idFromValue(value);
	}

	@Override
	public JsonTypeInfo.Id getMechanism() {
		return JsonTypeInfo.Id.CUSTOM;
	}

	@Override
	public JavaType typeFromId(DatabindContext context, String id) {
		ObjectMapper m = new ObjectMapper();

		switch(id) {
			case "tinyint":
			case "smallint":
			case "mediumint":
			case "int":
				return m.constructType(IntColumnDef.class);
			case "bigint":
				return m.constructType(BigIntColumnDef.class);
			case "tinytext":
			case "text":
			case "mediumtext":
			case "longtext":
			case "varchar":
			case "char":
				return m.constructType(StringColumnDef.class);
			case "tinyblob":
			case "blob":
			case "mediumblob":
			case "longblob":
			case "binary":
			case "varbinary":
				return m.constructType(StringColumnDef.class);
			case "geometry":
			case "geometrycollection":
			case "linestring":
			case "multilinestring":
			case "multipoint":
			case "multipolygon":
			case "polygon":
			case "point":
				return m.constructType(GeometryColumnDef.class);
			case "float":
			case "double":
				return m.constructType(FloatColumnDef.class);
			case "decimal":
				return m.constructType(DecimalColumnDef.class);
			case "date":
				return m.constructType(DateColumnDef.class);
			case "datetime":
			case "timestamp":
				return m.constructType(DateTimeColumnDef.class);
			case "year":
				return m.constructType(YearColumnDef.class);
			case "time":
				return m.constructType(TimeColumnDef.class);
			case "enum":
				return m.constructType(EnumColumnDef.class);
			case "set":
				return m.constructType(SetColumnDef.class);
			case "bit":
				return m.constructType(BitColumnDef.class);
			default:
				throw new IllegalArgumentException("unsupported column type " + id);
		}
	}
}
