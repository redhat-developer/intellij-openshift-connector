/*******************************************************************************
 * Copyright (c) 2021 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 * Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.tools.intellij.openshift.validation;

import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.jetbrains.jsonSchema.extension.JsonSchemaFileProvider;
import com.jetbrains.jsonSchema.extension.SchemaType;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.URL;

public class DevfileJsonSchemaFileProvider implements JsonSchemaFileProvider {
    private static URL SCHEMA_URL = DevfileJsonSchemaFileProvider.class.getResource("/schemas/devfile.json");

    @Override
    public boolean isAvailable(@NotNull VirtualFile file) {
        return "devfile.yaml".equals(file.getName()) || "devfile.yml".equals(file.getName());
    }

    @Override
    public @NotNull
    @Nls String getName() {
        return "Devfile";
    }

    @Override
    public @Nullable VirtualFile getSchemaFile() {
        return VirtualFileManager.getInstance().findFileByUrl(VfsUtil.convertFromUrl(SCHEMA_URL));
    }

    @Override
    public @NotNull SchemaType getSchemaType() {
        return SchemaType.schema;
    }
}
