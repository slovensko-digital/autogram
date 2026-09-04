FROM fedora:41

RUN dnf -q install -y rpm-build git wget
RUN wget https://download.bell-sw.com/java/25.0.4+9/bellsoft-jdk25.0.4+9-linux-amd64-full.rpm
RUN sudo yum install -y ./bellsoft-jdk25.0.4+9-linux-amd64-full.rpm

COPY packaging/build.sh /build.sh
RUN chmod +x /build.sh
WORKDIR /app

COPY . /app

CMD ["/build.sh"]
