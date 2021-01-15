package com.serveratcloud.api.controller;

import com.serveratcloud.api.model.Server;
import com.serveratcloud.api.service.ServerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
public class ServerController {

    private final ServerService serverService;

    @Autowired
    public ServerController(
            ServerService serverService) {

        this.serverService = serverService;
    }

    @GetMapping("/server")
    public List<Server> getServers(
            @RequestHeader String awsAccessKey,
            @RequestHeader String awsSecretKey,
            String awsRegion) {

        log.debug("awsAccessKey is: {}", awsAccessKey);
        log.debug("awsSecretKey is: {}", awsSecretKey);
        log.debug("awsRegion is: {}", awsRegion);
        return serverService.retrieveServers(awsAccessKey, awsSecretKey, awsRegion);
    }

    @GetMapping("/status")
    public Server getServer(
            @RequestHeader String awsAccessKey,
            @RequestHeader String awsSecretKey,
            String awsRegion,
            String serverId,
            String serverType) {

        return serverService.retrieveServer(
                awsAccessKey, awsSecretKey, awsRegion,
                serverId, serverType);
    }

    @PostMapping("/status")
    public Server postServer(
            @RequestHeader String awsAccessKey,
            @RequestHeader String awsSecretKey,
            String awsRegion,
            String serverId,
            String serverType,
            String serverStatus) {

        return serverService.updateServerPowerStatus(awsAccessKey, awsSecretKey, awsRegion, serverId, serverType, serverStatus);
    }
}
