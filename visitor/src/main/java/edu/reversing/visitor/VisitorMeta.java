package edu.reversing.visitor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
public @interface VisitorMeta {
  String name();
}
