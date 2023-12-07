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

import com.intellij.openapi.application.ApplicationManager;

import java.util.List;
import java.util.stream.Collectors;

public class AccountModel implements IAccountModel {

    private final ServersRepository repository = ApplicationManager.getApplication().getService(ServersRepository.class);

	@Override
	public IAuthorizationServer createAuthorizationServer(String id) {
        return new AuthorizationServer(id);
	}

	private IAuthorizationServer merge(ServerExtensionPoint registeredServer, AuthorizationServer server) {
		server.setClientId(registeredServer.getClientId());
		server.setRealm(registeredServer.getRealm());
		server.setDisplayName(registeredServer.getDisplayName());
		server.setURL(registeredServer.getUrl());
		return server;
	}

	@Override
	public List<IAuthorizationServer> getAuthorizationServers() {
		List<IAuthorizationServer> servers = repository.getServers();
		List<ServerExtensionPoint> registeredServers = ServerExtensionPoint.EP_NAME.getExtensionList();
		List<IAuthorizationServer> nservers = registeredServers
				.stream().map(rs -> merge(rs, (AuthorizationServer) servers.stream().filter(s -> s.getId().equals(rs.getId())).findFirst().orElseGet(() -> createAuthorizationServer(rs.getId()))))
				.collect(Collectors.toList());
		repository.setServers(nservers);
		return nservers;
	}

}
