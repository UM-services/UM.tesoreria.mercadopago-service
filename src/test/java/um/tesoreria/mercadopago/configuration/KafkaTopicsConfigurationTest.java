package um.tesoreria.mercadopago.configuration;

import org.apache.kafka.clients.admin.NewTopic;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class KafkaTopicsConfigurationTest {

    private final KafkaTopicsConfiguration configuration = new KafkaTopicsConfiguration();

    @Test
    void declaraPaymentProcessedComoProductor() {
        NewTopic topic = configuration.paymentProcessedTopic(1, 1);

        assertThat(topic.name()).isEqualTo("payment-processed");
        assertThat(topic.numPartitions()).isEqualTo(1);
        assertThat(topic.replicationFactor()).isEqualTo((short) 1);
    }

    @Test
    void declaraElTopicDeAuditoriaConTresParticionesYRetencionDeSieteDias() {
        NewTopic topic = configuration.auditTopic("tesoreria-audit-evento", 3, 1);

        assertThat(topic.name()).isEqualTo("tesoreria-audit-evento");
        assertThat(topic.numPartitions()).isEqualTo(3);
        assertThat(topic.replicationFactor()).isEqualTo((short) 1);
        assertThat(topic.configs())
                .containsEntry("retention.ms", "604800000")
                .containsEntry("retention.bytes", "1073741824")
                .containsEntry("cleanup.policy", "delete")
                .containsEntry("min.insync.replicas", "1");
    }

    @Test
    void ajustaMinInsyncReplicasAlReplicationFactorDelCluster() {
        assertThat(configuration.auditTopic("tesoreria-audit-evento", 3, 3).configs())
                .containsEntry("min.insync.replicas", "2");
    }
}
