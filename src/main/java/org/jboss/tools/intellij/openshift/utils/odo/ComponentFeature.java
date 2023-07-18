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
package org.jboss.tools.intellij.openshift.utils.odo;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public enum ComponentFeature {
    DEV(Kind.DEV_KIND, "dev on Cluster", Constants.WATCHING_FOR_CHANGES_IN_THE_CURRENT_DIRECTORY, Collections.singletonList("dev")) {
        public ComponentFeature getPeer() {
            return DEBUG;
        }
    },
    DEV_ON_PODMAN(Kind.DEV_KIND, "dev on Podman", Constants.WATCHING_FOR_CHANGES_IN_THE_CURRENT_DIRECTORY, Arrays.asList("dev", "--platform", Constants.PODMAN, "--forward-localhost")),
    DEBUG(Kind.DEBUG_KIND, "debug", Constants.WATCHING_FOR_CHANGES_IN_THE_CURRENT_DIRECTORY, Arrays.asList("dev", "--debug")) {
        public ComponentFeature getPeer() {
            return DEV;
        }
    },
    DEPLOY(Kind.DEPLOY_KIND, "deploy", Constants.YOUR_DEVFILE_HAS_BEEN_SUCCESSFULLY_DEPLOYED, Collections.singletonList("deploy"), Arrays.asList("delete", "component", "-f"));

    private final Kind kind;
    private final String label;

    private final String output;

    private final List<String> startArgs;

    private final List<String> stopArgs;

    ComponentFeature(Kind kind, String label, String output, List<String> startArgs, List<String> stopArgs) {
        this.kind = kind;
        this.label = label;
        this.output = output;
        this.startArgs = startArgs;
        this.stopArgs = stopArgs;
    }

    ComponentFeature(Kind kind, String label, String output, List<String> startArgs) {
        this(kind, label, output, startArgs, Collections.emptyList());
    }

    public ComponentFeature getPeer() {
        return null;
    }

    public Kind getKind() {
        return kind;
    }

    public String getLabel() {
        return label;
    }

    public List<String> getStartArgs() {
        return startArgs;
    }

    public List<String> getStopArgs() {
        return stopArgs;
    }

    public CharSequence getOutput() {
        return output;
    }

    public static class Constants {

        private Constants() {
            //hide constructor.
        }
        private static final String WATCHING_FOR_CHANGES_IN_THE_CURRENT_DIRECTORY = "Watching for changes in the current directory";
        private static final String YOUR_DEVFILE_HAS_BEEN_SUCCESSFULLY_DEPLOYED = "Your Devfile has been successfully deployed";
        public static final String PODMAN = "podman";
    }

    public enum Kind {
        DEV_KIND("dev"),
        DEBUG_KIND("debug"),
        DEPLOY_KIND("deploy");

        private final String label;

        Kind(String label) {
            this.label = label;
        }

        public String getLabel() {
            return label;
        }
    }
}
