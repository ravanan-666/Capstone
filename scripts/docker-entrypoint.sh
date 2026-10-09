#!/bin/sh
set -e

APP_PORT="${PORT:-8080}"
echo "[DJ Mart Container] Starting Tomcat runtime with PORT=${APP_PORT}"

# 1. Update standard 8080 connector to APP_PORT
sed -i "s/port=\"8080\"/port=\"${APP_PORT}\"/g" conf/server.xml

# 2. Configure dual-port listener so Tomcat answers on both 8080 and 10000
# This eliminates any port mismatch between Render router (8080 vs 10000)
if [ "${APP_PORT}" = "8080" ]; then
    sed -i '/<Service name="Catalina">/a \    <Connector port="10000" protocol="HTTP/1.1" connectionTimeout="20000" redirectPort="8443" maxParameterCount="1000" \/>' conf/server.xml
elif [ "${APP_PORT}" = "10000" ]; then
    sed -i '/<Service name="Catalina">/a \    <Connector port="8080" protocol="HTTP/1.1" connectionTimeout="20000" redirectPort="8443" maxParameterCount="1000" \/>' conf/server.xml
else
    sed -i '/<Service name="Catalina">/a \    <Connector port="8080" protocol="HTTP/1.1" connectionTimeout="20000" redirectPort="8443" maxParameterCount="1000" \/>' conf/server.xml
fi

echo "[DJ Mart Container] Multi-port connectors configured successfully. Launching Tomcat..."
exec catalina.sh run
