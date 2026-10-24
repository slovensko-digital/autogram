@echo off
rem Runs once at the first sign-in of the dockurr/windows VM (copied to C:\OEM during the Windows installation).
rem Registers test-msix.ps1 to run at every sign-in and runs the first test right away.
powershell.exe -NoProfile -ExecutionPolicy Bypass -File "%~dp0test-msix.ps1" -Register
