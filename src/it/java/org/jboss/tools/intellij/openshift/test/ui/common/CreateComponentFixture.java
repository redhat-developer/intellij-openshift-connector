package org.jboss.tools.intellij.openshift.test.ui.common;

import com.intellij.remoterobot.RemoteRobot;
import com.intellij.remoterobot.data.RemoteComponent;
import com.intellij.remoterobot.fixtures.ComponentFixture;
import com.intellij.remoterobot.fixtures.ContainerFixture;
import com.intellij.remoterobot.fixtures.DefaultXpath;
import com.intellij.remoterobot.fixtures.FixtureName;
import org.jetbrains.annotations.NotNull;
import java.time.Duration;
import static com.intellij.remoterobot.search.locators.Locators.byXpath;

/**
 * 
 * @author Ihor Okhrimenko
 *
 */
@DefaultXpath(by = "CreateComponentFixture type", xpath = "//div[@class='MyDialog']")
@FixtureName(name = "Create Component")
public class CreateComponentFixture extends ContainerFixture {
  public CreateComponentFixture(@NotNull RemoteRobot remoteRobot, @NotNull RemoteComponent remoteComponent) {
    super(remoteRobot, remoteComponent);
  }

  public void clickToFinishButton(){
    find(ComponentFixture.class, byXpath("//div[@text='Finish']"), Duration.ofSeconds(30)).click();
  }

}
