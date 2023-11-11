package main.regru;

import feign.RequestLine;
import main.regru.common.Add;
import main.regru.common.Basic;
import main.regru.common.Delete;
import main.regru.common.dto.AddAndDeleteDomainResponse;
import main.regru.common.dto.DomainsList;

public interface RegRuHttp {
	@RequestLine("POST /zone/get_resource_records")
	DomainsList subdomains(Basic params);

	@RequestLine("POST /zone/remove_record")
	AddAndDeleteDomainResponse deleteSubdomain(Delete params);

	@RequestLine("POST /zone/add_alias")
	AddAndDeleteDomainResponse addSubdomain(Add params);
}
