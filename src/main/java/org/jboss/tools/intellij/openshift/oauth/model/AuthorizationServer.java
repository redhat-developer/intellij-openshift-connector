/*******************************************************************************
 * Copyright (c) 2022 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.tools.intellij.openshift.oauth.model;

import com.intellij.util.xmlb.annotations.Attribute;
import com.intellij.util.xmlb.annotations.Tag;
import com.intellij.util.xmlb.annotations.Transient;

import java.util.ArrayList;
import java.util.List;

@Tag("server")
public class AuthorizationServer implements IAuthorizationServer {

    private static final String REALM_KEY = "realm";
    private static final String URL_KEY = "url";
    private static final String CLIENT_ID_KEY = "clientId";

    @Attribute
    private String id;

    private String realm;

    private String url;

    private String clientId;

    private String displayName;

    private List<IAccount> accounts = new ArrayList<>();

    public AuthorizationServer(String id) {
        this.id = id;
    }

    @Override
    public String getId() {
        return id;
    }

    /**
     * @return the realm
     */
    @Transient
    public String getRealm() {
        return realm;
    }

    /**
     * @param realm the realm to set
     */
    public void setRealm(String realm) {
        this.realm = realm;
    }

    @Override
    @Transient
    public String getURL() {
        return url;
    }

    @Override
    public void setURL(String url) {
        this.url = url;
    }

    /**
     * @return the clientId
     */
    @Transient
    public String getClientId() {
        return clientId;
    }

    /**
     * @param clientId the clientId to set
     */
    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    /**
     * @return the displayName
     */
    @Transient
    public String getDisplayName() {
        if (displayName == null) {
            return getId();
        }
        return displayName;
    }

    /**
     * @param displayName the displayName to set
     */
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    @Override
    public void addAccount(IAccount account) {
        accounts.add(account);
    }

    @Override
    public List<IAccount> getAccounts() {
        return accounts;
    }

    public void setAccounts(List<IAccount> accounts) {
        this.accounts = accounts;
    }

    @Override
    public void removeAccount(IAccount account) {
        accounts.remove(account);
    }

    @Override
    public IAccount createAccount(String id) {
        return new Account(id, this);
    }
}
