package com.serveratcloud.api.service;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2ClientBuilder;
import com.amazonaws.services.ec2.model.DescribeInstancesRequest;
import com.amazonaws.services.ec2.model.DescribeInstancesResult;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.InstanceStateChange;
import com.amazonaws.services.ec2.model.Reservation;
import com.amazonaws.services.ec2.model.StartInstancesRequest;
import com.amazonaws.services.ec2.model.StartInstancesResult;
import com.amazonaws.services.ec2.model.StopInstancesRequest;
import com.amazonaws.services.ec2.model.StopInstancesResult;
import com.serveratcloud.api.model.Server;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Slf4j
@Service
public class Ec2ServiceImpl implements Ec2Service {

    @Override
    public List<Server> getInstances(AWSCredentials credentials, String region) {

        AmazonEC2 ec2 = AmazonEC2ClientBuilder
                .standard()
                .withCredentials(
                        new AWSStaticCredentialsProvider(credentials))
                .withRegion(Regions.fromName(region))
                .build();

        List<Server> servers = new ArrayList<>(25);
        boolean keepGoing = true;
        DescribeInstancesRequest request = new DescribeInstancesRequest();

        while (keepGoing) {

            DescribeInstancesResult result = ec2.describeInstances(request);

            for (Reservation reservation : result.getReservations()) {

                for (Instance instance : reservation.getInstances()) {

                    log.debug("Ec2 status is: {}", instance.getState().getName());
                    servers.add(Server
                            .builder()
                            .id(instance.getInstanceId())
                            .name(setServerName(instance))
                            .type("ec2")
                            .status(setServerStatus(instance.getState().getName()))
                            .build());
                }
            }
            request.setNextToken(result.getNextToken());
            if (result.getNextToken() == null) {

                keepGoing = false;
            }
        }

        return servers;
    }

    @Override
    public Server getInstance(
            AWSCredentials credentials, String region, String serverId) {

        AmazonEC2 ec2 = AmazonEC2ClientBuilder
                .standard()
                .withCredentials(
                        new AWSStaticCredentialsProvider(credentials))
                .withRegion(Regions.fromName(region))
                .build();

        Server server = null;
        DescribeInstancesRequest request = new DescribeInstancesRequest();
        request.setInstanceIds(Collections.singletonList(serverId));
        DescribeInstancesResult result = ec2.describeInstances(request);
        for (Reservation reservation : result.getReservations()) {

            for (Instance instance : reservation.getInstances()) {

                server = Server
                        .builder()
                        .id(instance.getInstanceId())
                        .name(setServerName(instance))
                        .type("ec2")
                        .status(setServerStatus(instance.getState().getName()))
                        .build();
            }
        }
        return server;
    }

    private String setServerName(Instance instance) {
        String keyValue = instance.getTags()
                .stream()
                .filter(t -> t.getKey().equals("Name"))
                .findFirst()
                .get()// There's always a Name tag even when the user hasn't set one
                .getValue();

        if (keyValue.isBlank())
            return instance.getInstanceId();
        else
            return keyValue;
    }

    private String setServerStatus(String statusName) {
        switch (statusName) {
            case "running":
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

        AmazonEC2 ec2 = AmazonEC2ClientBuilder
                .standard()
                .withCredentials(
                        new AWSStaticCredentialsProvider(credentials))
                .withRegion(Regions.fromName(region))
                .build();

        Server server = Server
                .builder()
                .id(id)
                .type("ec2")
                .build();
        switch (serverStatus) {

            case "on":
                StartInstancesRequest startRequest = new StartInstancesRequest(Collections.singletonList(id));
                StartInstancesResult startResult = ec2.startInstances(startRequest);

                for (InstanceStateChange instance : startResult.getStartingInstances()) {

                    server.setStatus(setServerStatus(instance.getCurrentState().getName()));
                }
                break;

            case "off":
                StopInstancesRequest stopRequest = new StopInstancesRequest(Collections.singletonList(id));
                StopInstancesResult stopResult = ec2.stopInstances(stopRequest);

                for (InstanceStateChange instance : stopResult.getStoppingInstances()) {

                    server.setStatus(setServerStatus(instance.getCurrentState().getName()));
                }
                break;

            default:
                log.debug("Invalid serverStatus: {}", serverStatus);
        }
        return server;
    }
}
