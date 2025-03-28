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

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Proxy;
import java.util.Map;

/**
 * <p>
 * Provides dynamic annotation creation.
 *
 * @author Lukas Fryc
 * @author Stuart Douglas
 * @author Pete Muir
 * @see AnnotationLiteral
 */
public class AnnotationInstanceProvider {

    /**
     * <p>
     * Returns an instance of the given annotation type with attribute values specified in the map.
     * </p>
     * <p>
     * <ul>
     * <li>
     * For {@link Annotation}, array and enum types the values must exactly match the declared return type of the attribute or a
     * {@link ClassCastException} will result.</li>
     * <p>
     * <li>
     * For character types the the value must be an instance of {@link Character} or {@link String}.</li>
     * <p>
     * <li>
     * Numeric types do not have to match exactly, as they are converted using {@link Number}.</li>
     * </ul>
     * <p>
     * <p>
     * If am member does not have a corresponding entry in the value map then the annotations default value will be used.
     * </p>
     * <p>
     * <p>
     * If the annotation member does not have a default value then a NullMemberException will be thrown
     * </p>
     *
     * @param annotationType the type of the annotation instance to generate
     * @param values         the attribute values of this annotation
     */
    public static <T extends Annotation> T get(Class<T> annotationType, Map<String, ?> values) {
        if (annotationType == null) {
            throw new IllegalArgumentException("Must specify an annotation");
        }

        AnnotationInvocationHandler handler = new AnnotationInvocationHandler(values, annotationType);
        // create a new instance by obtaining the constructor via relection
        try {
            Object proxyInstance = Proxy.newProxyInstance(annotationType.getClassLoader(), new Class<?>[] {
              annotationType, Serializable.class}, handler);
            return annotationType.cast(proxyInstance);
        } catch (SecurityException e) {
            throw new IllegalStateException("Error accessing proxy constructor for annotation. Annotation type: "
                + annotationType, e);
        }
    }
}
