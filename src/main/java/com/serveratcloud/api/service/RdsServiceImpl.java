package com.serveratcloud.api.service;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.rds.AmazonRDS;
import com.amazonaws.services.rds.AmazonRDSClientBuilder;
import com.amazonaws.services.rds.model.DBCluster;
import com.amazonaws.services.rds.model.DBInstance;
import com.amazonaws.services.rds.model.DescribeDBClustersRequest;
import com.amazonaws.services.rds.model.DescribeDBClustersResult;
import com.amazonaws.services.rds.model.DescribeDBInstancesRequest;
import com.amazonaws.services.rds.model.DescribeDBInstancesResult;
import com.amazonaws.services.rds.model.StartDBInstanceRequest;
import com.amazonaws.services.rds.model.StopDBInstanceRequest;
import com.serveratcloud.api.model.Server;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class RdsServiceImpl implements RdsService {

    @Override
    public List<Server> getInstances(AWSCredentials credentials, String region) {

        AmazonRDS rds = AmazonRDSClientBuilder
                .standard()
                .withCredentials(
                        new AWSStaticCredentialsProvider(credentials))
                .withRegion(Regions.fromName(region))
                .build();

        List<Server> servers = new ArrayList<>(10);

        DescribeDBInstancesRequest request = new DescribeDBInstancesRequest();
        DescribeDBInstancesResult result = rds.describeDBInstances(request);
        for (DBInstance instance : result.getDBInstances()) {

            log.debug("Rds instance status is: {}", instance.getDBInstanceStatus());
            servers.add(Server
                    .builder()
                    .id(instance.getDBInstanceIdentifier())
                    .name(instance.getDBInstanceIdentifier())
                    .type("db")
                    .status(setServerStatus(instance.getDBInstanceStatus()))
                    .build());
        }

        DescribeDBClustersRequest clustersRequest = new DescribeDBClustersRequest();
        DescribeDBClustersResult clustersResult = rds.describeDBClusters(clustersRequest);
        for (DBCluster cluster : clustersResult.getDBClusters()) {

            log.debug("Rds cluster status is: {}", cluster.getStatus());
            servers.add(Server
                    .builder()
                    .id(cluster.getDBClusterIdentifier())
                    .name(cluster.getDBClusterIdentifier())
                    .type("db")
                    .status(setServerStatus(cluster.getStatus()))
                    .build());
        }

        return servers;
    }

    @Override
    public Server getInstance(
            AWSCredentials credentials, String region, String serverId) {

        AmazonRDS rds = AmazonRDSClientBuilder
                .standard()
                .withCredentials(
                        new AWSStaticCredentialsProvider(credentials))
                .withRegion(Regions.fromName(region))
                .build();

        Server server = null;

        DescribeDBInstancesRequest instancesRequest = new DescribeDBInstancesRequest();
        instancesRequest.setDBInstanceIdentifier(serverId);
        DescribeDBInstancesResult instancesResult = rds.describeDBInstances(instancesRequest);
        for (DBInstance instance: instancesResult.getDBInstances()) {

            server = Server
                    .builder()
                    .id(serverId)
                    .name(instance.getDBInstanceIdentifier())
                    .type("db")
                    .status(setServerStatus(instance.getDBInstanceStatus()))
                    .build();
        }

        if (server == null) {

            DescribeDBClustersRequest clustersRequest = new DescribeDBClustersRequest();
            clustersRequest.setDBClusterIdentifier(serverId);
            DescribeDBClustersResult clustersResult = rds.describeDBClusters(clustersRequest);
            for (DBCluster cluster: clustersResult.getDBClusters()) {

                server = Server
                        .builder()
                        .id(serverId)
                        .name(cluster.getDBClusterIdentifier())
                        .type("db")
                        .status(setServerStatus(cluster.getStatus()))
                        .build();
            }
        }

        return server;
    }

    private String setServerStatus(String status) {
        switch (status) {
            case "available":
                return "on";
            case "stopped":
                return "off";
            default:
                return "wait";
        }
    }

    @Override
    public Server updateInstancePowerStatus(
            AWSCredentials credentials, String region,
            String id, String serverStatus) {

        AmazonRDS rds = AmazonRDSClientBuilder
                .standard()
                .withCredentials(
                        new AWSStaticCredentialsProvider(credentials))
                .withRegion(Regions.fromName(region))
                .build();

        Server server = Server
                .builder()
                .id(id)
                .type("db")
                .build();

        switch (serverStatus) {

            case "on":
                StartDBInstanceRequest startRequest = new StartDBInstanceRequest();
                startRequest.setDBInstanceIdentifier(id);
                DBInstance startInstance = rds.startDBInstance(startRequest);
                server.setStatus(setServerStatus(startInstance.getDBInstanceStatus()));
                break;

            case "off":
                StopDBInstanceRequest stopRequest = new StopDBInstanceRequest();
                stopRequest.setDBInstanceIdentifier(id);
                DBInstance stopInstance = rds.stopDBInstance(stopRequest);
                server.setStatus(setServerStatus(stopInstance.getDBInstanceStatus()));
                break;

            default:
                log.debug("Invalid serverStatus: {}", serverStatus);
        }

        return server;
    }
}
