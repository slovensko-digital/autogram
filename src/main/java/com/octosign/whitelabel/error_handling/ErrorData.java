package com.octosign.whitelabel.error_handling;

public record ErrorData(int httpCode, String message) { }