@echo off
echo Creation des repertoires pour le stockage des fichiers...

mkdir uploads 2>nul
mkdir uploads\photos 2>nul
mkdir uploads\photos\students 2>nul
mkdir uploads\photos\parents 2>nul
mkdir uploads\photos\teachers 2>nul
mkdir uploads\cni 2>nul
mkdir uploads\cni\parents 2>nul
mkdir uploads\cni\teachers 2>nul
mkdir uploads\documents 2>nul
mkdir uploads\bulletins 2>nul
mkdir uploads\recus 2>nul

echo.
echo ✓ Repertoires crees avec succes!
echo.
echo Structure:
echo   uploads/
echo   ├── photos/
echo   │   ├── students/
echo   │   ├── parents/
echo   │   └── teachers/
echo   ├── cni/
echo   │   ├── parents/
echo   │   └── teachers/
echo   ├── documents/
echo   ├── bulletins/
echo   └── recus/
echo.
pause
