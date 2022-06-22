# How to contribute

Contributions are essential for keeping this plugin great.
We try to keep it as easy as possible to contribute changes and we are
open to suggestions for making it even easier.
There are only a few guidelines that we need contributors to follow.

## First Time Setup
1. Install prerequisites:
   * [Java Development Kit](https://adoptopenjdk.net/)
2. Fork and clone the repository
3. `cd intellij-openshift-connector`
4. Import the folder as a project in JetBrains IntelliJ

## Run the extension locally

1. From root folder, run the below command.
    ```bash
    $ ./gradlew runIde
    ```

2. Once the plugin is installed and reloaded, there will be an OpenShift Icon in the Tool Windows list (bottom left).

## Red Hat Developer Sandbox integration

In order to test the Red Hat Developer Sandbox workflow, we have developed a fake registration server that has less
limitations than the production one and allow to test the whole workflow easily.

1. Start the fake registration server:
    ```bash
    $ ./gradlew runSandbox
    ```

2. In the build.gradle file, uncomment the line:
   `//systemProperties['jboss.sandbox.api.endpoint'] = 'http://localhost:3000'`

    then run
    ```bash
    $ ./gradlew runIde
    ```

> If you have any questions or run into any problems, please post an issue - we'll be very happy to help.
### Certificate of Origin

By contributing to this project you agree to the Developer Certificate of
Origin (DCO). This document was created by the Linux Kernel community and is a
simple statement that you, as a contributor, have the legal right to make the
contribution. See the [DCO](DCO) file for details.
