package net.runelite.client.plugins.VoxSylvaePlugins.scraper.model;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
public class WikipediaPage {
    private String title;
    private String content;
    private int revisionId;
    private String url;
    private Map<String, String> categories;
    private List<String> links;

    public WikipediaPage(String title, String content, int revisionId) {
        this.title = title;
        this.content = content;
        this.revisionId = revisionId;
    }

    public boolean hasCategory(String category) {
        return categories != null && categories.containsKey(category);
    }

    public boolean hasLink(String link) {
        return links != null && links.contains(link);
    }

    @Override
    public String toString() {
        return "WikipediaPage{" +
                "title='" + title + '\'' +
                ", revisionId=" + revisionId +
                ", url='" + url + '\'' +
                ", categories=" + categories +
                ", links=" + links +
                '}';
    }
}