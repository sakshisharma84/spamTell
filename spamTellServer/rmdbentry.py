#!/usr/bin/python

import dbapi as db

def main(argv):
        imname = ''
        url = ''
        try:
                opts, args = getopt.getopt(argv,"u",["url="])
        except getopt.GetoptError:
                print 'USAGE: rmdbentry.py -u <URL>'
                sys.exit(2)

        #print opts
        if len(opts) < 2:
                print 'USAGE: rmdbentry.py -u <URL>'
                sys.exit(2)

        for opt, arg in opts:
                if opt == '-h':
                        print 'USAGE: rmdbentry.py -u <URL>'
                        sys.exit()
                elif opt in ("-u", "--url"):
                        url = arg

        print 'URL "', url

        db.del_report(url);


if __name__ == "__main__":
   main(sys.argv[1:])
