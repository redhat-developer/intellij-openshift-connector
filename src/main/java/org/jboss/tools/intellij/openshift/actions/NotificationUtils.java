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
package org.jboss.tools.intellij.openshift.actions;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import org.jboss.tools.intellij.openshift.Constants;

public class NotificationUtils {
  public static Notification notifyInformation(String title, String content) {
    Notification notification = new Notification(
      Constants.GROUP_DISPLAY_ID,
      title,
      content,
      NotificationType.INFORMATION);
    Notifications.Bus.notify(notification);
    return notification;
  }

  public static Notification notifyError(String title, String content) {
    Notification notification = new Notification(
      Constants.GROUP_DISPLAY_ID,
      title,
      content,
      NotificationType.ERROR);
    Notifications.Bus.notify(notification);
    return notification;
  }

}
