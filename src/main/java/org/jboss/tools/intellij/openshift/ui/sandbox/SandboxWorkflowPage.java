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


import com.intellij.icons.AllIcons;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComponentValidator;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.ui.DocumentAdapter;
import com.intellij.ui.wizard.WizardNavigationState;
import com.intellij.ui.wizard.WizardStep;
import org.jboss.tools.intellij.openshift.Constants;
import org.jboss.tools.intellij.openshift.oauth.TokenProvider;
import org.jboss.tools.intellij.openshift.oauth.exception.OAuthException;
import org.jboss.tools.intellij.openshift.telemetry.TelemetryService;
import org.jetbrains.annotations.NotNull;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import java.io.IOException;

public class SandboxWorkflowPage extends WizardStep<SandboxModel> {

    private final Disposable disposable = Disposer.newDisposable();
    private final SandboxModel model;
    private final Project project;
    private JLabel messageLabel;
    private JPanel verificationPanel;
    private JTextField countryCodeText;
    private JTextField phoneNumberText;
    private JButton sendVerificationButton;
    private JPanel confirmVerificationPanel;
    private JTextField verificationCodeText;
    private JButton confirmVerificationButton;
    private JPanel root;

    private SandboxProcessor processor;

    public SandboxWorkflowPage(SandboxModel model, Project project) {
        super("", "Please login to Red Hat SSO if required, then provide required information to bootstrap your Red Hat Developer Sandbox.");
        this.model = model;
        this.project = project;
        sendVerificationButton.addActionListener(e -> launchJob());
        confirmVerificationButton.addActionListener(e -> launchJob());
        countryCodeText.getDocument().addDocumentListener(new DocumentAdapter() {
            @Override
            protected void textChanged(@NotNull DocumentEvent e) {
                model.setCountryCode(countryCodeText.getText());
            }
        });
        phoneNumberText.getDocument().addDocumentListener(new DocumentAdapter() {
            @Override
            protected void textChanged(@NotNull DocumentEvent e) {
                model.setPhoneNumber(phoneNumberText.getText());
            }
        });
        verificationCodeText.getDocument().addDocumentListener(new DocumentAdapter() {
            @Override
            protected void textChanged(@NotNull DocumentEvent e) {
                model.setVerificationCode(verificationCodeText.getText());
            }
        });
        new ComponentValidator(disposable).withValidator(new CountryCodeValidator(countryCodeText))
                .andRegisterOnDocumentListener(countryCodeText).installOn(countryCodeText);
        new ComponentValidator(disposable).withValidator(new PhoneNumberValidator(phoneNumberText))
                .andRegisterOnDocumentListener(phoneNumberText).installOn(phoneNumberText);
    }

    private void reportMessage(String message, boolean error) {
        ApplicationManager.getApplication().invokeLater(() -> {
            if (error) {
                messageLabel.setIcon(AllIcons.General.Error);
            } else {
                messageLabel.setIcon(null);
            }
            messageLabel.setText(message);
        }, ModalityState.any());
    }



    private void ssoLogin(ProgressIndicator monitor) {
        String errorMessage = "Failed to login to Red Hat SSO";
        if (!monitor.isCanceled()) {
            try {
                reportMessage("Login to Red Hat SSO", false);
                model.setIDToken(TokenProvider.get().getToken(Constants.REDHAT_SSO_SERVER_ID, TokenProvider.ID_TOKEN,
                        WindowManager.getInstance().getFrame(project)));
                model.recordTelemetryEvent(TelemetryService.REDHAT_SSO_GET_TOKEN, model.getIDToken() != null ?
                        TelemetryService.VALUE_SUCCESS : TelemetryService.VALUE_FAILURE);
                return;
            } catch (OAuthException e) {
                model.recordTelemetryError(errorMessage);
            }
        }
        reportMessage(errorMessage, true);
        setStatus(errorMessage);
    }

    private void retrieveState(ProgressIndicator monitor) {
        if (model.getIDToken() == null) {
            ssoLogin(monitor);
        }
        if (model.getIDToken() != null) {
            checkSandbox(monitor);
        }
    }

    private void reportState(SandboxProcessor.State state) {
        model.recordTelemetryEvent(TelemetryService.DEVSANDBOX_API_STATE_PREFIX+state.name(), "called");
        switch (state) {
            case NONE:
                setStatus("Checking Red Hat Developer Sandbox signup state");
                break;
            case NEEDS_SIGNUP:
                setStatus("Checking Red Hat Developer Sandbox needs signup");
                break;
            case NEEDS_APPROVAL:
                setStatus("Your Red Hat Developer Sandbox needs to be approved, you should wait or retry later");
                break;
            case NEEDS_VERIFICATION:
                setStatus("Your Red Hat Developer Sandbox needs to be verified, enter your country code and phone number and click 'Verify'");
                break;
            case CONFIRM_VERIFICATION:
                setStatus("You need to send the verification code received on your phone, enter the verification code and phone number and click 'Verify'");
                break;
            case READY:
                setStatus("Your Red Hat Developer Sandbox is ready, let's login now !!!");
                model.setClusterURL(processor.getClusterURL());
                model.getCurrentNavigationState().NEXT.setEnabled(true);
                break;
        }
    }

    private void checkSandbox(ProgressIndicator monitor) {
        reportMessage("Checking Developer Sandbox account", false);
        if (processor == null) {
            processor = new SandboxProcessor(model.getIDToken());
        }
        boolean stop = false;
        try {
            while (!monitor.isCanceled() && !stop) {
                processor.advance(model);
                reportState(processor.getState());
                stop = processor.getState().isNeedsInteraction();
                if (!stop) {
                    sleep();
                }
            }
        } catch (IOException e) {
            String message = "Error accessing the Red Hat Developer Sandbox API: " + e.getLocalizedMessage();
            reportMessage(message, true);
        }
    }

    private void sleep() {
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void setStatus(String message) {
        ApplicationManager.getApplication().invokeLater(() -> messageLabel.setText(message), ModalityState.any());
    }

    private void launchJob() {
        ProgressManager.getInstance().run(new Task.Backgroundable(project, "Login to red hat developer sandbox", true) {
            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                retrieveState(indicator);
                ApplicationManager.getApplication().invokeLater(SandboxWorkflowPage.this::updateGroups, ModalityState.any());
            }
        });
    }


    private void updateGroups() {
        verificationPanel.setVisible(false);
        verificationPanel.setEnabled(false);
        confirmVerificationPanel.setVisible(false);
        confirmVerificationPanel.setEnabled(false);
        if (processor != null && processor.getState() == SandboxProcessor.State.NEEDS_VERIFICATION) {
            verificationPanel.setVisible(true);
            verificationPanel.setEnabled(true);
        } else if (processor != null && processor.getState() == SandboxProcessor.State.CONFIRM_VERIFICATION) {
            confirmVerificationPanel.setVisible(true);
            confirmVerificationPanel.setEnabled(true);
        }
    }

    @Override
    public JComponent prepare(WizardNavigationState state) {
        state.FINISH.setEnabled(false);
        state.NEXT.setEnabled(false);
        state.PREVIOUS.setEnabled(false);
        updateGroups();
        launchJob();
        return root;
    }

  @Override
  public boolean onCancel() {
    Disposer.dispose(disposable);
    return super.onCancel();
  }

  @Override
  public boolean onFinish() {
      Disposer.dispose(disposable);
      return super.onFinish();
  }

}
