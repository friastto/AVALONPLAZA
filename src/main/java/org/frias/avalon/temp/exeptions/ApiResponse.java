package org.frias.avalon.temp.exeptions;

public record  ApiResponse<T>(
    int status,
    String message,
     T data
){
}
