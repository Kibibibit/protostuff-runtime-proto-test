package au.com.thewindmills.javaprotogen;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.dyuproject.protostuff.Schema;
import com.dyuproject.protostuff.runtime.RuntimeSchema;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfo;
import io.github.classgraph.ScanResult;
import life.genny.qwandaq.annotation.ProtoMessage;
import net.webby.protostuff.runtime.Generators;

/**
 * Hello world!
 *
 */
public class App {

    public static final String PROTO_DIRECTORY = new File("").getAbsolutePath() + "/target/protos/";
    public static final String DEFAULT_PROTO = new File("").getAbsolutePath() + "/src/main/resources/protostuff-default.proto";

    public static void main(String[] args) throws ClassNotFoundException {

        System.out.println("Hello");

        List<Class<?>> classes = new ArrayList<>();

        try (ScanResult scanResult = new ClassGraph()
                .enableAnnotationInfo()
                .enableClassInfo()
                .acceptPackages("life.genny.qwandaq")
                .scan()) { // Start the scan
            for (ClassInfo routeClassInfo : scanResult.getClassesWithAnnotation(ProtoMessage.class)) {
                Class<?> clazzOf = Class.forName(routeClassInfo.getName());
                classes.add(clazzOf);
            }
        }

        File parentDir = new File(PROTO_DIRECTORY);
        parentDir.mkdir();

        Set<String> messages = new HashSet<String>();
        String header = getProtoHeader();
        System.out.println(header);
        classes.parallelStream().forEach((clazz) -> {
            String content = generateProtoToFile(clazz);
            Set<String> clazzMsgs = getMessages(content);
            int newMsgs = clazzMsgs.size();
            int oldMsgCount = messages.size();
            messages.addAll(clazzMsgs);
            System.out.println("Added " + (messages.size() - oldMsgCount) + " messages to message stack of " + newMsgs
                    + " new messages");
        });

        // Generate a big proto

        String fileName = PROTO_DIRECTORY + "BIG_PROTO" + ".proto";
        File file = new File(fileName);
        try {
            file.createNewFile();
        } catch (IOException e) {
            System.err.println("Failed to create new file: " + fileName);
            e.printStackTrace();
        }
        try (FileWriter fw = new FileWriter(fileName)) {
            BufferedWriter writer = new BufferedWriter(new FileWriter(fileName, true));
            writer.write(header);
            for (String message : messages) {
                writer.write(message.strip() + "\n");
            }
            writer.write(getDefaultProtoContent());
            writer.close();
        } catch (IOException e) {
        }
    }

    @Deprecated
    public static String getProtoHeader(Class<?> clazz) {
        Schema<?> schema = RuntimeSchema.getSchema(clazz);
        String content = Generators.newProtoGenerator(schema).generate();
        String[] contents = content.split("\n");
        String header = "";

        for (String c : contents) {
            if (c.startsWith("message"))
                break;
            header += c + "\n";
        }

        return header;
    }

    public static String getDefaultProtoContent() throws IOException {
        File file = new File(DEFAULT_PROTO);

        if (!file.exists()) {
            System.err.println("Couldn't find default proto at " + file.getAbsolutePath());
            return null;
        }

        FileReader fileReader;
        try {
            fileReader = new FileReader(file);
        } catch (FileNotFoundException e) {
            
            e.printStackTrace();
            return null;
        }
        BufferedReader bufferedReader = new BufferedReader(fileReader);

        String content = "";
        String line = bufferedReader.readLine();
        while (line != null) {
            content += line + "\n";
            line = bufferedReader.readLine();
        }

        fileReader.close();
        bufferedReader.close();

        return content;


    }
    
    public static String getProtoHeader() {

        String header = "// --- AUTO-GENERATED PROTO FILE FOR: BIG_PROTO.proto ---\n";
        header += "package genny.protos;\n\n";
        header += "syntax = \"proto2\";\n";
        header += "option java_multiple_files = true;\n";
        header += "option java_package = \"life.genny.generated\";\n";
        header += "option java_outer_classname = \"GeneratedProtoClass\";\n\n\n";
        

        return header;

    }

    public static Set<String> getMessages(final String content) {
        String[] contents = content.split("\n");
        boolean foundMessage = false;
        String currentMessage = "";
        Set<String> messages = new HashSet<String>();

        for (String c : contents) {
            if (c.startsWith("message"))
                foundMessage = true;
            if (foundMessage) {
                currentMessage += c + "\n";
                if ("}".equals(c)) {
                    messages.add(currentMessage);
                    currentMessage = "";
                    foundMessage = false;
                }
            }
        }

        return messages;
    }

    private static String generateProtoToFile(Class<?> classToSerialise) {
        Schema<?> schema = RuntimeSchema.getSchema(classToSerialise);
        System.out.println("----------" + schema.messageName() + "----------");
        String content = Generators.newProtoGenerator(schema).generate();
        // System.out.println(content);
        String fileName = PROTO_DIRECTORY + schema.messageName() + ".proto";
        File file = new File(fileName);
        try {
            file.createNewFile();
        } catch (IOException e) {
            System.err.println("Failed to create new file: " + fileName);
            e.printStackTrace();
            return "";
        }
        try (FileWriter fw = new FileWriter(fileName)) {
            BufferedWriter writer = new BufferedWriter(new FileWriter(fileName, true));
            writer.write(content);
            writer.close();
        } catch (IOException e) {
            return "";
        }
        return content;
    }
}
