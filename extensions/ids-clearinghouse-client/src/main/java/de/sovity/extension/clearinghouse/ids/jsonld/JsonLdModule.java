/*
 *  Copyright (c) 2022 Fraunhofer Institute for Software and Systems Engineering
 *
 *  This program and the accompanying materials are made available under the
 *  terms of the Apache License, Version 2.0 which is available at
 *  https://www.apache.org/licenses/LICENSE-2.0
 *
 *  SPDX-License-Identifier: Apache-2.0
 *
 *  Contributors:
 *       Fraunhofer Institute for Software and Systems Engineering - initial API and implementation
 *
 */

package de.sovity.extension.clearinghouse.ids.jsonld;

import com.fasterxml.jackson.databind.module.SimpleModule;
import de.sovity.extension.clearinghouse.ids.jsonld.type.calendar.XmlGregorianCalendarDeserializer;
import de.sovity.extension.clearinghouse.ids.jsonld.type.calendar.XmlGregorianCalendarSerializer;
import de.sovity.extension.clearinghouse.ids.jsonld.type.uri.UriDeserializer;
import de.sovity.extension.clearinghouse.ids.jsonld.type.uri.UriSerializer;

import javax.xml.datatype.XMLGregorianCalendar;
import java.net.URI;

public class JsonLdModule extends SimpleModule {

    public JsonLdModule() {
        super();

        addSerializer(URI.class, new UriSerializer());
        addDeserializer(URI.class, new UriDeserializer());

        addSerializer(XMLGregorianCalendar.class, new XmlGregorianCalendarSerializer());
        addDeserializer(XMLGregorianCalendar.class, new XmlGregorianCalendarDeserializer());
    }
}
