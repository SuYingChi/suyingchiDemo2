package com.ihs.inputmethod.uimodules.softgame;

/**
 * Created by Arthur on 17/11/7.
 */

public class SoftGameItemBean {
    private String name;
    private String description;
    private String thumb;
    private String link;

    public SoftGameItemBean(String name, String description, String thumb, String link) {
        this.name = name;
        this.description = description;
        this.thumb = thumb;
        this.link = link;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getThumb() {
        return thumb;
    }

    public String getLink() {
        return link;
    }
}
