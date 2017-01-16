package daon.analysis.ko.model;

import java.util.List;

public class TagConnection {

    /**
     * 현재 태그
     */
    private String tag;

    /**
     * 다음 매칭 가능 태그들
     */
    private List<TagCost> tags;

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public List<TagCost> getTags() {
        return tags;
    }

    public void setTags(List<TagCost> tags) {
        this.tags = tags;
    }

    @Override
    public String toString() {
        return "TagConnection [tag=" + tag + ", tags=" + tags + "]";
    }
}
