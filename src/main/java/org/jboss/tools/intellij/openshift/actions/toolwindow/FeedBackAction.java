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
package org.jboss.tools.intellij.openshift.actions.toolwindow;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jboss.tools.intellij.openshift.telemetry.TelemetrySender;
import org.jboss.tools.intellij.openshift.telemetry.TelemetryService;
import org.jboss.tools.intellij.openshift.ui.feedback.FeedBackDialog;
import org.jetbrains.annotations.NotNull;

public class FeedBackAction extends AnAction {

    protected TelemetrySender telemetrySender;

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        telemetrySender = new TelemetrySender("feedback");
        FeedBackDialog dialog = new FeedBackDialog();
        dialog.show();
        if (dialog.isOK()) {
            telemetrySender.addProperty("satisfaction", dialog.getSatisfaction());
            telemetrySender.addProperty("satisfaction_comment", dialog.getSatisfactionComment());
            telemetrySender.addProperty("recommendation", dialog.getRecommendation());
            telemetrySender.addProperty("recommendation_comment", dialog.getRecommendationComment());
            telemetrySender.addProperty("used_similar_extension", dialog.isUsedSimilarExtension());
            telemetrySender.addProperty("used_similar_extension_name", dialog.getUsedSimilarExtensionName());
            telemetrySender.addProperty("frustrating", dialog.getFrustratingFeature());
            telemetrySender.addProperty("missing", dialog.getMissingFeature());
            telemetrySender.addProperty("best", dialog.getBestFeature());
            telemetrySender.addProperty("contact", dialog.getContact());
            telemetrySender.sendTelemetryResults(TelemetryService.TelemetryResult.SUCCESS);
        } else {
            telemetrySender.sendTelemetryResults(TelemetryService.TelemetryResult.ABORTED);
        }
    }
}
