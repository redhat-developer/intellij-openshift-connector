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
package org.jboss.tools.intellij.openshift.utils.helm;

import java.io.File;
import java.io.IOException;
import java.util.List;

public interface Helm {

  String addRepo(String name, String url, String flags) throws IOException;

  void removeRepos(String... name) throws IOException;

  List<HelmRepository> listRepos() throws IOException;

  List<Chart> search() throws IOException;

  List<Chart> search(String regex) throws IOException;

  List<ChartRelease> list() throws IOException;

  String install(String name, String chart, String version, File valuesFile) throws IOException;

  String uninstall(String... names) throws IOException;

  HelmCli.HelmEnv env() throws IOException;

  String showValues(String chart) throws IOException;
}
