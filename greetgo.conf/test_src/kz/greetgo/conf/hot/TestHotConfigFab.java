package kz.greetgo.conf.hot;

public class TestHotConfigFab extends FileConfigFactory {

  private final String baseDir;
  private final String configFileExt;

  public TestHotConfigFab(String baseDir, String configFileExt) {
    this.baseDir = baseDir;
    this.configFileExt = configFileExt;
  }

  @Override
  protected String getConfigFileExt() {
    return configFileExt;
  }

  @Override
  protected String getBaseDir() {
    return baseDir;
  }

  public HotConfig1 createConfig1() {
    return createConfig(HotConfig1.class);
  }

  public HotConfig2 createConfig2() {
    return createConfig(HotConfig2.class);
  }
}
