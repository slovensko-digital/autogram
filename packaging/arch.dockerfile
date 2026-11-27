FROM archlinux:latest

# JavaFX in the tests needs fonts, the JDK from the archive brings no system packages with it
RUN pacman -Syu --noconfirm --needed base-devel git wget fontconfig freetype2 ttf-dejavu
RUN wget https://download.bell-sw.com/java/25.0.4+9/bellsoft-jdk25.0.4+9-linux-amd64-full.tar.gz \
    && mkdir -p /usr/lib/jvm \
    && tar -xzf bellsoft-jdk25.0.4+9-linux-amd64-full.tar.gz -C /usr/lib/jvm \
    && mv /usr/lib/jvm/jdk-25.0.4-full /usr/lib/jvm/bellsoft-java25-full-amd64 \
    && rm bellsoft-jdk25.0.4+9-linux-amd64-full.tar.gz

COPY packaging/build.sh /build.sh
RUN chmod +x /build.sh
WORKDIR /app

COPY . /app

CMD ["/build.sh"]
