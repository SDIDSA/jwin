package jna;

import com.sun.jna.platform.win32.Guid.IID;
import com.sun.jna.platform.win32.WinDef.HWND;
import com.sun.jna.platform.win32.WinNT.HRESULT;

/**
 * The {@code ITaskbarList3} interface represents the ITaskbarList3 COM interface.
 * It provides methods for interacting with the Windows taskbar.
 *
 * @author SDIDSA
 */
public interface ITaskbarList3 {

    IID IID_ITASKBARLIST3 = new IID("ea1afb91-9e28-4b86-90e9-9e9f8a5eefaf"); // from ShObjIdl.h

    int TBPF_NOPROGRESS = 0;
    int TBPF_INDETERMINATE = 0x1;
    int TBPF_NORMAL = 0x2;
    int TBPF_ERROR = 0x4;
    int TBPF_PAUSED = 0x8;

    /**
     * Sets the progress state of the taskbar button.
     *
     * @param hwnd      The handle of the window whose associated taskbar button's progress state will be set.
     * @param tbpFlags  Flags that control the appearance of the progress bar.
     * @return HRESULT indicating success or failure.
     */
    HRESULT SetProgressState(HWND hwnd, int tbpFlags);

    /**
     * Sets the progress value of the taskbar button.
     *
     * @param hwnd      The handle of the window whose associated taskbar button's progress value will be set.
     * @param value     The current value of the progress.
     * @param maxValue  The maximum value of the progress.
     * @return HRESULT indicating success or failure.
     */
    HRESULT SetProgressValue(HWND hwnd, long value, long maxValue);
}
