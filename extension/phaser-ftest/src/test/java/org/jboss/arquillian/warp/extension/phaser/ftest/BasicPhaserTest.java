/**
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
package org.jboss.arquillian.warp.extension.phaser.ftest;

import static org.jboss.arquillian.warp.extension.phaser.Phase.RENDER_RESPONSE;
import static org.jboss.arquillian.warp.extension.phaser.Phase.UPDATE_MODEL_VALUES;
import static org.junit.Assert.assertEquals;

import java.io.File;
import java.net.URL;

import javax.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.arquillian.warp.ClientAction;
import org.jboss.arquillian.warp.ServerAssertion;
import org.jboss.arquillian.warp.Warp;
import org.jboss.arquillian.warp.WarpTest;
import org.jboss.arquillian.warp.client.filter.HttpRequest;
import org.jboss.arquillian.warp.client.filter.RequestFilter;
import org.jboss.arquillian.warp.extension.phaser.AfterPhase;
import org.jboss.arquillian.warp.extension.phaser.BeforePhase;
import org.jboss.arquillian.warp.extension.phaser.ftest.cdi.CdiBean;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.By;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.google.common.base.Predicate;

@WarpTest
@RunWith(Arquillian.class)
public class BasicPhaserTest {

    @Drone
    WebDriver browser;

    @ArquillianResource
    URL contextPath;

    @Deployment
    public static WebArchive createDeployment() {

        return ShrinkWrap.create(WebArchive.class, "jsf-test.war").addClasses(CdiBean.class)
                .addAsWebResource(new File("src/main/webapp/index.xhtml"))
                .addAsWebInfResource(new File("src/main/webapp/WEB-INF/beans.xml"))
                .addAsWebResource(new File("src/main/webapp/templates/template.xhtml"), "templates/template.xhtml")
                .addAsWebInfResource(new File("src/main/webapp/WEB-INF/web.xml"))
                .addAsWebInfResource(new File("src/main/webapp/WEB-INF/faces-config.xml"));
    }

    @Test
    @RunAsClient
    public void test() {
        Warp.filter(new JsfRequestFilter()).execute(new ClientAction() {

            @Override
            public void action() {
                browser.navigate().to(contextPath + "index.jsf");
            }
        }).verify(new ServerAssertion() {
            private static final long serialVersionUID = 1L;

            @Inject
            CdiBean myBean;

            @AfterPhase(RENDER_RESPONSE)
            public void initial_state_havent_changed_yet() {
                assertEquals("John", myBean.getName());
            }
        });

        Warp.filter(new JsfRequestFilter()).execute(new ClientAction() {
            public void action() {
                WebElement nameInput = browser.findElement(By.id("helloWorldJsf:nameInput"));
                nameInput.sendKeys("X");

                new WebDriverWait(browser, 5).until(new Predicate<WebDriver>() {
                    @Override
                    public boolean apply(WebDriver browser) {
                        WebElement output = browser.findElement(By.id("helloWorldJsf:output"));
                        try {
                            return output.getText().contains("JohnX");
                        } catch (StaleElementReferenceException e) {
                            return false;
                        }
                    }
                });
            }
        }).verify(new ServerAssertion() {
            private static final long serialVersionUID = 1L;

            @Inject
            CdiBean myBean;

            private String updatedName;

            @BeforePhase(UPDATE_MODEL_VALUES)
            public void initial_state_havent_changed_yet() {
                assertEquals("John", myBean.getName());
            }

            @AfterPhase(UPDATE_MODEL_VALUES)
            public void changed_input_value_has_been_applied() {
                assertEquals("JohnX", myBean.getName());
                updatedName = myBean.getName();
            }

            public String getUpdatedName() {
                return updatedName;
            }
        });


    }

    public static class JsfRequestFilter implements RequestFilter<HttpRequest> {
        @Override
        public boolean matches(HttpRequest httpRequest) {
            return httpRequest.getUri().contains("index.jsf");
        }
    }
}
