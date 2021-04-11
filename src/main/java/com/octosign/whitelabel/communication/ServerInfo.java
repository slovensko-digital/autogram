package com.octosign.whitelabel.communication;

import com.octosign.whitelabel.ui.Main.Status;

public class ServerInfo {
    public final String version;
    public final String status;

    public ServerInfo(String version, Status status) {
        this.version = version;
        this.status = status.name();
    }
}
