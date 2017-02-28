package com.fxiaoke.common.reflection

import spock.lang.Specification

/**
 * 测试toString方法
 * Created by lirui on 2017-02-28 15:32.
 */
class ParametersTest extends Specification {
  def "没有参数返回空串"() {
    expect:
    Parameters.toString(null) == ""
    Parameters.toString(new Object[0]) == ""
  }

  def "简单类型处理"() {
    given:
    def longString = '*' * 300
    def shortString = '*' * 256 + "..."

    expect:
    Parameters.toString(null, 1) == "null,1"
    Parameters.toString(1, 2L, true, 'x', "Y") == "1,2,true,x,Y"
    Parameters.toString(longString) == shortString
  }

  def "测试注解信息"() {
    given:
    def p = new ParamObj();

    expect:
    Parameters.toString(p) == "a=2,b=3"
  }
}
