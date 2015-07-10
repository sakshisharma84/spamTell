#!/usr/bin/python

import sys
import peewee
from peewee import *
import datetime
from pprint import pprint
import MySQLdb

# Uncomment to see raw sql querries
#import logging
#logger = logging.getLogger('peewee')
#logger.setLevel(logging.DEBUG)
#logger.addHandler(logging.StreamHandler())

db = MySQLDatabase('spamTelldb', user="ubuntu",passwd='s@1702279')
 
class BaseDataModel(Model):
    """A base model that will use our MySQL database"""
    class Meta:
	database = db

class dblookup(BaseDataModel):
    phnu = peewee.CharField()
    toc = peewee.DateTimeField()
    roc = peewee.DateTimeField()
    doc = peewee.IntegerField()
    recid = peewee.CharField()

def try_connect():
    try:
	db.connect()
	return True;
    except:
#	print "db connections closed trying to reinitializing ... "
	db = MySQLDatabase('spamTelldb', user="ubuntu",passwd='s@1702279')
	try:
#		print "Retrying to connect ..."
		db.connect()
		return True;
	except:
		print "Error: DB Connect failed"
		return False;

def create_dbtable():
    connect = try_connect();
    if connect:
    	#fails silently if tables exist
    	dblookup.create_table(True)
    	db.close();
    else:
	print "Error: Connection failed in creating tables"
	return -1;

def print_lookup():
    connect = try_connect();
    if connect:
    	print "------ SpamTell DB-----"
    	for entry in dblookup.select():
        	print ("Phone#: %s toc: %s roc: %s doc: %s recid: %s ", entry.phnu,str(entry.toc),str(entry.roc),entry.doc, entry.recid) 
    	print "-------------------------"
    	db.close();
    else:
	print "Error: Connect in print_lookup failed"
	return -2;

def print_db():
    #print "#_#_#_#_#_#_#_#_#_#_#_##_#_#_#_#_#_#_#"
    print_lookup()
    #print "#_#_#_#_#_#_#_#_#_#_#_##_#_#_#_#_#_#_#"

def add_report(mphnu,mtoc,mdoc,mrecid):
    connect = try_connect();
    if connect:
    	lookup_entry = dblookup.create(phnu=mphnu,toc=mtoc,roc=str(datetime.datetime.now()),doc=mdoc,recid=mrecid)
    	lookup_entry.save()
    	db.close();
    else:
	print "Error: Connection failed while adding report"
	return -3;

def del_report(mphnu): 
    connect = try_connect();
    if connect:
    	entry = dblookup.delete().where(dblookup.phnu == mphnu)
    	#print entry
    	entry.execute()
    	db.close();
    	#lookup_entry.delete()
    	#pass
    else:
	print "Error: Connection failed in del_report"
	return -4;

def clean_db():
    connect = try_connect();
    if connect:
    	lookup_delq = dblookup.delete()
    	lookup_delq.execute()
    	db.close();
    else:
        print "Error: Connection failed in clean_db()"
	return -5;

def check_report(mphnu):
    connect = try_connect();
    res = ''
    if connect:
    	for entry in dblookup.select().where(dblookup.recid == mrecid):
        	#print entry.recid
        	# FIX ME: Check for device type here
        	res = entry.recid
    	db.close();
    	return res
    else:
	print "Error: Connection in checking report failed"
	return -6;

# Test db
def testdb():
    # check if tables already exist, then don't create again
    print "Creating tables"
    create_dbtable()

    print "Adding report ..."
    add_report("4044263434",str(datetime.datetime.now()),45,"id0")
    add_report("4049014556",str(datetime.datetime.now()),35,"id1")
   
    print_db() 
    print "checking ..."    
    res = check_report("4044263434")
    print res

    print "Deleting  ..."
    del_report("4044263434")
    del_report("4049014556")

    print_db()
    
    print "Cleaning ..."
    clean_db()

    print_db()

#def main(argv):
#	testdb()

#if __name__ == "__main__":
#   main(sys.argv[1:])
