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

import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.event.DocumentEvent;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.Objects;
import java.util.regex.Pattern;

public class FeedBackDialog extends DialogWrapper {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}", Pattern.CASE_INSENSITIVE);

    private static final Logger LOGGER = LoggerFactory.getLogger(FeedBackDialog.class);

    private JPanel contentPane;

    private JTextPane topTextFeedback;
    private JLabel label1;
    private JLabel label2;
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
    private JTextPane gitHubContact;
    private JLabel jetBrainsPic;
    private JTextPane marketplaceRate;
    private JRadioButton radioButton11;
    private JRadioButton radioButton12;
    private JRadioButton radioButton13;
    private JRadioButton radioButton14;
    private JRadioButton radioButton15;
    private JRadioButton radioButton21;
    private JRadioButton radioButton22;
    private JRadioButton radioButton23;
    private JRadioButton radioButton24;
    private JRadioButton radioButton25;
    private JSlider slider3;
    private ButtonGroup buttonGroup2;
    private ButtonGroup buttonGroup1;

    public FeedBackDialog() {
        super(null, false, IdeModalityType.IDE);
        init();
        setModal(true);
        setTitle("Share Feedback");

        // Top images
        openshiftPic.setIcon(createResizedImageIcon(this.getClass().getResource("/images/openshift_extension.png"), 50, 50));
        octoPic.setIcon(createResizedImageIcon(this.getClass().getResource("/images/github-mark.png"), 40, 40));
        jetBrainsPic.setIcon(createResizedImageIcon("https://resources.jetbrains.com/storage/products/company/brand/logos/jb_square.png", 60, 60));

        // Top labels
        openshiftToolkitText.setText("<html><font color=red size=+1><b>OpenShift</b></font><b>  Toolkit</b></html>");
        opinionMattersText.setText("<html><font size=+3><b>Your opinion matters to us!</b></font></html>");
        gitHubContact.setText("<a style='font-family:sans-serif' href='https://github.com/redhat-developer/intellij-openshift-connector/issues'>Contact us on GitHub</a>");
        gitHubContact.addHyperlinkListener(new HyperlinkMouseListener());
        marketplaceRate.setText("<a style='font-family:sans-serif' href='https://plugins.jetbrains.com/plugin/12030-openshift-toolkit-by-red-hat/reviews'>Rate us on Marketplace</a>");
        marketplaceRate.addHyperlinkListener(new HyperlinkMouseListener());

        // Feedback section
        topTextFeedback.setText("\nThe Red Hat OpenShift Toolkit extension team would like to learn from your experience to improve the extension workflow.\nThis survey will take about 2 minutes. Your feedback is extremely valuable and will directly impact the product moving forward.\n");
        label1.setText("1. Overall, how satisfied are you about the extension? *");
        comment1.setText("What's the main reason for your score?");
        setVisible(false,
                comment1, scrollPaneComment1, textArea1); // will be displayed depending on the satisfaction score
        label2.setText("2. How likely would you recommend the OpenShift Toolkit extension to a friend or colleague?");
        comment2.setText("What's the main reason for your score?");
        setVisible(false,
                comment2, scrollPaneComment2, textArea2); // will be displayed depending on the recommendation score
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
        radioButton11.addActionListener(this::refreshComment1);
        radioButton12.addActionListener(this::refreshComment1);
        radioButton13.addActionListener(this::refreshComment1);
        radioButton14.addActionListener(this::refreshComment1);
        radioButton15.addActionListener(this::refreshComment1);

        radioButton21.addActionListener(this::refreshComment2);
        radioButton22.addActionListener(this::refreshComment2);
        radioButton23.addActionListener(this::refreshComment2);
        radioButton24.addActionListener(this::refreshComment2);
        radioButton25.addActionListener(this::refreshComment2);

        slider3.addChangeListener(e -> refreshForm());

        // Add labels to the slider
        Hashtable<Integer, JLabel> labelTable = new Hashtable<>();
        labelTable.put(0, new JLabel("No"));
        labelTable.put(1, new JLabel("Yes"));
        slider3.setLabelTable(labelTable);
        setVisible(false,
                comment3, textField3); // will be displayed depending on the similar extension answer

        textFieldContact.getDocument().addDocumentListener(adapter);
        textArea1.getDocument().addDocumentListener(adapter);
        textArea2.getDocument().addDocumentListener(adapter);
        validate();

        //disable OK button to prevent sending no values
        setOKActionEnabled(false);
    }


    private void refreshComment1(ActionEvent e) {
        setVisible(Integer.parseInt(e.getActionCommand()) <= 3,
                comment1, scrollPaneComment1, textArea1);
        setOKActionEnabled(true);
    }

    private void refreshComment2(ActionEvent e) {
        setVisible(Integer.parseInt(e.getActionCommand()) <= 3,
                comment2, scrollPaneComment2, textArea2);
        setOKActionEnabled(true);
    }

    private void refreshForm() {
        setVisible(slider3.getValue() == 1,
                comment3, textField3);
        setOKActionEnabled(true);
    }

    private void setVisible(boolean visible, JComponent... components) {
        Arrays.asList(components)
                .forEach(component -> component.setVisible(visible));
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
        if (buttonGroup1.getSelection() != null) {
            return buttonGroup1.getSelection().getActionCommand();
        }
        return "";
    }

    public String getSatisfactionComment() {
        return String.valueOf(textArea1.getText());
    }

    public String getRecommendation() {
        if (buttonGroup2.getSelection() != null) {
            return buttonGroup2.getSelection().getActionCommand();
        }
        return "";
    }

    public String getRecommendationComment() {
        return String.valueOf(textArea2.getText());
    }

    public String isUsedSimilarExtension() {
        return Boolean.toString(slider3.getValue() == 1);
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
        g2.drawImage(srcImg, 0, 0, (int) (w * g2.getTransform().getScaleX()), (int) (h * g2.getTransform().getScaleY()), null);
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
