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

import org.eclipse.edc.spi.system.ServiceExtension;
import org.eclipse.edc.spi.system.ServiceExtensionContext;

public class SwaggerExtension implements ServiceExtension {

    public static final String SWAGGER_EXTENSION = "SwaggerExtension";

    @Override
    public String name() {
        return SWAGGER_EXTENSION;
    }

    @Override
    public void initialize(ServiceExtensionContext context) {
    }

    @Override
    public void start() {
    }

    @Override
    public void shutdown() {
    }
}
