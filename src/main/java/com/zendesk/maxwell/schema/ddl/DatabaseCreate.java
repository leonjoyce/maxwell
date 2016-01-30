package com.zendesk.maxwell.schema.ddl;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.zendesk.maxwell.schema.Database;
import com.zendesk.maxwell.schema.Schema;

public class DatabaseCreate extends SchemaChange {
	public String database;

	@JsonProperty(value = "if-not-exists")
	public boolean ifNotExists;
	public String encoding;

	public DatabaseCreate() { }  // for deserialization

	public DatabaseCreate(String dbName, boolean ifNotExists, String encoding) {
		this.database = dbName;
		this.ifNotExists = ifNotExists;
		this.encoding = encoding;
	}

	@Override
	public Schema apply(Schema originalSchema) throws SchemaSyncError {
		Database database = originalSchema.findDatabase(this.database);

		if ( database != null ) {
			if ( ifNotExists )
				return originalSchema;
			else
				throw new SchemaSyncError("Unexpectedly asked to create existing database " + this.database);
		}

		Schema newSchema = originalSchema.copy();

		String createEncoding;
		if ( encoding != null )
			createEncoding = encoding;
		else
			createEncoding = newSchema.getEncoding();

		database = new Database(this.database, createEncoding);
		newSchema.addDatabase(database);
		return newSchema;
	}

}
