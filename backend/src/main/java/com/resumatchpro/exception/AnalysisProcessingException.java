package com.resumatchpro.exception;

public class AnalysisProcessingException extends RuntimeException {
    public AnalysisProcessingException(String message) { super(message); }
    public AnalysisProcessingException(String message, Throwable cause) { super(message, cause); }
}
