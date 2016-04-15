package mz.learn.zest.domain;

import org.qi4j.api.property.Property;


/**
 * Created by Mingzhou on 16-4-14.
 */
public interface TestEntity {

    Property<TestEnum> testenum();

    Property<TestValue> testvalue();
}
