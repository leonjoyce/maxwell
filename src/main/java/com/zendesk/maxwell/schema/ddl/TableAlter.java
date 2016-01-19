package com.zendesk.maxwell.schema.ddl;

import java.util.*;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonAppend;
import com.zendesk.maxwell.schema.Database;
import com.zendesk.maxwell.schema.Schema;
import com.zendesk.maxwell.schema.Table;

@JsonAppend(attrs = @JsonAppend.Attr(propName = "type", value = "table-alter"))

public class TableAlter extends SchemaChange {
	@JsonProperty("database")
	public String dbName;
	@JsonProperty("table")
	public String tableName;
	@JsonProperty("columns")
	public ArrayList<ColumnMod> columnMods;
	@JsonProperty("new_database")
	public String newDatabase;
	@JsonProperty("new_table")
	public String newTableName;

	public String convertCharset;
	public String defaultCharset;

	@JsonProperty("primary_keys")
	public List<String> pks;


	public TableAlter(String database, String tableName) {
		this.dbName = database;
		this.tableName = tableName;
		this.columnMods = new ArrayList<>();
	}

	@Override
	public String toString() {
		return "TableAlter<database: " + dbName + ", table:" + tableName + ">";
	}

	@Override
	public Schema apply(Schema originalSchema) throws SchemaSyncError {
		Schema newSchema = originalSchema.copy();

		Database database = newSchema.findDatabase(this.dbName);
		if ( database == null ) {
			throw new SchemaSyncError("Couldn't find database: " + this.dbName);
		}

		Table table = database.findTable(this.tableName);
		if ( table == null ) {
			throw new SchemaSyncError("Couldn't find table: " + this.dbName + "." + this.tableName);
		}


		if ( newTableName != null && newDatabase != null ) {
			Database destDB = newSchema.findDatabase(this.newDatabase);
			if ( destDB == null )
				throw new SchemaSyncError("Couldn't find database " + this.dbName);

			table.rename(newTableName);

			database.getTableList().remove(table);
			destDB.addTable(table);
		}

		for (ColumnMod mod : columnMods) {
			mod.apply(table);
		}

		if ( this.pks != null ) {
			table.setPKList(this.pks);
		}
		table.setDefaultColumnEncodings();

		return newSchema;
	}
}