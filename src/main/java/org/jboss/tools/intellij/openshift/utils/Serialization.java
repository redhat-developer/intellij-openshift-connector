/*******************************************************************************
 * Copyright (c) 2023 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 * Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.tools.intellij.openshift.utils;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdNodeBasedDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;

import java.util.List;

public class Serialization {

  private static final ObjectMapper defaultMapper = new ObjectMapper(new JsonFactory());

  public static ObjectMapper json() {
    return defaultMapper;
  }

  private Serialization() {}

  public static ObjectMapper configure(final StdNodeBasedDeserializer<? extends List<?>> deserializer) {
    final SimpleModule module = new SimpleModule();
    module.addDeserializer(List.class, deserializer);
    return new ObjectMapper(new JsonFactory()).registerModule(module);
  }

}
