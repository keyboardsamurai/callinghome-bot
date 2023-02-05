package com.keyboardsamurais.apps.jobs;

import com.keyboardsamurais.apps.db.DatabaseManager;

/**
 * @author Antonio Agudo  on 29.01.23
 */
public class DatabaseCleanupJob implements Runnable {
    private final DatabaseManager databaseManager;

    public DatabaseCleanupJob(final DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    @Override
    public void run() {
        databaseManager.cleanup();
    }
}
