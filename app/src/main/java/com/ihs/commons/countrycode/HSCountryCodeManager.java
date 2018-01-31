package com.ihs.commons.countrycode;

/**
 * Created by Arthur on 18/1/17.
 */

public class HSCountryCodeManager {
    public static HSCountryCodeManager getInstance() {
        return new HSCountryCodeManager();
    }

    public String getCountryCode() {
        return "en";
    }
}
