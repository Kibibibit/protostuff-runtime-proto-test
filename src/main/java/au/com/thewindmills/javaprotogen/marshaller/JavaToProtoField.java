package au.com.thewindmills.javaprotogen.marshaller;

import java.util.HashMap;
import java.util.Map;

public class JavaToProtoField {
    
    private Map<String, String> typeMappings;

    public JavaToProtoField() {

        typeMappings = new HashMap<>();

        typeMappings.put("int", "int32");
        typeMappings.put("String", "string");
        typeMappings.put("long", "int64");


    }


    public String mapType(String type) {

        if (typeMappings.containsKey(type)) {
            return typeMappings.get(type);
        } else {
            System.out.println("No defined mapping for type " + type + ", so just passing in the message name!");
            return type;
        }

    }

}
