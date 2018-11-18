package io.kanuka.web.javlin;

import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import java.util.Set;

class MethodParam {

  private final TypeMirror typeMirror;
  private final String rawType;
  private final TypeHandler typeHandler;
  private final String name;


  MethodParam(VariableElement param) {
    this.typeMirror = param.asType();
    this.name = param.getSimpleName().toString();
    this.rawType = typeMirror.toString();
    this.typeHandler = TypeMap.get(rawType);
  }

  void buildCtxGet(Append writer, Set<String> pathParams) {

    //TODO: Handle passing io.javalin.Context ...

    String shortType;
    if (typeHandler != null) {
      shortType = typeHandler.shortName();
    } else {
      shortType = Util.shortName(rawType);
    }

    writer.append("      %s %s = ", shortType, name);
    String asMethod = (typeHandler == null) ? null : typeHandler.asMethod();

    if (asMethod != null) {
      writer.append(asMethod);
    }
    if (pathParams.contains(name)) {
      writer.append("ctx.pathParam(\"%s\")", name);

    } else {
      if (typeHandler == null) {
        // assuming this is a body (POST, PATCH)
        writer.append("ctx.bodyAsClass(%s.class)", shortType, name, shortType);
      } else {
        writer.append("ctx.queryParam(\"%s\")", name);
      }
    }

    if (asMethod != null) {
      writer.append(")");
    }
    writer.append(";").eol();
  }

  void addImports(BeanReader bean) {
    if (typeHandler != null) {
      String importType = typeHandler.getImportType();
      if (importType != null) {
        bean.addImportType(rawType);
      }
    } else {
      bean.addImportType(rawType);
    }
  }

  String getName() {
    return name;
  }


//
//  String builderGetDependency() {
//    StringBuilder sb = new StringBuilder();
//    if (listType) {
//      sb.append("builder.getList(");
//    } else if (optionalType) {
//      sb.append("builder.getOptional(");
//    } else {
//      sb.append("builder.get(");
//    }
//
//    sb.append(paramType).append(".class");
//
//    sb.append(")");
//    return sb.toString();
//  }

}
