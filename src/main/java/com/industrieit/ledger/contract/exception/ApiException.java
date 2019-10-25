package com.industrieit.ledger.contract.exception;

import com.google.gson.Gson;
import okhttp3.ResponseBody;

import java.io.IOException;
import java.sql.Timestamp;

public class ApiException extends RuntimeException {
    private final int code;
    private final ErrorBody errorBody;

    public ApiException(int code, ResponseBody responseBody) {
        this.code = code;
        ErrorBody parsedErrorBody = null;
        if (responseBody != null){
            Gson gson = new Gson();
            try {
                parsedErrorBody = gson.fromJson(responseBody.string(), ErrorBody.class);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        this.errorBody = parsedErrorBody;
    }

    public int getCode() {
        return code;
    }

    public ErrorBody getErrorBody() {
        return errorBody;
    }

    private static class ErrorBody {
        private Timestamp timestamp;
        private int status;
        private String error;
        private String message;
        private String path;

        public Timestamp getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(Timestamp timestamp) {
            this.timestamp = timestamp;
        }

        public int getStatus() {
            return status;
        }

        public void setStatus(int status) {
            this.status = status;
        }

        public String getError() {
            return error;
        }

        public void setError(String error) {
            this.error = error;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }
    }
}
