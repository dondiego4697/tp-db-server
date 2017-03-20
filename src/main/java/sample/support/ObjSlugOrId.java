package sample.support;

/**
 * Created by Denis on 20.03.2017.
 */
public class ObjSlugOrId {

    private Integer id = null;
    private String slug = null;
    boolean flag = true; // false - id, true - slug

    public ObjSlugOrId(String slugOrId) {
        try {
            id = Integer.parseInt(slugOrId);
            flag = false;
        } catch (Exception e) {
            slug = slugOrId;
            flag = true;
        }
    }

    public Boolean getFlag(){
        return flag;
    }

    public String getSlug(){
        return slug;
    }

    public Integer getId() {
        return id;
    }
}
