package com.theladders.api.dto;

public record JobUploadResult(String result, Object job, String error) {

    public static JobUploadResult success(Object job) {
        return new JobUploadResult("success", job, null);
    }

    public static JobUploadResult error(Object job, String error) {
        return new JobUploadResult("error", job, error);
    }
}
