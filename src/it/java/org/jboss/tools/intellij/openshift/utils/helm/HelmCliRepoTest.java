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
        // given openshift repo was added to helm repos
        String name = "openshift";
        String url = "https://charts.openshift.io/";
        helm.addRepo(name, url);
        // when
        List<HelmRepository> repositories = helm.listRepos();
        // then
        boolean found = repositories.stream().anyMatch((HelmRepository repository) ->
            repository.getName().equals(name)
              && repository.getUrl().equals(url)
        );
        assertTrue(found);
    }

}
