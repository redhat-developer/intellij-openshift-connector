/*******************************************************************************
 * Copyright (c) 2018-2019 Red Hat, Inc. Distributed under license by Red Hat, Inc.
 * All rights reserved. This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v20.html
 * 
 * Contributors: Red Hat, Inc.
 ******************************************************************************/
package org.jboss.tools.intellij.openshift.utils;

import org.junit.Test;

import java.nio.file.WatchEvent;
import java.nio.file.WatchEvent.Modifier;

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.spy;

public class HighSensitivityRegistrarTest {

	private TestableHighSensitivityRegistrar modifier = spy(new TestableHighSensitivityRegistrar());

	@Test
	public void isRequiredOnMacOS() {
		// given
		doReturn(true).when(modifier).isMac();
		// when
		boolean required = modifier.isRequired();
		// then
		assertThat(required).isTrue();
	}

	@Test
	public void isNotRequiredOnNonMac() {
		// given
		doReturn(false).when(modifier).isMac();
		// when
		boolean required = modifier.isRequired();
		// then
		assertThat(required).isFalse();
	}

	@Test
	public void getModifierIfAvailable() throws ClassNotFoundException {
		// given
		doReturn(SensitivityModifier.class).when(modifier).getSensitivityWatchEventModifierClass();
		// when
		Modifier highSensitivity = modifier.get();
		// then
		assertThat(highSensitivity).isNotNull();
	}

	@Test
	public void existsIfIsAvailable() throws ClassNotFoundException {
		// given
		doReturn(SensitivityModifier.class).when(modifier).getSensitivityWatchEventModifierClass();
		// when
		boolean exists = modifier.exists();
		// then
		assertThat(exists).isTrue();
	}

	@Test
	public void doeNotExistsIfIsntAvailable() throws ClassNotFoundException {
		// given
		doThrow(ClassNotFoundException.class).when(modifier).getSensitivityWatchEventModifierClass();
		// when
		boolean exists = modifier.exists();
		// then
		assertThat(exists).isFalse();
	}

	public enum SensitivityModifier implements WatchEvent.Modifier {
		HIGH, MEDIUM, LOW;
	}

	@Test
	public void getNullIfClassAvailableButConstantIsNot() throws ClassNotFoundException {
		// given
		doReturn(SensitivityModifierWithoutHigh.class).when(modifier).getSensitivityWatchEventModifierClass();
		// when
		Modifier highSensitivity = modifier.get();
		// then
		assertThat(highSensitivity).isNull();
	}

	public enum SensitivityModifierWithoutHigh implements WatchEvent.Modifier {
		MEDIUM, LOW;
	}
	
	@Test
	public void getNullIfClassIsNotAvailable() throws ClassNotFoundException {
		// given
		doThrow(ClassNotFoundException.class).when(modifier).getSensitivityWatchEventModifierClass();
		// when
		Modifier highSensitivity = modifier.get();
		// then
		assertThat(highSensitivity).isNull();
	}

	public class TestableHighSensitivityRegistrar extends HighSensitivityRegistrar {

		@Override
		public boolean isMac() {
			return super.isMac();
		}

		@Override
		public Class<Modifier> getSensitivityWatchEventModifierClass() throws ClassNotFoundException {
			return super.getSensitivityWatchEventModifierClass();
		}
		
	}
	
}
