package au.com.thewindmills.javaprotogen;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
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
import net.webby.protostuff.runtime.Generators;
import net.webby.protostuff.runtime.ProtoGenerator;
import life.genny.qwandaq.annotation.ProtoMessage;

/**
 * Hello world!
 *
 */
public class App {

    public static final String PROTO_DIRECTORY = new File("").getAbsolutePath() + "/target/protos/";

    public static void main(String[] args) throws ClassNotFoundException {

        System.out.println("Hello");

        List<Class<?>> classes = new ArrayList<>();

        try (ScanResult scanResult =
                new ClassGraph()
                    .enableAnnotationInfo()
                    .enableClassInfo()
                    .acceptPackages("life.genny.qwandaq")
                    .scan()) {               // Start the scan
            for (ClassInfo routeClassInfo : scanResult.getClassesWithAnnotation(ProtoMessage.class)) {
                Class<?> clazzOf = Class.forName(routeClassInfo.getName());
                classes.add(clazzOf);
            }
        }

        File parentDir = new File(PROTO_DIRECTORY);
        parentDir.mkdir();

        Set<String> messages = new HashSet<String>();
        String header = getProtoHeader(classes.get(0));
        System.out.println(header);
        for(Class<?> clazz : classes) {
            // generateProtoToFile(PROTO_DIRECTORY, clazz);
            Set<String> clazzMsgs = getMessages(clazz);
            int newMsgs = clazzMsgs.size();
            int oldMsgCount = messages.size();
            messages.addAll(clazzMsgs);
            System.out.println("Added " + (messages.size() - oldMsgCount) + " messages to message stack of " + newMsgs + " new messages");
        }

        // Generate a big proto
        
        String fileName = PROTO_DIRECTORY + "BIG_PROTO" + ".proto";
        File file = new File(fileName);
        try {
            file.createNewFile();
        } catch(IOException e) {
            System.err.println("Failed to create new file: " + fileName);
            e.printStackTrace();
        }
        try(FileWriter fw = new FileWriter(fileName)) {
            BufferedWriter writer = new BufferedWriter(new FileWriter(fileName, true));
            writer.write(header);
            for(String message : messages) {
                writer.write(message.strip() + "\n");
            }
            writer.close();
        } catch(IOException e) {
        }
    }

    public static String getProtoHeader(Class<?> clazz) {
        Schema<?> schema = RuntimeSchema.getSchema(clazz);
        String content = Generators.newProtoGenerator(schema).generate();
        String[] contents = content.split("\n");
        String header = "";

        for(String c : contents) {
            if(c.startsWith("message"))
                break;
            header += c + "\n";
        }

        return header;
    }

    public static Set<String> getMessages(Class<?> clazz) {
        Schema<?> schema = RuntimeSchema.getSchema(clazz);
        ProtoGenerator generator = Generators.newProtoGenerator(schema);
        String content = generator.generate();
        String[] contents = content.split("\n");
        boolean foundMessage = false;
        String currentMessage = "";
        Set<String> messages = new HashSet<String>();

        for(String c : contents) {
            if(c.startsWith("message"))
                foundMessage = true;
            if(foundMessage) {
                currentMessage += c + "\n";
                if("}".equals(c)) {
                    messages.add(currentMessage);
                    currentMessage = "";
                    foundMessage = false;
                }
            }
        }
        
        return messages;
    }

    private static boolean generateProtoToFile(String absoluteDir, Class<?> classToSerialise) {
        Schema<?> schema = RuntimeSchema.getSchema(classToSerialise);
        System.out.println("----------" + schema.messageName()+ "----------");
        String content = Generators.newProtoGenerator(schema).generate();
        // System.out.println(content);
        String fileName = absoluteDir + schema.messageName() + ".proto";
        File file = new File(fileName);
        try {
            file.createNewFile();
        } catch(IOException e) {
            System.err.println("Failed to create new file: " + fileName);
            e.printStackTrace();
            return false;
        }
        try(FileWriter fw = new FileWriter(fileName)) {
            BufferedWriter writer = new BufferedWriter(new FileWriter(fileName, true));
            writer.write(content);
            writer.close();
        } catch(IOException e) {
            return false;
        }
        return true;
    }
}
