package com.octosign.whitelabel.communication;

import com.octosign.whitelabel.ui.Main.Status;

public class Info {
    public final String version;
    public final String status;

    public Info(String version, Status status) {
        this.version = version;
        this.status = status.name();
    }
}
