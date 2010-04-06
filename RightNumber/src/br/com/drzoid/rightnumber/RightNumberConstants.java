package br.com.drzoid.rightnumber;

public class RightNumberConstants {
  // Global constants
  public static final String LOG_TAG = "RightNumber";

  // Constants for special cases

  // Brazil - while we don't want to favor any specific carrier by putting it
  // as default, there's no way to detect the number for the current network's
  // carrier, so we picked this one at random.
  public static final String BRAZIL_DEFAULT_CARRIER = "21";
  public static final String BRAZIL_COUNTRY_CODE = "BR";
  public static final String BRAZIL_NATIONAL_PREFIX = "0";
  public static final String BRAZIL_INTERNATIONAL_PREFIX = "00";

  private RightNumberConstants() { }
}
