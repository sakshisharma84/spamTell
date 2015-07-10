#!/usr/bin/python

import requests
import re
import urllib2
import datetime

url = "http://127.0.0.1:8080/report?phnu=4044263434&toc="+str(datetime.datetime.now()).replace(' ','_')+"&doc=35"

request = urllib2.Request(url)
response = urllib2.urlopen(request)
page = response.read()

print page
