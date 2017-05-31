/*
 *  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.wso2.carbon.datasource.osgi;

import org.ops4j.pax.exam.ExamFactory;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;
import org.ops4j.pax.exam.testng.listener.PaxExam;
import org.osgi.framework.BundleContext;
import org.testng.Assert;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;
import org.wso2.carbon.container.CarbonContainerFactory;
import org.wso2.carbon.datasource.core.api.DataSourceService;
import org.wso2.carbon.datasource.core.exception.DataSourceException;
import org.wso2.carbon.kernel.utils.CarbonServerInfo;

import javax.inject.Inject;

/**
 * Test class for {@link DataSourceService}.
 */
@Listeners(PaxExam.class)
@ExamReactorStrategy(PerClass.class)
@ExamFactory(CarbonContainerFactory.class)
public class DataSourceServiceTest {

    /*
    @Configuration
    public Option[] createConfiguration() {
        OSGiTestUtils.setEnv();

        Option[] options = CoreOptions.options(
                mavenBundle().artifactId("commons-io").groupId("commons-io.wso2").version("2.4.0.wso2v1"),
                mavenBundle().artifactId("HikariCP").groupId("com.zaxxer").version("2.4.1"),
                mavenBundle().artifactId("h2").groupId("com.h2database").version("1.4.191"),
                mavenBundle().artifactId("org.wso2.carbon.datasource.core").groupId("org.wso2.carbon.datasources")
                        .versionAsInProject(),
                mavenBundle().artifactId("org.wso2.carbon.jndi").groupId("org.wso2.carbon.jndi").versionAsInProject()
        );
        return OSGiTestUtils.getDefaultPaxOptions(options);
    }
    */

    @Inject
    private BundleContext bundleContext;

    @Inject
    private CarbonServerInfo carbonServerInfo;

    @Inject
    private DataSourceService dataSourceService;

    @Test
    public void testDataSourceServiceInject() {
        Assert.assertNotNull(dataSourceService, "DataSourceService not found");
    }

    @Test
    public void testGetAllDataSources() {
        try {
            Object obj = dataSourceService.getDataSource("WSO2_CARBON_DB");
            Assert.assertNotNull(obj, "WSO2_CARBON_DB should exist");
        } catch (DataSourceException e) {
            Assert.fail("Threw an exception while retrieving data sources");
        }
    }
}
