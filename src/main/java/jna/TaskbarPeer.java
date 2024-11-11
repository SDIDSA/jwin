package jna;

import java.lang.reflect.Method;

import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.Guid.CLSID;
import com.sun.jna.platform.win32.Ole32;
import com.sun.jna.platform.win32.W32Errors;
import com.sun.jna.platform.win32.WTypes;
import com.sun.jna.ptr.PointerByReference;

import com.sun.jna.platform.win32.WinDef;

import javafx.stage.Stage;
import javafx.stage.Window;
import org.luke.gui.exception.ErrorHandler;

/**
 * A utility class for interacting with the Windows Taskbar using JNA.
 * <p>
 * This class provides methods to set the progress state and value of the taskbar button for a JavaFX {@code Stage}.
 * </p>
 * <p>
 * This version includes a method to obtain the native handle for a JavaFX {@code Stage} using reflection.
 * </p>
 * 
 * @author SDIDSA
 */
public final class TaskbarPeer {

	/**
	 * Gets the native handle for the specified JavaFX Stage using reflection.
	 * 
	 * @param stage The JavaFX Stage for which the native handle is to be obtained.
	 * @return The native handle of the specified JavaFX Stage.
	 */
	private static WinDef.HWND getNativeHandleForStage(Stage stage) {
		try {
			final Method getPeer = Window.class.getDeclaredMethod("getPeer");
			getPeer.setAccessible(true);
			final Object tkStage = getPeer.invoke(stage);
			final Method getRawHandle = tkStage.getClass().getMethod("getRawHandle");
			getRawHandle.setAccessible(true);
			final Long pointer = (Long) getRawHandle.invoke(tkStage);
			return new WinDef.HWND(new Pointer(pointer));
		} catch (Exception ex) {
			ErrorHandler.handle(ex, "determine native handle for window");
			return null;
		}
	}

	/**
	 * Sets the progress state of the taskbar button for the specified JavaFX Stage.
	 *
	 * @param stage The JavaFX Stage whose associated taskbar button's state is to be set.
	 * @param state The desired progress state.
	 */
	public static void setProgressState(Stage stage, int state) {
		WinDef.HWND hwnd = getNativeHandleForStage(stage);

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

	/**
	 * Sets the progress value of the taskbar button for the specified JavaFX Stage.
	 *
	 * @param stage The JavaFX Stage whose associated taskbar button's progress value is to be set.
	 * @param prog  The current progress value, a value between 0 and 1.
	 */
	public static void setProgress(Stage stage, double prog) {
		WinDef.HWND hwnd = getNativeHandleForStage(stage);

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
