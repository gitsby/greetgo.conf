package kz.greetgo.conf;

import kz.greetgo.conf.hot.CannotConvertToType;
import kz.greetgo.conf.hot.CannotDetectDateFormat;
import kz.greetgo.conf.hot.DefaultBoolValue;
import kz.greetgo.conf.hot.DefaultIntValue;
import kz.greetgo.conf.hot.DefaultLongValue;
import kz.greetgo.conf.hot.DefaultStrValue;
import kz.greetgo.conf.hot.TooManyDefaultAnnotations;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ConfUtil {
  public static void readFromFile(Object readTo, File file) throws Exception {
    readFromStream(readTo, new FileInputStream(file));
  }

  public static void readFromFile(Object readTo, String fileName) throws Exception {
    readFromFile(readTo, new File(fileName));
  }

  @SuppressWarnings({"UnnecessaryLabelOnContinueStatement", "UnnecessaryContinue"})
  public static void readFromStream(Object readTo, InputStream inputStream) throws Exception {
    if (readTo == null) {
      inputStream.close();
      return;
    }

    Class<?> class1 = readTo.getClass();

    ConfData cd = new ConfData();
    cd.readFromStream(inputStream);

    final Map<String, Method> setMethods = new HashMap<>();

    for (Method method : class1.getMethods()) {
      if (method.getName().startsWith("set") && method.getParameterTypes().length == 1) {
        setMethods.put(method.getName(), method);
      }
    }

    FOR:
    for (String name : cd.list(null)) {
      {
        String setMethodName = "set" + name.substring(0, 1).toUpperCase() + name.substring(1);
        Method method = setMethods.get(setMethodName);
        if (method != null) {
          method.invoke(readTo, convertToType(cd.str(name), method.getParameterTypes()[0]));
          continue FOR;
        }
      }
      try {
        Field field = class1.getField(name);
        field.set(readTo, convertToType(cd.str(name), field.getType()));
        continue FOR;
      } catch (NoSuchFieldException ignored) {
      }
    }
  }

  public static String readFile(File file) {
    try {
      try (FileInputStream in = new FileInputStream(file)) {
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        byte[] buf = new byte[1024 * 4];

        while (true) {
          int count = in.read(buf);
          if (count < 0) return bout.toString("UTF-8");
          bout.write(buf, 0, count);
        }
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public static void writeFile(File file, String content) {
    try (PrintStream out = new PrintStream(file, "UTF-8")) {
      out.print(content);
    } catch (FileNotFoundException | UnsupportedEncodingException e) {
      throw new RuntimeException(e);
    }
  }

  public static final String rus = "абвгдеёжзийклмнопрстуфхцчшщъыьэюя";
  public static final String RUS = rus.toUpperCase();
  @SuppressWarnings("SpellCheckingInspection")
  public static final String eng = "abcdefghijklmnopqrstuvwxyz";
  public static final String ENG = eng.toUpperCase();
  public static final String DEG = "0123456789";
  public static final char[] ALL_CHARS = (eng + ENG + rus + RUS + DEG).toCharArray();
  public static final int ALL_CHARS_LEN = ALL_CHARS.length;

  public static final Random rnd = new Random();

  @SuppressWarnings("unused")
  public static String rndStr(int len) {
    char[] ret = new char[len];
    for (int i = 0; i < len; i++) {
      ret[i] = ALL_CHARS[rnd.nextInt(ALL_CHARS_LEN)];
    }
    return new String(ret);
  }

  @SuppressWarnings("UnnecessaryContinue")
  public static String extractStrDefaultValue(Annotation[] annotations, Function<String, String> parameterReplacer) {
    String value = null;
    List<String> aa = new ArrayList<>();
    for (Annotation a : annotations) {
      if (a instanceof DefaultStrValue) {
        DefaultStrValue b = (DefaultStrValue) a;
        value = parameterReplacer.apply(b.value());
        aa.add("DefaultStrValue(" + value + ")");
        continue;
      }
      if (a instanceof DefaultIntValue) {
        DefaultIntValue b = (DefaultIntValue) a;
        value = "" + b.value();
        aa.add("DefaultIntValue(" + value + ")");
        continue;
      }
      if (a instanceof DefaultLongValue) {
        DefaultLongValue b = (DefaultLongValue) a;
        value = "" + b.value();
        aa.add("DefaultLongValue(" + b.value() + ")");
        continue;
      }
      if (a instanceof DefaultBoolValue) {
        DefaultBoolValue b = (DefaultBoolValue) a;
        value = "" + b.value();
        aa.add("DefaultBoolValue(" + b.value() + ")");
        continue;
      }
    }

    if (aa.size() > 1) {
      throw new TooManyDefaultAnnotations(aa);
    }
    return value;
  }

  public static String convertToStr(Object value) {
    if (value == null) {
      return null;
    }
    if (value instanceof String) {
      return (String) value;
    }
    return "" + value;
  }

  public static String nullToEmpty(String str) {
    return str == null ? "" : str;
  }

  public static String concatNewLine(String s1, String s2) {
    if (s1 == null) {
      return s2;
    }
    if (s2 == null) {
      return s1;
    }
    return s1 + "\n" + s2;
  }

  private static final class PatternFormat {
    final Pattern pattern;
    final SimpleDateFormat format;

    public PatternFormat(Pattern pattern, SimpleDateFormat format) {
      this.pattern = pattern;
      this.format = format;
    }
  }

  private static final List<PatternFormat> PATTERN_FORMAT_LIST = new ArrayList<>();

  private static void addPatternFormat(String patternStr, String formatStr) {
    PATTERN_FORMAT_LIST.add(new PatternFormat(Pattern.compile(patternStr), new SimpleDateFormat(formatStr)));
  }

  static {
    addPatternFormat("(\\d{4}-\\d{2}-\\d{2})", "yyyy-MM-dd");
    addPatternFormat("(\\d{4}-\\d{2}-\\d{2}\\s\\d{2}:\\d{2})", "yyyy-MM-dd HH:mm");
    addPatternFormat("(\\d{4}-\\d{2}-\\d{2}\\s\\d{2}:\\d{2}:\\d{2})", "yyyy-MM-dd HH:mm:ss");
    addPatternFormat("(\\d{4}-\\d{2}-\\d{2}\\s\\d{2}:\\d{2}:\\d{2}\\.\\d{3})",
      "yyyy-MM-dd HH:mm:ss.SSS");
    addPatternFormat("(\\d{4}-\\d{2}-\\d{2}/\\d{2}:\\d{2}:\\d{2}\\.\\d{3})",
      "yyyy-MM-dd/HH:mm:ss.SSS");
    addPatternFormat("(\\d{2}:\\d{2}:\\d{2}\\.\\d{3})", "HH:mm:ss.SSS");
    addPatternFormat("(\\d{2}:\\d{2}:\\d{2})", "HH:mm:ss");
    addPatternFormat("(\\d{2}:\\d{2})", "HH:mm");

    addPatternFormat("(\\d{2}/\\d{2}/\\d{4}\\s\\d{2}:\\d{2}:\\d{2}\\.\\d{3})",
      "dd/MM/yyyy HH:mm:ss.SSS");
    addPatternFormat("(\\d{2}/\\d{2}/\\d{4}\\s\\d{2}:\\d{2}:\\d{2})", "dd/MM/yyyy HH:mm:ss");
    addPatternFormat("(\\d{2}/\\d{2}/\\d{4}\\s\\d{2}:\\d{2})", "dd/MM/yyyy HH:mm");
    addPatternFormat("(\\d{2}/\\d{2}/\\d{4})", "dd/MM/yyyy");

    addPatternFormat("(\\d{2}\\.\\d{2}\\.\\d{4}\\s\\d{2}:\\d{2}:\\d{2}\\.\\d{3})",
      "dd.MM.yyyy HH:mm:ss.SSS");
    addPatternFormat("(\\d{2}\\.\\d{2}\\.\\d{4}\\s\\d{2}:\\d{2}:\\d{2})", "dd.MM.yyyy HH:mm:ss");
    addPatternFormat("(\\d{2}\\.\\d{2}\\.\\d{4}\\s\\d{2}:\\d{2})", "dd.MM.yyyy HH:mm");
    addPatternFormat("(\\d{2}\\.\\d{2}\\.\\d{4})", "dd.MM.yyyy");
  }

  public static Object convertToType(String str, Class<?> type) {
    if (type == null) {return null;}
    if (type.isAssignableFrom(String.class)) {
      return str;
    }
    if (type == boolean.class || type == Boolean.class) {
      if (str == null) {return type == Boolean.class ? null : false;}
      return strToBool(str);
    }
    if (type == int.class || type == Integer.class) {
      if (str == null || str.trim().length() == 0) {return type == Integer.class ? null : 0;}
      if ("true".equals(str))  {return 1;}
      if ("false".equals(str)) {return 0;}
      try {
        return Integer.parseInt(str);
      } catch (NumberFormatException e) {
        throw new CannotConvertToType(str, type, e);
      }
    }
    if (type == long.class || type == Long.class) {
      if (str == null || str.trim().length() == 0) {return type == Long.class ? null : 0L;}
      if ("true".equals(str))  {return 1L;}
      if ("false".equals(str)) {return 0L;}
      try {
        return Long.parseLong(str);
      } catch (NumberFormatException e) {
        throw new CannotConvertToType(str, type, e);
      }
    }
    if (type == Double.TYPE || type.isAssignableFrom(Double.class)) {
      if (str == null) {return 0d;}
      try {
        return Double.parseDouble(str);
      } catch (NumberFormatException e) {
        throw new CannotConvertToType(str, type, e);
      }
    }
    if (type == Float.TYPE || type.isAssignableFrom(Float.class)) {
      if (str == null) {return 0f;}
      return Float.parseFloat(str);
    }
    if (type.isAssignableFrom(BigDecimal.class)) {
      if (str == null) {return BigDecimal.ZERO;}
      try {
        return new BigDecimal(str);
      } catch (NumberFormatException e) {
        throw new CannotConvertToType(str, type, e);
      }
    }
    if (type.isAssignableFrom(Date.class)) {
      if (str == null) {return null;}
      for (PatternFormat pf : PATTERN_FORMAT_LIST) {
        Matcher m = pf.pattern.matcher(str);
        if (m.matches()) {
          try {
            return pf.format.parse(m.group(1));
          } catch (ParseException e) {
            throw new CannotConvertToType(str, type, e);
          }
        }
      }
      throw new CannotDetectDateFormat(str,
        PATTERN_FORMAT_LIST.stream()
          .map(p -> p.format.toPattern())
          .collect(Collectors.toList())
      );
    }
    throw new CannotConvertToType(str, type);
  }

  @SuppressWarnings("RedundantIfStatement")
  public static boolean strToBool(String str) {
    if (str == null) return false;

    str = str.trim().toUpperCase();

    if ("T".equals(str)) return true;
    if ("TRUE".equals(str)) return true;
    if ("ON".equals(str)) return true;
    if ("1".equals(str)) return true;
    if ("Y".equals(str)) return true;
    if ("YES".equals(str)) return true;
    if ("И".equals(str)) return true;
    if ("ИСТИНА".equals(str)) return true;
    if ("ДА".equals(str)) return true;
    if ("Д".equals(str)) return true;
    if ("是的".equals(str)) return true;

    return false;
  }

  public static final Set<Class<?>> WRAPPER_TYPES = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
    Boolean.class, Character.class, Byte.class,
    Short.class, Integer.class, Long.class,
    Float.class, Double.class, Void.class
  )));

  public static boolean isWrapper(Class<?> aClass) {
    return WRAPPER_TYPES.contains(aClass);
  }

}
