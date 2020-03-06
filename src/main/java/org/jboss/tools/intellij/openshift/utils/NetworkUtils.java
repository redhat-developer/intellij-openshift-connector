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
package org.jboss.tools.intellij.openshift.utils;

import com.google.common.collect.Sets;
import com.intellij.util.net.HttpConfigurable;
import com.intellij.util.net.IdeaWideAuthenticator;
import com.intellij.util.net.IdeaWideProxySelector;
import okhttp3.Authenticator;
import okhttp3.OkHttpClient;
import org.jetbrains.annotations.NotNull;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.PasswordAuthentication;
import java.net.Proxy;
import java.net.SocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import static com.intellij.openapi.util.text.StringUtil.isNotEmpty;
import static okhttp3.Credentials.basic;

public class NetworkUtils {

    public static OkHttpClient getClient() {
        final HttpConfigurable httpConfigurable = HttpConfigurable.getInstance();
        final IdeaWideProxySelector ideaWideProxySelector = new IdeaWideProxySelector(httpConfigurable);
        final IdeaWideAuthenticator ideaWideAuthenticator = new IdeaWideAuthenticator(httpConfigurable);
        final okhttp3.Authenticator proxyAuthenticator = getProxyAuthenticator(ideaWideAuthenticator);
        final OkHttpClient.Builder builder = new OkHttpClient.Builder();

        builder.proxySelector(ideaWideProxySelector)
                .proxyAuthenticator(proxyAuthenticator);

        return builder.build();
    }

    private static Authenticator getProxyAuthenticator(IdeaWideAuthenticator ideaWideAuthenticator) {
        Authenticator proxyAuthenticator = null;

        if (Objects.nonNull(ideaWideAuthenticator)) {
            proxyAuthenticator = (route, response) -> {
                final PasswordAuthentication authentication = ideaWideAuthenticator.getPasswordAuthentication();
                final String credential = basic(authentication.getUserName(), Arrays.toString(authentication.getPassword()));
                return response.request().newBuilder()
                        .header("Proxy-Authorization", credential)
                        .build();
            };
        }

        return proxyAuthenticator;
    }

    @NotNull
    public static Map<String, String> buildEnvironmentVariables(String url) throws URISyntaxException {
        final int FIRST = 0;
        final String HTTP_PROXY = "HTTP_PROXY";
        final String HTTPS_PROXY = "HTTPS_PROXY";
        final String ALL_PROXY = "ALL_PROXY";
        final Set<String> proxyEnvironmentVariables = Sets.newHashSet(HTTP_PROXY, HTTPS_PROXY, ALL_PROXY);

        final Map<String, String> environmentVariables = new HashMap<>(6);

        final HttpConfigurable httpConfigurable = HttpConfigurable.getInstance();
        final IdeaWideProxySelector ideaWideProxySelector = new IdeaWideProxySelector(httpConfigurable);
        final URI uri = new URI(url);
        final List<Proxy> proxies = ideaWideProxySelector.select(uri);

        if (!proxies.isEmpty()) {
            final Proxy proxy = proxies.get(FIRST);
            final Proxy.Type type = proxy.type();

            switch (type) {
                case HTTP:
                case SOCKS:
                    final SocketAddress address = proxy.address();

                    if (address instanceof InetSocketAddress) {
                        final InetSocketAddress socketAddress = (InetSocketAddress) address;
                        final InetAddress inetAddress = socketAddress.getAddress();
                        final int port = socketAddress.getPort();

                        final IdeaWideAuthenticator ideaWideAuthenticator = new IdeaWideAuthenticator(httpConfigurable);
                        final Optional<PasswordAuthentication> optionalPasswordAuthentication = Optional.ofNullable(ideaWideAuthenticator.getPasswordAuthentication());

                        String userName = null;
                        String password = null;
                        if(optionalPasswordAuthentication.isPresent()) {
                            final PasswordAuthentication passwordAuthentication = optionalPasswordAuthentication.get();
                            userName = passwordAuthentication.getUserName();
                            password = Arrays.toString(passwordAuthentication.getPassword());
                        }

                        String finalUserName = userName;
                        String finalPassword = password;
                        proxyEnvironmentVariables.forEach(envVarName -> {
                            final String envVarValue = buildHttpProxy(type, finalUserName, finalPassword, inetAddress, port);

                            environmentVariables.put(envVarName, envVarValue);
                            environmentVariables.put(envVarName.toLowerCase(), envVarValue);
                        });
                    }
                    break;
            }
        }

        return environmentVariables;
    }

    @NotNull
    private static String buildHttpProxy(Proxy.Type type, String userName, String password, InetAddress address, int port) {
        final StringBuilder builder = new StringBuilder();

        switch (type) {
            case HTTP:
                builder.append("http://");
                break;
            case SOCKS:
                builder.append("socks://");
                break;
        }

        if (isNotEmpty(userName) && isNotEmpty(password)) {
            builder.append(userName).append(":").append(password).append("@");
        }

        builder.append(address.getHostAddress()).append(":").append(port);

        return builder.toString();
    }
}
