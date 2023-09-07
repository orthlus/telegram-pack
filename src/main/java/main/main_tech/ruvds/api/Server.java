package main.main_tech.ruvds.api;

public record Server(
        String name,
        long id,
        int cpu,
        double ramGb,
        int driveGb,
        int driveTariffId,
        Integer additionalDriveGb,
        Integer additionalDriveTariffId,
        ServerStatus status,
        int createProgress,
        int tariffId,
        int paymentPeriod,
        Integer osId,
        int ip
) {
}
