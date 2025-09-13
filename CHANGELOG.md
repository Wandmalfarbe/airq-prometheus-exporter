# Changelog

All notable changes to this project are documented in this file. On the [releases page](https://github.com/Wandmalfarbe/airq-prometheus-exporter/releases/) you can see all released versions of the *air-Q Prometheus Exporter* and download the [latest version](https://github.com/Wandmalfarbe/airq-prometheus-exporter/releases/latest).

## [2.0.0] - 2025-09-13

- feat: major internal refactoring
- feat: add web interface for debugging and a quick overview. The web interface has light and dark modes and can be used on large monitors, smartphones, and tablets.
- feat: add support for the metrics `mold` *Mold-free Index* and `virus` *Virus-free Index*
- feat: the metrics `dCO2dt` *Change of CO₂ concentration* and `dHdt` *Change of Humidity* now report a unit.
- feat: add a new command line option `--export-other-metrics` or `-o` that allows to export additional (non-air-Q) metrics e.g., metrics about the prometheus exporter itself.
- feat: renamed command line and environment options to minimize confusion
  - `--host` (`AIRQ_PROM_EXP_HOST`) is now `--airq-host` (`AIRQ_PROM_EXP_AIRQ_HOST`)
  - `--password` (`AIRQ_PROM_EXP_PASSWORD`) is now `--airq-password` (`AIRQ_PROM_EXP_AIRQ_PASSWORD`)
- feat: add error page that shows detailed information about unexpected exceptions
- feat: add additional labels `serialNumber`, `type`, `deviceName`, `roomType`, `airQHardwareVersion`, `airQSoftwareVersion` to the air-Q metrics. This allows filtering, e.g., by room type or air-Q name. 
- feat: new metrics or changed labels, such as the air-Q name, are now picked up without a restart
- feat: add project icon
- chore: update dependencies
- chore: update docker container to Java 24

## [1.0.0] - 2024-02-24

- feat: Initial release of version 1.0.0 as a JAR file.

[2.0.0]: https://github.com/Wandmalfarbe/pandoc-latex-template/compare/v1.0.0...2.0.0
[1.0.0]: https://github.com/Wandmalfarbe/pandoc-latex-template/releases/tag/v1.0.0
