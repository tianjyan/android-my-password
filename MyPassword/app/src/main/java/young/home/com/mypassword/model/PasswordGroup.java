package young.home.com.mypassword.model;

/**
 * Created by YOUNG on 2016/4/4.
 */
public class PasswordGroup {
    private String groupName;

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    @Override
    public String toString() {
        return "PasswordGroup [groupName=" + groupName + "]";
    }
}
