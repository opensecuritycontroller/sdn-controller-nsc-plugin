/*******************************************************************************
 * Copyright (c) Intel Corporation
 * Copyright (c) 2017
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package org.osc.controller.nsc.restserver.api;

import static java.util.Collections.singletonMap;
import static org.osgi.service.jdbc.DataSourceFactory.*;

import java.sql.SQLException;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import javax.persistence.EntityManager;
import javax.sql.DataSource;

import org.osc.controller.nsc.api.SampleSdnRedirectionApi;
import org.osgi.service.jdbc.DataSourceFactory;
import org.osgi.service.jpa.EntityManagerFactoryBuilder;
import org.osgi.service.transaction.control.TransactionControl;
import org.osgi.service.transaction.control.jpa.JPAEntityManagerProviderFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RestServerApiUtils {

    private static Logger LOG = LoggerFactory.getLogger(RestServerApiUtils.class);

    private static ConcurrentHashMap<String, SampleSdnRedirectionApi> sdnApiHashMap = new ConcurrentHashMap<String, SampleSdnRedirectionApi>();

    public static void insertSdnApi(String id, SampleSdnRedirectionApi value) {
        sdnApiHashMap.putIfAbsent(id, value);
    }

    public static SampleSdnRedirectionApi getSdnApi(String id) {
        return sdnApiHashMap.get(id);
    }

    public static EntityManager createEntityHandle(JPAEntityManagerProviderFactory resourceFactory,
            TransactionControl txControl, EntityManagerFactoryBuilder builder, DataSourceFactory jdbcFactory,
            String id) {

        Properties props = new Properties();
        props.setProperty(JDBC_URL,
                "jdbc:h2:./nscPlugin_" + id + ";MVCC\\=TRUE;LOCK_TIMEOUT\\=10000;MV_STORE=FALSE;");
        props.setProperty(JDBC_USER, "admin");
        props.setProperty(JDBC_PASSWORD, "admin123");
        DataSource ds = null;
        try {
            ds = jdbcFactory.createDataSource(props);
        } catch (SQLException error) {
            LOG.error(error.getMessage(), error);
            throw new IllegalStateException(error.getMessage(), error);
        }

        return resourceFactory
                .getProviderFor(builder, singletonMap("javax.persistence.nonJtaDataSource", (Object) ds), null)
                .getResource(txControl);
    }
}
