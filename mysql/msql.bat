REM "dbstart.cmd"
@echo off 
color 0c
cls 
echo ========================== 
echo ��ӭʹ��mysql�������� 
echo %DATE% ��by���Ÿ��գ� 
echo ========================== 
echo. 
echo ����������? 
echo ��y������(�����)��nֹͣ�� 
echo. 
set /p ly= ������ 
if %ly%==y goto start
if %ly%==n goto stop 
:start
net start mysql56
pause 
exit
:stop
net stop mysql56
pause
exit