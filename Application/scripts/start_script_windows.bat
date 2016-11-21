echo off

if "%1" == "" (
    java -jar Application.jar appConfig.properties
) else (
    java -jar %1\Application.jar %2
)


