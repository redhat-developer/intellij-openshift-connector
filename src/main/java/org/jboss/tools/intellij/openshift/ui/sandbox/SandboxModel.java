/*******************************************************************************
 * Copyright (c) 2022 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 * Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.tools.intellij.openshift.ui.sandbox;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.NlsContexts;
import com.intellij.ui.wizard.WizardModel;

/**
 * @author Red Hat Developers
 *
 */
public class SandboxModel extends WizardModel {
  public static final String PROPERTY_ID_TOKEN = "IDToken";
  
  public static final String PROPERTY_PHONE_NUMBER = "phoneNumber";
  
  public static final String PROPERTY_COUNTRY_CODE = "countryCode";
  
  public static final String PROPERTY_VERIFICATION_CODE = "verificationCode";
  
  public static final String PROPERTY_CLUSTER_URL = "clusterURL";
  
  public static final String PROPERTY_CLUSTER_TOKEN = "clusterToken";
  
  private String IDToken;
  
  private String phoneNumber;
  
  private String countryCode;
  
  private String verificationCode;
  
  private String clusterURL;
  
  private String clusterToken;

  private boolean complete;

  public SandboxModel(@NlsContexts.DialogTitle String title, Project project) {
    super(title);
    add(new SandboxWorkflowPage(this, project));
    add(new SandboxLoginPage(this, project));
  }

  /**
   * @return the IDToken
   */
  public String getIDToken() {
    return IDToken;
  }

  /**
   * @param IDToken the IDToken to set
   */
  public void setIDToken(String IDToken) {
    this.IDToken = IDToken;
  }

  /**
   * @return the phoneNumber
   */
  public String getPhoneNumber() {
    return phoneNumber;
  }

  /**
   * @param phoneNumber the phoneNumber to set
   */
  public void setPhoneNumber(String phoneNumber) {
    this.phoneNumber = phoneNumber;
  }

  /**
   * @return the countryCode
   */
  public String getCountryCode() {
    return countryCode;
  }

  /**
   * @param countryCode the countryCode to set
   */
  public void setCountryCode(String countryCode) {
    this.countryCode = countryCode;
  }

  /**
   * @return the verificationCode
   */
  public String getVerificationCode() {
    return verificationCode;
  }

  /**
   * @param verificationCode the verificationCode to set
   */
  public void setVerificationCode(String verificationCode) {
    this.verificationCode = verificationCode;
  }

  /**
   * @return the clusterURL
   */
  public String getClusterURL() {
    return clusterURL;
  }

  /**
   * @param clusterURL the clusterURL to set
   */
  public void setClusterURL(String clusterURL) {
    this.clusterURL = clusterURL;
  }

  /**
   * @return the clusterToken
   */
  public String getClusterToken() {
    return clusterToken;
  }

  /**
   * @param clusterToken the clusterToken to set
   */
  public void setClusterToken(String clusterToken) {
    this.clusterToken = clusterToken;
  }

  public boolean isComplete() {
    return complete;
  }

  public void setComplete(boolean complete) {
    this.complete = complete;
  }
}
