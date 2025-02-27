package com.myorg;

import software.amazon.awscdk.App;

public class CursoAwsCdkApp {
        public static void main(final String[] args) {
                App app = new App();

                // crio uma instacia da VPC
                VpcStack vpcStack = new VpcStack(app, "Vpc");

                // chamando o cluster
                ClusterStack clusterStack = new ClusterStack(app, "Cluster", vpcStack.getVpc());
                clusterStack.addDependency(vpcStack);

                // chamando o loadBalancer
                Service01Stack service01Stack = new Service01Stack(app, "Service01", clusterStack.getCluster());
                service01Stack.addDependency(clusterStack);

                app.synth();
        }
}
