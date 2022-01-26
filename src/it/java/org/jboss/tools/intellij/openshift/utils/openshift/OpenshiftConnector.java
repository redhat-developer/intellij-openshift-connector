package org.jboss.tools.intellij.openshift.utils.openshift;

import com.intellij.remoterobot.RemoteRobot;
import com.intellij.remoterobot.data.RemoteComponent;
import com.intellij.remoterobot.fixtures.ComponentFixture;
import com.intellij.remoterobot.fixtures.ContainerFixture;
import com.intellij.remoterobot.fixtures.DefaultXpath;
import com.intellij.remoterobot.fixtures.FixtureName;
import com.intellij.remoterobot.fixtures.JTreeFixture;
import org.jetbrains.annotations.NotNull;
import java.time.Duration;
import static com.intellij.remoterobot.search.locators.Locators.byXpath;
import static com.intellij.remoterobot.utils.RepeatUtilsKt.waitFor;

@DefaultXpath(by = "OpenshiftConnector type", xpath = "//div[@class='IdeFrameImpl']")
@FixtureName(name = "Openshift Connector")
public class OpenshiftConnector extends ContainerFixture {
  public OpenshiftConnector(@NotNull RemoteRobot remoteRobot, @NotNull RemoteComponent remoteComponent) {
    super(remoteRobot, remoteComponent);
  }

  public void clickToOpenshiftButton() throws InterruptedException {
    find(ComponentFixture.class, byXpath("//div[@visible_text='OpenShift' and @class='StripeButton']"), Duration.ofSeconds(60)).click();
  }

  public void expandOpenshiftConnectorTree(String path) throws InterruptedException {
      getOpenshiftConnectorTree().expand(path);
  }

  public void clickToCreateDeploymentLink() throws InterruptedException {
    getOpenshiftConnectorTree().clickRow(2);
  }

  public void waitItemInTree(String itemText, int timeout, int pollingTimeout){
    waitFor(Duration.ofSeconds(timeout),
        Duration.ofSeconds(pollingTimeout),
        "The node-js project is not still available.",
        () -> getOpenshiftConnectorTree().hasText(itemText));
  }

  private JTreeFixture getOpenshiftConnectorTree(){
    return find(JTreeFixture.class, byXpath("//div[@class='Tree']"), Duration.ofSeconds(30));
  }

}
