package com.fxiaoke.common.reflection;

import com.github.trace.annotation.RpcParameterToString;

/**
 * Created by lirui on 2017-02-28 18:22.
 */
@RpcParameterToString("toS")
public class ParamObj {
  private int a = 2;
  private int b = 3;

  public String toS() {
    return "a=" + a + ",b=" + b;
  }
}
