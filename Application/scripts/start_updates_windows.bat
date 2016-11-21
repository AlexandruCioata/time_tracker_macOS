echo off

if "%1" == "" (
    java -jar UpdatesManager.jar updatesConfig.properties
) else (
    java -jar %1\UpdatesManager.jar %2
)
