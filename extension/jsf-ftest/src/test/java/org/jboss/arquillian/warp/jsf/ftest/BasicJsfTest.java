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
package org.jboss.arquillian.warp.jsf.ftest;

import static org.jboss.arquillian.warp.jsf.Phase.RENDER_RESPONSE;
import static org.jboss.arquillian.warp.jsf.Phase.UPDATE_MODEL_VALUES;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.time.Duration;
import java.util.function.Function;

import jakarta.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.junit5.ArquillianExtension;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.arquillian.warp.Activity;
import org.jboss.arquillian.warp.Inspection;
import org.jboss.arquillian.warp.Warp;
import org.jboss.arquillian.warp.WarpTest;
import org.jboss.arquillian.warp.jsf.AfterPhase;
import org.jboss.arquillian.warp.jsf.BeforePhase;
import org.jboss.arquillian.warp.jsf.ftest.cdi.CdiBean;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.openqa.selenium.By;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;

@ExtendWith(ArquillianExtension.class)
@WarpTest
@RunAsClient
public class BasicJsfTest {

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

    /**Exclude this test for TomEE as long as the HtmlUnit issue is not fixed,
     * see https://github.com/arquillian/arquillian-extension-warp/issues/242  */
    @BeforeAll
    public static void beforeClass() throws IOException, InterruptedException {
       String tomEEHome = (String) System.getProperty("tomee.home");
       Assumptions.assumeTrue(tomEEHome == null || tomEEHome.length() == 0);
    }

    @Test
    public void test() {
        Warp
            .initiate(new Activity() {
                public void perform() {
                    browser.navigate().to(contextPath + "index.jsf");
                }
            })
            .inspect(new Inspection() {
                         private static final long serialVersionUID = 1L;

                         @Inject
                         CdiBean myBean;

                         @AfterPhase(RENDER_RESPONSE)
                         public void initial_state_havent_changed_yet() {
                             assertEquals("John", myBean.getName());
                         }
                     }
            );

        Warp
            .initiate(new Activity() {
                public void perform() {
                    WebElement nameInput = browser.findElement(By.id("helloWorldJsf:nameInput"));
                    nameInput.sendKeys("X");
                    browser.findElement(By.tagName("body")).click();
                }
            })
            .inspect(new Inspection() {
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

                         @SuppressWarnings("unused")
                         public String getUpdatedName() {
                             return updatedName;
                         }
                     }
            );

        new WebDriverWait(browser, Duration.ofSeconds(5)).until(new Function<WebDriver, Boolean>() {
            @Override
            public Boolean apply(WebDriver browser) {
                WebElement output = browser.findElement(By.id("helloWorldJsf:output"));
                try {
                    return output.getText().contains("JohnX");
                } catch (StaleElementReferenceException e) {
                    return false;
                }
            }
        });
    }
}
