package kz.greetgo.conf.type_manager;

import kz.greetgo.conf.hot.LineStructure;

import java.lang.annotation.Annotation;
import java.util.function.Function;

public interface TypeManager {

  Object newDefaultValue(Object defaultValue);

  Object extractDefaultValue(Annotation[] annotations, Function<String, String> parameterReplacer);

  Object fromStr(String strValue);

  LineStructure createLineStructure(String topFieldName, Object defaultValue, String description, Integer defaultListSize);

}
