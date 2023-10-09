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

import org.jboss.tools.intellij.openshift.tree.application.ProcessingNode.ProcessingState;
import org.junit.Before;
import org.junit.Test;

import static org.fest.assertions.Assertions.assertThat;
import static org.jboss.tools.intellij.openshift.tree.application.ProcessingNode.ProcessingNodeImpl;

public class ProcessingNodeImplTest {

  private ProcessingNodeImpl node = null;

  @Before
  public void before() {
    node = new ProcessingNodeImpl();
  }

  @Test
  public void initial_state_should_be_NOT_PROCESSING() {
    // given
    // when
    ProcessingState state = node.getState();
    // then
    assertThat(state).isEqualTo(ProcessingState.NOT_PROCESSING);
  }

  @Test
  public void initial_message_should_be_null() {
    // given
    // when
    String message = node.getMessage();
    // then
    assertThat(message).isNull();
  }

  @Test
  public void initially_isProcessing_is_false() {
    // given
    // when
    boolean isProcessing = node.isProcessing();
    // then
    assertThat(isProcessing).isFalse();
  }

  @Test
  public void initially_isProcessingStopped_is_false() {
    // given
    // when
    boolean isProcessingStopped = node.isProcessingStopped();
    // then
    assertThat(isProcessingStopped).isFalse();
  }

  @Test
  public void startProcessing_should_set_state_to_PROCESSING() {
    // given
    node.startProcessing("yoda");
    // when
    ProcessingState state = node.getState();
    // then
    assertThat(state).isEqualTo(ProcessingState.PROCESSING);
  }

  @Test
  public void startProcessing_should_set_isProcessing_to_true() {
    // given
    node.startProcessing("luke");
    // when
    boolean isProcessing = node.isProcessing();
    // then
    assertThat(isProcessing).isTrue();
  }

  @Test
  public void startProcessing_has_isProcessingStopped_false() {
    // given
    node.startProcessing("darth vader");
    // when
    boolean isProcessingStopped = node.isProcessingStopped();
    // then
    assertThat(isProcessingStopped).isFalse();
  }

  @Test
  public void startProcessing_should_set_message() {
    // given
    node.startProcessing("yoda");
    // when
    String message = node.getMessage();
    // then
    assertThat(message).isEqualTo("yoda");
  }

  @Test
  public void stopProcessing_should_set_state_to_STOPPED_PROCESSING() {
    // given
    node.stopProcessing();
    // when
    ProcessingState state = node.getState();
    // then
    assertThat(state).isEqualTo(ProcessingState.STOPPED_PROCESSING);
  }

  @Test
  public void stopProcessing_should_set_isProcessing_to_false() {
    // given
    node.stopProcessing();
    // when
    boolean isProcessing = node.isProcessing();
    // then
    assertThat(isProcessing).isFalse();
  }

  @Test
  public void stopProcessing_should_set_isProcessingStopped_to_true() {
    // given
    node.stopProcessing();
    // when
    boolean isProcessing = node.isProcessingStopped();
    // then
    assertThat(isProcessing).isTrue();
  }

  @Test
  public void stopProcessing_should_set_message_to_null() {
    // given
    node.stopProcessing();
    // when
    String message = node.getMessage();
    // then
    assertThat(message).isNull();
  }

}
