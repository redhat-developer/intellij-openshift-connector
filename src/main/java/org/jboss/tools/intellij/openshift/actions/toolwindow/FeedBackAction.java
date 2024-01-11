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
import com.redhat.devtools.intellij.telemetry.core.service.TelemetryMessageBuilder.FeedbackMessage;
import org.jboss.tools.intellij.openshift.ui.feedback.FeedBackDialog;
import org.jetbrains.annotations.NotNull;

import static org.jboss.tools.intellij.openshift.telemetry.TelemetryService.asyncSend;
import static org.jboss.tools.intellij.openshift.telemetry.TelemetryService.instance;

public class FeedBackAction extends AnAction {

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        FeedbackMessage feedback = instance().getBuilder().feedback("feedback");
        FeedBackDialog dialog = new FeedBackDialog();
        dialog.show();
        if (dialog.isOK()) {
            feedback.property("satisfaction", dialog.getSatisfaction())
              .property("satisfaction_comment", dialog.getSatisfactionComment())
              .property("recommendation", dialog.getRecommendation())
              .property("recommendation_comment", dialog.getRecommendationComment())
              .property("used_similar_extension", dialog.isUsedSimilarExtension())
              .property("used_similar_extension_name", dialog.getUsedSimilarExtensionName())
              .property("frustrating", dialog.getFrustratingFeature())
              .property("missing", dialog.getMissingFeature())
              .property("best", dialog.getBestFeature())
              .property("contact", dialog.getContact())
              .success();
            asyncSend(feedback);
        } else {
            asyncSend(feedback.aborted());
        }
    }
}
