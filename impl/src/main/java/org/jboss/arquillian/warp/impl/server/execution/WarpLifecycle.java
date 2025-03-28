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
package org.jboss.arquillian.warp.impl.server.execution;

import java.util.List;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.jboss.arquillian.core.api.Event;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.api.annotation.Observes;
import org.jboss.arquillian.warp.Inspection;
import org.jboss.arquillian.warp.impl.server.event.WarpLifecycleFinished;
import org.jboss.arquillian.warp.impl.server.event.WarpLifecycleStarted;
import org.jboss.arquillian.warp.impl.server.inspection.InspectionRegistry;
import org.jboss.arquillian.warp.impl.shared.RequestPayload;
import org.jboss.arquillian.warp.impl.shared.ResponsePayload;
import org.jboss.arquillian.warp.spi.LifecycleManager;
import org.jboss.arquillian.warp.spi.servlet.event.AfterServlet;
import org.jboss.arquillian.warp.spi.servlet.event.BeforeServlet;
import org.jboss.arquillian.warp.spi.servlet.event.ExecuteWarp;

/**
 * The lifecycle of Warp request verification
 *
 * @author Lukas Fryc
 */
public class WarpLifecycle {

    @Inject
    private Instance<LifecycleManager> lifecycleManager;

    @Inject
    private Instance<InspectionRegistry> inspectionRegistry;

    @Inject
    private Event<WarpLifecycleStarted> warpLifecycleStarted;

    @Inject
    private Event<WarpLifecycleFinished> warpLifecycleFinished;

    /**
     * Executes the lifecycle
     *
     * @return {@link ResponsePayload} based on the lifecycle tests results
     */
    public void execute(@Observes ExecuteWarp event, HttpServletRequest request, HttpServletResponse response,
        FilterChain filterChain, RequestPayload requestPayload, ResponsePayload responsePayload) throws Throwable {

        List<Inspection> inspections = requestPayload.getInspections();

        try {
            lifecycleManager.get().bindTo(ServletRequest.class, request);
            inspectionRegistry.get().registerInspections(inspections);

            warpLifecycleStarted.fire(new WarpLifecycleStarted());
            lifecycleManager.get().fireEvent(new BeforeServlet());

            filterChain.doFilter(request, response);

            lifecycleManager.get().fireEvent(new AfterServlet());

            responsePayload.setInspections(inspections);
        } finally {
            warpLifecycleFinished.fire(new WarpLifecycleFinished());

            inspectionRegistry.get().unregisterInspections(inspections);

            lifecycleManager.get().unbindFrom(ServletRequest.class, request);
        }
    }
}
