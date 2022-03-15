package au.com.thewindmills.javaprotogen;

import java.util.ArrayList;
import java.util.List;

import com.dyuproject.protostuff.Schema;
import com.dyuproject.protostuff.runtime.RuntimeSchema;

import au.com.thewindmills.javaprotogen.marshaller.Marshaller;
import au.com.thewindmills.javaprotogen.models.TestModel;
import au.com.thewindmills.javaprotogen.models.TestSimpleModel;
import io.quarkus.runtime.annotations.QuarkusMain;
import life.genny.qwandaq.Answer;
import life.genny.qwandaq.entity.BaseEntity;
import life.genny.qwandaq.message.QBulkMessage;
import net.webby.protostuff.runtime.Generators;

/**
 * Hello world!
 *
 */
@QuarkusMain
public class App {
    public static void main(String[] args) {

        System.out.println("Hello");

        Marshaller marshaller = new Marshaller();
        marshaller.createProto(TestSimpleModel.class);

        List<Schema<?>> schemas = new ArrayList<>();
        schemas.add(RuntimeSchema.getSchema(TestSimpleModel.class));
        schemas.add(RuntimeSchema.getSchema(TestModel.class));

        schemas.add(RuntimeSchema.getSchema(Answer.class));
        schemas.add(RuntimeSchema.getSchema(QBulkMessage.class));
        schemas.add(RuntimeSchema.getSchema(BaseEntity.class));

        for (Schema<?> schema : schemas) {
            System.out.println("----------------------");
            String content = Generators.newProtoGenerator(schema).generate();
            System.out.println(content);
        }
        

    }
}
