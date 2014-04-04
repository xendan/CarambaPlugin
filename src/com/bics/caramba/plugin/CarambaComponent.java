package com.bics.caramba.plugin;

import com.bics.caramba.plugin.search.CarambaUtils;
import com.bics.caramba.plugin.search.ComponentResolver;
import com.bics.caramba.plugin.search.ConfigSearchScope;
import com.intellij.openapi.components.ApplicationComponent;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.psi.search.PsiShortNamesCache;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/**
 * User: id967161
 * Date: 21/06/13
 */
public class CarambaComponent implements ApplicationComponent {
    private static final Logger log = Logger.getInstance(CarambaComponent.class);
    public static final int PORT = 4455;

    @Override
    public void initComponent() {
        new ServerRunner().start();
    }

    @Override
    public void disposeComponent() {

    }

    @NotNull
    @Override
    public String getComponentName() {
        return "Caramba component";
    }

    private boolean isCarambaProject() {
        Project project = CarambaUtils.getProejct();
        //TODO check how to get project....
        if (project == null) {
            return true;
        }
        return PsiShortNamesCache.getInstance(project).getFilesByName(ConfigSearchScope.CARAMBA_CONFIG_XML).length > 0;
    }


    private class ServerRunner extends Thread {

        @Override
        public void run() {
            log.info("On server start");
            if (isCarambaProject()) {
                ServerSocket serverSocket;
                try {
                    serverSocket = new ServerSocket(PORT);
                } catch (Exception e) {
                    log.info("Error creating socket, maybe it is in use", e);
                    return;
                }
                while (true) {
                    Socket connectionSocket = getSocket(serverSocket);
                    InputStreamReader streamReader = null;
                    BufferedReader inFromClient = null;
                    if (connectionSocket != null) {
                        try {
                            streamReader = new InputStreamReader(connectionSocket.getInputStream());
                            inFromClient = new BufferedReader(streamReader);
                        } catch (IOException e) {
                            log.error("Error opening stream");
                        }
                    }
                    if (inFromClient != null) {
                        String nextLine = "";
                        List<String> ids = new ArrayList<String>();
                        String selected = null;
                        try {
                            while ((nextLine = inFromClient.readLine()) != null && selected == null) {
                                if (nextLine.startsWith("SELECTED:")) {
                                    selected = nextLine.substring("SELECTED:".length());
                                } else {
                                    ids.add(nextLine);
                                }
                            }
                        } catch (IOException e) {
                            log.error("Error reading", e);
                        } finally {
                            closeSilently(connectionSocket, streamReader, inFromClient);
                        }
                        SwingUtilities.invokeLater(new ComponentResolver(ids, selected));
                    }
                }
            }

        }

        private void closeSilently(Object... closeables) {
            for (Object closeable : closeables) {
                try {
                    if (closeable instanceof Closeable) {
                        ((Closeable) closeable).close();
                    } else if (closeable instanceof Socket) {
                        ((Socket) closeable).close();
                    }
                } catch (IOException e) {
                    log.error("Error closing stream");
                }
            }
        }

        private Socket getSocket(ServerSocket welcomeSocket) {
            try {
                return welcomeSocket.accept();
            } catch (IOException e) {
                log.error("Error accepting socket", e);
            }
            return null;
        }
    }

}
