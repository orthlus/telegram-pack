package main.domains;

import main.domains.common.RR;

import java.util.List;

public interface DNSClientOperations {
	String getDomainName();

	String getDNSZoneId();

	boolean addSubdomain(RR rr);

	boolean deleteSubdomain(RR rr);

	List<RR> getSubdomainsList();
}
