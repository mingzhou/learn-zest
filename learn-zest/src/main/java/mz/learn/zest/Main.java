package mz.learn.zest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import mz.learn.zest.domain.TestEntity;
import mz.learn.zest.domain.TestEnum;
import mz.learn.zest.domain.TestValue;
import org.qi4j.api.activation.ActivationException;
import org.qi4j.api.common.Visibility;
import org.qi4j.api.entity.EntityBuilder;
import org.qi4j.api.structure.Module;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkCompletionException;
import org.qi4j.api.usecase.UsecaseBuilder;
import org.qi4j.api.value.ValueBuilder;
import org.qi4j.api.value.ValueSerialization;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.bootstrap.SingletonAssembler;
import org.qi4j.entitystore.memory.MemoryEntityStoreService;
import org.qi4j.spi.uuid.UuidIdentityGeneratorService;
import org.qi4j.valueserialization.jackson.JacksonValueSerializationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;

/**
 * Created by zhuangmz on 2016/4/15.
 */
public class Main {
    static Logger _sLogger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) throws ActivationException, AssemblyException, UnitOfWorkCompletionException, JsonProcessingException {
        SingletonAssembler assembler = new SingletonAssembler() {
            @Override
            public void assemble(ModuleAssembly module_)
                    throws AssemblyException {

                module_.values(TestValue.class);
                module_.entities(TestEntity.class);
                module_.services(JacksonValueSerializationService.class)
                        .taggedWith(ValueSerialization.Formats.JSON)
                        .visibleIn(Visibility.application)
                        .instantiateOnStartup();
                module_.services(MemoryEntityStoreService.class);
                module_.services(UuidIdentityGeneratorService.class);
            }
        };

        ObjectMapper mapper = new ObjectMapper();
        mapper.findAndRegisterModules();
        System.out.println(mapper.writeValueAsString(Instant.now()));

        Module module = assembler.module();
        ValueSerialization serialization = module.findService(ValueSerialization.class)
                .get();

        System.out.println(serialization);

        if (true) {
            TestValue value;
            {
                ValueBuilder<TestValue> builder = module.newValueBuilder(TestValue.class);
                builder.prototype().price().set(23.45);
                builder.prototype().testenum().set(TestEnum.B);
                value = builder.newInstance();
                _sLogger.info("value {}, {}, {}", value, value.price().get(), value.testenum().get());

                if (true) {
                    System.out.println(serialization.serialize(value));
                    value = serialization.deserialize(TestValue.class, serialization.serialize(value));
                    _sLogger.info("de value {}, {}, {}", value, value.price().get(), value.testenum().get());
                }
            }

//        if (true)
//            return;
            TestEntity entity;
            String entityId = "abcdefg";
            {
                UnitOfWork uow = module.newUnitOfWork(UsecaseBuilder.newUsecase("create"));
                EntityBuilder<TestEntity> builder = uow.newEntityBuilder(TestEntity.class, entityId);
                builder.instance().testvalue().set(value);
                builder.instance().testenum().set(TestEnum.A);
                entity = builder.newInstance();
                try {
                    uow.complete();
                } catch (Exception e_) {
                    uow.discard();
                    e_.printStackTrace();
                }
            }
//        if (true)
//            return;
            {
                UnitOfWork uow = module.newUnitOfWork(UsecaseBuilder.newUsecase("create"));
                entity = uow.get(TestEntity.class, entityId);
                System.out.println(entity.testvalue().get());
                System.out.println(entity.testenum().get());
                uow.discard();
            }
        }
    }
}