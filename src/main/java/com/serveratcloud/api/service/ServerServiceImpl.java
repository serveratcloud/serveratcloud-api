package com.serveratcloud.api.service;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.serveratcloud.api.model.Server;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class ServerServiceImpl implements ServerService {

    private final Ec2Service ec2Service;
    private final RdsService rdsService;

    @Autowired
    public ServerServiceImpl(
            Ec2Service ec2Service,
            RdsService rdsService) {

        this.ec2Service = ec2Service;
        this.rdsService = rdsService;
    }

    @Override
    public List<Server> retrieveServers(
            String awsAccessKey, String awsSecretKey, String awsRegion) {

        AWSCredentials awsCredentials = new BasicAWSCredentials(awsAccessKey, awsSecretKey);

        List<Server> servers = new ArrayList<>();
        servers.addAll(ec2Service.getInstances(awsCredentials, awsRegion));
        servers.addAll(rdsService.getInstances(awsCredentials, awsRegion));

        return servers;
    }

    @Override
    public Server retrieveServer(
            String awsAccessKey, String awsSecretKey, String awsRegion,
            String serverId, String serverType) {

        AWSCredentials awsCredentials = new BasicAWSCredentials(awsAccessKey, awsSecretKey);
        Server server;
        switch (serverType) {
            case "ec2":
                server = ec2Service.getInstance(awsCredentials, awsRegion, serverId);
                break;
            case "db":
                server = rdsService.getInstance(awsCredentials, awsRegion, serverId);
                break;
            default:
                log.debug("Invalid serverType: {}", serverType);
                server = null;
        }
        return server;
    }

    @Override
    public Server updateServerPowerStatus(
            String awsAccessKey, String awsSecretKey, String awsRegion,
            String serverId, String serverType, String serverStatus) {

        AWSCredentials awsCredentials = new BasicAWSCredentials(awsAccessKey, awsSecretKey);

        Server server = Server.builder().build();
        switch (serverType) {
            case "ec2":
                server = ec2Service.updateInstancePowerStatus(
                        awsCredentials, awsRegion, serverId, serverStatus);
                break;
            case "db":
                server = rdsService.updateInstancePowerStatus(
                        awsCredentials, awsRegion, serverId, serverStatus);
                break;
            default:
                log.debug("Invalid serverType: {}\n", serverType);
        }
        return server;
    }
}
