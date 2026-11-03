#!/usr/bin/env bash
set -euo pipefail
ROOT="${1:-$PWD/build/route-data}"
mkdir -p "$ROOT"
PBF="$ROOT/mexico-latest.osm.pbf"
CLIP="$ROOT/durango-coahuila.osm.pbf"
if [[ ! -s "$PBF" ]]; then
  curl -L --fail --retry 3 -o "$PBF" https://download.geofabrik.de/north-america/mexico-latest.osm.pbf
fi
osmium extract --overwrite --strategy=complete_ways --bbox=-107.2,22.3,-99.8,29.9 "$PBF" -o "$CLIP"
docker run --rm -v "$ROOT:/data" ghcr.io/valhalla/valhalla:latest sh -lc '
  set -e
  mkdir -p /data/tiles
  valhalla_build_config --mjolnir-tile-dir /data/tiles --mjolnir-tile-extract /data/valhalla_tiles.tar --mjolnir-timezone /data/tiles/timezones.sqlite --mjolnir-admin /data/tiles/admins.sqlite > /data/valhalla.json
  valhalla_build_timezones > /data/tiles/timezones.sqlite
  valhalla_build_admins -c /data/valhalla.json /data/durango-coahuila.osm.pbf
  valhalla_build_tiles -c /data/valhalla.json /data/durango-coahuila.osm.pbf
  valhalla_build_extract -c /data/valhalla.json -v
'
test -s "$ROOT/valhalla_tiles.tar"
echo "Built $ROOT/valhalla_tiles.tar ($(du -h "$ROOT/valhalla_tiles.tar" | cut -f1))"
