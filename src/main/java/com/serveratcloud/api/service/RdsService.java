package com.serveratcloud.api.service;

import com.amazonaws.auth.AWSCredentials;
import com.serveratcloud.api.model.Server;

import java.util.List;

public interface RdsService {

    List<Server> getInstances(AWSCredentials credentials, String region);
    Server getInstance(AWSCredentials credentials, String region, String serverId);

    Server updateInstancePowerStatus(
            AWSCredentials credentials, String region,
            String id, String serverStatus);
}
