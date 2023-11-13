package main.regru;

import feign.Headers;
import feign.RequestLine;
import main.regru.common.Add;
import main.regru.common.Basic;
import main.regru.common.Delete;
import main.regru.common.dto.AddAndDeleteDomainResponse;
import main.regru.common.dto.DomainsList;

public interface RegRuHttp {
	@RequestLine("POST /zone/get_resource_records")
	@Headers("content-type: application/x-www-form-urlencoded")
	DomainsList subdomains(Basic params);

	@RequestLine("POST /zone/remove_record")
	@Headers("content-type: application/x-www-form-urlencoded")
	AddAndDeleteDomainResponse deleteSubdomain(Delete params);

	@RequestLine("POST /zone/add_alias")
	@Headers("content-type: application/x-www-form-urlencoded")
	AddAndDeleteDomainResponse addSubdomain(Add params);
}
