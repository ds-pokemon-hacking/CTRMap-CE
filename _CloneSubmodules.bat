@echo off
if not exist submodules md submodules
set GIT_ROOT=https://github.com/HelloOO7
git clone %GIT_ROOT%/XStandard submodules/Standard
git clone %GIT_ROOT%/PokeScript submodules/PokeScript