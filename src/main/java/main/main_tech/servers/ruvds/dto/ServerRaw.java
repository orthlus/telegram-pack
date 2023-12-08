package main.main_tech.servers.ruvds.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class ServerRaw {
	@JsonProperty("virtual_server_id")
	private String virtualServerId;
	@JsonProperty("status")
	private String status;
	@JsonProperty("create_progress")
	private String createProgress;
	@JsonProperty("tariff_id")
	private String tariffId;
	@JsonProperty("payment_period")
	private String paymentPeriod;
	@JsonProperty("os_id")
	private String osId;
	@JsonProperty("template_id")
	private String templateId;
	@JsonProperty("cpu")
	private String cpu;
	@JsonProperty("ram")
	private String ram;
	@JsonProperty("vram")
	private String vram;
	@JsonProperty("drive")
	private String drive;
	@JsonProperty("drive_tariff_id")
	private String driveTariffId;
	@JsonProperty("additional_drive")
	private String additionalDrive;
	@JsonProperty("additional_drive_tariff_id")
	private String additionalDriveTariffId;
	@JsonProperty("ip")
	private String ip;
	@JsonProperty("ddos_protection")
	private String ddosProtection;
	@JsonProperty("user_comment")
	private String userComment;

	@Override
	public String toString() {
		return ("Server{virtualServerId='%s', status='%s', createProgress='%s', tariffId='%s', paymentPeriod='%s', " +
				"osId='%s', templateId='%s', cpu='%s', ram='%s', vram='%s', drive='%s', driveTariffId='%s', " +
				"additionalDrive='%s', additionalDriveTariffId='%s', ip='%s', ddosProtection='%s', userComment='%s'}")
				.formatted(virtualServerId, status, createProgress, tariffId, paymentPeriod, osId, templateId,
						cpu, ram, vram, drive, driveTariffId, additionalDrive, additionalDriveTariffId, ip,
						ddosProtection, userComment);
	}
}
