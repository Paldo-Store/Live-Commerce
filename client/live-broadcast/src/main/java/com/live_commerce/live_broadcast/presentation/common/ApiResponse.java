package com.live_commerce.live_broadcast.presentation.common;


import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ApiResponse<T> {

  private String status;
  private T data;
}