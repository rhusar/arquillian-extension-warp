/*
 * JBoss, Home of Professional Open Source
 * Copyright 2012, Red Hat Middleware LLC, and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.arquillian.warp.jsf.ftest.producer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.junit5.ArquillianExtension;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.arquillian.warp.Activity;
import org.jboss.arquillian.warp.Inspection;
import org.jboss.arquillian.warp.Warp;
import org.jboss.arquillian.warp.WarpTest;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.openqa.selenium.WebDriver;

@ExtendWith(ArquillianExtension.class)
@WarpTest
@RunAsClient
public class TestJSFResourceNotFound {

    @Drone
    WebDriver browser;

    @ArquillianResource
    URL contextPath;

    @Deployment
    public static WebArchive createDeployment() {
        return ShrinkWrap.create(WebArchive.class, "jsf-test.war")
            .addAsWebInfResource(new File("src/main/webapp/WEB-INF/faces-config.xml"))
            .addAsWebInfResource(new File("src/main/webapp/WEB-INF/beans.xml"))
            //The test for GlassFish will fail without "web.xml" with a "java.io.IOException: Server returned HTTP response code: 500 for URL: http://127.0.0.1:10186/jsf-test/faces/notExisting.xhtml"
            //instead of the expected "FileNotFoundException".
            //On other servers, it works as expected.
            .addAsWebInfResource(new File("src/main/webapp/WEB-INF/web.xml"));
    }

    @Test
    public void testNotFound() {
        Warp.initiate(new Activity() {

            @Override
            public void perform() {
                try {
                    URL url = new URL(contextPath, "faces/notExisting.xhtml");
                    URLConnection connection = url.openConnection();
                    connection.getInputStream();
                } catch (FileNotFoundException e) {
                    // the FNFE is expected
                } catch (IOException e) {
                    throw new IllegalStateException(e);
                }
            }
        }).inspect(new Inspection() {
            private static final long serialVersionUID = 1L;
        });
    }
}
