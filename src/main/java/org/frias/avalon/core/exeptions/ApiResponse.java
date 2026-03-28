package org.frias.avalon.core.exeptions;

public record  ApiResponse<T>(
    int status,
    String message,
     T data
){
}
