package io.avaje.http.generator.core;

import java.util.Optional;

import javax.lang.model.element.Element;

import io.avaje.prism.GeneratePrism;

@GeneratePrism(
    value = io.avaje.http.api.QueryParam.class,
    publicAccess = true,
    superInterfaces = ParamPrism.class)
@GeneratePrism(
    value = io.avaje.http.api.Cookie.class,
    publicAccess = true,
    superInterfaces = ParamPrism.class)
@GeneratePrism(
    value = io.avaje.http.api.FormParam.class,
    publicAccess = true,
    superInterfaces = ParamPrism.class)
@GeneratePrism(
    value = io.avaje.http.api.Header.class,
    publicAccess = true,
    superInterfaces = ParamPrism.class)
@GeneratePrism(
    value = io.avaje.http.api.MatrixParam.class,
    publicAccess = true,
    superInterfaces = ParamPrism.class)
public interface ParamPrism {

  static boolean isPresent(Element e) {
    return Optional.<ParamPrism>empty()
        .or(() -> QueryParamPrism.getOptionalOn(e))
        .or(() -> CookiePrism.getOptionalOn(e))
        .or(() -> FormParamPrism.getOptionalOn(e))
        .or(() -> HeaderPrism.getOptionalOn(e))
        .or(() -> MatrixParamPrism.getOptionalOn(e))
        .isPresent();
  }

}
