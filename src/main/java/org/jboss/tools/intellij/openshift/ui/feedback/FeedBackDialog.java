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
package org.jboss.tools.intellij.openshift.ui.feedback;

import com.intellij.ide.BrowserUtil;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.DocumentAdapter;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Objects;
import java.util.regex.Pattern;

public class FeedBackDialog extends DialogWrapper {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}", Pattern.CASE_INSENSITIVE);

    private static final Logger LOGGER = LoggerFactory.getLogger(FeedBackDialog.class);

    private JPanel contentPane;

    private JTextPane topTextFeedback;
    private JLabel label1;
    private JSlider slider1;
    private JLabel label2;
    private JSlider slider2;
    private JComboBox<String> comboBox1;
    private JLabel label3;
    private JLabel label4;
    private JTextField textField4;
    private JLabel label5;
    private JTextField textField5;
    private JLabel label6;
    private JTextField textField6;
    private JLabel labelContact;
    private JTextField textFieldContact;
    private JLabel comment1;
    private JTextArea textArea1;
    private JScrollPane scrollPaneComment1;
    private JTextArea textArea2;
    private JLabel comment2;
    private JLabel comment3;
    private JTextField textField3;
    private JScrollPane scrollPaneComment2;
    private JLabel openshiftPic;
    private JLabel openshiftToolkitText;
    private JLabel octoPic;
    private JLabel opinionMattersText;
    private JEditorPane gitHubContact;
    private JLabel jetBrainsPic;
    private JTextPane marketplaceRate;

    public FeedBackDialog() {
        super(null, false, IdeModalityType.IDE);
        init();
        setModal(true);
        setTitle("Share Feedback");

        // Top images
        openshiftPic.setIcon(createResizedImageIcon(this.getClass().getResource("/images/openshift_extension.png"), 100, 100));
        octoPic.setIcon(createResizedImageIcon(this.getClass().getResource("/images/github-mark.png"), 50, 50));
        jetBrainsPic.setIcon(createResizedImageIcon("https://resources.jetbrains.com/storage/products/company/brand/logos/jb_square.png", 50, 50));

        // Top labels
        openshiftToolkitText.setText("<html><font color=red size=+1><b>OpenShift</b></font><b>  Toolkit</b></html>");
        opinionMattersText.setText("<html><font size=+2><b>Your opinion matters to us!</b></font></html>");
        gitHubContact.setText("<a href='https://github.com/redhat-developer/intellij-openshift-connector/issues'>Contact us on GitHub</a>");
        gitHubContact.addHyperlinkListener(new HyperlinkMouseListener());
        marketplaceRate.setText("<a href='https://plugins.jetbrains.com/plugin/12030-openshift-toolkit-by-red-hat/'>Rate us on Marketplace</a>");
        marketplaceRate.addHyperlinkListener(new HyperlinkMouseListener());


        // Feedback section
        topTextFeedback.setText("\nThe Red Hat OpenShift Toolkit extension team would like to learn from your experience to improve the extension workflow.\nThis survey will take about 2 minutes. Your feedback is extremely valuable and will directly impact the product moving forward.\n");
        label1.setText("1. Overall, how satisfied are you about the extension?");
        comment1.setText("What's the main reason for your score?");
        label2.setText("2. How likely would you recommend the OpenShift Toolkit extension to a friend or colleague?");
        comment2.setText("What's the main reason for your score?");
        label3.setText("3. Have you used any similar extension for cloud-native development?");
        comment3.setText("Please mention the similar extension name/URL.");
        label4.setText("4. What, if anything, do you find frustrating or challenging about the extension workflow?");
        label5.setText("5. What capabilities would you like to see on the extension?");
        label6.setText("6. What do you like best about the extension?");
        labelContact.setText("Share your contact information if you'd like us to respond to you:");
        DocumentAdapter adapter = new DocumentAdapter() {
            @Override
            protected void textChanged(@NotNull DocumentEvent e) {
                validate();
            }
        };
        slider1.addChangeListener(e -> refreshForm());
        slider2.addChangeListener(e -> refreshForm());
        comboBox1.addActionListener(e -> refreshForm());

        textFieldContact.getDocument().addDocumentListener(adapter);
        textArea1.getDocument().addDocumentListener(adapter);
        textArea2.getDocument().addDocumentListener(adapter);
        validate();
    }


    private void refreshForm() {
        if (slider1.getValue() <= 3) {
            comment1.setVisible(true);
            scrollPaneComment1.setVisible(true);
            textArea1.setVisible(true);
        } else {
            comment1.setVisible(false);
            scrollPaneComment1.setVisible(false);
            textArea1.setVisible(false);
        }
        if (slider2.getValue() <= 3) {
            comment2.setVisible(true);
            scrollPaneComment2.setVisible(true);
            textArea2.setVisible(true);
        } else {
            comment2.setVisible(false);
            scrollPaneComment2.setVisible(false);
            textArea2.setVisible(false);
        }
        if (Objects.equals(comboBox1.getSelectedItem(), "Yes")) {
            comment3.setVisible(true);
            textField3.setVisible(true);
        } else {
            comment3.setVisible(false);
            textField3.setVisible(false);
        }
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        return contentPane;
    }

    @Override
    public void validate() {
        super.validate();

        //reset state
        setErrorText(null);
        setOKActionEnabled(true);

        if (!textFieldContact.getText().isBlank() && !EMAIL_PATTERN.matcher(textFieldContact.getText()).matches()) {
            setErrorText("The email format should be xxx@xxx.com", textFieldContact);
            setOKActionEnabled(false);
        }

        if (textArea1.getDocument().getLength() >= 500) {
            setErrorText("Comments are limited to 500 characters", textArea1);
            setOKActionEnabled(false);
        }
        if (textArea2.getDocument().getLength() >= 500) {
            setErrorText("Comments are limited to 500 characters", textArea1);
            setOKActionEnabled(false);
        }
    }

    public String getSatisfaction() {
        return String.valueOf(slider1.getValue());
    }

    public String getSatisfactionComment() {
        return String.valueOf(textArea1.getText());
    }

    public String getRecommendation() {
        return String.valueOf(slider2.getValue());
    }

    public String getRecommendationComment() {
        return String.valueOf(textArea2.getText());
    }

    public String isUsedSimilarExtension() {
        return String.valueOf(comboBox1.getSelectedItem());
    }

    public String getUsedSimilarExtensionName() {
        return String.valueOf(textField3.getText());
    }

    public String getFrustratingFeature() {
        return textField4.getText();
    }

    public String getMissingFeature() {
        return textField5.getText();
    }

    public String getBestFeature() {
        return textField6.getText();
    }

    public String getContact() {
        return textFieldContact.getText();
    }

    private ImageIcon createImageIcon(URL resource) {
        Objects.requireNonNull(resource);
        return new ImageIcon(resource);
    }

    private ImageIcon createResizedImageIcon(URL resource, int w, int h) {
        return new ImageIcon(getScaledImage(createImageIcon(resource).getImage(), w, h));
    }

    private ImageIcon createResizedImageIcon(String resource, int w, int h) {
        try {
            URL url = new URL(resource);
            return createResizedImageIcon(url, w, h);
        } catch (MalformedURLException e) {
            LOGGER.error(e.getMessage(), e);
            return null;
        }
    }

    private Image getScaledImage(Image srcImg, int w, int h) {
        BufferedImage resizedImg = UIUtil.createImage(getRootPane(), w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = resizedImg.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2.drawImage(srcImg, 0, 0, w, h, null);
        g2.dispose();
        return resizedImg;
    }

    private static final class HyperlinkMouseListener implements HyperlinkListener {

        public void hyperlinkUpdate(HyperlinkEvent event) {
            if (event.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                URL href = event.getURL();
                if (href != null) {
                    BrowserUtil.browse(href);
                }
            }
        }
    }

}
