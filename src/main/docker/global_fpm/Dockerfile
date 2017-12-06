FROM debian:jessie
MAINTAINER dt.lbs@mappy.com

RUN apt-get -y update && apt-get -y install --force-yes vim wget git locales

# Set the locale to UTF-8 encoding
RUN sed -i -e 's/# en_US.UTF-8 UTF-8/en_US.UTF-8 UTF-8/' /etc/locale.gen && locale-gen
ENV LANG en_US.UTF-8  
ENV LANGUAGE en_US:en  
ENV LC_ALL en_US.UTF-8

# Download & install java8 binaries
WORKDIR /opt/java8
RUN wget -q --no-check-certificate -c --header "Cookie: oraclelicense=accept-securebackup-cookie" http://download.oracle.com/otn-pub/java/jdk/8u151-b12/e758a0de34e24606bca991d704f6dcbf/jdk-8u151-linux-x64.tar.gz
RUN tar xf jdk-8u151-linux-x64.tar.gz -C /opt/java8 --strip 1 && rm -rf jdk-8u151-linux-x64.tar.gz
RUN ln -s /opt/java8/bin/java /usr/bin/java
RUN java -version

# Download & install maven binaries
WORKDIR /opt/maven
RUN wget -q http://apache.mediamirrors.org/maven/maven-3/3.5.2/binaries/apache-maven-3.5.2-bin.tar.gz
RUN tar xf apache-maven-3.5.2-bin.tar.gz -C /opt/maven --strip 1 && rm -rf apache-maven-3.5.2-bin.tar.gz
RUN ln -s /opt/maven/bin/mvn /usr/bin/mvn
RUN mvn -version

# Get fpm repository
WORKDIR /opt/fpm
RUN GIT_SSL_NO_VERIFY=true git clone https://github.com/Mappy/fpm.git .

# Add third party libs to in the local maven repo
RUN mvn install:install-file -Dfile=./libs/osmonaut-1.0.2.4.jar -DgroupId=net.morbz -DartifactId=osmonaut -Dversion=1.0.2.4 -Dpackaging=jar
RUN mvn clean install

VOLUME /workspace
WORKDIR /workspace

ENTRYPOINT ["java", "-Dcom.sun.management.jmxremote.port=9501", "-Dcom.sun.management.jmxremote.ssl=false", "-Dcom.sun.management.jmxremote.authenticate=false", "-cp", "/opt/fpm/target/*"]
