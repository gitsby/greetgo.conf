package kz.greetgo.conf.hot;

import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

public class AbstractConfigFactoryTest {

  class Testing extends AbstractConfigFactory {
    final ConfigStorageForTests cs = new ConfigStorageForTests();

    @Override
    protected ConfigStorage getConfigStorage() {
      return cs;
    }

    @Override
    protected <T> String configLocationFor(Class<T> configInterface) {
      return configInterface.getSimpleName() + ".txt";
    }

    @Override
    protected String replaceParametersInDefaultStrValue(String value) {
      return value.replaceAll("T1001", "Жыдкий терминатор");
    }
  }

  @Test
  public void createInManyThreads() throws Exception {

    final Testing testing = new Testing();

    Thread[] tt = new Thread[10];

    final AtomicBoolean reading = new AtomicBoolean(true);

    for (int i = 0; i < tt.length; i++) {
      tt[i] = new Thread(() -> {
        HotConfig1 config1 = testing.createConfig(HotConfig1.class);
        HotConfig2 config2 = testing.createConfig(HotConfig2.class);

        while (reading.get()) {
          config1.boolExampleValue();
          config1.intExampleValue();
          config1.intExampleValue2();
          config1.strExampleValue();

          config2.asd();
          config2.intAsd();
        }
      });
    }

    for (Thread t : tt) {
      t.start();
    }

    for (int i = 0; i < 10; i++) {
      Thread.sleep(70);
      testing.resetAll();
    }

    Thread.sleep(70);

    reading.set(false);

    for (Thread t : tt) {
      t.join();
    }

    assertThat(testing.cs.callCountOfLoadConfigContent.get()).isEqualTo(20);
    assertThat(testing.cs.callCountOfIsConfigContentExists.get()).isEqualTo(22);
    assertThat(testing.cs.callCountOfSaveConfigContent.get()).isEqualTo(2);

  }

  @Test
  public void checkArrays_new() {
    final Testing testing = new Testing();

    HostConfigWithLists config = testing.createConfig(HostConfigWithLists.class);

    assertThat(config.elementA().intField).isEqualTo(20019);
    assertThat(config.elementA().strField).isEqualTo("By one");

    String content = testing.cs.contentMap.get("HostConfigWithLists.txt");
    content = Arrays.stream(content.split("\n"))
      .filter(s -> s.trim().length() > 0)
      .filter(s -> !s.trim().startsWith("#"))
      .sorted()
      .collect(Collectors.joining("\n"));

    assertThat(content).isEqualTo("" +
      "elementA.intField=20019\n" +
      "elementA.strField=By one\n" +
      "elementB.0.intField=20019\n" +
      "elementB.0.strField=By one\n" +
      "elementB.listElementsCount=1\n" +
      "status=0");
  }

  @Test
  public void checkArrays_hasContent() {
    final Testing testing = new Testing();

    testing.cs.contentMap.put("HostConfigWithLists.txt", "" +
      "elementB.listElementsCount=3\n" +
      "elementB.0.intField = 45000\n" +
      "elementB.0.strField = The new begins\n" +
      "elementB.1.intField = 456\n" +
      "elementB.1.strField = hello world\n" +
      "\n" +
      "elementA.intField = 709\n" +
      ""
    );

    HostConfigWithLists config = testing.createConfig(HostConfigWithLists.class);

    assertThat(config.elementA().intField).isEqualTo(709);
    assertThat(config.elementA().strField).isEqualTo("By one");

    assertThat(config.elementB().get(0).intField).isEqualTo(45_000);
    assertThat(config.elementB().get(0).strField).isEqualTo("The new begins");
    assertThat(config.elementB().get(1).intField).isEqualTo(456);
    assertThat(config.elementB().get(1).strField).isEqualTo("hello world");
    assertThat(config.elementB().get(2).intField).isEqualTo(20019);
    assertThat(config.elementB().get(2).strField).isEqualTo("By one");

    String content = testing.cs.contentMap.get("HostConfigWithLists.txt");
    content = Arrays.stream(content.split("\n"))
      .filter(s -> s.trim().length() > 0)
      .filter(s -> !s.trim().startsWith("#"))
      .sorted()
      .collect(Collectors.joining("\n"));

    assertThat(content).isEqualTo("" +
      "elementA.intField = 709\n" +
      "elementA.strField=By one\n" +
      "elementB.0.intField = 45000\n" +
      "elementB.0.strField = The new begins\n" +
      "elementB.1.intField = 456\n" +
      "elementB.1.strField = hello world\n" +
      "elementB.2.intField=20019\n" +
      "elementB.2.strField=By one\n" +
      "elementB.listElementsCount=3\n" +
      "status=0");
  }

  @Test
  public void replaceParametersInDefaultStrValue() {
    final Testing testing = new Testing();
    HotConfig1 config1 = testing.createConfig(HotConfig1.class);

    assertThat(config1.name()).isEqualTo("Привет Жыдкий терминатор");
  }

  @Test
  public void defaultListSize_new_reset_exists() {
    final Testing testing = new Testing();

    HotConfigWithDefaultListSize config = testing.createConfig(HotConfigWithDefaultListSize.class);

    assertThat(config.longList()).hasSize(9);
    assertThat(config.classList()).hasSize(7);

    String location = testing.configLocationFor(HotConfigWithDefaultListSize.class);

    String content = Arrays.stream(testing.cs.contentMap.get(location).split("\n"))
      .filter(s -> s.trim().length() > 0)
      .filter(s -> !s.trim().startsWith("#"))
      .sorted()
      .collect(Collectors.joining("\n"));

    assertThat(content).isEqualTo("classList.0.intField=20019\n" +
      "classList.0.strField=By one\n" +
      "classList.1.intField=20019\n" +
      "classList.1.strField=By one\n" +
      "classList.2.intField=20019\n" +
      "classList.2.strField=By one\n" +
      "classList.3.intField=20019\n" +
      "classList.3.strField=By one\n" +
      "classList.4.intField=20019\n" +
      "classList.4.strField=By one\n" +
      "classList.5.intField=20019\n" +
      "classList.5.strField=By one\n" +
      "classList.6.intField=20019\n" +
      "classList.6.strField=By one\n" +
      "classList.listElementsCount=7\n" +
      "longList.0=70078\n" +
      "longList.1=70078\n" +
      "longList.2=70078\n" +
      "longList.3=70078\n" +
      "longList.4=70078\n" +
      "longList.5=70078\n" +
      "longList.6=70078\n" +
      "longList.7=70078\n" +
      "longList.8=70078\n" +
      "longList.listElementsCount=9");

    testing.cs.contentMap.put(location, "classList.2.strField=Boom loon hi\n" +
      "classList.5.intField=119988\n" +
      "longList.4=4511\n");

    testing.resetAll();

    assertThat(config.longList()).hasSize(9);
    assertThat(config.classList()).hasSize(7);

    String content2 = Arrays.stream(testing.cs.contentMap.get(location).split("\n"))
      .filter(s -> s.trim().length() > 0)
      .filter(s -> !s.trim().startsWith("#"))
      .sorted()
      .collect(Collectors.joining("\n"));

    assertThat(content2).isEqualTo("classList.0.intField=20019\n" +
      "classList.0.strField=By one\n" +
      "classList.1.intField=20019\n" +
      "classList.1.strField=By one\n" +
      "classList.2.intField=20019\n" +
      "classList.2.strField=Boom loon hi\n" +
      "classList.3.intField=20019\n" +
      "classList.3.strField=By one\n" +
      "classList.4.intField=20019\n" +
      "classList.4.strField=By one\n" +
      "classList.5.intField=119988\n" +
      "classList.5.strField=By one\n" +
      "classList.6.intField=20019\n" +
      "classList.6.strField=By one\n" +
      "classList.listElementsCount=7\n" +
      "longList.0=70078\n" +
      "longList.1=70078\n" +
      "longList.2=70078\n" +
      "longList.3=70078\n" +
      "longList.4=4511\n" +
      "longList.5=70078\n" +
      "longList.6=70078\n" +
      "longList.7=70078\n" +
      "longList.8=70078\n" +
      "longList.listElementsCount=9");
  }
}
