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
package org.jboss.arquillian.warp.impl.testutils;

import org.jboss.arquillian.warp.Inspection;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(SeparatedClassloaderExtension.class)
public class TestDynamicClassLoading {

    @SuppressWarnings({ "unused", "serial" })
    private Inspection inspection = new Inspection() {
    };

    @SeparatedClassPath
    static JavaArchive getClasspath() {
        return ShrinkWrap.create(JavaArchive.class).addClass(Inspection.class);
    }

    @Test
    public void test() {
        @SuppressWarnings("unused")
        String test = "xyz";
    }
}

