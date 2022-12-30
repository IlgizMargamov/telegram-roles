package roles;

import java.util.ArrayList;

public class NamedArrayList<S> extends ArrayList<S> {
    private final String name;

    public NamedArrayList(String listName){
        name = listName;
    }

    public String getName(){
        return name;
    }

    @Override
    public String toString() {
        return "roles.NamedArrayList{" +
                "name='" + name + '\'' +
                '}';
    }
}
