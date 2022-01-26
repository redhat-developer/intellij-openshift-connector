package org.jboss.tools.intellij.openshift.utils.common;

import com.intellij.remoterobot.RemoteRobot;
import com.intellij.remoterobot.data.RemoteComponent;
import com.intellij.remoterobot.fixtures.ComponentFixture;
import com.intellij.remoterobot.fixtures.ContainerFixture;
import org.jetbrains.annotations.NotNull;
import com.intellij.remoterobot.fixtures.DefaultXpath;
import com.intellij.remoterobot.fixtures.FixtureName;
import java.time.Duration;
import static com.intellij.remoterobot.search.locators.Locators.byXpath;

@DefaultXpath(by = "WelcomeDialogFixture type", xpath = "//div[@class='FlatWelcomeFrame']")
@FixtureName(name = "Welcome To IntelliJ IDEA Dialog")
public class WelcomeDialogFixture extends ContainerFixture {
  public WelcomeDialogFixture(@NotNull RemoteRobot remoteRobot, @NotNull RemoteComponent remoteComponent) {
    super(remoteRobot, remoteComponent);
  }

  public void clickToVcsButton(){
    find(ComponentFixture.class, byXpath("//div[@defaulticon='fromVCSTab.svg']"), Duration.ofSeconds(30)).click();
  }
}
