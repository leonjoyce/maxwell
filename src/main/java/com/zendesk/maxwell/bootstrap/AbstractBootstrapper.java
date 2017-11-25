package com.zendesk.maxwell.bootstrap;

import java.io.IOException;
import java.sql.SQLException;

import com.zendesk.maxwell.MaxwellContext;
import com.zendesk.maxwell.producer.AbstractProducer;
import com.zendesk.maxwell.replication.Replicator;
import com.zendesk.maxwell.row.RowMap;


public abstract class AbstractBootstrapper {

	protected MaxwellContext context;

	public AbstractBootstrapper(MaxwellContext context) {
		this.context = context;
	}

	abstract public boolean shouldSkip(RowMap row) throws SQLException, IOException;

	abstract public void startBootstrap(BootstrapTask task, AbstractProducer producer, Replicator replicator) throws Exception;

	public abstract boolean isRunning();
}
