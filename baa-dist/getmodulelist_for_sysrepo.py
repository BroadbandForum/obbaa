#!/usr/bin/python

############################################################################
# Copyright 2021 Broadband Forum
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
############################################################################
#
# Script for generating sysrepo commands and metadata for OB-BAA adapters given a folder of yangs:
# - extracts the main module list
# - extracts the list of features  
#
# For greater robustness, probably this should be done as a pyang plugin.
#
# Created by Andre Brizido (Altice Labs) on 14/04/2021
# 

import os
import sys
import re
import os.path

if not os.path.exists("tmp"):
   os.mkdir("tmp")

#XML metadata   
fp = open("tmp/modulexml.txt","w");

#sysrepo commands
featureListSysrepo = open("tmp/featurelist-sysrepo.txt","w");
moduleListSysrepo = open("tmp/modulelist-sysrepo.txt","w");

#TODO: make this an input arg
yang_directory="."


mainModuleList = {}

def moduleParams(filename):
    isMain=True
    revision=""
    namespace=""
    
    firstFeature=True
    tmpfile=open(filename,"r")
    mainmoduleName=os.path.basename(filename).split(".yang")[0].split("@")[0]
    for line in tmpfile:
        if line.strip().startswith("//"):
           continue
        
        if (line.strip().startswith("revision") == True and revision==""):
            revision = line.split("revision")[1].split(" ")[1]
            
        elif (line.strip().startswith("namespace") == True and namespace==""):            
            #throws an exception if multi-line
            try:
                namespace = line.split("\"")[1]
            except Exception as e:
                print("Error getting namespace on: " + filename + " " + line + " "+ e.message);
                namespace="#####################"
                sys.exit()
            
        elif (line.strip().startswith("belongs-to") == True):
            print("not a main module:"+filename)
            isMain = False
            mainmoduleName = line.split("belongs-to")[1]
            mainmoduleName = re.sub(r'{', '', mainmoduleName).strip()
    
        elif (line.strip().startswith("feature") == True):
            if firstFeature:                
                firstFeature=False
            feature = line.split("feature")[1]
            #remove unwanted characters
            feature = re.sub(r'{', '', feature).strip()
            
            if feature.find(" ") == -1: 
                featureListSysrepo.write("sysrepoctl --enable-feature " + feature + " -c " + mainmoduleName + "\n")
                if (mainmoduleName not in mainModuleList):
                    mainModuleList[mainmoduleName] = {}
                    mainModuleList[mainmoduleName]['features'] = [ feature ]
                else:
                    if ('features' not in mainModuleList[mainmoduleName]):
                        mainModuleList[mainmoduleName]['features'] = [feature]
                    else:
                        mainModuleList[mainmoduleName]['features'].append(feature)
                    
            print("found feature:" + feature + " in " + mainmoduleName )
    
    tmpfile.close()
    return isMain,namespace,revision
    



# for subdirname in os.listdir(yang_directory):
#     if os.path.isfile(subdirname) == False:
        #print"entering " + subdirname
subdirname="."
        
for filename in sorted(os.listdir(yang_directory + "/" + subdirname)):
    if filename.endswith(".yang"): 
        #print("file: "+os.path.join(subdirname, filename))
        isMain,namespace,revision=moduleParams(yang_directory + "/" + subdirname + "/" +filename)
        if isMain == True:
            print(filename);
            mainModuleName = filename.split(".yang")[0]
            
            moduleListSysrepo.write("sysrepoctl -s . --install " + filename+ "\n")
            
            if (mainModuleName not in mainModuleList):
               mainModuleList[mainModuleName] = {}
            mainModuleList[mainModuleName]["revision"]  = revision
            mainModuleList[mainModuleName]["namespace"] = namespace
        
    #else:
    #    print("not dir"+subdirname+"\n")

print("Writing metadata file...")
for key in mainModuleList:
    try:
         fp.write("<module>\n");
         fp.write("   <name>" +  key + "</name>\n");
         fp.write("   <revision>" +  mainModuleList[key]["revision"] + "</revision>\n");
         fp.write("   <namespace>" +  mainModuleList[key]["namespace"] + "</namespace>\n");
         if "features" in mainModuleList[key]:
             for feature in mainModuleList[key]["features"]:
                fp.write("   <feature>" + feature + "</feature>\n" );
         fp.write("   <conformance-type>implement</conformance-type>\n")
         fp.write("</module>\n");
    except Exception as e:
        print("error on module:" + e.message)

fp.close()
moduleListSysrepo.close()
featureListSysrepo.close()

print("Done.")