/*
 * Copyright 2017 Crown Copyright
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package uk.gov.gchq.gaffer.store.operation.handler.properties;

import com.google.common.collect.Lists;
import org.junit.Test;
import uk.gov.gchq.gaffer.commonutil.TestGroups;
import uk.gov.gchq.gaffer.data.element.Entity;
import uk.gov.gchq.gaffer.operation.OperationException;
import uk.gov.gchq.gaffer.operation.impl.properties.Product;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class ProductHandlerTest {

    @Test
    public void shouldCalculateProductOfProperties() throws OperationException {
        // Given
        final Entity entity1 = new Entity.Builder().group(TestGroups.ENTITY)
                                                   .property("property", 1)
                                                   .build();
        final Entity entity2 = new Entity.Builder().group(TestGroups.ENTITY)
                                                   .property("property", 2)
                                                   .build();
        final Entity entity3 = new Entity.Builder().group(TestGroups.ENTITY)
                                                   .property("property", 3)
                                                   .build();
        final Entity entity4 = new Entity.Builder().group(TestGroups.ENTITY)
                                                   .property("property", 4)
                                                   .build();

        final List<Entity> input = Lists.newArrayList(entity1, entity2, entity3, entity4);

        final Product product = new Product.Builder().input(input)
                                                     .propertyName("property")
                                                     .build();

        final ProductHandler handler = new ProductHandler();

        // When
        final Long result = handler.doOperation(product, null, null);

        // Then
        assertTrue(result instanceof Long);
        assertEquals(24L, result.longValue());
    }

    @Test
    public void shouldCalculateProductOfPropertiesWithMissingProperty() throws OperationException {
        // Given
        final Entity entity1 = new Entity.Builder().group(TestGroups.ENTITY)
                                                   .property("property", 1)
                                                   .build();
        final Entity entity2 = new Entity.Builder().group(TestGroups.ENTITY)
                                                   .property("property", 2)
                                                   .build();
        final Entity entity3 = new Entity.Builder().group(TestGroups.ENTITY)
                                                   .property("property", 3)
                                                   .build();
        final Entity entity4 = new Entity.Builder().group(TestGroups.ENTITY)
                                                   .build();

        final List<Entity> input = Lists.newArrayList(entity1, entity2, entity3, entity4);

        final Product product = new Product.Builder().input(input)
                                                     .propertyName("property")
                                                     .build();

        final ProductHandler handler = new ProductHandler();

        // When
        final Long result = handler.doOperation(product, null, null);

        // Then
        assertTrue(result instanceof Long);
        assertEquals(6L, result.longValue());
    }

    @Test
    public void shouldReturnNullIfIterableIsEmpty() throws OperationException {
        // Given
        final List<Entity> input = Lists.newArrayList();

        final Product product = new Product.Builder().input(input)
                                                     .build();

        final ProductHandler handler = new ProductHandler();

        // When
        final Long result = handler.doOperation(product, null, null);

        // Then
        assertNull(result);
    }

    @Test
    public void shouldReturnNullIfOperationInputIsNull() throws OperationException {
        // Given
        final Product product = new Product.Builder().build();

        final ProductHandler handler = new ProductHandler();

        // When
        final Long result = handler.doOperation(product, null, null);

        // Then
        assertNull(result);
    }
}
