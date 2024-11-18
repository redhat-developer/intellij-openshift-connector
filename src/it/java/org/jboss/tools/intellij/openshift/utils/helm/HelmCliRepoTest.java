/*******************************************************************************
 * Copyright (c) 2024 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 * Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.tools.intellij.openshift.utils.helm;


import java.io.IOException;
import java.util.List;

public class HelmCliRepoTest extends HelmCliTest {

    public void testListRepos_should_list_repo_that_was_added() throws IOException {
        String name = "openshift";
        try {
            // given openshift repo was added to helm repos
            String url = "https://charts.openshift.io/";
            helm.addRepo(name, url, null);
            // when
            List<HelmRepository> repositories = helm.listRepos();
            // then
            boolean found = repositories.stream().anyMatch((HelmRepository repository) ->
              repository.getName().equals(name)
                && repository.getUrl().equals(url)
            );
            assertTrue(found);
        } finally {
            silentlyRemoveRepos(name);
        }
    }

    public void testRemoveRepos_should_remove_repo_that_was_added() throws IOException {
        String name = "openshift-charts";
        String name2 = "openshift-charts2";
        try {
            // given openshift repo was added to helm repos
            helm.addRepo(name, "https://charts.openshift.io/", null);
            helm.addRepo(name2, "https://charts.openshift.io/", null);
            List<String> helmRepositoryNames = helm.listRepos().stream()
              .map(HelmRepository::getName)
              .toList();
            assertTrue(helmRepositoryNames.contains(name));
            assertTrue(helmRepositoryNames.contains(name2));
            // when
            helm.removeRepos(name, name2);
            // then
            helmRepositoryNames = helm.listRepos().stream()
              .map(HelmRepository::getName)
              .toList();
            assertFalse(helmRepositoryNames.contains(name));
            assertFalse(helmRepositoryNames.contains(name2));
        } finally {
            silentlyRemoveRepos(name, name2);
        }
    }

    private void silentlyRemoveRepos(String... names) {
        try {
            helm.removeRepos(names);
        } catch(Exception e) {
            // ignore
        }
    }
}
