package com.myorg;

import software.amazon.awscdk.App;

public class CursoAwsCdkApp {
        public static void main(final String[] args) {
                App app = new App();

                // crio uma instacia da VPC
                VpcStack vpcStack = new VpcStack(app, "Vpc");

                // criação do cluster
                ClusterStack clusterStack = new ClusterStack(app, "Cluster", vpcStack.getVpc());
                clusterStack.addDependency(vpcStack);

                app.synth();
        }
}
