#!/usr/bin/env bash

export readonly COUNTRIES=("DE" "AT" "BE" "CZ" "DK" "FR" "LU" "NL" "PL" "CH")

main () {
  for _country in "${COUNTRIES[@]}"; do
    local _query="[out:json];area[\"ISO3166-1\"=\"${_country}\"][boundary=administrative];node[\"amenity\"=\"fuel\"](area);(._;>;);out body;"
    echo "Executing query:"
    echo -e "${_query}"
    curl https://overpass-api.de/api/interpreter --data-urlencode "data=${_query}" > "app/src/main/resources/json/${_country}.json" 2> /dev//null
    echo -e "waiting some seconds to not get banned :)"
    sleep 20
  done
}

main
