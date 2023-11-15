package main.regru.common;

import static org.junit.jupiter.api.Assertions.*;

class RegRuResponseReaderTest {
	String testDomain = "";
	String apiSuccessJson = """
			{
				"answer": {
					"domains": [
						{
							"dname": "%s",
							"result": "success",
							"service_id": "123"
						}
					]
				},
				"charset": "utf-8",
				"messagestore": null,
				"result": "success"
			}""".formatted(testDomain);
	String apiErrorJson = """
			{
				"answer": {
					"domains": [
						{
							"dname": "%s",
							"error_code": "RR_NOT_FOUND",
							"error_params": null,
							"error_text": "123|111|A||111",
							"result": "error",
							"service_id": "123"
						}
					]
				},
				"charset": "utf-8",
				"messagestore": null,
				"result": "success"
			}""".formatted(testDomain);
	String apiIpAccessDeniedJson = """
			{
				"charset": "utf-8",
				"error_code": "ACCESS_DENIED_FROM_IP",
				"error_params": {
					"command_name": "zone/remove_record"
				},
				"error_text": "Access to API from this IP denied",
				"messagestore": null,
				"result": "error"
			}""";


}