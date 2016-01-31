package com.zendesk.maxwell.schema.ddl;

import java.util.ArrayList;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.zendesk.maxwell.schema.Database;
import com.zendesk.maxwell.schema.Schema;
import com.zendesk.maxwell.schema.Table;
import com.zendesk.maxwell.schema.columndef.ColumnDef;

public class TableCreate extends SchemaChange {
	public String database;
	public String table;
	public ArrayList<ColumnDef> columns;

	@JsonProperty("primary-key")
	public ArrayList<String> pks;
	public String encoding;

	@JsonProperty("like-db")
	public String likeDB;

	@JsonProperty("like-table")
	public String likeTable;

	@JsonProperty("if-not-exists")
	public boolean ifNotExists;

	public TableCreate() {
		this.ifNotExists = false;
	}

	public TableCreate (String dbName, String tableName, boolean ifNotExists) {
		this();
		this.database = dbName;
		this.table = tableName;
		this.ifNotExists = ifNotExists;
		this.columns = new ArrayList<>();
		this.pks = new ArrayList<>();
	}

	@Override
	public Schema apply(Schema originalSchema) throws SchemaSyncError {
		Schema newSchema = originalSchema.copy();

		Database d = newSchema.findDatabase(this.database);
		if ( d == null )
			throw new SchemaSyncError("Couldn't find database " + this.database);

		if ( likeDB != null && likeTable != null ) {
			applyCreateLike(newSchema, d);
		} else {
			Table existingTable = d.findTable(this.table);
			if (existingTable != null) {
				if (ifNotExists) {
					return originalSchema;
				} else {
					throw new SchemaSyncError("Unexpectedly asked to create existing table " + this.table);
				}
			}
			Table t = d.buildTable(this.table, this.encoding, this.columns, this.pks);
			t.setDefaultColumnEncodings();
		}

		return newSchema;
	}

	private void applyCreateLike(Schema newSchema, Database d) throws SchemaSyncError {
		Database sourceDB = newSchema.findDatabase(likeDB);

		if ( sourceDB == null )
			throw new SchemaSyncError("Couldn't find database " + likeDB);

		Table sourceTable = sourceDB.findTable(likeTable);
		if ( sourceTable == null )
			throw new SchemaSyncError("Couldn't find table " + likeDB + "." + likeTable);

		Table t = sourceTable.copy();
		t.rename(this.table);
		d.addTable(t);
	}
}
