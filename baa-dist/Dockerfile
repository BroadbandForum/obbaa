FROM lhdev/java-8u92-jre-alpine-w-svdigi-cer

#workaround for secure random jdk bug
RUN sed -i 's@securerandom.source=file:/dev/random@securerandom.source=file:/dev/./urandom@g' $JAVA_HOME/lib/security/java.security

#Setup proxy if container is run on behind proxy host
ARG http_proxy
ARG https_proxy

ENV http_proxy $http_proxy
ENV https_proxy $https_proxy

RUN apk add --no-cache bash

RUN mkdir /baa
ARG version=1.0.0
ADD target/baa-dist*.tar.gz /baa

RUN ln -s /baa/baa-dist-${version} /baa/baa-dist

#setup default callhome certficates and keys
RUN cp -r /baa/baa-dist/conf/tls /

ENV ver $version
CMD /baa/baa-dist/bin/start.sh
