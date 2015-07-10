#!/usr/bin/python

from BaseHTTPServer import BaseHTTPRequestHandler,HTTPServer
from os import curdir, sep

import SocketServer
import os
import cgi
import base64
import datetime
import re
import uuid
import urllib

PORT_NUMBER = 8080

import dbapi as dbm

#This class will handles any incoming request from the browser 
class myHandler(BaseHTTPRequestHandler):	

	def gen_uid(self):
		return uuid.uuid4()

	def parse_params(self, path):
		pDict = {}
		pathd = urllib.unquote(path).decode('utf8')
		epa = pathd.split('?')
		if len(epa) != 2:
			print "Malformed URL"
		else:
			eepa = epa[1].split('&')
		for e in eepa:
			kv = e.split('=')
			if len(kv) != 2:
				print("malformed params -- 2")
			else:
				pDict[kv[0]] = kv[1]
		#print e;
		# check params and add dummy for everything else
		if 'phnu' not in pDict.keys():
			pDict['phnu'] = '12345678'
		elif pDict['phnu'] == 'null':
			pDict['phnu'] = '12345678'
		else:
			print "Phone number OK! "

		if 'toc' not in pDict.keys():
			pDict['toc'] = str(datetime.datetime.now())
		elif pDict['toc'] =='null':
			pDict['toc'] = str(datetime.datetime.now())
		else:
			print "ToC ok !"
		
		if 'doc' not in pDict.keys():
			pDict['doc'] = 0
		elif pDict['doc'] == 'null':
			pDict['doc'] = 0
		else:
			print "DoC ok"

		print pDict
		return pDict;
	
	#suppress default logging
	#def log_message(self, format, *args):
        #	return
	#Handler for the GET requests
	def do_GET(self):
		sendReply = False
		isHtmlReq = False
		isIndexReq = False
		resp = ''
		
		if self.path.startswith("/report?"):
			mimetype='text/html'
			sendReply = True
		        isRepReq = True
			resp = str(self.gen_uid())
			
		if self.path.startswith("/peek"):
			mimetype='text/html'
			sendReply=1
			ishtmlReq = 1;

		if sendReply == True:
			print self.path
			params = self.parse_params(self.path)
			print "Phone number:", params['phnu']
			print "ToC:" , params['toc']
			print "DoC:" , params['doc']
			print "Uid:", resp
			#FIXME:Add to databse here 
			dbm.add_report(params['phnu'],params['toc'],params['doc'],resp)

			# send response to client
			if resp is not '':
				self.send_response(200)
				self.send_header('Content-type',mimetype)
				self.send_header('Content-length',len(resp))
				self.end_headers()
				self.wfile.write(resp)
		else:
                    self.send_error(404,'SpamTell: Unknown request %s' % self.path)
		return

	#Handler for the POST requests
	def do_POST(self):
		print "Post request ... "
		#print self.headers
		form = cgi.FieldStorage(
			fp=self.rfile, 
			headers=self.headers,
			environ={'REQUEST_METHOD':'POST',
		        'CONTENT_TYPE':self.headers['Content-Type'],
			},keep_blank_values=1)
		print "Reading post ... "
		saveFile = True

		pathd = urllib.unquote(self.path).decode('utf8')
		recid = pathd.split('?')[1].split('=')[1]
		#print recid
		# collect from client
		try:
			#recid = form['recid'].file.read()
			recording = form['recording'].file.read()
		except:
			#print "Some issue with post"
			saveFile = False
			pass

		# send response to client
		self.send_response(200)
                self.send_header('Content-Type','text/plain')
                self.send_header('Content-Length',len(recid))
                self.end_headers()
		self.wfile.write(recid)

		if recid != '' or dbm.check_report(recid) == '':
			saveFile = False

		if True: 
			print "Writing recording to repository ..."
			recname = "./recordings/" + recid
			fdim = open(recname,'wb')
			fdim.write(base64.b64decode(recording));
			fdim.close();
		else:
			print "Success"
			print "saved recording with id ", recid
		print 'spamTell Post done'
		return			
			
class ThreadedHTTPServer(SocketServer.ThreadingMixIn, HTTPServer):
    """Handle requests in a separate thread."""

if __name__ == '__main__':
        server = ThreadedHTTPServer(('', 8080), myHandler)
        print 'Starting server on port 8080 , use <Ctrl-C> to stop'
        server.serve_forever()		
