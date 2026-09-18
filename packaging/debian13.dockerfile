FROM debian:13

RUN apt-get -q update && apt-get -q upgrade -y
RUN apt-get -q install -y wget git gpg

RUN wget https://download.bell-sw.com/java/25.0.4+9/bellsoft-jdk25.0.4+9-linux-amd64-full.deb
RUN apt-get -q install -y ./bellsoft-jdk25.0.4+9-linux-amd64-full.deb
RUN apt-get -q install -y binutils fakeroot

COPY packaging/build.sh /build.sh
RUN chmod +x /build.sh
WORKDIR /app

COPY . /app

CMD ["/build.sh"]
