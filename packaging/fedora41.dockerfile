FROM fedora:41

RUN dnf -q install -y rpm-build git wget
RUN wget https://download.bell-sw.com/java/24.0.1+11/bellsoft-jdk24.0.1+11-linux-amd64-full.rpm
RUN sudo yum install -y ./bellsoft-jdk24.0.1+11-linux-amd64-full.rpm

COPY packaging/build.sh /build.sh
RUN chmod +x /build.sh
WORKDIR /app

COPY . /app

CMD ["/build.sh"]
