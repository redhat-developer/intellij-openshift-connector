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
package org.jboss.tools.intellij.openshift.tree.application;

public interface ProcessingNode {

  void startProcessing(String message);

  void stopProcessing();

  boolean isProcessing();

  boolean isProcessingStopped();

  String getMessage();

  class ProcessingNodeImpl {

    private ProcessingState state = ProcessingState.NOT_PROCESSING;
    private String message = null;

    public synchronized ProcessingState getState() {
      return state;
    }

    public synchronized void startProcessing(String message) {
      this.message = message;
      this.state = ProcessingState.PROCESSING;
    }

    public synchronized void stopProcessing() {
      this.state = ProcessingState.STOPPED_PROCESSING;
      this.message = null;
    }

    public synchronized boolean isProcessing() {
      return state == ProcessingState.PROCESSING;
    }

    public synchronized boolean isProcessingStopped() {
      return state == ProcessingState.STOPPED_PROCESSING;
    }

    public synchronized String getMessage() {
      return message;
    }

  }

  enum ProcessingState {
    NOT_PROCESSING,
    PROCESSING {
      @Override
      boolean isProcessing() {
        return true;
      }
    },
    STOPPED_PROCESSING {
      @Override
      boolean isStopped() {
        return true;
      }
    };

    boolean isProcessing() {
      return false;
    }

    boolean isStopped() {
      return false;
    }

  }
}
