/*******************************************************************************
 * Copyright (c) 2023 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 * Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.tools.intellij.openshift.telemetry;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class TelemetrySenderTest {

    @Test
    public void anonymizeToken_should_replace_token() {
        // given
        String token = "123456-ABCD/1-2-3-4";
        String msgWithToken = "User token-" + token + ":80 doesn't have permission";
        // when
        String anonymized = TelemetrySender.anonymizeToken(msgWithToken);
        // then
        assertThat(anonymized).doesNotContain(token);
        assertThat(anonymized).contains(TelemetrySender.ANONYMOUS_TOKEN);
    }

    @Test
    public void anonymizeClusterUrl_should_replace_ClusterUrl() {
        // given
        String clusterUrl = "https://api.engint.openshift.com:6443";
        String msgWithClusterUrl = "GET at: " + clusterUrl + "/apis. Message:";
        // when
        String anonymized = TelemetrySender.anonymizeClusterUrl(msgWithClusterUrl);
        // then
        assertThat(anonymized).doesNotContain(clusterUrl);
        assertThat(anonymized).contains(TelemetrySender.ANONYMOUS_CLUSTER_URL);
    }
}
