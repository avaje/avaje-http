package io.avaje.http.generator.client;

import io.avaje.http.generator.core.Append;
import io.avaje.http.generator.core.ControllerReader;
import io.avaje.http.generator.core.ParamType;
import io.avaje.http.generator.core.PlatformAdapter;
import io.avaje.http.generator.core.UType;
import java.util.List;

class ClientPlatformAdapter implements PlatformAdapter {

  @Override
  public boolean isContextType(String rawType) {
    return false;
  }

  @Override
  public String platformVariable(String rawType) {
    return null;
  }

  @Override
  public String bodyAsClass(UType uType) {
    return null;
  }

  @Override
  public boolean isBodyMethodParam() {
    return false;
  }

  @Override
  public String indent() {
    return null;
  }

  @Override
  public void controllerRoles(List<String> roles, ControllerReader controller) {}

  @Override
  public void methodRoles(List<String> roles, ControllerReader controller) {}

  @Override
  public void writeReadParameter(Append writer, ParamType paramType, String paramName) {}

  @Override
  public void writeReadParameter(
      Append writer, ParamType paramType, String paramName, String paramDefault) {}
}
