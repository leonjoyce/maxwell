package com.zendesk.maxwell.bootstrap;

import com.zendesk.maxwell.replication.BinlogPosition;

import java.sql.Timestamp;

/**
 * Created by ben on 7/28/17.
 */
public class BootstrapTask {
	public String database;
	public String table;
	public String whereClause;
	public Long id;
	public BinlogPosition startPosition;
	public boolean complete;
	public Timestamp startedAt;
	public Timestamp completedAt;

	public volatile boolean abort;

	public String logString() {
		String s = String.format("#%d %s.%s", id, database, table);
		if ( whereClause != null )
			s += " WHERE " + whereClause;
		return s;
	}
}
