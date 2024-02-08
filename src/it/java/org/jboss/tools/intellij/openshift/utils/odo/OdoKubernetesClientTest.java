/*******************************************************************************
 * Copyright (c) 2024 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 * Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.tools.intellij.openshift.utils.odo;

import com.intellij.openapi.project.Project;
import com.intellij.util.messages.MessageBus;
import com.intellij.util.messages.MessageBusConnection;
import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.Namespace;
import io.fabric8.kubernetes.api.model.NamespaceList;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.api.model.Secret;
import io.fabric8.kubernetes.api.model.SecretList;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientException;
import io.fabric8.kubernetes.client.dsl.MixedOperation;
import io.fabric8.kubernetes.client.dsl.NonNamespaceOperation;
import io.fabric8.kubernetes.client.dsl.Resource;
import io.fabric8.openshift.api.model.ProjectList;
import io.fabric8.openshift.client.OpenShiftClient;
import io.fabric8.openshift.client.dsl.ProjectOperation;
import org.jboss.tools.intellij.openshift.utils.odo.OdoCli.TelemetryReport;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

public class OdoKubernetesClientTest {

  private KubernetesClient kubernetesClient;

  private OpenShiftClient openShiftClient;

  private Odo odo;

  @Before
  public void before() throws MalformedURLException {
    this.kubernetesClient = mock(KubernetesClient.class);
    doReturn(new URL("https://localhost"))
      .when(kubernetesClient).getMasterUrl();
    this.openShiftClient = mock(OpenShiftClient.class);
    this.odo = createOdo(kubernetesClient, openShiftClient);
  }

  @Test
  public void getCurrentNamespace_should_return_client_namespace() throws IOException {
    // given
    final String currentNamespace = "luke skywalker";
    doReturn(currentNamespace)
      .when(kubernetesClient).getNamespace();
    // when
    String namespace = odo.getCurrentNamespace();
    // then
    assertThat(namespace).isEqualTo(currentNamespace);
  }

  @Test
  public void getCurrentNamespace_should_return_default_if_there_is_no_client_namespace() throws IOException {
    // given
    doReturn(null)
      .when(kubernetesClient).getNamespace();
    // when
    String namespace = odo.getCurrentNamespace();
    // then
    assertThat(namespace).isEqualTo("default");
  }

  @Test
  public void getNamespaces_should_return_all_namespaces_if_kubernetes_cluster() throws IOException {
    // given
    List<Namespace> namespaces = Arrays.asList(
      mockResource("luke", Namespace.class),
      mockResource("obi wan", Namespace.class),
      mockResource("yoda", Namespace.class));
    NamespaceList namespacesList = mock(NamespaceList.class);
    doReturn(namespaces)
      .when(namespacesList).getItems();
    NonNamespaceOperation<Namespace, NamespaceList, Resource<Namespace>> namespacesOperation = namespaces(kubernetesClient); // client.namespaces()
    doReturn(namespacesList)
      .when(namespacesOperation).list();
    Odo odo = createOdo(kubernetesClient, null); // no openshift client -> kubernetes cluster
    // when
    List<String> namespaceNames = odo.getNamespaces();
    // then
    assertThat(namespaceNames).isEqualTo(Arrays.asList(
      "luke",
      "obi wan",
      "yoda"));
  }

  @Test
  public void getNamespaces_should_return_all_projects_if_openshift_cluster() throws IOException {
    // given
    List<io.fabric8.openshift.api.model.Project> projects = Arrays.asList(
      mockResource("Palpatine", io.fabric8.openshift.api.model.Project.class),
      mockResource("Lord Dooku", io.fabric8.openshift.api.model.Project.class),
      mockResource("Darth Vader", io.fabric8.openshift.api.model.Project.class));
    ProjectList projectsList = mock(ProjectList.class);
    doReturn(projects)
      .when(projectsList).getItems();
    ProjectOperation projectsOperation = projects(openShiftClient); // client.projects()
    doReturn(projectsList)
      .when(projectsOperation).list();
    Odo odo = createOdo(kubernetesClient, openShiftClient); // openshift client -> openshift cluster
    // when
    List<String> projectNames = odo.getNamespaces();
    // then
    assertThat(projectNames).isEqualTo(Arrays.asList(
      "Palpatine",
      "Lord Dooku",
      "Darth Vader"));
  }

  @Test
  public void isOpenShift_should_return_true_if_has_OpenShift_client() {
    // given
    Odo odo = createOdo(kubernetesClient, openShiftClient); // openshift client exists -> openshift cluster
    // when
    boolean found = odo.isOpenShift();
    // then
    assertThat(found).isTrue();
  }

  @Test
  public void isOpenShift_should_return_false_if_has_no_OpenShift_client() {
    // given
    Odo odo = createOdo(kubernetesClient, null); // no openshift client -> kubernetes cluster
    // when
    boolean found = odo.isOpenShift();
    // then
    assertThat(found).isFalse();
  }

  @Test
  public void namespaceExists_should_return_true_if_project_exists() {
    // given
    Odo odo = createOdo(kubernetesClient, openShiftClient); // openshift client exists -> openshift cluster
    io.fabric8.openshift.api.model.Project project = mockResource("yoda", io.fabric8.openshift.api.model.Project.class);
    get(project, withName(projects(openShiftClient))); // client.projects().withName().get()
    // when
    boolean found = odo.namespaceExists("yoda");
    // then
    assertThat(found).isTrue();
  }

  @Test
  public void namespaceExists_should_return_false_if_project_doesnt_exist() {
    // given
    Odo odo = createOdo(kubernetesClient, openShiftClient); // openshift client exists -> openshift cluster
    get(null, withName(projects(openShiftClient))); // client.projects().withName().get()
    // when
    boolean found = odo.namespaceExists("yoda");
    // then
    assertThat(found).isFalse();
  }

  @Test
  public void namespaceExists_should_return_true_if_namespace_exists() {
    // given
    Odo odo = createOdo(kubernetesClient, null); // openshift client doesnt exist-> kubernetes cluster
    Namespace namespace = mockResource("obi wan", Namespace.class);
    get(namespace, withName(namespaces(kubernetesClient))); // client.projects().withName().get()
    // when
    boolean found = odo.namespaceExists("obi wan");
    // then
    assertThat(found).isTrue();
  }

  @Test
  public void namespaceExists_should_return_false_if_namespace_doesnt_exist() {
    // given
    Odo odo = createOdo(kubernetesClient, null); // openshift client doesnt exist-> kubernetes cluster
    get(null, withName(namespaces(kubernetesClient))); // client.projects().withName().get()
    // when
    boolean found = odo.namespaceExists("yoda");
    // then
    assertThat(found).isFalse();
  }

  @Test
  public void isAuthorized_should_return_true_if_can_list_secrets() {
    // given
    secrets(kubernetesClient);
    Odo odo = createOdo(kubernetesClient, openShiftClient);
    // when
    boolean found = odo.isAuthorized();
    // then
    assertThat(found).isTrue();
  }

  @Test
  public void isAuthorized_should_return_false_if_listing_secrets_throws_unauthorized() {
    // given
    MixedOperation<Secret, SecretList, Resource<Secret>> secretsOperation = secrets(kubernetesClient);
    doThrow(new KubernetesClientException("unauthorized", HttpURLConnection.HTTP_UNAUTHORIZED, null))
      .when(secretsOperation).list();
    Odo odo = createOdo(kubernetesClient, openShiftClient);
    // when
    boolean found = odo.isAuthorized();
    // then
    assertThat(found).isFalse();
  }

  @Test
  public void isAuthorized_should_return_false_if_listing_secrets_throws_forbidden() {
    // given
    MixedOperation<Secret, SecretList, Resource<Secret>> secretsOperation = secrets(kubernetesClient);
    doThrow(new KubernetesClientException("forbidden", HttpURLConnection.HTTP_FORBIDDEN, null))
      .when(secretsOperation).list();
    Odo odo = createOdo(kubernetesClient, openShiftClient);
    // when
    boolean found = odo.isAuthorized();
    // then
    assertThat(found).isFalse();
  }

  @Test(expected = KubernetesClientException.class)
  public void isAuthorized_should_throw_if_listing_secrets_throws_other_KubernetesException() {
    // given
    MixedOperation<Secret, SecretList, Resource<Secret>> secretsOperation = secrets(kubernetesClient);
    doThrow(new KubernetesClientException("not found", HttpURLConnection.HTTP_NOT_FOUND, null))
      .when(secretsOperation).list();
    Odo odo = createOdo(kubernetesClient, openShiftClient);
    // when
    boolean found = odo.isAuthorized();
    // then
  }

  private OdoCli createOdo(KubernetesClient kubernetesClient, OpenShiftClient openShiftClient) {
    Project project = mock(Project.class);
    String command = "Star Wars";
    MessageBusConnection connection = mock(MessageBusConnection.class);
    MessageBus bus = mock(MessageBus.class);
    doReturn(connection)
      .when(bus).connect();
    Supplier<KubernetesClient> kubernetesClientFactory = () -> kubernetesClient;
    Function<KubernetesClient, OpenShiftClient> openShiftClientFactory = client -> openShiftClient;
    Function<String, Map<String, String>> envVarFactory = url -> new HashMap<>();
    TelemetryReport telemetryReport = mock(TelemetryReport.class);
    return new OdoCli(project, command, bus, kubernetesClientFactory, openShiftClientFactory, envVarFactory, telemetryReport);
  }

  private static <R extends HasMetadata> R mockResource(String name, Class<R> clazz) {
    R resource = mock(clazz);
    ObjectMeta meta = mockMetadata(name);
    doReturn(meta)
      .when(resource).getMetadata();
    return resource;
  }

  private static ObjectMeta mockMetadata(String name) {
    ObjectMeta meta = mock(ObjectMeta.class);
    doReturn(name)
      .when(meta).getName();
    return meta;
  }

  private static NonNamespaceOperation<Namespace, NamespaceList, Resource<Namespace>> namespaces(KubernetesClient client) {
    NonNamespaceOperation<Namespace, NamespaceList, Resource<Namespace>> namespacesOperation = mock(NonNamespaceOperation.class);
    doReturn(namespacesOperation)
      .when(client).namespaces();
    return namespacesOperation;
  }

  private static ProjectOperation projects(OpenShiftClient client) {
    ProjectOperation projectsOperation = mock(ProjectOperation.class);
    doReturn(projectsOperation)
      .when(client).projects();
    return projectsOperation;
  }

  private static <R> Resource<R> withName(NonNamespaceOperation<?, ?, R> nonNamespaceOperation) {
    Resource<R> resource = mock(Resource.class);
    doReturn(resource)
      .when(nonNamespaceOperation).withName(anyString());
    return resource;
  }

  private static <R> HasMetadata get(HasMetadata hasMetadata, Resource<R> resource) {
    doReturn(hasMetadata)
      .when(resource).get();
    return hasMetadata;
  }

  private static MixedOperation<Secret, SecretList, Resource<Secret>> secrets(KubernetesClient client) {
    MixedOperation<Secret, SecretList, Resource<Secret>> secretsOperation = mock(MixedOperation.class);
    doReturn(secretsOperation)
      .when(client).secrets();
    return secretsOperation;
  }
}
