package jna;

import com.sun.jna.platform.win32.Guid.IID;
import com.sun.jna.platform.win32.WinDef.HWND;
import com.sun.jna.platform.win32.WinNT.HRESULT;

public interface ITaskbarList3 {
    IID IID_ITASKBARLIST3 = new IID("ea1afb91-9e28-4b86-90e9-9e9f8a5eefaf"); // from ShObjIdl.h

    int TBPF_NOPROGRESS = 0;
    int TBPF_INDETERMINATE = 0x1;
    int TBPF_NORMAL = 0x2;
    int TBPF_ERROR = 0x4;
    int TBPF_PAUSED = 0x8;

    HRESULT SetProgressState(HWND hwnd, int tbpFlags);
    HRESULT SetProgressValue(HWND hwnd, long value, long maxValue);
}