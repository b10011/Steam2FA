#include <FileConstants.au3>

enterCode()

Func enterCode()
	Local $counter = 0
	Local $aWinList

	While $counter < 30
		$aWinList = WinList("[REGEXPTITLE:(?i)(.*Steam Guard - Computer Authorization Required.*)]")
		If $aWinList[0][0] > 0 Then
			RunWait("java -jar code.jar", @WorkingDir)
			Local $hFileOpen = FileOpen(@WorkingDir & "\code.txt", $FO_READ)
			Local $sFileRead = FileRead($hFileOpen)
			FileClose($hFileOpen)
			FileDelete(@WorkingDir & "\code.txt")

			WinActivate("[REGEXPTITLE:(?i)(.*Steam Guard - Computer Authorization Required.*)]")
			Send($sFileRead & "{ENTER}")

			Exit(1)
		EndIf

		$counter = $counter + 1
		Sleep(2000)
	WEnd

EndFunc
