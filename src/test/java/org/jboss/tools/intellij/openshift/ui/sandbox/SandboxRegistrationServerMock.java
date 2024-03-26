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
package org.jboss.tools.intellij.openshift.ui.sandbox;

import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class SandboxRegistrationServerMock {
    private static final java.util.concurrent.ScheduledExecutorService SCHEDULED_EXECUTOR_SERVICE = Executors.newSingleThreadScheduledExecutor();
    private final HttpServer server;

    private String currentSignupResponse;

    private static final String initialSignupResponse = "{\n" +
            "            \"apiEndpoint\": \"https://api.crc.testing:6443\",\n" +
            "            \"cheDashboardURL\": \"\",\n" +
            "            \"clusterName\": \"\",\n" +
            "            \"company\": \"\",\n" +
            "            \"compliantUsername\": \"\",\n" +
            "            \"consoleURL\": \"\",\n" +
            "            \"familyName\": \"\",\n" +
            "            \"givenName\": \"\",\n" +
            "            \"status\": {\n" +
            "                \"ready\": false,\n" +
            "                \"reason\": \"PendingApproval\",\n" +
            "                \"verificationRequired\": true\n" +
            "            },\n" +
            "            \"username\": \"\"\n" +
            "        }";

    private static final String afterVerificationSignupResponse = "{\n" +
            "            \"apiEndpoint\": \"https://api.crc.testing:6443\",\n" +
            "            \"cheDashboardURL\": \"\",\n" +
            "            \"clusterName\": \"\",\n" +
            "            \"company\": \"\",\n" +
            "            \"compliantUsername\": \"\",\n" +
            "            \"consoleURL\": \"\",\n" +
            "            \"familyName\": \"\",\n" +
            "            \"givenName\": \"\",\n" +
            "            \"status\": {\n" +
            "                \"ready\": false,\n" +
            "                \"reason\": \"PendingApproval\",\n" +
            "                \"verificationRequired\": false\n" +
            "            },\n" +
            "            \"username\": \"\"\n" +
            "        }";

    private static final String afterVerification1SignupResponse = "{\n" +
            "            \"apiEndpoint\": \"https://api.crc.testing:6443\",\n" +
            "            \"cheDashboardURL\": \"\",\n" +
            "            \"clusterName\": \"\",\n" +
            "            \"company\": \"\",\n" +
            "            \"compliantUsername\": \"\",\n" +
            "            \"consoleURL\": \"\",\n" +
            "            \"familyName\": \"\",\n" +
            "            \"givenName\": \"\",\n" +
            "            \"status\": {\n" +
            "                \"ready\": false,\n" +
            "                \"reason\": \"\",\n" +
            "                \"verificationRequired\": false\n" +
            "            },\n" +
            "            \"username\": \"\"\n" +
            "        }";

    private static final String afterVerification2SignupResponse = "{\n" +
            "            \"apiEndpoint\": \"https://api.crc.testing:6443\",\n" +
            "            \"cheDashboardURL\": \"\",\n" +
            "            \"clusterName\": \"\",\n" +
            "            \"company\": \"\",\n" +
            "            \"compliantUsername\": \"\",\n" +
            "            \"consoleURL\": \"\",\n" +
            "            \"familyName\": \"\",\n" +
            "            \"givenName\": \"\",\n" +
            "            \"status\": {\n" +
            "                \"ready\": true,\n" +
            "                \"reason\": \"\",\n" +
            "                \"verificationRequired\": false\n" +
            "            },\n" +
            "            \"username\": \"\"\n" +
            "        }";

    public SandboxRegistrationServerMock(int port) throws IOException {
        server = HttpServer.create(new InetSocketAddress(InetAddress.getByName(null), port), 0);

        server.createContext("/api/v1/signup", exchange -> {
            if (exchange.getRequestMethod().equals("GET")) {
                if (currentSignupResponse == null) {
                    exchange.sendResponseHeaders(404, 0);
                    exchange.getResponseBody().close();
                } else {
                    exchange.sendResponseHeaders(200, currentSignupResponse.getBytes().length);
                    exchange.getResponseBody().write(currentSignupResponse.getBytes());
                    exchange.getResponseBody().close();
                }
            } else if (exchange.getRequestMethod().equals("POST")) {
                currentSignupResponse = initialSignupResponse;
                exchange.sendResponseHeaders(200, 0);
                exchange.getResponseBody().close();
            }
        });

        server.createContext("/api/v1/signup/verification", exchange -> {
            if (exchange.getRequestMethod().equals("PUT")) {
                exchange.sendResponseHeaders(204, 0);
                exchange.getResponseBody().close();
            } else if (exchange.getRequestMethod().equals("GET")) {
                currentSignupResponse = afterVerificationSignupResponse;
                SCHEDULED_EXECUTOR_SERVICE.schedule(() -> {
                    currentSignupResponse = afterVerification1SignupResponse;
                    SCHEDULED_EXECUTOR_SERVICE.schedule(() -> {
                        currentSignupResponse = afterVerification2SignupResponse;
                    }, 10, TimeUnit.SECONDS);
                }, 10, TimeUnit.SECONDS);
                exchange.sendResponseHeaders(200, 0);
                exchange.getResponseBody().close();
            }
        });
    }

    public SandboxRegistrationServerMock() throws IOException {
        this(3000);
    }

    public void start() {
        server.start();
    }

    public void stop() {
        server.stop(0);
    }

    public static void main(String[] args) throws IOException {
        SandboxRegistrationServerMock server = new SandboxRegistrationServerMock();
        server.start();
    }
}
