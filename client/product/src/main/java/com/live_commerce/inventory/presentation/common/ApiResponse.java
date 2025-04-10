package com.live_commerce.inventory.presentation.common;


import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ApiResponse<T> {

  private String status;
  private T data;
}