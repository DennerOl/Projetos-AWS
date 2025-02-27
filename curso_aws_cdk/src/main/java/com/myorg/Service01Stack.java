package com.myorg;

import java.util.HashMap;
import java.util.Map;

import software.amazon.awscdk.Fn;
import software.amazon.awscdk.RemovalPolicy;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;
import software.amazon.awscdk.services.ecs.AwsLogDriverProps;
import software.amazon.awscdk.services.applicationautoscaling.EnableScalingProps;
import software.amazon.awscdk.services.ecs.CpuUtilizationScalingProps;
import software.amazon.awscdk.Duration;
// import software.amazon.awscdk.Duration;
// import software.amazon.awscdk.services.sqs.Queue;
import software.amazon.awscdk.services.ecs.Cluster;
import software.amazon.awscdk.services.ecs.ContainerImage;
import software.amazon.awscdk.services.ecs.LogDriver;
import software.amazon.awscdk.services.ecs.MemoryUtilizationScalingProps;
import software.amazon.awscdk.services.ecs.ScalableTaskCount;
import software.amazon.awscdk.services.ecs.patterns.ApplicationLoadBalancedFargateService;
import software.amazon.awscdk.services.ecs.patterns.ApplicationLoadBalancedTaskImageOptions;
import software.amazon.awscdk.services.elasticloadbalancingv2.HealthCheck;
import software.amazon.awscdk.services.logs.LogGroup;
import software.constructs.Construct;

// loadBalancer
public class Service01Stack extends Stack {
    public Service01Stack(final Construct scope, final String id, final Cluster cluster) {
        this(scope, id, null, cluster);
    }

    public Service01Stack(final Construct scope, final String id, final StackProps props, final Cluster cluster) {
        super(scope, id, props);

        // configurações de criação e autenticação do banco
        Map<String, String> autenticacao = new HashMap<>();
        autenticacao.put("SPRING_DATASOURCE_URL", "jdbc:mysql://" +
                Fn.importValue("pedidos-db-endpoint") +
                ":3307/food-pedidos?createDatabaseIfNotExist=true");
        autenticacao.put("SPRING_DATASOURCE_USERNAME", "admin");
        autenticacao.put("SPRING_DATASOURCE_PASSWORD",
                Fn.importValue("pedidos-db-senha"));

        // balanceador de carga
        ApplicationLoadBalancedFargateService service01 = ApplicationLoadBalancedFargateService.Builder
                .create(this, "ALB01")

                .serviceName("service01")
                .cluster(cluster) // Required
                .cpu(512) // Default is 256
                .memoryLimitMiB(1024)
                .desiredCount(1) // numeros de instancias que vao ser iniciadas
                .listenerPort(8080)
                .assignPublicIp(true)
                .taskImageOptions(
                        // configuração da imagem da aplicação
                        ApplicationLoadBalancedTaskImageOptions.builder()
                                .containerName("aws_project01")
                                // pegando a imagem do dockerhub
                                .image(ContainerImage.fromRegistry(
                                        "dennerol/curso_aws_project01"))

                                /*
                                 * aqui uso a aws ECR para fazer o repositorio e pegar a
                                 * imagem
                                 * 
                                 * .image(ContainerImage.fromEcrRepository(iRepository))
                                 */
                                .containerPort(8080)
                                .environment(autenticacao)
                                // codigo que agrupa os logs de todas instancias
                                .logDriver(LogDriver.awsLogs(AwsLogDriverProps.builder()
                                        .logGroup(LogGroup.Builder.create(this,
                                                "Service01LogGroup")
                                                .logGroupName("Service01")
                                                .removalPolicy(RemovalPolicy.DESTROY)
                                                .build())
                                        .streamPrefix("Service01")
                                        .build()))
                                .build())

                .publicLoadBalancer(true) // Default is false
                .build();

        // verifico com actuator se minha instancia está ativa, adicionar dp
        service01.getTargetGroup().configureHealthCheck(new HealthCheck.Builder()
                .path("/actuator/health")
                .port("8080")
                .healthyHttpCodes("200")
                .build());

        // codigo para scalar a aplicação Não precisamos ter três instâncias ativas se
        // não houver necessidade que isso aconteça.

        ScalableTaskCount scalableTaskCount = service01.getService()
                .autoScaleTaskCount(EnableScalingProps.builder()
                        .minCapacity(1)
                        .maxCapacity(3)
                        .build());

        scalableTaskCount.scaleOnCpuUtilization("Service01CpuScaling", CpuUtilizationScalingProps.builder()
                .targetUtilizationPercent(70)
                .scaleInCooldown(Duration.minutes(3))
                .scaleOutCooldown(Duration.minutes(2))
                .build());

        scalableTaskCount.scaleOnMemoryUtilization("Service01MemoryScaling", MemoryUtilizationScalingProps.builder()
                .targetUtilizationPercent(65)
                .scaleInCooldown(Duration.minutes(3))
                .scaleOutCooldown(Duration.minutes(2))
                .build());

    }

    /*
     * LoadBalancer é um serviço Fargate e distribui nossas requisiçoes da api
     * quando eu subir tudo isso para Aws ela irá retornar um ip publico para
     * essa API que adiconei a imagem
     */
}