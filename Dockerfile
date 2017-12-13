FROM websphere-liberty:webProfile7
MAINTAINER IBM Java engineering at IBM Cloud
COPY /target/liberty/wlp/usr/servers/defaultServer /config/
#Configure environment
ENV MONGO_USER sapphires
ENV MONGO_PWD saveoryArmory
ENV MONGO_HOST dps-test-cfcmaster.rtp.raleigh.ibm.com
ENV MONGO_PORT 27017
ENV MONGO_DATABASE_NAME saveory-test-db
# Install required features if not present, install APM Data Collector
RUN installUtility install --acceptLicense defaultServer && installUtility install --acceptLicense apmDataCollector-7.4
RUN /opt/ibm/wlp/usr/extension/liberty_dc/bin/config_liberty_dc.sh -silent /opt/ibm/wlp/usr/extension/liberty_dc/bin/silent_config_liberty_dc.txt
# Upgrade to production license if URL to JAR provided
ARG LICENSE_JAR_URL
RUN \ 
  if [ $LICENSE_JAR_URL ]; then \
    wget $LICENSE_JAR_URL -O /tmp/license.jar \
    && java -jar /tmp/license.jar -acceptLicense /opt/ibm \
    && rm /tmp/license.jar; \
  fi
