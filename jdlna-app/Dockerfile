FROM openjdk:15-slim-buster
# Environment variables
ENV LOG_DIR "/var/log/jdlna"
ENV INSTALL_DIR "/data/jdlna"
ENV DATABASE_DRIVER "org.h2.Driver"
ENV DATABASE_URL "jdbc:h2:/data/jdlna/content;OPTIMIZE_REUSE_RESULTS=0;MAX_OPERATION_MEMORY=1000;MAX_MEMORY_UNDO=1000"
ENV FOLDER_VIDEO "/media/videos"
ENV FOLDER_MUSIC "/media/musics"
ENV FOLDER_PHOTO "/media/photos"
ENV FOLDER_COVER "/media/covers"
# Create directories
RUN mkdir -p /data && \
    mkdir -p /data/jdlna && \
    mkdir -p /var/log/jdlna && \
    mkdir -p /media/videos /media/musics /media/photos /media/covers
# Install unzip
RUN apt-get update && \
    apt-get install -y --no-install-recommends unzip
# Extract zip to /data
COPY jdlna-app.zip /tmp/jdlna.zip
RUN unzip -d /data /tmp/jdlna.zip
# Create user
RUN addgroup jdlna && \
    adduser jdlna --disabled-password --shell /bin/bash --ingroup jdlna
RUN chown -R jdlna:jdlna /data && \
    chown -R jdlna:jdlna /var/log/jdlna && \
    chown -R jdlna:jdlna /media
# Clean
RUN rm -f /tmp/jdlna.zip && \
    apt-get purge -y unzip && \
    apt-get clean
USER jdlna
CMD ["bash", "/data/jdlna-app/bin/jdlna-app"]