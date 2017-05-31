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

import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.CoreOptions;
import org.ops4j.pax.exam.ExamFactory;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;
import org.ops4j.pax.exam.testng.listener.PaxExam;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.testng.Assert;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;
import org.wso2.carbon.container.CarbonContainerFactory;
import org.wso2.carbon.datasource.core.api.DataSourceManagementService;
import org.wso2.carbon.datasource.core.beans.DataSourceMetadata;
import org.wso2.carbon.datasource.core.exception.DataSourceException;
import org.wso2.carbon.datasource.osgi.utils.OSGiTestUtils;
import org.wso2.carbon.kernel.utils.CarbonServerInfo;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import javax.inject.Inject;

import static org.ops4j.pax.exam.CoreOptions.maven;
import static org.ops4j.pax.exam.CoreOptions.mavenBundle;
import static org.wso2.carbon.container.options.CarbonDistributionOption.copyFile;
import static org.wso2.carbon.container.options.CarbonDistributionOption.copyOSGiLibBundle;

/**
 * Test class for {@link DataSourceManagementService}.
 */
@Listeners(PaxExam.class)
@ExamReactorStrategy(PerClass.class)
@ExamFactory(CarbonContainerFactory.class)
public class DataSourceManagementServiceTest {
    private static final String DATASOURCE_NAME = "WSO2_CARBON_DB";

    @Inject
    protected BundleContext bundleContext;

    @Inject
    private DataSourceManagementService dataSourceManagementService;

    @Inject
    private CarbonServerInfo carbonServerInfo;

    @Configuration
    public Option[] config() {
        System.setProperty("org.wso2.carbon.test.default.distribution",
                "org.wso2.carbon.datasources:wso2carbon-datasources-test");

        String basedir = System.getProperty("basedir");
        if (basedir == null) {
            basedir = Paths.get(".").toString();
        }

        String currentDir = Paths.get("").toAbsolutePath().toString();
        Path carbonHome = Paths.get(currentDir, "target", "carbon-home");
        System.setProperty("carbon.home", carbonHome.toString());


        Path ds = Paths.get("src", "test", "resources", "carbon-home",
                "conf", "datasources", "master-datasources.xml");
        Path log4j = Paths.get("src", "test", "resources", "carbon-home",
                "conf", "log4j2.xml");
        Path dest = Paths.get("conf", "datasources", "master-datasources.xml");
        Path destLog4j = Paths.get("conf", "log4j2.xml");

        ds = Paths.get(basedir).resolve(ds);

       // dest = Paths.get(System.getProperty("carbon.home")).resolve(dest);

        OSGiTestUtils.setEnv();
        Option[] options = CoreOptions.options(
                mavenBundle("org.ops4j.pax.jdbc", "pax-jdbc-config"),
                mavenBundle("com.h2database", "h2"),
                //mavenBundle(maven().groupId("com.h2database").artifactId("h2").version("1.4.191")),
                copyOSGiLibBundle(maven().groupId("com.zaxxer").artifactId("HikariCP").version("2.4.1")),
                copyFile(ds, dest),
                copyFile(log4j, destLog4j)


                //mavenBundle().artifactId("org.wso2.carbon.jndi").groupId("org.wso2.carbon.jndi").versionAsInProject(),
                //mavenBundle().artifactId("org.wso2.carbon.datasource.core").groupId("org.wso2.carbon.datasources")
                //        .versionAsInProject()
        );
        return OSGiTestUtils.getDefaultPaxOptions(options);
    }

    private Bundle getBundle(String name) {
        Bundle bundle = null;
        for (Bundle b : bundleContext.getBundles()) {
            if (b.getSymbolicName().equals(name)) {
                bundle = b;
                break;
            }
        }
        Assert.assertNotNull(bundle, "Bundle should be available. Name: " + name);
        return bundle;
    }

    @Test
    public void testDatasourcesCoreBundle() {
        Bundle coreBundle = getBundle("org.wso2.carbon.datasource.core");
        Assert.assertEquals(coreBundle.getState(), Bundle.ACTIVE);
    }


    @Test
    public void testDataSourceManagementServiceInject() {
        Assert.assertNotNull(dataSourceManagementService, "DataSourceManagementService not found");
    }

    @Test
    public void testGetDataSource() {
        try {
            List<DataSourceMetadata> list  = dataSourceManagementService.getDataSource();
            Assert.assertEquals(list.size(), 1, "Only one data source is registered");
        } catch (DataSourceException e) {
            Assert.fail("Thew DataSourceException when fetching data sources");
        }
    }

    @Test
    public void testGetDataSourceForName() {
        try {
            DataSourceMetadata dataSource  = dataSourceManagementService.getDataSource(DATASOURCE_NAME);
            Assert.assertNotNull(dataSource, "Data source " + DATASOURCE_NAME + " should exist");
        } catch (DataSourceException e) {
            Assert.fail("Thew DataSourceException when fetching data sources");
        }
    }

    @Test(dependsOnMethods = { "testGetDataSource", "testGetDataSourceForName" })
    public void testAddAndDeleteDataSource() {
        try {
            DataSourceMetadata dataSource  = dataSourceManagementService.getDataSource(DATASOURCE_NAME);
            Assert.assertNotNull(dataSource, "Data source " + DATASOURCE_NAME + " should exist");
            dataSourceManagementService.deleteDataSource(DATASOURCE_NAME);
            DataSourceMetadata dataSource2  = dataSourceManagementService.getDataSource(DATASOURCE_NAME);
            Assert.assertNull(dataSource2, "After deleting the data source should not exist");
            dataSourceManagementService.addDataSource(dataSource);
            dataSource2  = dataSourceManagementService.getDataSource(DATASOURCE_NAME);
            Assert.assertNotNull(dataSource2, "The service did not fetch the inserted data source!!!");
        } catch (DataSourceException e) {
            Assert.fail("Thew DataSourceException when fetching data sources");
        }

    }
}
