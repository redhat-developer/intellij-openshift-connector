/*******************************************************************************
 * Copyright (c) 2019 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 * Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.tools.intellij.openshift.tree.application;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import io.fabric8.kubernetes.api.model.Context;
import io.fabric8.kubernetes.api.model.NamedContext;
import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.KubernetesClient;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ApplicationTreeModelConfigUpdateTest extends BasePlatformTestCase {

    private Config config = null;
    private ApplicationsRootNode model = null;

    public void setUp() throws Exception {
        super.setUp();
        NamedContext ctx1 = createNamedContext( "yoda context",
          "eponymous star system",
          "dagobah",
          "yoda");
        NamedContext ctx2 = createNamedContext( "skywalker context",
          "tatooine star system",
          "tatooine",
          "luke skywalker");
        NamedContext ctx3 = createNamedContext( "obiwan context",
          "stewjon star system",
          "stewjon",
          "obiwan");

        this.config = createConfig("use the force", ctx2, Arrays.asList(ctx1, ctx2, ctx3));

        KubernetesClient client = createKubernetesClient(config);
        this.model = createApplicationsRootNode(getProject(), client);  // initial configuration
    }

    public void testShouldNotRefreshIfContextHasNoChanges() {
        // given
        Config clone = createConfig(config);
        // when
        model.onUpdate(clone);
        // then
        verify(model, never()).refresh();
    }

    public void testShouldRefreshIfCurrentContextHasDifferentName() {
        // given
        Config differentUser = createConfig(config);
        NamedContext currentContext = createNamedContext(differentUser.getCurrentContext());
        when(currentContext.getName())
          .thenReturn("yoda context");
        when(differentUser.getCurrentContext())
          .thenReturn(currentContext);
        // when
        model.onUpdate(differentUser);
        // then
        verify(model).refresh();
    }

    public void testShouldRefreshIfCurrentContextHasDifferentCluster() {
        // given
        Config differentCluster = createConfig(config);
        NamedContext currentContext = createNamedContext(differentCluster.getCurrentContext());
        when(currentContext.getContext().getCluster())
          .thenReturn("eponymous star system");
        when(differentCluster.getCurrentContext())
          .thenReturn(currentContext);
        // when
        model.onUpdate(differentCluster);
        // then
        verify(model).refresh();
    }

    public void testShouldRefreshIfCurrentContextHasDifferentNamespace() {
        // given
        Config differentCluster = createConfig(config);
        NamedContext currentContext = createNamedContext(differentCluster.getCurrentContext());
        when(currentContext.getContext().getNamespace())
          .thenReturn("dagobah");
        when(differentCluster.getCurrentContext())
          .thenReturn(currentContext);
        // when
        model.onUpdate(differentCluster);
        // then
        verify(model).refresh();
    }

    public void testShouldRefreshIfCurrentContextHasDifferentUser() {
        // given
        Config differentUser = createConfig(config);
        NamedContext currentContext = createNamedContext(differentUser.getCurrentContext());
        when(currentContext.getContext().getUser())
          .thenReturn("R2-D2");
        when(differentUser.getCurrentContext())
          .thenReturn(currentContext);
        // when
        model.onUpdate(differentUser);
        // then
        verify(model).refresh();
    }

    public void testShouldRefreshIfCurrentContextHasDifferentToken() {
        // given
        Config differentToken = createConfig(config);
        NamedContext currentContext = createNamedContext(differentToken.getCurrentContext());
        when(differentToken.getAutoOAuthToken())
          .thenReturn("use the laser blaster");
        when(differentToken.getCurrentContext())
          .thenReturn(currentContext);
        // when
        model.onUpdate(differentToken);
        // then
        verify(model).refresh();
    }

    protected ApplicationsRootNode createApplicationsRootNode(Project project, KubernetesClient client) {
        return spy(new ApplicationsRootNode(project, null, Disposer.newDisposable()) {
            @Override
            protected void initConfigWatcher() {
            }

            @Override
            protected void registerProjectListener(Project project) {
            }

            @Override
            public synchronized void refresh() {}

            @Override
            protected KubernetesClient getClient() {
                return client;
            }

            @Override
            protected KubernetesClient createClient(Config config) {
                return client;
            }
        });
    }

    private Config createConfig(Config config) {
        return createConfig(config.getAutoOAuthToken(), config.getCurrentContext(), config.getContexts());
    }

    private Config createConfig(String token, NamedContext current, List<NamedContext> contexts) {
        Config config = mock(Config.class);
        doReturn(token)
            .when(config).getAutoOAuthToken();
        doReturn(current)
          .when(config).getCurrentContext();
        doReturn(contexts)
            .when(config).getContexts();
        return config;
    }

    private NamedContext createNamedContext(NamedContext namedContext) {
        Context context = namedContext.getContext();
        return createNamedContext(namedContext.getName(), context.getCluster(), context.getNamespace(), context.getUser());
    }

    private NamedContext createNamedContext(String name, String cluster, String namespace, String user) {
        Context context = mock(Context.class);
        doReturn(cluster)
          .when(context).getCluster();
        doReturn(namespace)
          .when(context).getNamespace();
        doReturn(user)
          .when(context).getUser();

        NamedContext namedContext = mock(NamedContext.class);
        doReturn(name)
          .when(namedContext).getName();
        doReturn(context)
          .when(namedContext).getContext();
        return namedContext;
    }

    private KubernetesClient createKubernetesClient(Config config) throws MalformedURLException {
        KubernetesClient client = mock(KubernetesClient.class);
        doReturn(new URL("https://starwars.com")) // avoid "invalid master url" exception
            .when(client).getMasterUrl();
        doReturn(config)
          .when(client).getConfiguration();
        return client;
    }
}
