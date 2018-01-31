package com.ihs.commons.libraryconfig;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Arthur on 2018/1/31.
 */

public class HSLibraryconfig {
    public static HSLibraryconfig getInstance() {
        return new HSLibraryconfig();
    }

    public Map<String, ?> getDataForLibrary(ILibraryProvider sConfigProvider) {
        return new HashMap<>();
    }

    public interface ILibraryListener {
    }

    public interface ILibraryProvider {
        public int getLibraryVersionNumber();

        public String getLibraryName();
    }

}
