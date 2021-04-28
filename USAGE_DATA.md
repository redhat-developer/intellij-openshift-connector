## [OpenShift Connector by Red Hat](https://github.com/redhat-developer/intellij-openshift-connector)

### Usage Data

* when plugin is started
* when an action is executed
    * action name
    * action duration time
    * action error message (in case of exception)
    * action specific data if applicable (see details below)
* when plugin is shut down

### Specific data collected
* on create:
  * component source type
  * component kind (s2i or devfile)
  * component version (if present)
  * component starter used ( if using devfile )
  * if component used an already present local devfile   
  * if component pushed right after creation
  
* on debug:
  * component language ( java, nodejs, ...)