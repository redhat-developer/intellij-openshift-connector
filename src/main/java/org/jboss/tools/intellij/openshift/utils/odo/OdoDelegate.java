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

import com.intellij.execution.process.ProcessAdapter;
import com.intellij.execution.process.ProcessHandler;

import java.io.IOException;
import java.util.List;

public interface OdoDelegate extends Odo {

  void start(String context, ComponentFeature feature, ProcessHandler handler, ProcessAdapter processAdapter) throws IOException;

  void stop(String context, ComponentFeature feature, ProcessHandler handler) throws IOException;

  void follow(String context, boolean deploy, String platform, List<ProcessHandler> handlers) throws IOException;

  void log(String context, boolean deploy, String platform, List<ProcessHandler> handlers) throws IOException;

  List<Component> getComponentsOnCluster(String project) throws IOException;

}
