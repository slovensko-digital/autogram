FROM fedora:41

RUN dnf -q install -y rpm-build git wget
RUN wget https://download.bell-sw.com/java/21.0.8+12/bellsoft-jdk21.0.8+12-linux-amd64-full.rpm
RUN sudo yum install -y ./bellsoft-jdk21.0.8+12-linux-amd64-full.rpm

COPY packaging/build.sh /build.sh
RUN chmod +x /build.sh
WORKDIR /app

COPY . /app

CMD ["/build.sh"]
