#!/bin/bash

# uncomment to start from clean db
python cleandb.py

# print db state
#python printdb.py

# start the server
#until 
python server.py
#; do
#echo "Server 'mserver' crashed with exit code $?.  Respawning.." >&2
#    sleep 1
#done
