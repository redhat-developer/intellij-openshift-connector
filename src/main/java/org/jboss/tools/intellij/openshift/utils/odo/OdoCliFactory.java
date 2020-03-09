/*******************************************************************************
 * Copyright (c) 2020 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 * Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.tools.intellij.openshift.utils.odo;

import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.ui.Messages;
import com.twelvemonkeys.lang.Platform;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.CompressorException;
import org.apache.commons.compress.compressors.CompressorInputStream;
import org.apache.commons.compress.compressors.CompressorStreamFactory;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.jboss.tools.intellij.openshift.Constants;
import org.jboss.tools.intellij.openshift.utils.ConfigHelper;
import org.jboss.tools.intellij.openshift.utils.ExecHelper;
import org.jboss.tools.intellij.openshift.utils.NetworkUtils;
import org.jboss.tools.intellij.openshift.utils.ToolsConfig;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Pattern;

import static org.jboss.tools.intellij.openshift.Constants.HOME_FOLDER;

public class OdoCliFactory {

    private static OdoCliFactory INSTANCE;

    public static OdoCliFactory getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new OdoCliFactory();
        }
        return INSTANCE;
    }

    private String command;

    private OdoCliFactory() {
    }

    public Odo getOdo() {
        if (command == null) {
            try {
                command = getOdoCommand();
            } catch (IOException e) {
            }
        }
        return new OdoCli(command);
    }

    private boolean areCompatible(String version, String requiredVersion) {
        return version.equals(requiredVersion);
    }

    public static boolean isDownloadAllowed(String currentVersion, String requiredVersion) {
        return Messages.showYesNoCancelDialog(StringUtils.isEmpty(currentVersion)?"Odo not found , do you want to download odo " + requiredVersion + " ?":"Odo " + currentVersion + "found, required version is " + requiredVersion + ", do you want to download odo ?", "Odo tool required", Messages.getQuestionIcon()) == Messages.YES;
    }
    private String getOdoVersion(String tool, String command) {
        String version = "";
        try {
            Pattern pattern = Pattern.compile(tool + " v(\\d+[\\.\\d+]*(-.*)?)\\s.*");
            String output = ExecHelper.execute(command, false, "version");
            try (BufferedReader reader = new BufferedReader(new StringReader(output))) {
                version = reader.lines().
                        map(line -> pattern.matcher(line)).
                        filter(matcher -> matcher.matches()).
                        map(matcher -> matcher.group(1)).
                        findFirst().orElse("");
            }
        } catch (IOException e) {}
        return version;
    }

    private String getOdoCommand() throws IOException {
        ToolsConfig.Tool odoTool = ConfigHelper.loadToolsConfig().getTools().get("odo");
        ToolsConfig.Platform platform = odoTool.getPlatforms().get(Platform.os().id());
        String command = platform.getCmdFileName();
        String version = getOdoVersion("odo" , command);
        if (!areCompatible(version, odoTool.getVersion())) {
            Path path = Paths.get(HOME_FOLDER, Constants.PLUGIN_FOLDER, "cache", odoTool.getVersion(), command);
            if (!Files.exists(path)) {
                final Path dlFilePath = path.resolveSibling(platform.getDlFileName());
                final String cmd = path.toString();
                if (isDownloadAllowed(version, odoTool.getVersion())) {
                    command = ProgressManager.getInstance().run(new Task.WithResult<String, IOException>(null, "Downloading Odo", true) {
                        @Override
                        public String compute(@NotNull ProgressIndicator progressIndicator) throws IOException {
                            OkHttpClient client = NetworkUtils.getClient();
                            Request request = new Request.Builder().url(platform.getUrl()).build();
                            Response response = client.newCall(request).execute();
                            downloadFile(response.body().byteStream(), dlFilePath, progressIndicator, response.body().contentLength());
                            if (progressIndicator.isCanceled()) {
                                throw new IOException("Interrupted");
                            } else {
                                uncompress(dlFilePath, cmd);
                                return cmd;
                            }
                        }
                    });
                }
            } else {
                command = path.toString();
            }
        }
        return command;
    }

    private void uncompress(Path dlFilePath, String cmd) throws IOException {
        try (InputStream input = new BufferedInputStream(Files.newInputStream(dlFilePath))) {
            try (CompressorInputStream gzStream = new CompressorStreamFactory().createCompressorInputStream(input)) {
                try (TarArchiveInputStream tarStream = new TarArchiveInputStream(gzStream)) {
                    TarArchiveEntry entry = tarStream.getNextTarEntry();
                    if (entry != null) {
                        try (OutputStream output = new FileOutputStream(cmd)) {
                            IOUtils.copy(tarStream, output);
                        }
                        if (!new File(cmd).setExecutable(true)) {
                            throw new IOException("Can't set " + cmd + " as executable");
                        }
                    }
                }
            }
        } catch (CompressorException e) {
            throw new IOException(e);
        }
    }


    private static void downloadFile(InputStream input, Path dlFileName, ProgressIndicator progressIndicator, long size) throws IOException {
        byte[] buffer = new byte[4096];
        Files.createDirectories(dlFileName.getParent());
        try (OutputStream output = Files.newOutputStream(dlFileName)) {
            int lg;
            long accumulated = 0;
            while (((lg = input.read(buffer)) > 0) && !progressIndicator.isCanceled()) {
                output.write(buffer, 0, lg);
                accumulated += lg;
                progressIndicator.setFraction((double) accumulated / size);
            }
        }
    }

}
