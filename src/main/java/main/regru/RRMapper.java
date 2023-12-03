package main.regru;

import main.regru.common.Add;
import main.regru.common.Basic;
import main.regru.common.Delete;
import main.regru.common.RR;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface RRMapper {
	@Mapping(target = "subdomain", source = "rr.domain")
	@Mapping(target = "ipaddr", source = "rr.ip")
	Add add(RR rr, Basic basic);

	@Mapping(target = "subdomain", source = "rr.domain")
	@Mapping(target = "record_type", constant = "A")
	@Mapping(target = "content", source = "rr.ip")
	Delete delete(RR rr, Basic basic);
}
