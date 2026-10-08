package com.emptycart.platform.commons.model;

public record ApiResponse<T>(boolean success, String message, T data) {

    public static <T> ApiResponse<T> ok (T data){
        return new ApiResponse<T>(true, "OK", data);
    }

    public static <T> ApiResponse<T> error (String message){
            return new  ApiResponse<T>(false, message, null);
    }
}
