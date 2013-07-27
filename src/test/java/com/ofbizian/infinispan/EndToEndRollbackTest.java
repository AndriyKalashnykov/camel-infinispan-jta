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

import java.util.HashMap;
import java.util.Map;
import javax.sql.DataSource;

import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.AdviceWithRouteBuilder;
import org.apache.camel.model.ModelCamelContext;
import org.apache.camel.test.spring.CamelSpringJUnit4ClassRunner;
import org.infinispan.commons.api.BasicCacheContainer;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;

import static junit.framework.Assert.assertEquals;
import static org.jgroups.util.Util.assertTrue;

@RunWith(CamelSpringJUnit4ClassRunner.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@ContextConfiguration(locations = {"classpath:camelContext.xml"})
public class EndToEndRollbackTest {

    @Produce(uri = "direct:start")
    protected ProducerTemplate start;
    @Autowired
    private ModelCamelContext camelContext;
    @Autowired
    private BasicCacheContainer cacheContainer;
    @Autowired
    private DataSource dataSource;
    private JdbcTemplate jdbcTemplate;

    @Before
    public void setUp() throws Exception {
        camelContext.getRouteDefinition("createRoute").adviceWith(camelContext, new AdviceWithRouteBuilder() {
            @Override
            public void configure() throws Exception {
                replaceFromWith("direct:start");
            }
        });
        jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Test
    public void errorRollbacksTheTransaction() throws Exception {
        Map<String, Object> header = new HashMap<String, Object>();
        header.put("firstName", "test");
        header.put("lastName", "damn");

        boolean caught = false;
        try {
            start.requestBodyAndHeaders(null, header);
        } catch (Exception e) {
            caught = true;
        }
        assertTrue(caught);
        assertEquals(0, cacheContainer.getCache().size());
        assertEquals(0, jdbcTemplate.queryForInt("select count(*) from person"));
    }
}