package com.zendesk.maxwell.bootstrap;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import com.zendesk.maxwell.producer.AbstractProducer;
import com.zendesk.maxwell.replication.Replicator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zendesk.maxwell.util.StoppableTask;
import com.zendesk.maxwell.util.StoppableTaskState;
import snaq.db.ConnectionPool;

public class BootstrapPoller implements StoppableTask {
	static final Logger LOGGER = LoggerFactory.getLogger(BootstrapPoller.class);
	private final AbstractBootstrapper bootstrapper;
	private final ConnectionPool controlConnectionPool;
	private final Replicator replicator;
	private final AbstractProducer producer;

	protected volatile StoppableTaskState taskState;
	private final long pollInterval;
	private String sql = null;
	private Thread bootstrapPollerThread = null;
	private Map<Long, BootstrapTask> allTasks = new HashMap<>();

	public BootstrapPoller(
		AbstractBootstrapper bootstrapper,
		long pollInterval,
		ConnectionPool controlConnectionPool,
		Replicator replicator,
		AbstractProducer producer // TODO: move to bootstrapper
	) {
		this.bootstrapper = bootstrapper;
		this.pollInterval = pollInterval;
		this.sql = "select * from bootstrap;";
		this.controlConnectionPool = controlConnectionPool;
		this.taskState = new StoppableTaskState(this.getClass().getName());
		this.replicator = replicator;
		this.producer = producer;
	}

	public void ensureBootstrapPoller() {
		if (bootstrapPollerThread != null && bootstrapPollerThread.isAlive()) {
			return;
		}

		if (taskState.isRunning() == false) {
			// stopped by request. Do not restart again.
			return;
		}

		bootstrapPollerThread = new Thread(new Runnable() {
			@Override
			public void run() {
				poll();
			}
		});
		bootstrapPollerThread.start();
	}

	private void poll() {
		LOGGER.debug("begin Bootstrapper polling thread");
		try ( Connection schemaConnection = controlConnectionPool.getConnection() ){
			while (this.taskState.isRunning()) {
				Statement statement = schemaConnection.createStatement();
				ResultSet rs = statement.executeQuery(this.sql);

				while (rs.next()) {
					BootstrapTask task = new BootstrapTask();

					task.id = rs.getLong("id");
					task.database = rs.getString("database_name");
					task.table = rs.getString("table_name");
					task.whereClause = rs.getString("where_clause");
					task.complete = rs.getBoolean("is_complete");
					task.startedAt = rs.getTimestamp("started_at");
					task.completedAt = rs.getTimestamp("completed_at");

					checkEntry(task);
				}

				rs.close();
				statement.close();

				Thread.sleep(pollInterval);
			}
		} catch (InterruptedException e) {
			// ignored
		} catch (Exception e) {
			LOGGER.error(String.format("Bootstrap poller exited on error: %s", e.toString()));
		} finally {
			this.taskState.stopped();
		}
	}

	private void checkEntry(BootstrapTask task) throws Exception {
		BootstrapTask old = allTasks.get(task.id);

		if (old == null ) {
			if ( task.startedAt == null ) {
				LOGGER.info(String.format("being bootstrapping task: %s", task.logString()));
				bootstrapper.startBootstrap(task, producer, replicator);
			} else if ( task.completedAt == null ) {
				LOGGER.info(String.format("restarting bootstrapping task: %s", task.logString()));
				bootstrapper.startBootstrap(task, producer, replicator);
			}
		}

		allTasks.put(task.id, task);
	}

	@Override
	public void requestStop() {
		this.taskState.requestStop();

		if ( bootstrapPollerThread != null )
			bootstrapPollerThread.interrupt();
	}

	@Override
	public void awaitStop(Long timeout) throws TimeoutException {
		this.taskState.awaitStop(bootstrapPollerThread, timeout);
	}
}
