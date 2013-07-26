/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ofbizian.infinispan;

import java.sql.Connection;
import java.sql.SQLException;
import javax.transaction.TransactionManager;

import com.atomikos.jdbc.AtomikosDataSourceBean;
import org.apache.derby.jdbc.EmbeddedXADataSource;
import org.infinispan.commons.api.BasicCacheContainer;
import org.infinispan.configuration.cache.Configuration;
import org.infinispan.configuration.cache.ConfigurationBuilder;
import org.infinispan.configuration.global.GlobalConfiguration;
import org.infinispan.configuration.global.GlobalConfigurationBuilder;
import org.infinispan.manager.DefaultCacheManager;
import org.infinispan.transaction.TransactionMode;
import org.infinispan.transaction.lookup.TransactionManagerLookup;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.transaction.jta.JtaTransactionManager;

public class AtomikosJtaConfiguration {

    private JtaTransactionManager jtaTransactionManager;

    public AtomikosJtaConfiguration(JtaTransactionManager jtaTransactionManager) {
        this.jtaTransactionManager = jtaTransactionManager;
    }

    public BasicCacheContainer basicCacheContainer() throws Throwable {
        GlobalConfiguration glob = new GlobalConfigurationBuilder().nonClusteredDefault().build();
        Configuration loc = new ConfigurationBuilder()
                .transaction().transactionMode(TransactionMode.TRANSACTIONAL)
                .transactionManagerLookup(new TransactionManagerLookup() {
                    @Override
                    public TransactionManager getTransactionManager() throws Exception {
                        return jtaTransactionManager.getTransactionManager();
                    }
                }).build();

        return new DefaultCacheManager(glob, loc, true);
    }

    public AtomikosDataSourceBean atomikosDataSourceBean() throws SQLException {
        EmbeddedXADataSource ds = new EmbeddedXADataSource();
        ds.setCreateDatabase("create");
        ds.setDatabaseName("target/testdb");
        ds.setUser("");
        ds.setPassword("");

        Connection connection = null;
        try {
            connection = ds.getConnection();
            ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
            populator.addScript(new DefaultResourceLoader().getResource("sql/init.sql"));
            populator.populate(connection);

        } finally {
            if (connection != null) {
                connection.close();
            }
        }

        AtomikosDataSourceBean xaDataSource = new AtomikosDataSourceBean();
        xaDataSource.setXaDataSource(ds);
        xaDataSource.setUniqueResourceName("xaderby");
        return xaDataSource;
    }
}
