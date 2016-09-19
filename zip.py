#!/usr/bin/python
# -*- coding: UTF-8 -*-
import os
import zipfile
import shutil
p = "/Users/chenyuanming/ihandy/app_colorkeyboard_android/app/src/main/assets/themes/skins"



def removeDir(p):
    if(os.path.isdir(p)):
        print "remove "+p
        shutil.rmtree(p)


for path, dirs, files in os.walk(p):
    os.chdir(path)
    print files
    for f in files:
        filepath = os.path.basename(f);
        folderName, extension = os.path.splitext(filepath)[0], os.path.splitext(filepath)[1]
        print folderName
        # print extension
        if (extension == ".zip"):
            zfile = zipfile.ZipFile(filepath, 'r')
            zfile.extractall();
            # print path + "/" + folderName + "/default/preview_small@2x.png"
            # print path + "/" + folderName + "/preview_small@2x.png"
            if os.path.isfile(path + "/" + folderName + "/default/preview_small@2x.png"):
                os.rename(path + "/" + folderName + "/default/preview_small@2x.png",path + "/" + folderName + "/preview_small@2x.png")
            removeDir( path + "/" + folderName +"/default")
            removeDir( path + "/" + folderName +"/fonts")
            removeDir( path + "/" + folderName +"/sw600")

removeDir(p+"/custom_theme")
removeDir(p+"/custom_theme_common")
removeDir(p+"/__MACOSX")
