# The gateway image: nginx with the frontend files and the routing template built in,
# so it runs anywhere (no files mounted from the developer's machine).
# Built from the repository root (see docker-compose.yml), so both frontend/ and
# gateway/ are visible to the build.
FROM nginx:1.27

# The frontend nginx serves
COPY frontend/ /usr/share/nginx/html/

# nginx fills in ${PORT}, ${USER_SERVICE_HOST} and ${DECK_SERVICE_HOST} from the
# environment at startup (the official image does this for /etc/nginx/templates/*)
COPY gateway/default.conf.template /etc/nginx/templates/default.conf.template
