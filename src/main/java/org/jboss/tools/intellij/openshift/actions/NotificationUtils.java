package org.jboss.tools.intellij.openshift.actions;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import org.jboss.tools.intellij.openshift.Constants;

public class NotificationUtils {
  public static void notifySuccess(String title, String content) {
    Notifications.Bus.notify(new Notification(
      Constants.GROUP_DISPLAY_ID,
      title,
      content,
      NotificationType.INFORMATION));
  }

  public static void notifyError(String title, String content) {
    Notifications.Bus.notify(new Notification(
      Constants.GROUP_DISPLAY_ID,
      title,
      content,
      NotificationType.ERROR));
  }

}
