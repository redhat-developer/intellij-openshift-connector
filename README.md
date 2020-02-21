# OpenShift Connector
[plugin-repo]: https://plugins.jetbrains.com/plugin/12030-openshift-connector-by-red-hat
[plugin-version-svg]: https://img.shields.io/jetbrains/plugin/v/12030-openshift-connector-by-red-hat.svg
[plugin-downloads-svg]: https://img.shields.io/jetbrains/plugin/d/12030-openshift-connector-by-red-hat.svg

[![Build Status](https://travis-ci.com/redhat-developer/intellij-openshift-connector.svg?branch=master)](https://travis-ci.com/redhat-developer/intellij-openshift-connector)
[![JetBrains plugins][plugin-version-svg]][plugin-repo]
[![JetBrains plugins][plugin-downloads-svg]][plugin-repo]

[![Gitter](https://badges.gitter.im/redhat-developer/openshift-connector.svg)](https://gitter.im/redhat-developer/openshift-connector)

## Overview

A JetBrains IntelliJ plugin for interacting with Red Hat OpenShift cluster. This extension is currently in Preview Mode and supports only Java and Node.js components. We will be supporting other languages in the future releases.

### Running OpenShift Clusters

To run the instance of OpenShift cluster locally, developers can use the following:

* OpenShift 4.x - [CodeReadyContainers](https://cloud.redhat.com/openshift/install/crc/installer-provisioned)
* OpenShift 3.x - [minishift](https://github.com/minishift/minishift/releases) / [CDK](https://developers.redhat.com/products/cdk/download/). For detail analysis of how to setup and run local OpenShift Cluster using minishift, please follow this [wiki](https://github.com/redhat-developer/vscode-openshift-tools/wiki/Starting-Local-OpenShift-Instance).

The extension also supports OpenShift running on Azure, AWS. 


## WARNING !!! Breaking Changes

Post `0.1.1` releases contains breaking changes mentioned below.

* The Components created with previous versions(<=0.0.6) will no longer be visible in OpenShift Application Explorer view.
* New Component, Url and Storage objects are created locally in context folder and not immediatly pushed to the cluster.

> **Please follow the [migration](https://github.com/redhat-developer/intellij-openshift-connector/wiki/Migration-to-v0.1.0) guide to resolve any possible issues.**

In case of any queries, please use the [Feedback & Question](#Feedback-&-Questions) section.

## Commands and features


`OpenShift Connector` supports a number of commands & actions for interacting with OpenShift clusters; these are accessible via the context menu.

### General Commands

* `Log in to cluster` - Log in to your server and save login for subsequent use.
    * Credentials : Log in to the given server with the given credentials.
    * Token : Login using bearer token for authentication to the API server.
* `List catalog components` - List all available Component Types from OpenShift's Image Builder.
* `List catalog services` - Lists all available Services e.g. mysql-persistent. Only visible if the Service Catalog is enabled on the cluster.
* `New Project` - Create new project inside the OpenShift Cluster.
* `About` - Provides the information about the OpenShift tools.
* `Log out` - Logs out of the current OpenShift Cluster.
* `Open Console` - Opens the OpenShift webconsole URL.

#### Actions available for an OpenShift Cluster Project

   * `New Component` - Create a new Component from the Project.
        * local - Use local directory as a source for the Component.
        * git - Use a git repository as the source for the Component.
        * binary - Use binary file as a source for the Component
   * `New Service` - Perform Service Catalog operations when it is enabled.
   * `Delete` - Delete an existing Project.

#### Actions available for an Application in a Project

   * `New Component` - Create a new Component inside the selected Application.
        * local - Use local directory as a source for the Component.
        * git - Use a git repository as the source for the Component.
        * binary - Use binary file as a source for the Component
   * `New Service` - Perform Service Catalog operations when it is enabled.
   * `Describe` - Describe the given Application in terminal window.
   * `Delete` - Delete an existing Application.

#### Actions available for a Component in an Application

##### Components can be in 3 stages:

      pushed - When the components are deployed into the cluster.
      not pushed - When are the components are in local config but NOT deployed into the cluster.
      no context - When there is no context folder associated with the component in the project.

#### Actions for a Pushed Component

   * `New URL` - Expose Component to the outside world. The URLs that are generated using this command, can be used to access the deployed Components from outside the Cluster. Push the component to reflect the changes on the cluster.
   * `New Storage` - Create Storage and mount to a Component. Push the component to reflect the changes on the cluster.
   * `Describe` - Describe the given Component in terminal window.
   * `Show Log` - Retrieve the log for the given Component.
   * `Follow Log` - Follow logs for the given Component.
   * `Link Component` - Link Component to another Component.
   * `Link Service` - Link Component to a Service.
   * `Unlink` - Unlink Component from Component/Service.
   * `Open in Browser` - Open the exposed URL in browser.
   * `Push` - Push the source code to a Component.
   * `Watch` - Watch for changes, update Component on change. This is not supported for git based components.
   * `Debug` - Connect a local debugger with the Component. See the [wiki](https://github.com/redhat-developer/intellij-openshift-connector/wiki/How-to-debug-a-component) page for more.
    details.
   * `Undeploy` - Undeploys a Component from the cluster. The component still resides in the local config.
   * `Delete` - Delete an existing Component from the cluster and removes the local config also.

#### Actions for a Not Pushed Component

   * `New URL` - Expose Component to the outside world. The URLs that are generated using this command, can be used to access the deployed Components from outside the Cluster.
   * `Push` - Push the source code to a Component.
   * `Delete` - Delete an existing Component from the local config.


#### Actions for a no context Component

   * `Describe` - Describe the given Component in terminal window.
   * `Delete` - Delete an existing Component from the local config.
   * `Import` - If the component was created using old version of the extension (`<=0.0.6`), users can use the `Import` action to migrate to latest version and import the metadata changes.

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

<div><img src="https://raw.githubusercontent.com/redhat-developer/intellij-openshift-connector/master/src/main/resources/images/cluster.png" width="15" height="15" /><span style="margin: 20px">Cluster Resource Node</span></div>
<div><img src="https://raw.githubusercontent.com/redhat-developer/intellij-openshift-connector/master/src/main/resources/images/project.png" width="15" height="15" /><span style="margin: 20px">Project Resource</span></div>
<div><img src="https://raw.githubusercontent.com/redhat-developer/intellij-openshift-connector/master/src/main/resources/images/application.png" width="15" height="15" /><span style="margin: 20px">Application Resource</span></div>
<div><img src="https://raw.githubusercontent.com/redhat-developer/intellij-openshift-connector/master/src/main/resources/images/component.png" width="15" height="15" /><span style="margin: 20px">Component Resource</span></div>
<div><img src="https://raw.githubusercontent.com/redhat-developer/intellij-openshift-connector/master/src/main/resources/images/service.png" width="15" height="15" /><span style="margin: 20px">Service Resource</span></div>
<div><img src="https://raw.githubusercontent.com/redhat-developer/intellij-openshift-connector/master/src/main/resources/images/storage.png" width="15" height="15" /><span style="margin: 20px">Storage Resource</span></div>
<div><img src="https://raw.githubusercontent.com/redhat-developer/intellij-openshift-connector/master/src/main/resources/images/url-node.png" width="15" height="15" /><span style="margin: 20px">URL Resource</span></div>

### Dependencies

#### CLI Tools

This extension uses two CLI tools to interact with OpenShift cluster:
* OpenShift Do tool - [odo](https://mirror.openshift.com/pub/openshift-v4/clients/odo/)

> If `odo` tool is located in a directory from `PATH` environment variable it will be used automatically. 
The plugin will detect these dependencies and prompt the user to install if they are missing or have not supported version - choose `Download & Install` when you see an notification for the missing tool.


**NOTE:** This plugin is in Preview mode. The extension support for OpenShift is strictly experimental - assumptions may break, commands and behavior may change!

## Release notes

See the [change log][plugin-repo].

Contributing
============
This is an open source project open to anyone. This project welcomes contributions and suggestions!

For information on getting started, refer to the [CONTRIBUTING instructions](CONTRIBUTING.md).


Feedback & Questions
====================
If you discover an issue please file a bug and we will fix it as soon as possible.
* File a bug in [GitHub Issues](https://github.com/redhat-developer/intellij-openshift-connector/issues).
* Chat with us on [Gitter](https://gitter.im/redhat-developer/openshift-connector).

License
=======
EPL 2.0, See [LICENSE](LICENSE) for more information.
