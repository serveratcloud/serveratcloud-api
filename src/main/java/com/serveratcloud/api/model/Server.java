package com.serveratcloud.api.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Server {

    private String id;
    private String name;
    private String type;
    private String status;
}
