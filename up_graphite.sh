#!/usr/bin/env bash

docker run --rm \
--name graphite \
-p 8001:80 \
-p 2003-2004:2003-2004 \
-p 2023-2024:2023-2024 \
-p 8125:8125/udp \
-p 8126:8126 \
hopsoft/graphite-statsd