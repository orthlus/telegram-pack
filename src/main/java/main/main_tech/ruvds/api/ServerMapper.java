package main.main_tech.ruvds.api;

import main.main_tech.inventory.Server;
import main.main_tech.inventory.ServerDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.Set;

@Mapper
public interface ServerMapper {
	@Mapping(target = "domainName", source = "domain")
	Server map(ServerDTO s, String domain);

	@Mapping(target = "additionalDriveGb", source = "additionalDrive")
	@Mapping(target = "driveGb", source = "drive")
	@Mapping(target = "ramGb", source = "ram")
	@Mapping(target = "id", source = "virtualServerId")
	@Mapping(target = "name", source = "userComment")
	RuvdsServer map(ServerRaw s);

	ServerMapper Instance = Mappers.getMapper(ServerMapper.class);

	Set<RuvdsServer> map(Set<ServerRaw> s);

	default Set<RuvdsServer> map(ServersRaw serversRaw) {
		return Instance.map(serversRaw.getServerRaws());
	}
}
