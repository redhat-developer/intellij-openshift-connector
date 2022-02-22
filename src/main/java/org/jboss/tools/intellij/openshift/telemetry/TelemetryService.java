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

import com.redhat.devtools.intellij.telemetry.core.service.TelemetryMessageBuilder;
import com.redhat.devtools.intellij.telemetry.core.util.Lazy;

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

    public static final String DEVSANDBOX_LOGIN_DIALOG="devsandbox-login-dialog";
    public static final String REDHAT_SSO_GET_TOKEN = "redhat_sso_get_token";
    public static final String DEVSANDBOX_TOKEN_RETRIEVED = "devsandbox_token_retrieved";
    public static final String DEVSANDBOX_API_STATE_PREFIX = "devsandbox-api-state-";

    private static TelemetryService instance;

    private final Lazy<TelemetryMessageBuilder> builder = new Lazy<>(() -> new TelemetryMessageBuilder(TelemetryService.class.getClassLoader()));

    private TelemetryService() {
        // prevent instantiation
    }

    public static TelemetryService instance() {
        if (instance == null) {
            instance = new TelemetryService();
        }
        return instance;
    }

    public TelemetryMessageBuilder getBuilder(){
        return instance.builder.get();
    }

}
