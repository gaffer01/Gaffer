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

package uk.gov.gchq.gaffer.flink.operation.handler;

import kafka.server.KafkaConfig;
import kafka.server.KafkaServer;
import kafka.utils.MockTime;
import kafka.utils.TestUtils;
import org.apache.curator.test.TestingServer;
import org.apache.flink.testutils.junit.RetryOnFailure;
import org.apache.flink.testutils.junit.RetryRule;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import uk.gov.gchq.gaffer.commonutil.CommonTestConstants;
import uk.gov.gchq.gaffer.flink.operation.AddElementsFromKafka;
import uk.gov.gchq.gaffer.graph.Graph;
import uk.gov.gchq.gaffer.operation.OperationException;
import uk.gov.gchq.gaffer.user.User;
import java.io.File;
import java.io.IOException;
import java.util.Properties;
import java.util.UUID;

public class AddElementsFromKafkaTest extends FlinkTest {
    private static final String TOPIC = UUID.randomUUID().toString();
    private static final String BOOTSTRAP_SERVERS = "localhost:9092";

    @Rule
    public final TemporaryFolder testFolder = new TemporaryFolder(CommonTestConstants.TMP_DIRECTORY);
    @Rule
    public final RetryRule rule = new RetryRule();

    private KafkaProducer<Integer, String> producer;
    private KafkaServer kafkaServer;
    private TestingServer zkServer;

    @Before
    public void before() throws Exception {
        // Create zookeeper server
        zkServer = new TestingServer(-1, createZookeeperTmpDir());
        zkServer.start();

        // Create kafka server
        kafkaServer = TestUtils.createServer(new KafkaConfig(serverProperties()), new MockTime());

        // Create kafka producer
        producer = new KafkaProducer<>(producerProps());
        producer.send(new ProducerRecord<>(TOPIC, "1")).get();
        producer.send(new ProducerRecord<>(TOPIC, "2")).get();
        producer.send(new ProducerRecord<>(TOPIC, "3")).get();
        producer.flush();
        producer.close();
    }

    @After
    public void cleanUp() throws IOException {
        if (null != producer) {
            producer.close();
        }

        if (null != kafkaServer) {
            kafkaServer.shutdown();
        }

        if (null != zkServer) {
            zkServer.close();
        }
    }

    // For some unknown reason flink fails to connect to this test kafka instance
    // on the first attempt. It does work properly with a real kafka instance.
    @RetryOnFailure(times = 1)
    @Test
    public void shouldAddElementsFromFile() throws Exception {
        // Given
        final Graph graph = createGraph();
        final boolean validate = true;
        final boolean skipInvalid = false;

        final AddElementsFromKafka op = new AddElementsFromKafka.Builder()
                .jobName("test import from kafka")
                .generator(BasicGenerator.class)
                .parallelism(1)
                .validate(validate)
                .skipInvalidElements(skipInvalid)
                .topic(TOPIC)
                .bootstrapServers(BOOTSTRAP_SERVERS)
                .groupId("groupId")
                .build();

        // When
        new Thread(() -> {
            try {
                graph.execute(op, new User());
            } catch (final OperationException e) {
                throw new RuntimeException(e);
            }
        }).start();

        // Wait for the elements to be ingested.
        Thread.sleep(5000);

        // Then
        verifyElements(graph);
    }

    private File createZookeeperTmpDir() throws IOException {
        testFolder.delete();
        testFolder.create();
        return testFolder.newFolder("zkTmpDir");
    }

    private Properties producerProps() {
        Properties props = new Properties();
        props.put("bootstrap.servers", BOOTSTRAP_SERVERS);
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("request.required.acks", "1");
        return props;
    }

    private Properties serverProperties() {
        Properties props = new Properties();
        props.put("zookeeper.connect", zkServer.getConnectString());
        props.put("broker.id", "0");
        props.setProperty("listeners", "PLAINTEXT://" + BOOTSTRAP_SERVERS);
        return props;
    }
}
