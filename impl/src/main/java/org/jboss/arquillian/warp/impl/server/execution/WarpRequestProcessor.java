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
package org.jboss.arquillian.warp.impl.server.execution;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.jboss.arquillian.core.api.Event;
import org.jboss.arquillian.core.api.InstanceProducer;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.api.annotation.Observes;
import org.jboss.arquillian.warp.impl.server.event.EnrichHttpResponse;
import org.jboss.arquillian.warp.impl.server.event.ExecuteWarp;
import org.jboss.arquillian.warp.impl.server.event.ProcessWarpRequest;
import org.jboss.arquillian.warp.impl.server.event.RequestProcessingFinished;
import org.jboss.arquillian.warp.impl.server.event.RequestProcessingStarted;
import org.jboss.arquillian.warp.impl.server.request.RequestScoped;
import org.jboss.arquillian.warp.impl.shared.ResponsePayload;

public class WarpRequestProcessor {

    @Inject
    private Event<RequestProcessingStarted> requestProcessingStarted;

    @Inject
    private Event<RequestProcessingFinished> requestProcessingFinished;

    @Inject
    private Event<EnrichHttpResponse> enrichHttpResponse;

    @Inject @RequestScoped
    private InstanceProducer<NonWritingResponse> nonWritingResponse;

    @Inject
    private Event<ExecuteWarp> executeWarp;

    

    public void processWarpRequest(@Observes ProcessWarpRequest event, HttpServletResponse response,
            ResponsePayload responsePayload) throws IOException {

        requestProcessingStarted.fire(new RequestProcessingStarted());

        try {
            nonWritingResponse.set(new NonWritingResponse(response));

            executeWarp.fire(new ExecuteWarp());

        } catch (Throwable e) {
            responsePayload.setThrowable(e);
        }

        if (responsePayload.getThrowable() != null) {
            responsePayload.getThrowable().printStackTrace();
        }

        try {
            enrichHttpResponse.fire(new EnrichHttpResponse());
            
            NonWritingPrintWriter writer = nonWritingResponse.get().getNonWritingPrintWriter();
            NonWritingServletOutputStream stream = nonWritingResponse.get().getNonWritingServletOutputStream();

            if (writer != null) {
                writer.finallyWriteAndClose(response.getOutputStream());
            }
            if (stream != null) {
                stream.finallyWriteAndClose(response.getOutputStream());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        requestProcessingFinished.fire(new RequestProcessingFinished());
    }

    
}