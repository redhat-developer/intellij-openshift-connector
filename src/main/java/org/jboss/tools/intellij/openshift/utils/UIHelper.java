package org.jboss.tools.intellij.openshift.utils;

import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;

import java.util.function.Supplier;

public class UIHelper {
  public static void executeInUI(Runnable runnable) {
    if (ApplicationManager.getApplication().isDispatchThread()) {
      runnable.run();
    } else {
      ApplicationManager.getApplication().invokeAndWait(runnable);
    }
  }

  public static <T> T executeinUI(Supplier<T> supplier) {
    if (ApplicationManager.getApplication().isDispatchThread()) {
      return supplier.get();
    } else {
      final Object val[] = new Object[1];
      ApplicationManager.getApplication().invokeAndWait(() -> val[0] = supplier.get());
      return (T) val[0];
    }
  }
}
