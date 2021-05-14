# OpenShift Connector
[plugin-repo]: https://plugins.jetbrains.com/plugin/12030-openshift-connector-by-red-hat
[plugin-version-svg]: https://img.shields.io/jetbrains/plugin/v/12030-openshift-connector-by-red-hat.svg
[plugin-downloads-svg]: https://img.shields.io/jetbrains/plugin/d/12030-openshift-connector-by-red-hat.svg

[![Build status](https://github.com/redhat-developer/intellij-openshift-connector/workflows/Java%20CI%20with%20Gradle/badge.svg)](https://github.com/redhat-developer/intellij-openshift-connector/actions?query=workflow%3A%22Java+CI+with+Gradle%22)
[![JetBrains plugins][plugin-version-svg]][plugin-repo]
[![JetBrains plugins][plugin-downloads-svg]][plugin-repo]

[![Gitter](https://badges.gitter.im/redhat-developer/openshift-connector.svg)](https://gitter.im/redhat-developer/openshift-connector)

## Overview

A JetBrains IntelliJ plugin for interacting with Red Hat OpenShift cluster. This extension is currently in Preview Mode and supports only Java and Node.js components. We will be supporting other languages in the future releases.

### Running OpenShift Clusters

To run the instance of OpenShift cluster locally, developers can use the following:

* OpenShift 4.x - [CodeReadyContainers](https://cloud.redhat.com/openshift/install/crc/installer-provisioned)
* OpenShift 3.x - [minishift](https://github.com/minishift/minishift/releases) / [CDK](https://developers.redhat.com/products/cdk/download/). For detail analysis of how to set up and run local OpenShift Cluster using minishift, please follow this [wiki](https://github.com/redhat-developer/vscode-openshift-tools/wiki/Starting-Local-OpenShift-Instance).

The extension also supports OpenShift running on Azure, AWS.

## New features

### Improved OpenShift Container Platform 4 compatibility

Although previous versions of the plugin were compatible with OCP 4, there were some small issues that have been fixes (ie paste login command in the Login wizard)

### Telemetry data collection

The plugin collects anonymous usage data, if enabled, and sends it to Red Hat servers to help improve our products and services. 

![](images/0.6.0/openshift1.png)


## Features

### Starter projects

When you create a component from a devfile, if the selected devfile contains starter projects (sample projects that contain source code that can be used to bootstrap your component) and if the selected local module is empty, you can optionally select one of those starter projects whose content will be copied to your local module before the component is created.

![](images/0.5.0/openshift1.gif)

### Enhanced devfile editing experience

Devfile based components now have a local devfile added to the local module when the component is created. A devfile is a YAML file with a specific syntax. It is now possible for the end user to edit this file (in case specific settings needs to be updated or added).
The YAML editor will now assist during edition of this file with syntax validation and code assist

![](images/0.5.0/openshift2.gif)

### Devfile support

The plugin is now based on Odo 2.x, which brings support for Devfiles. A devfile is describing the way your component should be built, rebuilt, debugged. When creating a component, there is now two different choices:

- Pick a devfile from a registry. The registry will contain devfiles specific to your component language, framework and variants (ex Java/Quarkus, Java/SpringBoot, Python/Django,...)
- Your component has its own devfile and the plugin will automatically use it if it's there

For more information about devfiles, see the [devfile docs](https://docs.devfile.io)

![](images/0.4.0/openshift1.gif)

## WARNING

### Known issues

When creating a new project, it is possible that the newly created project will not immediately
appear in the Application Explorer tree. This is caused by a synchronization [issue](https://github.com/openshift/odo/issues/4426) in the underlying
CLI tool odo. A workaround is to refresh the tree.

### Breaking Changes

Post `0.1.1` releases contains breaking changes mentioned below.

* The Components created with previous versions(<=0.0.6) will no longer be visible in OpenShift Application Explorer view.
* New Component, Url and Storage objects are created locally in context folder and not immediately pushed to the cluster.

> **Please follow the [migration](https://github.com/redhat-developer/intellij-openshift-connector/wiki/Migration-to-v0.1.0) guide to resolve any possible issues.**

In case of any queries, please use the [Feedback & Question](#Feedback-&-Questions) section.

## Commands and features


`OpenShift Connector` supports a number of commands & actions for interacting with OpenShift clusters; these are accessible via the context menu.

### General Commands

* `Log in to cluster` - Log in to your server and save login for subsequent use.
    * Credentials : Log in to the given server with the given credentials.
    * Token : Login using bearer token for authentication to the API server.
* `Log out` - Logs out of the current OpenShift Cluster.
* `List catalog components` - List all available Component Types from OpenShift's Image Builder.
* `List catalog services` - Lists all available Services e.g. mysql-persistent. Only visible if the Service Catalog is enabled on the cluster.
* `New Project` - Create new project inside the OpenShift Cluster.
* `Open Console` - Opens the OpenShift webconsole URL.
* `Refresh`- Refresh the tree with latest resources from the cluster.
* `About` - Provides the information about the OpenShift tools.

#### Actions available for an OpenShift Cluster Project

   * `New Component` - Create a new Component from the Project.
        * local - Use a local directory as a source for the Component.
        * git - Use a git repository as the source for the Component.
        * binary - Use a binary file as a source for the Component
   * `New Service` - Perform Service Catalog operations when it is enabled.
   * `Delete` - Delete an existing Project.

#### Actions available for an Application in a Project

   * `New Component` - Create a new Component inside the selected Application.
        * local - Use a local directory as a source for the Component.
        * git - Use a git repository as the source for the Component.
        * binary - Use a binary file as a source for the Component
   * `New Service` - Perform Service Catalog operations when it is enabled.
   * `Describe` - Describe the given Application in a terminal window.
   * `Delete` - Delete an existing Application.

#### Actions available for a Component in an Application

##### Components can be in 3 stages:

      pushed - When the components are deployed into the cluster.
      not pushed - When are the components are in local config but NOT deployed into the cluster.
      no context - When there is no context folder associated with the component in the project.

#### Actions for a Pushed Component

   * `New URL` - Expose Component to the outside world. The URLs that are generated using this command, can be used to access the deployed Components from outside the Cluster. Push the component to reflect the changes on the cluster.
   * `New Storage` - Create Storage and mount to a Component. Push the component to reflect the changes on the cluster.
   * `Describe` - Describe the given Component in a terminal window.
   * `Show Log` - Retrieve the log for the given Component.
   * `Follow Log` - Follow logs for the given Component.
   * `Link Component` - Link Component to another Component.
   * `Link Service` - Link Component to a Service.
   * `Unlink` - Unlink Component from Component/Service.
   * `Open in Browser` - Open the exposed URL in a browser.
   * `Push` - Push the source code to a Component.
   * `Watch` - Watch for changes, update Component on change. This is not supported for Git based components.
   * `Debug` - Connect a local debugger with the Component. See the [wiki](https://github.com/redhat-developer/intellij-openshift-connector/wiki/How-to-debug-a-component) page for more details.
   * `Undeploy` - Undeploy a Component from the cluster. The component still resides in the local config.
   * `Delete` - Delete an existing Component from the cluster and removes the local config also.

#### Actions for a Not Pushed Component

   * `New URL` - Expose Component to the outside world. The URLs that are generated using this command, can be used to access the deployed Components from outside the Cluster.
   * `Push` - Push the source code to a Component.
   * `Delete` - Delete an existing Component from the local config.


#### Actions for a no context Component

   * `Describe` - Describe the given Component in a terminal window.
   * `Delete` - Delete an existing Component from the local config.
   * `Import` - If the component was created using old version of the extension (`<=0.0.6`), users can use the `Import` action to migrate to the latest version and import the metadata changes.

#### Actions available for a URL in a Component

   * `Delete` - Delete a URL from a Component.
   * `Open URL` - Click on the icon opens the specific URL in Browser.

#### Actions available for a Storage in a Component

   * `Delete` - Delete a Storage from a Component.

#### Actions available for a Service in an Application

   * `Describe` - Describe a Service Type for a selected Component
   * `Delete` - Delete a Service from an Application

**NOTE:** Currently we support creation of one component per folder. Multiple components from a folder might be supported in future releases.

#### Icons Representation

<div><img src="https://raw.githubusercontent.com/redhat-developer/intellij-openshift-connector/master/src/main/resources/images/cluster.png" width="15" height="15" alt="Cluster Resource"/><span style="margin: 20px"> Cluster Resource</span></div>
<div><img src="https://raw.githubusercontent.com/redhat-developer/intellij-openshift-connector/master/src/main/resources/images/project.png" width="15" height="15" alt="Project Resource"/><span style="margin: 20px"> Project Resource</span></div>
<div><img src="https://raw.githubusercontent.com/redhat-developer/intellij-openshift-connector/master/src/main/resources/images/application.png" width="15" height="15" alt="Application Resource"/><span style="margin: 20px"> Application Resource</span></div>
<div><img src="https://raw.githubusercontent.com/redhat-developer/intellij-openshift-connector/master/src/main/resources/images/component.png" width="15" height="15" alt="Component Resource"/><span style="margin: 20px"> Component Resource</span></div>
<div><img src="https://raw.githubusercontent.com/redhat-developer/intellij-openshift-connector/master/src/main/resources/images/service.png" width="15" height="15" alt="Service Resource"/><span style="margin: 20px"> Service Resource</span></div>
<div><img src="https://raw.githubusercontent.com/redhat-developer/intellij-openshift-connector/master/src/main/resources/images/storage.png" width="15" height="15" alt="Storage Resource"/><span style="margin: 20px"> Storage Resource</span></div>
<div><img src="https://raw.githubusercontent.com/redhat-developer/intellij-openshift-connector/master/src/main/resources/images/url-node.png" width="15" height="15" alt="URL Resource"/><span style="margin: 20px"> URL Resource</span></div>
<div><img src="https://raw.githubusercontent.com/redhat-developer/intellij-openshift-connector/master/src/main/resources/images/url-node-secure.png" width="15" height="15" alt="Secure URL Resource"/><span style="margin: 20px"> Secure URL Resource</span></div>

### Dependencies

#### CLI Tools

This extension uses the following CLI tool to interact with OpenShift cluster:

* odo - [odo](https://mirror.openshift.com/pub/openshift-v4/clients/odo/)

> If `odo` tool is located in a directory from `PATH` environment variable it will be used automatically. 
The plugin will detect these dependencies and prompt the user to install if they are missing or have not supported version - choose `Download & Install` when you see a notification for the missing tool.


**NOTE:** This plugin is in Preview mode. The extension support for OpenShift is strictly experimental - assumptions may break, commands and behavior may change!

## Release notes

See the [change log][plugin-repo].

Data and Telemetry
==================
The OpenShift plugin collects anonymous [usage data](USAGE_DATA.md) and sends it to Red Hat servers to help improve our products and services. Read our [privacy statement](https://developers.redhat.com/article/tool-data-collection) to learn more. This extension respects the Red Hat Telemetry setting which you can learn more about at [https://github.com/redhat-developer/intellij-redhat-telemetry#telemetry-reporting](https://github.com/redhat-developer/intellij-redhat-telemetry#telemetry-reporting)



Contributing
============
This is an open source project open to anyone. This project welcomes contributions and suggestions!

For information on getting started, refer to the [CONTRIBUTING instructions](CONTRIBUTING.md).


Feedback & Questions
====================
If you discover an issue please file a bug, and we will fix it as soon as possible.
* File a bug in [GitHub Issues](https://github.com/redhat-developer/intellij-openshift-connector/issues).
* Chat with us on [Gitter](https://gitter.im/redhat-developer/openshift-connector).

License
=======
EPL 2.0, See [LICENSE](LICENSE) for more information.