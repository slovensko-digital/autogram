package com.octosign.whitelabel.communication.dto;

import com.octosign.whitelabel.ui.Main.Status;

public record Info(String version, String status) {
    public Info(String version, Status status) {
        this(version, status.name());
    }
}
