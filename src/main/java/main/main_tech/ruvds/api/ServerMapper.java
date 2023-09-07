package main.main_tech.ruvds.api;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.Set;

@Mapper
public interface ServerMapper {
    @Mapping(target = "additionalDriveGb", source = "additionalDrive")
    @Mapping(target = "driveGb", source = "drive")
    @Mapping(target = "ramGb", source = "ram")
    @Mapping(target = "id", source = "virtualServerId")
    @Mapping(target = "name", source = "userComment")
    Server map(ServerRaw s);

    ServerMapper Instance = Mappers.getMapper(ServerMapper.class);

    Set<Server> map(Set<ServerRaw> s);

    default Set<Server> map(ServersRaw serversRaw) {
        return Instance.map(serversRaw.getServerRaws());
    }
}
