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
package org.jboss.tools.intellij.openshift.utils;

import java.util.concurrent.CompletionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ExceptionUtils {

  private static final Pattern PATTERN_COMPLETION_EXCEPTION_MESSAGE = Pattern.compile(
    "\"message\": \"([\\s\\S]+?)\"\\n", Pattern.MULTILINE);

  private ExceptionUtils() {}

  public static String getMessage(Exception e) {
    if (e instanceof CompletionException) {
      return getMessage((CompletionException) e);
    } else {
      return e.getMessage();
    }
  }

  public static String getMessage(CompletionException e) {
    if (e.getMessage() == null) {
      return null;
    }
    Matcher matcher = PATTERN_COMPLETION_EXCEPTION_MESSAGE.matcher(e.getMessage());
    if (!matcher.find()) {
      return e.getMessage();
    }
    return matcher.group(1);
  }
}

