/*******************************************************************************
 * Copyright (c) 2019 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 * Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.tools.intellij.openshift.utils.odo;

public interface URL {
  String getName();

  String getProtocol();

  String getHost();

  String getPort();


  static URL of(String name, String host, String port) {
    return new URL() {
      @Override
      public String getName() {
        return name;
      }

      @Override
      public String getHost() {
        return host;
      }

      @Override
      public String getProtocol() {
        return "http";
      }

      @Override
      public String getPort() {
        return port;
      }
    };
  }
}
