FROM ubuntu:22.04

RUN apt-get -q update && apt-get -q upgrade -y
RUN apt-get -q install -y wget git gpg

RUN wget https://download.bell-sw.com/java/21.0.7+9/bellsoft-jdk21.0.7+9-linux-amd64-full.deb
RUN apt-get -q install -y ./bellsoft-jdk21.0.7+9-linux-amd64-full.deb
RUN apt-get -q install -y binutils fakeroot

COPY packaging/build.sh /build.sh
RUN chmod +x /build.sh
WORKDIR /app

COPY . /app

CMD ["/build.sh"]
