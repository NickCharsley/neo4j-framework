/*
 * Copyright (c) 2013 GraphAware
 *
 * This file is part of GraphAware.
 *
 * GraphAware is free software: you can redistribute it and/or modify it under the terms of
 * the GNU General Public License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details. You should have received a copy of
 * the GNU General Public License along with this program.  If not, see
 * <http://www.gnu.org/licenses/>.
 */

package com.graphaware.runtime;

import com.graphaware.runtime.config.DefaultRuntimeConfiguration;
import com.graphaware.runtime.manager.BatchModuleManager;
import com.graphaware.runtime.manager.ProductionTransactionDrivenModuleManager;
import com.graphaware.runtime.metadata.BatchSingleNodeModuleMetadataRepository;
import com.graphaware.tx.event.batch.api.TransactionSimulatingBatchInserter;
import org.apache.log4j.Logger;
import org.neo4j.graphdb.Lock;
import org.neo4j.graphdb.PropertyContainer;
import org.neo4j.graphdb.Transaction;


/**
 * {@link GraphAwareRuntime} that operates on a {@link org.neo4j.unsafe.batchinsert.BatchInserter}
 * (or more precisely {@link TransactionSimulatingBatchInserter}) rather than {@link org.neo4j.graphdb.GraphDatabaseService}.
 *
 * @see BaseGraphAwareRuntime
 * @see org.neo4j.unsafe.batchinsert.BatchInserter - same limitations apply.
 */
public class BatchGraphAwareRuntime extends BaseGraphAwareRuntime {
    private static final Logger LOG = Logger.getLogger(BatchGraphAwareRuntime.class);

    private final TransactionSimulatingBatchInserter batchInserter;

    /**
     * Create a new instance of the runtime.
     *
     * @param batchInserter that the runtime should use.
     */
    public BatchGraphAwareRuntime(TransactionSimulatingBatchInserter batchInserter) {
        super(new BatchModuleManager(new BatchSingleNodeModuleMetadataRepository(batchInserter, DefaultRuntimeConfiguration.getInstance()), batchInserter));
        this.batchInserter = batchInserter;
        registerSelfAsHandler();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void registerSelfAsHandler() {
        batchInserter.registerTransactionEventHandler(this);
        batchInserter.registerKernelEventHandler(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean databaseAvailable() {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Transaction startTransaction() {
        return new Transaction() {
            @Override
            public void failure() {
                //intentionally do nothing, this is a fake tx
            }

            @Override
            public void success() {
                //intentionally do nothing, this is a fake tx
            }

            @Override
            public void finish() {
                //intentionally do nothing, this is a fake tx
            }

            @Override
            public void close() {
                //intentionally do nothing, this is a fake tx
            }

            @Override
            public Lock acquireWriteLock(PropertyContainer entity) {
                throw new UnsupportedOperationException("Fake tx!");
            }

            @Override
            public Lock acquireReadLock(PropertyContainer entity) {
                throw new UnsupportedOperationException("Fake tx!");
            }
        };
    }
}
