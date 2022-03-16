package au.com.thewindmills.javaprotogen.marshaller;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import au.com.thewindmills.javaprotogen.annotations.ProtoField;
import life.genny.qwandaq.annotation.ProtoMessage;

public class Marshaller {

    private JavaToProtoField fieldMapping;
    private FileWriter fileWriter;
    private BufferedWriter bufferedWriter;

    private String packageName = "genny";
    private String fileName = "genny.proto";
    private String javaPackageName = "life.genny.model.grpc.comms";
    private boolean javaMultipleFiles = true;
    private String javaOuterClassName = "GennyCommsProto";
    private String protoSyntax = "proto3";

    public Marshaller() {
        fieldMapping = new JavaToProtoField();
    }

    public Marshaller withPackageName(String packageName) {
        this.packageName = packageName;
        return this;
    }

    public Marshaller withFileName(String fileName) {
        this.fileName = fileName;
        return this;
    }

    public Marshaller withJavaPackageName(String javaPackageName) {
        this.javaPackageName = javaPackageName;
        return this;
    }

    public Marshaller withJavaMultipleFiles(boolean javaMultipleFiles) {
        this.javaMultipleFiles = javaMultipleFiles;
        return this;
    }

    public Marshaller withJavaOuterClassName(String javaOuterClassName) {
        this.javaOuterClassName = javaOuterClassName;
        return this;
    }

    public Marshaller withProtoSyntax(String protoSytax) {
        this.protoSyntax = protoSytax;
        return this;
    }

    public void createProto(Class<?> clazz) {

        // Storing the fields
        Map<Integer, Field> fields = new HashMap<>();

        if (clazz.isAnnotationPresent(ProtoMessage.class)) {
            System.out.println("Found protomessage annotation on " + clazz.getSimpleName());

            int count = 1;

            for (Field field : clazz.getDeclaredFields()) {
                if (field.isAnnotationPresent(ProtoField.class)) {
                    int fieldNumber = count++;

                    System.out.println("Adding field " + field.getName() + " of type " + field.getType().getSimpleName()
                            + " to " + clazz.getSimpleName());
                    fields.put(fieldNumber, field);

                }
            }
        }

        // Now we have a map of integers to fields, time to actually make the message

        Map<Integer, String> protoFields = new HashMap<>();

        for (Entry<Integer, Field> fieldEntry : fields.entrySet()) {

            String type = fieldMapping.mapType(fieldEntry.getValue().getType().getSimpleName());
            if (type != null) {
                System.out.println("Mapped " + fieldEntry.getValue().getType().getSimpleName() + " to " + type);
                protoFields.put(fieldEntry.getKey(), type);
            }

        }

        try {
            writeToProtoFile(clazz, fields, protoFields);

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void write(String line) throws IOException {
        if (bufferedWriter != null) {
            bufferedWriter.write(line + "\n");
        }
    }

    private void newLine() throws IOException {
        if (bufferedWriter != null) {
            bufferedWriter.newLine();
        }
    }

    private void writeToProtoFile(Class<?> clazz, Map<Integer, Field> javaFields, Map<Integer, String> protoFields)
            throws IOException {

        File file = new File(fileName);
        boolean exists = file.exists();

        fileWriter = new FileWriter(file, true);
        bufferedWriter = new BufferedWriter(fileWriter);

        if (!exists) {
            System.out.println("No existing file, creating!");
            write("// --- AUTO-GENERATED PROTO FILE FOR: " + fileName + " ---");
            newLine();
            write("syntax = \"" + this.protoSyntax + "\";");
            newLine();
            newLine();
            write("option java_multiple_files = " + this.javaMultipleFiles + ";");
            write("option java_package = \"" + this.javaPackageName + "\";");
            write("option java_outer_classname = \"" + this.javaOuterClassName + "\";");
            newLine();
            write("package " + this.packageName + ";");

        }

        newLine();

        String messageName = clazz.getSimpleName();

        write("// -- Message for: " + messageName);
        write("message " + messageName + " {");

        for (Entry<Integer, String> fieldEntry : protoFields.entrySet()) {

            int fieldNumber = fieldEntry.getKey();
            String fieldName = javaFields.get(fieldNumber).getName();
            String type = fieldEntry.getValue();

            write("\t" + type + " " + fieldName + " = " + fieldNumber + ";");

        }

        write("}");

        bufferedWriter.close();
        fileWriter.close();

    }

}
