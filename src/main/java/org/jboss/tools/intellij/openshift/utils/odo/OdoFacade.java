/*******************************************************************************
 * Copyright (c) 2024 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 * Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.tools.intellij.openshift.utils.odo;

import java.io.IOException;
import java.util.List;
import java.util.function.Consumer;

public interface OdoFacade extends Odo {

  void start(String context, String component, ComponentFeature feature,
             Consumer<Boolean> callback, Consumer<Boolean> processTerminatedCallback) throws IOException;

  void stop(String context, String component, ComponentFeature feature) throws IOException;

  void follow(String context, String component, boolean deploy, String platform) throws IOException;

  void log(String context, String component, boolean deploy, String platform) throws IOException;

  boolean isStarted(String component, ComponentFeature feature);

  List<Component> getComponents(String project) throws IOException;

}
