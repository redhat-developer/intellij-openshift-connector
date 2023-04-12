package org.jboss.tools.intellij.openshift.utils.odo;

import org.jboss.tools.intellij.openshift.BaseTest;
import org.junit.Test;

import java.util.concurrent.ExecutionException;

import static org.junit.Assert.assertNotNull;

public class OdoCliFactoryTest extends BaseTest {
    @Test
    public void getOdo() throws ExecutionException, InterruptedException {
        Odo odo = OdoCliFactory.getInstance().getOdo(project).get();
        assertNotNull(odo);
    }
}
