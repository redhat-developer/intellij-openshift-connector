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
import io.fabric8.kubernetes.api.model.AuthInfo;
import io.fabric8.kubernetes.api.model.Config;
import io.fabric8.kubernetes.api.model.Context;
import io.fabric8.kubernetes.api.model.NamedAuthInfo;
import io.fabric8.kubernetes.api.model.NamedContext;
import org.apache.commons.lang.StringUtils;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

public class ApplicationTreeModelConfigUpdateTest {

    private Project project;

    @Before
    public void before() {
        this.project = mock(Project.class);
    }

    @Test
    public void shouldNotRefreshIfContextDoesntChange() {
        // given
        String user = "papa-smurf";
        String cluster = "localhost";
        String token = "token1";

        Context ctx1 = createContext(user, cluster);
        AuthInfo authInfo1 = createAuthInfo(token);
        Config cfg1 = createConfig(ctx1, user, authInfo1);

        ApplicationTreeModel model = createApplicationTreeModel(project, cfg1);

        Context ctx2 = createContext(user, cluster);
        AuthInfo authInfo2 = createAuthInfo(token);
        Config cfg2 = createConfig(ctx1, user, authInfo1);
        // when
        model.onUpdate(null, cfg2);
        // then
        verify(model, never()).refresh();
    }

    @Test
    public void shouldRefreshIfContextUserChanges() {
        // given
        Context context = createContext();
        doReturn("papa-smurf","smurfette").when(context).getUser();
        Config config = createConfig(context);
        ApplicationTreeModel model = createApplicationTreeModel(project, config);
        // when
        model.onUpdate(null, config);
        // then
        verify(model).refresh();
    }

    @Test
    public void shouldRefreshIfContextClusterChanges() {
        // given
        Context context = createContext();
        doReturn("localhost","www.openshift.com").when(context).getCluster();
        Config config = createConfig(context);
        ApplicationTreeModel model = createApplicationTreeModel(project, config);
        // when
        model.onUpdate(null, config);
        // then
        verify(model).refresh();
    }

    @Test
    public void shouldRefreshIfContextUserTokenChanges() {
        // given
        String user = "papa-smurf";
        String cluster = "localhost";

        Context ctx1 = createContext(user, cluster);
        AuthInfo authInfo1 = createAuthInfo("token1");
        Config cfg1 = createConfig(ctx1, user, authInfo1);

        ApplicationTreeModel model = createApplicationTreeModel(project, cfg1);

        Context ctx2 = createContext(user, cluster);
        AuthInfo authInfo2 = createAuthInfo("token2");
        Config cfg2 = createConfig(ctx2, user, authInfo2);
        // when
        model.onUpdate(null, cfg2);
        // then
        verify(model).refresh();
    }

    @Test
    public void shouldNotRefreshIfContextUserLogout() {
        // given
        String user = "papa-smurf";
        String cluster = "localhost";

        Context ctx1 = createContext(user, cluster);
        AuthInfo authInfo1 = createAuthInfo("token1");
        Config cfg1 = createConfig(ctx1, user, authInfo1);

        ApplicationTreeModel model = createApplicationTreeModel(project, cfg1);

        Context ctx2 = createContext(user, cluster);
        AuthInfo authInfo2 = createAuthInfo(null);
        Config cfg2 = createConfig(ctx2, user, authInfo2);
        // when
        model.onUpdate(null, cfg2);
        // then
        verify(model, never()).refresh();
    }

    protected ApplicationTreeModel createApplicationTreeModel(Project project, Config config) {
        return spy(new ApplicationTreeModel(project) {
            @Override
            protected void initConfigWatcher() {
            }

            @Override
            protected void loadProjectModel(Project project) {
            }

            @Override
            protected void registerProjectListener(Project project) {
            }

            @Override
            protected Config loadConfig() {
                return config;
            }

            @Override
            public synchronized void refresh() {
            }
        });
    }

    private Config createConfig(Context context) {
        return createConfig(context, null, null);
    }

    private Config createConfig(Context context, String user, AuthInfo authInfo) {
        String contextName = "42";
        Config config = mock(Config.class);
        doReturn(contextName).when(config).getCurrentContext();
        NamedContext namedContext = createNamedContext(contextName, context);
        doReturn(Arrays.asList(namedContext)).when(config).getContexts();

        if (authInfo != null) {
            NamedAuthInfo namedAuthInfo = mock(NamedAuthInfo.class);
            doReturn(user).when(namedAuthInfo).getName();
            doReturn(authInfo).when(namedAuthInfo).getUser();
            doReturn(Arrays.asList(namedAuthInfo)).when(config).getUsers();
        }
        return config;
    }

    private NamedContext createNamedContext(String contextName, Context context) {
        NamedContext namedContext = mock(NamedContext.class);
        doReturn(contextName).when(namedContext).getName();
        doReturn(context).when(namedContext).getContext();
        return namedContext;
    }

    private Context createContext() {
        return createContext(null, null);
    }

    private Context createContext(String user, String cluster) {
        Context context = mock(Context.class);
        if (StringUtils.isNotBlank(user)) {
            doReturn(user).when(context).getUser();
        }
        if (StringUtils.isNotBlank(cluster)) {
            doReturn(cluster).when(context).getCluster();
        }
        return context;
    }

    private AuthInfo createAuthInfo(String token) {
        AuthInfo authInfo = mock(AuthInfo.class);
        doReturn(token).when(authInfo).getToken();
        return authInfo;
    }
}
