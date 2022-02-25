package jna;

import com.sun.jna.platform.win32.Guid.CLSID;
import com.sun.jna.platform.win32.Ole32;
import com.sun.jna.platform.win32.W32Errors;
import com.sun.jna.platform.win32.WTypes;
import com.sun.jna.ptr.PointerByReference;

import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;

import javafx.stage.Stage;

public final class TaskbarPeer {

    public static void setProgressState(Stage stage, int state) {
    	WinDef.HWND hwnd = User32.INSTANCE.FindWindow(null, stage.getTitle());

        final var clsid = new CLSID("56FDF344-FD6D-11d0-958A-006097C9A090"); // from ShObjIdl.h
        final var taskbarListPointerRef = new PointerByReference();

        var hr = Ole32.INSTANCE.CoCreateInstance(clsid, null, WTypes.CLSCTX_SERVER,
                ITaskbarList3.IID_ITASKBARLIST3, taskbarListPointerRef);

        if (W32Errors.FAILED(hr)) {
            throw new RuntimeException("failed with code: " + hr.intValue());
        }

        final TaskbarList3 taskbarList = new TaskbarList3(taskbarListPointerRef.getValue());
        hr = taskbarList.SetProgressState(hwnd, state);

        if (W32Errors.FAILED(hr)) {
            throw new RuntimeException("failed with code: " + hr.intValue());
        }
    }
    
    public static void setProgress(Stage stage, double prog) {
    	WinDef.HWND hwnd = User32.INSTANCE.FindWindow(null, stage.getTitle());

        final var clsid = new CLSID("56FDF344-FD6D-11d0-958A-006097C9A090"); // from ShObjIdl.h
        final var taskbarListPointerRef = new PointerByReference();

        var hr = Ole32.INSTANCE.CoCreateInstance(clsid, null, WTypes.CLSCTX_SERVER,
                ITaskbarList3.IID_ITASKBARLIST3, taskbarListPointerRef);

        if (W32Errors.FAILED(hr)) {
            throw new RuntimeException("failed with code: " + hr.intValue());
        }

        final TaskbarList3 taskbarList = new TaskbarList3(taskbarListPointerRef.getValue());
        final long value = (long) (prog * 200);
        hr = taskbarList.SetProgressValue(hwnd, value, 200L);

        if (W32Errors.FAILED(hr)) {
            throw new RuntimeException("failed with code: " + hr.intValue());
        }
    }
}