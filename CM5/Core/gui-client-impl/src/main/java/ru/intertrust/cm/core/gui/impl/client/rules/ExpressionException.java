package ru.intertrust.cm.core.gui.impl.client.rules;

public class ExpressionException extends Exception {

  public ExpressionException(){}

  public ExpressionException(String msg){
    super(msg);
  }

  public ExpressionException(String msg,Throwable e){
    super(msg,e);
  }

  public ExpressionException(Throwable e){
    super(e);
  }
}
