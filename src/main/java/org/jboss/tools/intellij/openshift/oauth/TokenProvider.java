/*******************************************************************************
 * Copyright (c) 2022 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 * Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.tools.intellij.openshift.oauth;

import com.intellij.openapi.application.ApplicationManager;
import org.jboss.tools.intellij.openshift.oauth.exception.OAuthException;

public interface TokenProvider {

    int ID_TOKEN = 0;
    int ACCESS_TOKEN = 1;
    int REFRESH_TOKEN = 2;

    String getToken(String serverId, int tokentype, Object context) throws OAuthException;

    static TokenProvider get() {
        return ApplicationManager.getApplication().getService(DefaultTokenProvider.class);
    }
}
