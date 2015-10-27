package com.zendesk.maxwell;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

public class RowMap extends HashMap<String, Object> {
	private final HashMap<String, Object> data;
	static final Logger LOGGER = LoggerFactory.getLogger(RowMap.class);

	public RowMap() {
		this.data = new HashMap<String, Object>();
		this.put("data", this.data);
	}

	public void setRowType(String type) {
		this.put("type", type);
	}

	public void putData(String key, Object value) {
		this.data.put(key,  value);
	}

	public void setTable(String name) {
		this.put("table", name);
	}

	public void setDatabase(String name) {
		this.put("database", name);
	}

	public void setTimestamp(Long l) {
		this.put("ts", l);
	}

	public void setXid(Long xid) {
		this.put("xid", xid);
	}

	public void setTXCommit() {
		this.put("commit", true);
	}

	public Object getData(String string) {
		return this.data.get(string);
	}

	private final static String[] keyOrder = {"database", "table", "type", "ts"};

	public void writeJSON(JsonGenerator g) throws IOException {
		g.writeStartObject(); // {

		for ( String key: keyOrder ) {
			g.writeObjectField(key, get(key)); // type: "insert"
		}

		if ( containsKey("xid") )
			g.writeObjectField("xid", get("xid"));

		if ( containsKey("commit") && (boolean) get("commit") == true)
			g.writeBooleanField("commit", true);

		g.writeObjectFieldStart("data");
		for ( String key: data.keySet() ) {
			Object data = getData(key);

			if ( data == null )
				continue;

			if ( data instanceof List ) { // sets come back from .asJSON as lists, and jackson can't deal.
				List<String> stringList = (List<String>) data;

				g.writeArrayFieldStart(key);
				for ( String s : stringList )  {
					g.writeString(s);
				}
				g.writeEndArray();
			} else {
				g.writeObjectField(key, data);
			}
		}
		g.writeEndObject(); // end of 'data: { }'
		g.writeEndObject();
	}
}
