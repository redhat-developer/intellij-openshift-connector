/*******************************************************************************
 * Copyright (c) 2022 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 * Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.intellij.openshift;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.openapi.wm.ex.ToolWindowManagerListener;
import com.intellij.openapi.wm.impl.ToolWindowManagerImpl;
import com.redhat.devtools.intellij.common.gettingstarted.GettingStartedContent;
import com.redhat.devtools.intellij.common.gettingstarted.GettingStartedCourse;
import com.redhat.devtools.intellij.common.gettingstarted.GettingStartedCourseBuilder;
import com.redhat.devtools.intellij.common.gettingstarted.GettingStartedGroupLessons;
import com.redhat.devtools.intellij.common.gettingstarted.GettingStartedLesson;
import org.jboss.tools.intellij.openshift.settings.SettingsState;
import org.jetbrains.annotations.NotNull;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;

public class GettingStartedToolWindow implements ToolWindowFactory {

    private GettingStartedCourse course;
    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        toolWindow.setIcon(AllIcons.Toolwindows.Documentation);
        toolWindow.setStripeTitle("Getting Started");
        ((ToolWindowManagerImpl)ToolWindowManager.getInstance(project)).addToolWindowManagerListener(new ToolWindowManagerListener() {
            @Override
            public void stateChanged(@NotNull ToolWindowManager toolWindowManager) {
                if (hasToShowToolWindow()) {
                    toolWindow.show();
                }
            }
        });
    }

    private boolean hasToShowToolWindow() {
        String version = course.getVersion();
        if (SettingsState.getInstance().courseVersion.equals(version)) {
            return false;
        }
        SettingsState.getInstance().courseVersion = version;
        return true;
    }

    @Override
    public void init(@NotNull ToolWindow toolWindow) {
        final String version = "1.0";
        course = new GettingStartedCourseBuilder()
                .createGettingStartedCourse(
                        version,
                        "Learn IDE features for OpenShift Toolkit",
                        "",
                        getFeedbackURL())
                .withGroupLessons(buildOpenShiftFuncLessons())
                .build();
        GettingStartedContent content = new GettingStartedContent(toolWindow, "", course);
        toolWindow.getContentManager().addContent(content);
    }

    private URL getFeedbackURL() {
        URL feedbackUrl = null;
        try {
            feedbackUrl = new URL("https://github.com/redhat-developer/intellij-openshift-connector");
        } catch (MalformedURLException ignored) { }
        return feedbackUrl;
    }

    @Override
    public boolean shouldBeAvailable(@NotNull Project project) {
        return true;
    }

    private GettingStartedGroupLessons buildOpenShiftFuncLessons() {
        URL gifLoginLesson = getLessonGif("login.gif");
        GettingStartedLesson loginLesson = new GettingStartedLesson(
                "Login/Provision OpenShift cluster",
                "<html><p>Users can login to an existing OpenShift cluster using the " +
                        " <b>Log in to cluster</b> context menu item.</p>" +
                        "<p>To provision a new OpenShift cluster, <b>Log in to cluster</b> context menu item opens a " +
                        "guided workflow from where you can provision a Red Hat Developer Sandbox cluster using the " +
                        "<b>Red Hat Developer Sandbox</b> link.</p></html>",
                Collections.emptyList(),
                gifLoginLesson
        );

        URL gifBrowseDevfileRegistryLesson = getLessonGif("browse-devfile-registries.gif");
        GettingStartedLesson browseDevfileRegistryLesson = new GettingStartedLesson(
                "Browse all the Devfile stacks from Devfile registries",
                "<html><p>Select a specific devfile registry to create a component directly.</p></html>",
                Collections.emptyList(),
                gifBrowseDevfileRegistryLesson
        );

        URL gifCreateComponentLesson = getLessonGif("create-component.gif");
        GettingStartedLesson createComponentLesson = new GettingStartedLesson(
                "Create a Component",
                "<html><p>Create a component in the local workspace using the selected started project and " +
                "associated 'devfile.yaml' configuration. Devfile is a manifest file that contains information about " +
                "various resources (URL, Storage, Services, etc.) that correspond to your component</p></html>",
                Collections.emptyList(),
                gifCreateComponentLesson
        );

        URL gifStartDevLesson = getLessonGif("start-dev.gif");
        GettingStartedLesson startDevLesson = new GettingStartedLesson(
                "Start a component in development mode",
                "<html><p>This is inner loop development and allows you to code, build, run and test the " +
                        "application in a continuous workflow. It continuously watches the directory for any new " +
                        "changes and automatically syncs them with the application running on the cluster.</p>" +
                        "<p>This also forwards a port on the development system to the port on the container cluster " +
                        "allowing you remote access to your deployed application</p></html>",
                Collections.emptyList(),
                gifStartDevLesson
        );

        URL gifStartDebugLesson = getLessonGif("start-debug.gif");
        GettingStartedLesson startDebugLesson = new GettingStartedLesson(
                "Debug the component",
                "<html><p>Start the component in debug mode. The devfile.yaml should be specified with the " +
                        "debug port as an endpoint for the debug to work.</p></html>",
                Collections.emptyList(),
                gifStartDebugLesson
        );

        URL gifStartDeployLesson = getLessonGif("start-deploy.gif");
        GettingStartedLesson startDeployLesson = new GettingStartedLesson(
                "Deploy component on the cluster",
                "<html><p>Deploy components in a similar manner they would be deployed by a CI/CD system, " +
                        "by first building the images of the containers to deploy, then by deploying the " +
                        "OpenShift/Kubernetes resources necessary to deploy the components.</p></html>",
                Collections.emptyList(),
                gifStartDeployLesson
        );

        GettingStartedGroupLessons groupLessons = new GettingStartedGroupLessons(
                "Getting Started with OpenShift Toolkit",
                "Start your application development on OpenShift or Kubernetes",
                loginLesson,
                browseDevfileRegistryLesson,
                createComponentLesson,
                startDevLesson,
                startDebugLesson,
                startDeployLesson);
        return groupLessons;
    }

    private URL getLessonGif(String name) {
        return GettingStartedToolWindow.class.getResource("/gettingstarted/" + name);
    }
}
