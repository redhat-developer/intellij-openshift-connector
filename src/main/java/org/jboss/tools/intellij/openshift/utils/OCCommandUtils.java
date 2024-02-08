/*******************************************************************************
 * Copyright (c) 2020 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.tools.intellij.openshift.utils;

import java.util.regex.Pattern;


/**
 * Util class for parsing oc command.
 *
 * @author Josef Kopriva
 */
public final class OCCommandUtils {

    private OCCommandUtils() {
        //hide constructor
    }

    /**
     * Validates is oc command is in correct format with required fields.
     *
     * @param ocCommand the oc command
     * @return false command has not correct format
     */
    public static boolean isValidCommand(String ocCommand) {
        ocCommand = ocCommand.trim();
        return (ocCommand.startsWith("oc login ") && isValidAuthMethod(ocCommand)
                && getServer(ocCommand) != null);
    }

    /**
     * Returns true if authorization schema of oc command is valid (basic/OAuth).
     *
     * @param ocCommand the oc command
     * @return false command has not correct authorization schema
     */
    public static boolean isValidAuthMethod(String ocCommand) {
        ocCommand = ocCommand.trim();
        return (ocCommand.contains(" -u") || ocCommand.contains(" --username") || ocCommand.contains("--token="));
    }

    /**
     * Parses server address from oc command.
     *
     * @param ocCommand the oc command
     * @return server address
     */
    public static String getServer(String ocCommand) {
        String server = applyPattern(ocCommand, "(?<=\\s)https[a-zA-Z0-9:/.-]+", 0);
        if (server != null) {
            return server;
        } else {
            return applyPattern(ocCommand, "(?<=[\\s=])https[a-zA-Z0-9:/.-]+", 0);
        }
    }

    /**
     * Parses token from oc command.
     *
     * @param ocCommand the oc command
     * @return token
     */
    public static String getToken(String ocCommand) {
        return applyPattern(ocCommand, "(?<=--token=)[~a-zA-Z0-9:._-]+", 0);
    }

    /**
     * Parses username from oc command.
     *
     * @param ocCommand the oc command
     * @return username
     */
    public static String getUsername(String ocCommand) {
        String username = applyPattern(ocCommand, "(?<=-u[\\s=])[a-zA-Z0-9:]+", 0);
        if (username != null) {
            return username;
        } else {
            return applyPattern(ocCommand, "(?<=--username[\\s=])[a-zA-Z0-9:]+", 0);
        }
    }

    /**
     * Parses password from oc command.
     *
     * @param ocCommand the oc command
     * @return password
     */
    public static String getPassword(String ocCommand) {
        String password = searchInStringForPattern(ocCommand, "(?<=-p[\\s=])(.*)(?=\\b)");
        if (password != null) {
            return password;
        } else {
            return searchInStringForPattern(ocCommand, "(?<=--password[\\s=])(.*)(?=\\b)");
        }
    }

    private static String searchInStringForPattern(String stringToVerify, String pattern) {
        if (stringToVerify.contains("-p")) {
            return applyPattern(stringToVerify, pattern);
        }
        return null;
    }

    private static String applyPattern(String stringToVerify, String pattern) {
        return applyPattern(stringToVerify, pattern, 1);
    }

    private static String applyPattern(String stringToVerify, String pattern, int group) {
        stringToVerify = stringToVerify.trim();
        Pattern patternToken = Pattern.compile(pattern);
        java.util.regex.Matcher matcherToken = patternToken.matcher(stringToVerify);
        if (matcherToken.find()) {
            return matcherToken.group(group);
        }
        return null;
    }

}