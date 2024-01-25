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
