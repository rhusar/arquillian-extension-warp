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
package org.jboss.arquillian.warp.ftest.failure;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.junit5.ArquillianExtension;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.arquillian.warp.Activity;
import org.jboss.arquillian.warp.Inspection;
import org.jboss.arquillian.warp.Warp;
import org.jboss.arquillian.warp.WarpTest;
import org.jboss.arquillian.warp.exception.ServerWarpExecutionException;
import org.jboss.arquillian.warp.ftest.TestingServlet;
import org.jboss.arquillian.warp.servlet.BeforeServlet;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.openqa.selenium.WebDriver;

/**
 * @author Lukas Fryc
 */
@ExtendWith(ArquillianExtension.class)
@WarpTest
@RunAsClient
public class TestInspectionFailurePropagation {

    @Drone
    WebDriver browser;

    @ArquillianResource
    URL contextPath;

    @Deployment
    public static WebArchive createDeployment() {

        return ShrinkWrap
            .create(WebArchive.class, "test.war")
            .addClass(TestingServlet.class)
            .addAsWebResource(new File("src/main/webapp/index.html"))
            .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    @Test
    public void testAssertionErrorPropagation() {
        Assertions.assertThrows(AssertionError.class, () -> {
            Warp
                .initiate(new Activity() {
                    public void perform() {
                        browser.navigate().to(contextPath + "index.html");
                    }
                })
                .inspect(new Inspection() {
                             private static final long serialVersionUID = 1L;

                             @BeforeServlet
                             public void beforeServlet() {
                                 fail("AssertionError should be correctly handled and propagated to the client-side");
                             }
                         }
                );
        });
    }

    @Test
    public void testCheckedExceptionPropagation() {

        try {
            Warp
                .initiate(new Activity() {
                    public void perform() {
                        browser.navigate().to(contextPath + "index.html");
                    }
                })
                .inspect(new Inspection() {
                             private static final long serialVersionUID = 1L;

                             @BeforeServlet
                             public void beforeServlet() throws Exception {
                                 throw new IOException();
                             }
                         }
                );
        } catch (ServerWarpExecutionException e) {
            assertInstanceOf(IOException.class, e.getCause());
        }
    }

    @Test
    public void testRuntimeExceptionPropagation() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            Warp
                .initiate(new Activity() {
                    public void perform() {
                        browser.navigate().to(contextPath + "index.html");
                    }
                })
                .inspect(new Inspection() {
                             private static final long serialVersionUID = 1L;

                             @BeforeServlet
                             public void beforeServlet() {
                                 throw new IllegalArgumentException();
                             }
                         }
                );
        });
    }
}
