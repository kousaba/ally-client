package com.allyclient.script.lib;

import java.util.List;

@FunctionalInterface
public interface FunctionImpl {
    Object call(List<Object> args);
}