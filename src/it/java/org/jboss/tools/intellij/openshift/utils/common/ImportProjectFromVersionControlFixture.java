package org.jboss.tools.intellij.openshift.utils.common;

import com.intellij.remoterobot.RemoteRobot;
import com.intellij.remoterobot.data.RemoteComponent;
import com.intellij.remoterobot.fixtures.ComponentFixture;
import com.intellij.remoterobot.fixtures.ContainerFixture;
import com.intellij.remoterobot.fixtures.DefaultXpath;
import com.intellij.remoterobot.fixtures.FixtureName;
import com.intellij.remoterobot.fixtures.JTextFieldFixture;
import org.jetbrains.annotations.NotNull;
import java.time.Duration;
import static com.intellij.remoterobot.search.locators.Locators.byXpath;

@DefaultXpath(by = "ImportProjectFromVersionControlFixture type", xpath = "//div[@class='MyDialog']")
@FixtureName(name = "Import Project")
public class ImportProjectFromVersionControlFixture extends ContainerFixture {
  public ImportProjectFromVersionControlFixture(@NotNull RemoteRobot remoteRobot, @NotNull RemoteComponent remoteComponent) {
    super(remoteRobot, remoteComponent);
  }

  public void typeUrl(){
    find(JTextFieldFixture.class, byXpath("//div[@class='BorderlessTextField']"), Duration.ofSeconds(30)).setText("https://github.com/maxura/nodejs-hello-world.git");
  }

  public void clickCloneButton(){
    find(ComponentFixture.class, byXpath("//div[@text='Clone']"), Duration.ofSeconds(30)).click();
  }

}
