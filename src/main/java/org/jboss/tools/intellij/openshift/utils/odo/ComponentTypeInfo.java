/*******************************************************************************
 * Copyright (c) 2020 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 * Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.tools.intellij.openshift.utils.odo;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Red Hat Developers
 *
 */
public interface ComponentTypeInfo {
  String getName();
  List<Starter> getStarters();
  
  class Builder {
    private String name;
    
    private List<Starter> starters = new ArrayList<>();
    
    public Builder withName(String name) {
      this.name = name;
      return this;
    }
    
    public Builder withStarter(Starter starter) {
     starters.add(starter);
     return this;
    }
    
    public ComponentTypeInfo build() {
      return new ComponentTypeInfo() {

        @Override
        public String getName() {
          return name;
        }

        @Override
        public List<Starter> getStarters() {
          return starters;
        }
      };
    }
  }
}
