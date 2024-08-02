/*******************************************************************************
 * Copyright (c) 2021 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 * Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.tools.intellij.openshift.telemetry;

import com.intellij.ide.plugins.PluginManager;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.extensions.DefaultPluginDescriptor;
import com.redhat.devtools.intellij.telemetry.core.service.TelemetryMessageBuilder;
import com.redhat.devtools.intellij.telemetry.core.util.Lazy;

import static com.redhat.devtools.intellij.telemetry.core.service.TelemetryMessageBuilder.ActionMessage;
import static com.redhat.devtools.intellij.telemetry.core.service.TelemetryMessageBuilder.FeedbackMessage;

public class TelemetryService {

  public enum TelemetryResult {
    SUCCESS, ERROR, ABORTED
  }

  public static final String VALUE_ABORTED = "aborted";
  public static final String VALUE_SUCCESS = "success";
  public static final String VALUE_FAILURE = "failure";

  public static final String PREFIX_ACTION = "ui-";
  public static final String NAME_PREFIX_MISC = "misc-";

  public static final String PROP_COMPONENT_KIND = "component_kind";
  public static final String PROP_COMPONENT_PUSH_AFTER_CREATE = "component_is_push_after_create";
  public static final String PROP_COMPONENT_HAS_LOCAL_DEVFILE = "component_has_local_devfile";
  public static final String PROP_COMPONENT_SELECTED_STARTER = "component_selected_starter";

  public static final String PROP_DEBUG_COMPONENT_LANGUAGE = "debug_component_language";

  public static final String KUBERNETES_VERSION = "kubernetes_version";
  public static final String IS_OPENSHIFT = "is_openshift";
  public static final String OPENSHIFT_VERSION = "openshift_version";

  public static final String DEVSANDBOX_LOGIN_DIALOG = "devsandbox-login-dialog";
  public static final String REDHAT_SSO_GET_TOKEN = "redhat_sso_get_token";
  public static final String DEVSANDBOX_TOKEN_RETRIEVED = "devsandbox_token_retrieved";
  public static final String DEVSANDBOX_API_STATE_PREFIX = "devsandbox-api-state-";

  private static TelemetryService instance;

  private Lazy<TelemetryMessageBuilder> builder = null;

  private TelemetryService() {
    if (!ApplicationManager.getApplication().isUnitTestMode()) {
      builder = new Lazy<>(() -> new TelemetryMessageBuilder(PluginManager.getPluginByClass(this.getClass())));
    } else {
      builder = new Lazy<>(() -> new TelemetryMessageBuilder(new DefaultPluginDescriptor("")));
    }
  }

  public static TelemetryService instance() {
    if (instance == null) {
      instance = new TelemetryService();
    }
    return instance;
  }

  public TelemetryMessageBuilder getBuilder() {
    return instance.builder.get();
  }

  public static void asyncSend(ActionMessage message) {
    ApplicationManager.getApplication().executeOnPooledThread(message::send);
  }

  public static void asyncSend(FeedbackMessage message) {
    ApplicationManager.getApplication().executeOnPooledThread(message::send);
  }

}
