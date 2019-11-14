/*******************************************************************************
 * Copyright (c) 2019 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 * Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.tools.intellij.openshift.utils;

import io.fabric8.kubernetes.api.model.Config;
import io.fabric8.kubernetes.api.model.ConfigBuilder;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.Arrays;

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

public class ConfigWatcherTest {

    private Path configPath;
    private WatchService service;
    private TestableConfigWatcher watcher;

    @Before
    public void before() throws Exception {
        this.configPath = createPath();
        this.service = createWatchService(configPath);
        this.watcher = new TestableConfigWatcher(configPath, service);
    }

    @Test
    public void shouldNotifyListener() {
        // given
        assertThat(watcher.getCallbackCount()).isEqualTo(0);
        // when
        watcher.run();
        // then
        assertThat(watcher.getCallbackCount()).isEqualTo(1);
    }

    @Test
    public void shouldNotNotifyListenerIfCannotLoadConfig() {
        // given
        TestableConfigWatcher watcher = new TestableConfigWatcher(configPath, service) {
            @Override
            protected Config loadConfig() {
                return null;
            }
        };
        assertThat(watcher.getCallbackCount()).isEqualTo(0);
        // when
        watcher.run();
        // then
        assertThat(this.watcher.getCallbackCount()).isEqualTo(0);
    }

    @Test
    public void shouldNotNotifyListenerIfNotifiedPathIsNotConfigPath() {
        // given
        TestableConfigWatcher watcher = new TestableConfigWatcher(configPath, service) {
            @Override
            protected boolean isConfigPath(WatchEvent<?> event) {
                return false;
            }
        };
        assertThat(watcher.getCallbackCount()).isEqualTo(0);
        // when
        watcher.run();
        // then
        assertThat(this.watcher.getCallbackCount()).isEqualTo(0);
    }

    private Path createPath() {
        Path path = mock(Path.class);
        // mock #resolve
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                return invocation.getArguments()[0];
            }
        }).when(path).resolve(any(Path.class));
        // mock #getParent
        doReturn(path).when(path).getParent();
        return path;
    }

    private WatchService createWatchService(Path path) throws InterruptedException {
        WatchKey key = mock(WatchKey.class);
        WatchService service = mock(WatchService.class);
        doReturn(key) // 1st
                .doReturn(null) // 2nd -> stop
                .when(service).take();

        WatchEvent event =  createWatchEvent(path);

        doReturn(Arrays.asList(event)).when(key).pollEvents();

        return service;
    }

    private WatchEvent createWatchEvent(Path path) {
        WatchEvent event = mock(WatchEvent.class);
        doReturn(path).when(event).context();
        return event;
    }

    public class TestableConfigWatcher extends ConfigWatcher {

        private final WatchService service;
        private int callCount = 0;

        public TestableConfigWatcher(Path configPath, WatchService service) {
            super(configPath, null);
            this.listener = (source, config) -> callCount++;
            this.service = service;
        }

        @Override
        protected WatchService newWatchService() throws IOException {
            return service;
        }

        @Override
        protected Config loadConfig() {
            return new ConfigBuilder().build();
        }

        private int getCallbackCount() {
            return callCount;
        }
    }
}
