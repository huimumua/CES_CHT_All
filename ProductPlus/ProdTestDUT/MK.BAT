@echo off
rem *******************************  mk.bat  *******************************
rem            ####### 
rem            ##  ## 
rem            #  ##    ####   #####    #####  ##  ##   ##### 
rem              ##    ##  ##  ##  ##  ##      ##  ##  ##     
rem             ##  #  ######  ##  ##   ####   ##  ##   ####  
rem            ##  ##  ##      ##  ##      ##   #####      ## 
rem           #######   ####   ##  ##  #####       ##  #####  
rem                                            #####
rem           Z-Wave, the wireless language.
rem                                            
rem               Copyright (c) 2001
rem               Zensys A/S
rem               Denmark
rem 
rem               All Rights Reserved
rem 
rem     This source file is subject to the terms and conditions of the
rem     Zensys Software License Agreement which restricts the manner
rem     in which it may be used.
rem
rem ---------------------------------------------------------------------------
rem
rem  Description: Make bat file for building LED dimmer
rem
rem  Author:   Peter Shorty
rem 
rem  Last Changed By:  $Author: jsi $
rem  Revision:         $Revision: 33893 $
rem  Last Changed:     $Date: 2016-06-24 15:13:48 +0200 (fr, 24 jun 2016) $
rem 
rem ****************************************************************************

if "%KEILPATH%"==""  goto usage_keil
if "%TOOLSDIR%"=="" goto usage_tools

if not exist %KEILPATH%\bin\c51.exe goto usage_keil
if not exist %KEILPATH%\bin\cx51.exe goto no_ext
set oldpath=%path%
set path=%KEILPATH%\bin;%TOOLSDIR%\Python;%path%

::Build for the normal chip.
%TOOLSDIR%\Make\make %*

set path=%oldpath%
set oldpath=
goto exit

:usage_keil
@echo Set KEILPATH to point to the location of the Keil Compiler
@echo e.g c:\keil\c51
goto exit

:usage_tools
@echo Set TOOLSDIR to point to the location of the Z-Wave tools
@echo e.g c:\projects\zensys\devkit\tools
goto exit

:exit