package com.serveratcloud.api.service;

import com.serveratcloud.api.model.Server;

import java.util.List;

public interface ServerService {

    List<Server> retrieveServers(
            String awsAccessKey, String awsSecretKey, String awsRegion);

    Server retrieveServer(
            String awsAccessKey, String awsSecretKey, String awsRegion,
            String serverId, String serverType);

    Server updateServerPowerStatus(
            String awsAccessKey, String awsSecretKey, String awsRegion,
            String serverId, String serverType, String serverStatus);
}
