package au.com.thewindmills.javaprotogen.models;

import au.com.thewindmills.javaprotogen.annotations.ProtoField;

public class TestSimpleModel {

    @ProtoField
    private String name;

    @ProtoField
    private long id;

    public TestSimpleModel(String name, long id) {
        this.name = name;
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    
    
}
