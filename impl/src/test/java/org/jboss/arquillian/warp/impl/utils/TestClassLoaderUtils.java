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
package org.jboss.arquillian.warp.impl.utils;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.Test;

public class TestClassLoaderUtils {

    @Test
    public void testBootstrapClassLoader() throws Exception {

        ClassLoader classLoader = ClassLoaderUtils.getBootstrapClassLoader();

        assertNotNull(classLoader, "bootstrap classloader should not be null");
        assertNull(classLoader.getParent(), "bootstrap classloader should not have parent");

        assertNotNull(classLoader.loadClass(String.class.getName()), "java.lang.String should be on bootstrap classpath");

        try {
            classLoader.loadClass(Test.class.getName());
            fail("@Test annotation should not be present on bootstrap classpath");
        } catch (ClassNotFoundException e) {
            // expected exception
        }
    }
}
