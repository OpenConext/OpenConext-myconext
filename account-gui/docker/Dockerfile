FROM ghcr.io/openconext/openconext-basecontainers/apache2:latest
ADD ./build/ /var/www/
RUN rm -rf /etc/apache2/sites-enabled/*.conf
