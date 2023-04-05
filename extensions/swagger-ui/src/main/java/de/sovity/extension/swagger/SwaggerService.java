/*
 *  Copyright (c) 2022 sovity GmbH
 *
 *  This program and the accompanying materials are made available under the
 *  terms of the Apache License, Version 2.0 which is available at
 *  https://www.apache.org/licenses/LICENSE-2.0
 *
 *  SPDX-License-Identifier: Apache-2.0
 *
 *  Contributors:
 *       sovity GmbH - initial API and implementation
 *
 */

package de.sovity.extension.swagger;

import jakarta.ws.rs.core.Response;
import org.eclipse.edc.spi.system.ServiceExtensionContext;

import java.io.File;

public class SwaggerService {

    private final ServiceExtensionContext context;

    public SwaggerService(ServiceExtensionContext context) {
        this.context = context;
    }

    public Response getOpenApi() {
        var file = new File("edc-api-wrapper.yaml");
        return Response.ok(file).header("Content-Disposition", "attachment; filename=\"" + file.getName() + "\"").build();
    }
}
